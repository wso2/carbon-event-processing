/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.input.adaptor.file.internal.listener;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileTailerListener extends TailerListenerAdapter {

    private String fileName;

    boolean isFileFound = true;
    private Map<String, InputEventAdaptorListener> inpuEventListenerMap = new ConcurrentHashMap<String, InputEventAdaptorListener>();
    private static final Log log = LogFactory.getLog(FileTailerListener.class);
    private volatile InputEventAdaptorListener[] inputEventAdaptorListeners;
    private int tenantId;

    public FileTailerListener(String fileName) {
        this.fileName = fileName;
    }


    @Override
    public void init(Tailer tailer) {
        super.init(tailer);
    }

    @Override
    public void handle(String line) {
        super.handle(line);
        if (line == null || line.isEmpty()) {
            return;
        }
        isFileFound = true;

        if(log.isDebugEnabled()){
            log.debug("Event received in File Event Adaptor - "+line);
        }

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
        for (InputEventAdaptorListener id : inputEventAdaptorListeners) {
            id.onEventCall(line + " fileName: " + fileName + "\n");
        }
        PrivilegedCarbonContext.endTenantFlow();
    }

    @Override
    public void fileNotFound() {
        if (isFileFound) {
            isFileFound = false;
            log.warn(" fileName: " + fileName + " not found, will retry continuously.");
        }
        log.debug("File  " + fileName + " not found");
        super.fileNotFound();
    }

    @Override
    public void handle(Exception ex) {
        log.error("Exception occurred : ", ex);
        super.handle(ex);
    }

    public synchronized void addListener(String id, InputEventAdaptorListener inputListener) {
        inpuEventListenerMap.put(id, inputListener);
        inputEventAdaptorListeners = inpuEventListenerMap.values().toArray(new InputEventAdaptorListener[inpuEventListenerMap.size()]);
    }

    public synchronized void removeListener(String id) {
        inpuEventListenerMap.remove(id);
        inputEventAdaptorListeners = inpuEventListenerMap.values().toArray(new InputEventAdaptorListener[inpuEventListenerMap.size()]);
    }

    public boolean hasOneSubscriber() {
        return inpuEventListenerMap.size() == 1;
    }

    public boolean hasNoSubscriber() {
        return inpuEventListenerMap.isEmpty();
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }
}
