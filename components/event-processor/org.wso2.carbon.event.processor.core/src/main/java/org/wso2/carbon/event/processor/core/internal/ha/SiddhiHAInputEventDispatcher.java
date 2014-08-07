/**
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.event.processor.core.internal.ha;

import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.internal.listener.SiddhiInputEventDispatcher;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class SiddhiHAInputEventDispatcher extends SiddhiInputEventDispatcher {

    private final BlockingQueue<Object[]> eventQueue = new LinkedBlockingQueue<Object[]>();
    private final ThreadBarrier threadBarrier;
    private final AtomicLong blockedThreads;


    public SiddhiHAInputEventDispatcher(String streamId, InputHandler inputHandler, ExecutionPlanConfiguration executionPlanConfiguration, int tenantId, ExecutorService executorService, ThreadBarrier threadBarrier) {
        super(streamId, inputHandler, executionPlanConfiguration, tenantId);
        this.threadBarrier = threadBarrier;
        blockedThreads = threadBarrier.getBlockedThreads();
        executorService.execute(new SiddhiProcessInvoker());

    }

    public void sendEvent(Event event) throws InterruptedException {
        sendEvent(event.getData());
    }


    public void sendEvent(Object[] eventData) throws InterruptedException {
        eventQueue.put(eventData);
    }

    public BlockingQueue<Object[]> getEventQueue() {
        return eventQueue;
    }

    class SiddhiProcessInvoker implements Runnable {

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            while (true) {
                try {
                    blockedThreads.incrementAndGet();
                    threadBarrier.pass();
                    Object[] eventData = eventQueue.take();
                    blockedThreads.decrementAndGet();
                    inputHandler.send(eventData);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
