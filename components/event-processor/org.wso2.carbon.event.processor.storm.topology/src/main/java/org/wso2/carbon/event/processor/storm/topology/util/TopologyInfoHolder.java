package org.wso2.carbon.event.processor.storm.topology.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sajith on 9/10/14.
 */
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
