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
package org.wso2.carbon.event.output.adaptor.logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.publisher.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.publisher.core.MessageType;
import org.wso2.carbon.event.publisher.core.Property;
import org.wso2.carbon.event.publisher.core.config.EndpointAdaptorConfiguration;
import org.wso2.carbon.event.publisher.core.exception.TestConnectionUnavailableException;
import org.wso2.carbon.event.output.adaptor.logger.internal.util.LoggerEventAdaptorConstants;

import java.util.*;

public final class LoggerEventAdaptorType extends AbstractOutputEventAdaptor {

    private static LoggerEventAdaptorType loggerEventAdaptorType = new LoggerEventAdaptorType();
    private ResourceBundle resourceBundle;
    private static final Log log = LogFactory.getLog(LoggerEventAdaptorType.class);

    private LoggerEventAdaptorType() {

    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.TEXT);
        supportOutputMessageTypes.add(MessageType.XML);
        supportOutputMessageTypes.add(MessageType.JSON);
        return supportOutputMessageTypes;
    }

    /**
     * @return logger adaptor instance
     */
    public static LoggerEventAdaptorType getInstance() {

        return loggerEventAdaptorType;
    }

    /**
     * @return name of the logger adaptor
     */
    @Override
    protected String getName() {
        return LoggerEventAdaptorConstants.ADAPTOR_TYPE_LOGGER;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adaptor.logger.i18n.Resources", Locale.getDefault());
    }

    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        // set stream definition
        Property streamDefinitionProperty = new Property(LoggerEventAdaptorConstants.ADAPTOR_MESSAGE_UNIQUE_ID);
        streamDefinitionProperty.setDisplayName(
                resourceBundle.getString(LoggerEventAdaptorConstants.ADAPTOR_MESSAGE_UNIQUE_ID));
        streamDefinitionProperty.setHint(resourceBundle.getString(LoggerEventAdaptorConstants.ADAPTOR_MESSAGE_UNIQUE_ID_HINT));
        streamDefinitionProperty.setRequired(true);

        propertyList.add(streamDefinitionProperty);

        return propertyList;
    }

    /**
     * @param message  - is and Object[]{Event, EventDefinition}
     * @param endpointAdaptorConfiguration
     *
     * @param tenantId
     */
    public void publish(
            Object message,
            EndpointAdaptorConfiguration endpointAdaptorConfiguration, int tenantId) {

        if (message instanceof Object[]) {
            log.info("Unique ID : " + endpointAdaptorConfiguration.getOutputAdaptorProperties().get(LoggerEventAdaptorConstants.ADAPTOR_MESSAGE_UNIQUE_ID) + " , Event : " + Arrays.deepToString((Object[]) message));
        } else {
            log.info("Unique ID : " + endpointAdaptorConfiguration.getOutputAdaptorProperties().get(LoggerEventAdaptorConstants.ADAPTOR_MESSAGE_UNIQUE_ID) + " , Event : " + message);
        }
    }


    @Override
    public void testConnection(
            EndpointAdaptorConfiguration endpointAdaptorConfiguration, int tenantId) {
        throw new TestConnectionUnavailableException("not-available");

    }

    @Override
    public void removeConnectionInfo(
            EndpointAdaptorConfiguration endpointAdaptorConfiguration, int tenantId) {
        //not required
    }


}
