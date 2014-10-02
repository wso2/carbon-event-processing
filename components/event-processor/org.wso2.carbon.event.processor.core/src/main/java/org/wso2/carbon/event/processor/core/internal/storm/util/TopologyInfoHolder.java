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
package org.wso2.carbon.event.processor.core.internal.storm.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TopologyInfoHolder {
    /**
     * List of wiring information of all the components
     */
    private ArrayList<ComponentInfoHolder> components = new ArrayList<ComponentInfoHolder>();

    /**
     * Indexing Bolts against input stream name to enable retrieving  all the components which consumes a given
     * stream.
     */
    HashMap<String, Set<ComponentInfoHolder>> inputStreamToPublishingComponents = new HashMap<String, Set<ComponentInfoHolder>>();


    public void addComponent(ComponentInfoHolder componentInfoHolder){
        components.add(componentInfoHolder);
    }

    /**
     * Index all the storm components(i.e. spouts and bolts) of the topology against input stream definitions,
     * to make it possible to retrieve which components consumes a given stream
     */
    public void indexComponents(){
        for (ComponentInfoHolder component: components){
                for (String inputStream : component.getOutputStreamIds()) {
                    Set<ComponentInfoHolder> publishingComponents = inputStreamToPublishingComponents.get(inputStream);

                    if (publishingComponents == null) {
                        publishingComponents = new HashSet<ComponentInfoHolder>();
                        inputStreamToPublishingComponents.put(inputStream, publishingComponents);
                    }

                    publishingComponents.add(component);
                }
            }
    }

    /**
     * Return the set of components which consumes a given stream.
     * @param streamId Siddhi stream id
     * @return Set of objects containing metadata of components which consumes the given stream
     */
    public Set<ComponentInfoHolder> getPublishingComponents(String streamId){
        return inputStreamToPublishingComponents.get(streamId);
    }

    public ArrayList<ComponentInfoHolder> getComponents(){
        return components;
    }
}
