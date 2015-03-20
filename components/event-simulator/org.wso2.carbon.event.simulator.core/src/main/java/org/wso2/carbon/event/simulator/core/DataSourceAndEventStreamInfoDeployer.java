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

package org.wso2.carbon.event.simulator.core;


import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.event.simulator.core.internal.ds.CarbonEventSimulator;
import org.wso2.carbon.event.simulator.core.internal.ds.EventSimulatorValueHolder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class DataSourceAndEventStreamInfoDeployer extends AbstractDeployer {
    private static Log log = LogFactory.getLog(DataSourceAndEventStreamInfoDeployer.class);
    private ConfigurationContext configurationContext;

    @Override
    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

        String path = deploymentFileData.getAbsolutePath();

        try {
            processDeploy(deploymentFileData);

        } catch (Exception e) {
            throw new DeploymentException("XML file not deployed and in inactive state :  " + new File(path).getName(), e);
        }

    }

    @Override
    public void setDirectory(String s) {

    }

    @Override
    public void setExtension(String s) {

    }

    public void processDeploy(DeploymentFileData deploymentFileData)
            throws DeploymentException {

        File XMLFile = deploymentFileData.getFile();
        CarbonEventSimulator eventSimulator = EventSimulatorValueHolder.getEventSimulator();
        DataSourceTableAndStreamInfo dataSourceTableAndStreamInfo = new DataSourceTableAndStreamInfo();


        dataSourceTableAndStreamInfo.setFileName(XMLFile.getName());
        dataSourceTableAndStreamInfo.setFilePath(XMLFile.getAbsolutePath());
        dataSourceTableAndStreamInfo = getEventMappingConfiguration(XMLFile.getName(),XMLFile.getAbsolutePath(), dataSourceTableAndStreamInfo);
        if(dataSourceTableAndStreamInfo!=null){
            eventSimulator.addDataSourceTableAndStreamInfo(dataSourceTableAndStreamInfo);
        }


    }

    public DataSourceTableAndStreamInfo getEventMappingConfiguration(String fileName,String absolutePath, DataSourceTableAndStreamInfo dataSourceTableAndStreamInfo) {

        try {
            File xmlFile = new File(absolutePath);

            if (xmlFile.exists()) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(xmlFile);


                Element element = doc.getDocumentElement();
                if(element.getAttribute("type").equalsIgnoreCase("database")){
                    dataSourceTableAndStreamInfo.setConfigurationName(element.getAttribute("name"));
                    dataSourceTableAndStreamInfo.setDataSourceName(element.getElementsByTagName(EventSimulatorConstant.DATA_SOURCE_NAME).item(0).getTextContent());
                    dataSourceTableAndStreamInfo.setTableName(element.getElementsByTagName(EventSimulatorConstant.TABLE_NAME).item(0).getTextContent());
                    dataSourceTableAndStreamInfo.setEventStreamID(element.getElementsByTagName(EventSimulatorConstant.EVENT_STREAM_ID).item(0).getTextContent());
                    dataSourceTableAndStreamInfo.setEventStreamID(element.getElementsByTagName(EventSimulatorConstant.EVENT_STREAM_ID).item(0).getTextContent());
                    NodeList columnMappings = element.getElementsByTagName("columnMapping");
                    String dataSourceColumnsAndTypes [][] = new String[2][columnMappings.getLength()];

                    for(int i=0; i<columnMappings.getLength(); i++){
                        dataSourceColumnsAndTypes[0][i] = columnMappings.item(i).getAttributes().item(0).getNodeValue();
                        dataSourceColumnsAndTypes[1][i] = columnMappings.item(i).getAttributes().item(1).getNodeValue();
                    }
                    dataSourceTableAndStreamInfo.setDataSourceColumnsAndTypes(dataSourceColumnsAndTypes);

                }else{
                    return null;
                }


            }
        } catch (Exception e) {
            log.error(e);
        }
        return dataSourceTableAndStreamInfo;
    }


}
