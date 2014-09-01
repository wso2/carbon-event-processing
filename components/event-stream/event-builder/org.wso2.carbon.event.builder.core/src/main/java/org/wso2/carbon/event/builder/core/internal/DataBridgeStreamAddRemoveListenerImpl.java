/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.event.builder.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.core.definitionstore.StreamAddRemoveListener;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.ds.EventBuilderServiceValueHolder;

public class DataBridgeStreamAddRemoveListenerImpl implements StreamAddRemoveListener {

    private static final Log log = LogFactory.getLog(DataBridgeStreamAddRemoveListenerImpl.class);

    @Override
    public void streamAdded(int tenantId, String streamId) {
        try {
            EventBuilderServiceValueHolder.getCarbonEventBuilderService().saveDefaultEventBuilder(streamId, tenantId);
        } catch (EventBuilderConfigurationException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void streamRemoved(int tenantId, String streamId) {
        //ignored
    }
}
