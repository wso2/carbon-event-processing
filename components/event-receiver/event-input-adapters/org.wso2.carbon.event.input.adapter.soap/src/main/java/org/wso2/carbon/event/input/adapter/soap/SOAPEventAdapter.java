/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.event.input.adapter.soap;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterRuntimeException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.input.adapter.soap.internal.Axis2ServiceManager;
import org.wso2.carbon.event.input.adapter.soap.internal.util.SOAPEventAdapterConstants;

import java.util.Map;
import java.util.UUID;

public final class SOAPEventAdapter implements InputEventAdapter {

    private static final Log log = LogFactory.getLog(SOAPEventAdapter.class);
    private final InputEventAdapterConfiguration eventAdapterConfiguration;
    private final Map<String, String> globalProperties;
    private InputEventAdapterListener eventAdaptorListener;
    private final String id = UUID.randomUUID().toString();

//    public static ExecutorService executorService = new ThreadPoolExecutor(SOAPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE,
//            SOAPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE, SOAPEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME, TimeUnit.SECONDS,
//            new LinkedBlockingQueue<Runnable>(SOAPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE));

    public SOAPEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration, Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }


    @Override
    public void init(InputEventAdapterListener eventAdaptorListener) throws InputEventAdapterException {
        this.eventAdaptorListener = eventAdaptorListener;
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-supported");
    }

    @Override
    public void connect() {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String operation = eventAdapterConfiguration.getProperties().get(SOAPEventAdapterConstants.ADAPTER_MESSAGE_OPERATION_NAME);
        try {

            Axis2ServiceManager.registerService(eventAdapterConfiguration.getName(), operation, this, EventAdapterUtil.getAxisConfiguration());
        } catch (AxisFault axisFault) {
            throw new InputEventAdapterRuntimeException("Cannot register Input Adapter " + eventAdapterConfiguration.getName() + " for the operation " + operation + " on tenant " + tenantId, axisFault);
        }
    }

    @Override
    public void disconnect() {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String operation = eventAdapterConfiguration.getProperties().get(SOAPEventAdapterConstants.ADAPTER_MESSAGE_OPERATION_NAME);
        try {
            Axis2ServiceManager.unregisterService(eventAdapterConfiguration.getName(), operation, this, EventAdapterUtil.getAxisConfiguration());
        } catch (AxisFault axisFault) {
            throw new InputEventAdapterRuntimeException("Cannot unregister Input Adapter " + eventAdapterConfiguration.getName() + " for the operation " + operation + " on tenant " + tenantId, axisFault);
        }
    }

    @Override
    public void destroy() {
    }

    public InputEventAdapterListener getEventAdaptorListener() {
        return eventAdaptorListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SOAPEventAdapter)) return false;

        SOAPEventAdapter that = (SOAPEventAdapter) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}