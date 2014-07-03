package org.wso2.carbon.event.processor.storm.common.client;

import org.wso2.siddhi.core.util.collection.Pair;

/**
 * Created by sajith on 6/27/14.
 */
;
public interface ManagerServiceClientCallback {
    void OnResponseReceived(Pair<String, Integer> endpoint);
}
