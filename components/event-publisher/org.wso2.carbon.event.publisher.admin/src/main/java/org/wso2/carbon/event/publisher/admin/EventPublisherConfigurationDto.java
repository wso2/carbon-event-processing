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
package org.wso2.carbon.event.publisher.admin;


public class EventPublisherConfigurationDto {

    private String eventPublisherName;

    private String fromStreamNameWithVersion;

    private EndpointPropertyConfigurationDto endpointPropertyConfigurationDto;

    private XMLOutputMappingDto xmlOutputMappingDto;

    private JSONOutputMappingDto jsonOutputMappingDto;

    private MapOutputMappingDto mapOutputMappingDto;

    private WSO2EventOutputMappingDto wso2EventOutputMappingDto;

    private TextOutputMappingDto textOutputMappingDto;

    private String streamDefinition;

    private String mappingType;


    public String getEventPublisherName() {
        return eventPublisherName;
    }

    public void setEventPublisherName(String eventPublisherName) {
        this.eventPublisherName = eventPublisherName;
    }

    public String getFromStreamNameWithVersion() {
        return fromStreamNameWithVersion;
    }

    public void setFromStreamNameWithVersion(String fromStreamNameWithVersion) {
        this.fromStreamNameWithVersion = fromStreamNameWithVersion;
    }

    public EndpointPropertyConfigurationDto getEndpointPropertyConfigurationDto() {
        return endpointPropertyConfigurationDto;
    }

    public void setEndpointPropertyConfigurationDto(
            EndpointPropertyConfigurationDto endpointPropertyConfigurationDto) {
        this.endpointPropertyConfigurationDto = endpointPropertyConfigurationDto;
    }

    public String getStreamDefinition() {
        return streamDefinition;
    }

    public void setStreamDefinition(String streamDefinition) {
        this.streamDefinition = streamDefinition;
    }

    public String getMappingType() {
        return mappingType;
    }

    public void setMappingType(String mappingType) {
        this.mappingType = mappingType;
    }

    public XMLOutputMappingDto getXmlOutputMappingDto() {
        return xmlOutputMappingDto;
    }

    public void setXmlOutputMappingDto(XMLOutputMappingDto xmlOutputMappingDto) {
        this.xmlOutputMappingDto = xmlOutputMappingDto;
    }

    public JSONOutputMappingDto getJsonOutputMappingDto() {
        return jsonOutputMappingDto;
    }

    public void setJsonOutputMappingDto(JSONOutputMappingDto jsonOutputMappingDto) {
        this.jsonOutputMappingDto = jsonOutputMappingDto;
    }

    public MapOutputMappingDto getMapOutputMappingDto() {
        return mapOutputMappingDto;
    }

    public void setMapOutputMappingDto(MapOutputMappingDto mapOutputMappingDto) {
        this.mapOutputMappingDto = mapOutputMappingDto;
    }

    public WSO2EventOutputMappingDto getWso2EventOutputMappingDto() {
        return wso2EventOutputMappingDto;
    }

    public void setWso2EventOutputMappingDto(WSO2EventOutputMappingDto wso2EventOutputMappingDto) {
        this.wso2EventOutputMappingDto = wso2EventOutputMappingDto;
    }

    public TextOutputMappingDto getTextOutputMappingDto() {
        return textOutputMappingDto;
    }

    public void setTextOutputMappingDto(TextOutputMappingDto textOutputMappingDto) {
        this.textOutputMappingDto = textOutputMappingDto;
    }
}
