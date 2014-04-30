package org.wso2.carbon.event.stream.manager.core.internal.stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.core.definitionstore.StreamAddRemoveListener;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.manager.core.internal.CarbonEventStreamService;


public class DataBridgeStreamAddRemoveListenerImpl implements StreamAddRemoveListener {
    private static final Log log = LogFactory.getLog(CarbonEventStreamService.class);
    private CarbonEventStreamService carbonEventStreamService;

    public DataBridgeStreamAddRemoveListenerImpl(CarbonEventStreamService carbonEventStreamService) {

        this.carbonEventStreamService = carbonEventStreamService;
    }

    @Override
    public void streamAdded(int tenantId, String streamId) {
        try {
            carbonEventStreamService.loadEventStream(streamId, tenantId);
        } catch (EventStreamConfigurationException e) {
            log.error("Loading stream from Data Bridge failed, " + e.getMessage(), e);
        }
    }

    @Override
    public void streamRemoved(int tenantId, String streamId) {
        try {
            carbonEventStreamService.unloadEventStream(streamId, tenantId);
        } catch (EventStreamConfigurationException e) {
            log.error("Unloading stream from Data Bridge failed, " + e.getMessage(), e);
        }

    }
}
