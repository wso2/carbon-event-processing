package org.wso2.carbon.event.stream.manager.core.internal.util;

import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;

/**
 * Created by suho on 3/26/14.
 */
public class EventConverter {

    public static Event convertToWso2Event(Object[] objArray, StreamDefinition streamDefinition) {
        int metaSize;
        int correlationSize;
        int payloadSize;

        Object[] metaAttributes = null;
        Object[] correlationAttributes = null;
        Object[] payloadAttributes = null;

        int attributeIndex = 0;

        if (streamDefinition.getMetaData() != null) { // If there is at least 1 meta data field
            metaSize = streamDefinition.getMetaData().size();
            metaAttributes = new Object[metaSize];
            for (int i = 0; i < metaSize; i++) {
                metaAttributes[i] = objArray[attributeIndex++];
            }
        }
        if (streamDefinition.getCorrelationData() != null) { // If there is at least 1 correlation data field
            correlationSize = streamDefinition.getCorrelationData().size();
            correlationAttributes = new Object[correlationSize];
            for (int i = 0; i < correlationSize; i++) {
                correlationAttributes[i] = objArray[attributeIndex++];
            }
        }
        if (streamDefinition.getPayloadData() != null) { // If there is at least 1 payload data field
            payloadSize = streamDefinition.getPayloadData().size();
            payloadAttributes = new Object[payloadSize];
            for (int i = 0; i < payloadSize; i++) {
                payloadAttributes[i] = objArray[attributeIndex++];
            }
        }

        return new Event(streamDefinition.getStreamId(), System.currentTimeMillis(), metaAttributes, correlationAttributes, payloadAttributes);
    }

    public static Object[] convertToEventData(Event event, boolean metaFlag, boolean correlationFlag, boolean payloadFlag, int size) {

        Object[] eventObject = new Object[size];
        int count = 0;
        Object[] metaData = event.getMetaData();
        Object[] correlationData = event.getCorrelationData();
        Object[] payloadData = event.getPayloadData();

        if (metaFlag) {
            System.arraycopy(metaData, 0, eventObject, 0, metaData.length);
            count += metaData.length;
        }

        if (correlationFlag) {
            System.arraycopy(correlationData, 0, eventObject, count, correlationData.length);
            count += correlationData.length;
        }

        if (payloadFlag) {
            System.arraycopy(payloadData, 0, eventObject, count, payloadData.length);
            count += payloadData.length;
        }

        return eventObject;
    }

}
