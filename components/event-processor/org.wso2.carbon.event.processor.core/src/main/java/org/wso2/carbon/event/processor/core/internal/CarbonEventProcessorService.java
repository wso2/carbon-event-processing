/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.processor.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.processor.common.storm.config.StormDeploymentConfig;
import org.wso2.carbon.event.processor.core.*;
import org.wso2.carbon.event.processor.core.internal.util.helper.EventProcessorHelper;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanDependencyValidationException;
import org.wso2.carbon.event.processor.core.exception.ServiceDependencyValidationException;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.ha.CEPMembership;
import org.wso2.carbon.event.processor.core.internal.listener.AbstractSiddhiInputEventDispatcher;
import org.wso2.carbon.event.processor.core.internal.listener.SiddhiInputEventDispatcher;
import org.wso2.carbon.event.processor.core.internal.listener.SiddhiOutputStreamListener;
import org.wso2.carbon.event.processor.core.internal.storm.TopologyManager;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConfigurationFilesystemInvoker;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorUtil;
import org.wso2.carbon.event.stream.core.EventProducer;
import org.wso2.carbon.event.stream.core.SiddhiEventConsumer;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.util.AnnotationHelper;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CarbonEventProcessorService implements EventProcessorService {
    private static final Log log = LogFactory.getLog(CarbonEventProcessorService.class);
    // deployed query plans
    private Map<Integer, TreeMap<String, ExecutionPlan>> tenantSpecificExecutionPlans;
    // not distinguishing between deployed vs failed here.
    private Map<Integer, List<ExecutionPlanConfigurationFile>> tenantSpecificExecutionPlanFiles;
    private CEPMembership currentCepMembershipInfo;

//    private List<String> importDefinitions;              //old code block kept for reference
//    private List<String> exportDefinitions;              //old code block kept for reference

    public CarbonEventProcessorService() {
        tenantSpecificExecutionPlans = new ConcurrentHashMap<Integer, TreeMap<String, ExecutionPlan>>();
        tenantSpecificExecutionPlanFiles = new ConcurrentHashMap<Integer, List<ExecutionPlanConfigurationFile>>();
    }


    @Override
    public void deployExecutionPlan(String executionPlan)
            throws ExecutionPlanDependencyValidationException, ExecutionPlanConfigurationException {
        //validate execution plan
        org.wso2.siddhi.query.api.ExecutionPlan parsedExecutionPlan = null;
        try {
            parsedExecutionPlan = SiddhiCompiler.parse(executionPlan);
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            String executionPlanName = AnnotationHelper.getAnnotationElement(EventProcessorConstants.ANNOTATION_NAME_NAME, null, parsedExecutionPlan.getAnnotations()).getValue();

            if (!(checkExecutionPlanValidity(executionPlanName, tenantId))) {
                throw new ExecutionPlanConfigurationException(executionPlanName + " already registered as an execution in this tenant");
            }

            String repoPath = EventProcessorUtil.getAxisConfiguration().getRepository().getPath();
            File directory = new File(repoPath);
            if (!directory.exists()) {
                synchronized (repoPath.intern()) {
                    if (!directory.mkdir()) {
                        throw new ExecutionPlanConfigurationException("Cannot create directory to add tenant specific " +
                                "execution plan : " + executionPlanName);
                    }
                }
            }

            String eventProcessorConfigPath = directory.getAbsolutePath() + File.separator + EventProcessorConstants.EP_ELE_DIRECTORY;
            directory = new File(eventProcessorConfigPath);
            if (!directory.exists()) {
                synchronized (eventProcessorConfigPath.intern()) {
                    if (!directory.mkdir()) {
                        throw new ExecutionPlanConfigurationException("Cannot create directory " +
                                EventProcessorConstants.EP_ELE_DIRECTORY + " to add tenant specific  execution plan :" + executionPlanName);
                    }
                }
            }

            validateToRemoveInactiveExecutionPlanConfiguration(executionPlanName);
            EventProcessorConfigurationFilesystemInvoker.save(executionPlan, executionPlanName,
                    executionPlanName + EventProcessorConstants.SIDDHIQL_EXTENSION);

        } catch (SiddhiParserException re) {
            throw new ExecutionPlanConfigurationException("Couldn't parse execution plan: \n" + executionPlan + "\n");
        }
    }


    @Override
    public void undeployInactiveExecutionPlan(String filename)
            throws ExecutionPlanConfigurationException {
        EventProcessorConfigurationFilesystemInvoker.delete(filename);
    }

    @Override
    public void undeployActiveExecutionPlan(String planName) throws
            ExecutionPlanConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventProcessorConfigurationFilesystemInvoker.delete(getExecutionPlanConfigurationFileByPlanName(planName,
                tenantId).getFileName());
    }

    public void editActiveExecutionPlan(String executionPlan,
                                        String executionPlanName)
            throws ExecutionPlanConfigurationException, ExecutionPlanDependencyValidationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventProcessorHelper.validateExecutionPlan(executionPlan, tenantId);
        org.wso2.siddhi.query.api.ExecutionPlan parsedExecutionPlan = SiddhiCompiler.parse(executionPlan);
        String newExecutionPlanName = AnnotationHelper.getAnnotationElement(EventProcessorConstants.ANNOTATION_NAME_NAME, null, parsedExecutionPlan.getAnnotations()).getValue();
        if (!(newExecutionPlanName.equals(executionPlanName))) {
            if (!(checkExecutionPlanValidity(newExecutionPlanName, tenantId))) {
                throw new ExecutionPlanConfigurationException(newExecutionPlanName + " " +
                        "already registered as an execution in this tenant");
            }
        }
        if (executionPlanName != null && executionPlanName.length() > 0) {
            String fileName;
            ExecutionPlanConfigurationFile file = getExecutionPlanConfigurationFileByPlanName(executionPlanName, tenantId);
            if (file == null) {
                fileName = executionPlanName + EventProcessorConstants.EP_CONFIG_FILE_EXTENSION_WITH_DOT;
            } else {
                fileName = file.getFileName();
            }
            EventProcessorConfigurationFilesystemInvoker.delete(fileName);
            EventProcessorConfigurationFilesystemInvoker.save(executionPlan, newExecutionPlanName, fileName);
        } else {
            throw new ExecutionPlanConfigurationException("Invalid configuration provided, No execution plan name.");
        }
    }

    public void editInactiveExecutionPlan(String executionPlan, String filename)
            throws ExecutionPlanConfigurationException, ExecutionPlanDependencyValidationException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventProcessorHelper.validateExecutionPlan(executionPlan, tenantId);
        org.wso2.siddhi.query.api.ExecutionPlan parsedExecutionPlan = SiddhiCompiler.parse(executionPlan);
        String newExecutionPlanName = AnnotationHelper.getAnnotationElement(EventProcessorConstants.ANNOTATION_NAME_NAME, null, parsedExecutionPlan.getAnnotations()).getValue();
        EventProcessorConfigurationFilesystemInvoker.delete(filename);
        EventProcessorConfigurationFilesystemInvoker.save(executionPlan, newExecutionPlanName, filename);
    }

    /**
     * Starts an execution plan runtime for the given (valid) execution plan.
     *
     * @param executionPlan Execution plan. It is assumed that the execution plan is a valid one when reaching this function.
     * @param isEditable    whether the execution plan is editable.
     * @throws ExecutionPlanConfigurationException
     * @throws ServiceDependencyValidationException
     */
    public void addExecutionPlan(String executionPlan, boolean isEditable)
            throws ExecutionPlanConfigurationException, ServiceDependencyValidationException {
        //Assumption: executionPlanAs is valid

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        SiddhiManager siddhiManager = EventProcessorValueHolder.getSiddhiManager();
        ExecutionPlanRuntime executionPlanRuntime = null;
        org.wso2.siddhi.query.api.ExecutionPlan parsedExecutionPlan = SiddhiCompiler.parse(executionPlan);

        ExecutionPlanConfiguration executionPlanConfiguration = new ExecutionPlanConfiguration();
        executionPlanConfiguration.setExecutionPlan(executionPlan);

        String executionPlanName = AnnotationHelper.getAnnotationElement(EventProcessorConstants.ANNOTATION_NAME_NAME, null, parsedExecutionPlan.getAnnotations()).getValue();   //Element is not null since the plan is a valid one.
        executionPlanConfiguration.setName(executionPlanName);

        Element executionPlanDescriptionElement = AnnotationHelper.getAnnotationElement(EventProcessorConstants.ANNOTATION_NAME_DESCRIPTION, null, parsedExecutionPlan.getAnnotations());
        if (executionPlanDescriptionElement != null) {
            String executionPlanDescription = executionPlanDescriptionElement.getValue();
            executionPlanConfiguration.setDescription(executionPlanDescription);
        } else {
            executionPlanConfiguration.setDescription("");
        }

        Element isTracingEnabledElement = AnnotationHelper.getAnnotationElement(EventProcessorConstants.ANNOTATION_NAME_TRACE, null, parsedExecutionPlan.getAnnotations());
        if (isTracingEnabledElement != null) {
            String isTracingEnabled = isTracingEnabledElement.getValue();
            executionPlanConfiguration.setTracingEnabled(Boolean.valueOf(isTracingEnabled));
        } else {
            executionPlanConfiguration.setTracingEnabled(false);
        }

        Element isStatsEnabledElement = AnnotationHelper.getAnnotationElement(EventProcessorConstants.ANNOTATION_NAME_STATISTICS, null, parsedExecutionPlan.getAnnotations());
        if (isStatsEnabledElement != null) {
            String isStatsEnabled = isStatsEnabledElement.getValue();
            executionPlanConfiguration.setStatisticsEnabled(Boolean.valueOf(isStatsEnabled));
        } else {
            executionPlanConfiguration.setStatisticsEnabled(false);
        }
        executionPlanConfiguration.setEditable(isEditable);

        TreeMap<String, ExecutionPlan> tenantExecutionPlans = tenantSpecificExecutionPlans.get(tenantId);
        if (tenantExecutionPlans == null) {
            tenantExecutionPlans = new TreeMap<String, ExecutionPlan>();
            tenantSpecificExecutionPlans.put(tenantId, tenantExecutionPlans);
        } else if (tenantExecutionPlans.get(executionPlanName) != null) {
            // if an execution plan with the same name already exists, we are not going to override it with this plan.
            throw new ExecutionPlanConfigurationException("Execution plan with the same name already exists. Please remove it and retry.");
        }

        //buidling Import/Export Map
        Map<String, String> importsMap = new HashMap<String, String>();   //<SiddhiStreamName, StreamID>
        Map<String, String> exportsMap = new HashMap<String, String>();   //<SiddhiStreamName, StreamID>
        for (Map.Entry<String, org.wso2.siddhi.query.api.definition.StreamDefinition> entry : parsedExecutionPlan.getStreamDefinitionMap().entrySet()) {

            String siddhiStreamName = entry.getKey();
            Element importElement = AnnotationHelper.getAnnotationElement(EventProcessorConstants.ANNOTATION_IMPORT, null, entry.getValue().getAnnotations());
            Element exportElement = AnnotationHelper.getAnnotationElement(EventProcessorConstants.ANNOTATION_EXPORT, null, entry.getValue().getAnnotations());

            if (importElement != null) {
                String streamId = importElement.getValue();
                importsMap.put(siddhiStreamName, streamId);
                String[] streamIdComponents = streamId.split(EventProcessorConstants.STREAM_SEPARATOR);
                String streamName = streamIdComponents[0];
                String streamVersion = streamIdComponents[1];
                executionPlanConfiguration.addImportedStream(new StreamConfiguration(streamName, streamVersion));
            }
            if (exportElement != null) {
                String streamId = exportElement.getValue();
                exportsMap.put(siddhiStreamName, streamId);
                String[] streamIdComponents = streamId.split(EventProcessorConstants.STREAM_SEPARATOR);
                String streamName = streamIdComponents[0];
                String streamVersion = streamIdComponents[1];
                executionPlanConfiguration.addExportedStream(new StreamConfiguration(streamName, streamVersion));
            }
        }

        Map<String, InputHandler> inputHandlerMap = new ConcurrentHashMap<String,
                InputHandler>(importsMap.size());

          /* Keeping an old code-block for reference. */
        /**
         * Section to handle stream definitions
         */
//        importDefinitions = new ArrayList<String>(executionPlanConfiguration.getImportedStreams().size());
//        for (StreamConfiguration importedStreamConfiguration : executionPlanConfiguration.getImportedStreams()) {
//            StreamDefinition streamDefinition;
//            try {
//                streamDefinition = EventProcessorValueHolder.getEventStreamService().getStreamDefinition
//                        (importedStreamConfiguration.getStreamId());
//                importDefinitions.add(EventProcessorUtil.getDefinitionString(streamDefinition,
//                        importedStreamConfiguration.getSiddhiStreamName()));
//            } catch (EventStreamConfigurationException e) {
//                //ignored as this will not happen
//            }
//        }
//        exportDefinitions = new ArrayList<String>(executionPlanConfiguration.getExportedStreams().size());
//        for (StreamConfiguration exportedStreamConfiguration : executionPlanConfiguration.getExportedStreams()) {
//            StreamDefinition streamDefinition;
//            try {
//
//                streamDefinition = EventProcessorValueHolder.getEventStreamService().getStreamDefinition(
//                        exportedStreamConfiguration.getStreamId());
//                exportDefinitions.add(EventProcessorUtil.getDefinitionString(streamDefinition,
//                        exportedStreamConfiguration.getSiddhiStreamName()));
//            } catch (EventStreamConfigurationException e) {
//                //ignored as this will not happen
//            }
//        }
//
//        String isDistributedProcessingEnabledString = executionPlanConfiguration.getSiddhiConfigurationProperties()
//                .get(EventProcessorConstants.SIDDHI_DISTRIBUTED_PROCESSING);
//        StormDeploymentConfig stormDeploymentConfig = EventProcessorValueHolder.getStormDeploymentConfig();
//
//        boolean distributed = false;
//        if (isDistributedProcessingEnabledString != null && isDistributedProcessingEnabledString.equalsIgnoreCase("Distributed")) {
//            distributed = true;
//        }
//
//        if (distributed) {
//            String queryExpression = EventProcessorUtil.constructQueryExpression(executionPlanConfiguration.getName(),
//                    importDefinitions, exportDefinitions, "");
//            executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(queryExpression);
//            if (stormDeploymentConfig != null && stormDeploymentConfig.isManagerNode() && EventProcessorValueHolder
//                    .getStormManagerServer().isStormManager()) {
//                try {
//                    TopologyManager.submitTopology(executionPlanConfiguration, importDefinitions, exportDefinitions,
//                            tenantId, stormDeploymentConfig.getTopologySubmitRetryInterval());
//                } catch (StormDeploymentException e) {
//                    log.error("Invalid distributed query/configuration specified, " + e.getMessage(), e);
//                    throw new ExecutionPlanConfigurationException("Invalid distributed query specified, " + e.getMessage(), e);
//                }
//            }
//        } else {
        try {
            executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        } catch (Exception e) {
            throw new ExecutionPlanConfigurationException("Invalid query specified, " + e.getMessage(), e);
        }
//    }

        for (Map.Entry<String, String> entry : importsMap.entrySet()) {
            inputHandlerMap.put(entry.getValue(), executionPlanRuntime.getInputHandler
                    (entry.getKey()));
        }

//        HAManager haManager = null;
//        if (isDistributedProcessingEnabledString != null && isDistributedProcessingEnabledString.equalsIgnoreCase("RedundantNode")) {
//            haManager = new HAManager(EventProcessorValueHolder.getHazelcastInstance(),
//                    executionPlanConfiguration.getName(), tenantId, executionPlanRuntime, inputHandlerMap.size(),
//                    currentCepMembershipInfo);
//        }
//
//        PersistenceManager persistenceManager = null;
//        try {
//            int persistenceTimeInterval = Integer.parseInt(executionPlanConfiguration.getSiddhiConfigurationProperties().
//                    get(EventProcessorConstants.SIDDHI_SNAPSHOT_INTERVAL));
//            if (persistenceTimeInterval > 0) {
//                persistenceManager = new PersistenceManager(executionPlanRuntime, EventProcessorValueHolder.getScheduledExecutorService(),
//                        persistenceTimeInterval, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
//            }
//        } catch (NumberFormatException e) {
//
//        }

        ExecutionPlan processorExecutionPlan = new ExecutionPlan(executionPlanName, executionPlanRuntime,
                executionPlanConfiguration, null, null);    //todo: haManager, persistenceM are set to be null
        tenantExecutionPlans.put(executionPlanName, processorExecutionPlan);

        /**
         * Section to configure outputs
         */
//        SiddhiStormOutputEventListener stormOutputListener = null;
//        if (distributed && stormDeploymentConfig != null && stormDeploymentConfig.isPublisherNode()) {
//            stormOutputListener = new SiddhiStormOutputEventListener(executionPlanConfiguration, tenantId,
//                    stormDeploymentConfig);
//            executionPlan.addStormOutputListener(stormOutputListener);
//        }
        for (Map.Entry<String, String> entry : exportsMap.entrySet()) {

            SiddhiOutputStreamListener streamCallback;

//            if (haManager != null) {
//                streamCallback = new SiddhiHAOutputStreamListener(exportedStreamConfiguration.getSiddhiStreamName(),
//                        exportedStreamConfiguration.getStreamId(), executionPlanConfiguration, tenantId);
//                haManager.addStreamCallback((SiddhiHAOutputStreamListener) streamCallback);
//            } else {

            streamCallback = new SiddhiOutputStreamListener(entry.getKey(),
                    entry.getValue(), executionPlanConfiguration, tenantId);
//        }

//            if (distributed && stormDeploymentConfig != null && stormDeploymentConfig.isPublisherNode()) {
//                try {
//                    StreamDefinition databridgeDefinition = EventProcessorValueHolder.getEventStreamService()
//                            .getStreamDefinition(exportedStreamConfiguration.getStreamId());
//                    org.wso2.siddhi.query.api.definition.StreamDefinition siddhiStreamDefinition = EventProcessorUtil
//                            .convertToSiddhiStreamDefinition(databridgeDefinition, exportedStreamConfiguration.getSiddhiStreamName());
//                    stormOutputListener.registerOutputStreamListener(siddhiStreamDefinition, streamCallback);
//                } catch (EventStreamConfigurationException e) {
//                    //ignored as this will not happen
//                }
//            } else {
            executionPlanRuntime.addCallback(entry.getKey(), streamCallback);
//        }
            try {
                EventProcessorValueHolder.getEventStreamService().subscribe(streamCallback);
            } catch (EventStreamConfigurationException e) {
                //ignored as this will never happen
            }
            processorExecutionPlan.addProducer(streamCallback);
        }

        /**
         * Section to configure inputs
         */
//        for (StreamConfiguration importedStreamConfiguration : executionPlanConfiguration.getImportedStreams()) {
//            InputHandler inputHandler = inputHandlerMap.get(importedStreamConfiguration.getStreamId());
        for (Map.Entry<String, String> entry : importsMap.entrySet()) {
            InputHandler inputHandler = inputHandlerMap.get(entry.getValue());

            AbstractSiddhiInputEventDispatcher eventDispatcher;
//            if (haManager != null) {
//                eventDispatcher = new SiddhiHAInputEventDispatcher(importedStreamConfiguration.getStreamId(),
//                        inputHandler, executionPlanConfiguration, tenantId, haManager.getProcessThreadPoolExecutor(),
//                        haManager.getThreadBarrier());
//                haManager.addInputEventDispatcher(importedStreamConfiguration.getStreamId(),
//                        (SiddhiHAInputEventDispatcher) eventDispatcher);
//            } else if (distributed && stormDeploymentConfig != null && stormDeploymentConfig.isReceiverNode()) {
//                StreamDefinition streamDefinition = null;
//                try {
//                    streamDefinition = EventProcessorValueHolder.getEventStreamService().getStreamDefinition
//                            (importedStreamConfiguration.getStreamId());
//                } catch (EventStreamConfigurationException e) {
//                    // Ignore as this would never happen
//                }
//                eventDispatcher = new SiddhiStormInputEventDispatcher(streamDefinition,
//                        importedStreamConfiguration.getSiddhiStreamName(), executionPlanConfiguration, tenantId,
//                        stormDeploymentConfig);
//            } else {
            eventDispatcher = new SiddhiInputEventDispatcher(entry.getValue(),
                    inputHandler, executionPlanConfiguration, tenantId);
//        }
            try {
                EventProcessorValueHolder.getEventStreamService().subscribe(eventDispatcher);
                processorExecutionPlan.addConsumer(eventDispatcher);
            } catch (EventStreamConfigurationException e) {
                //ignored as this will never happen
            }
        }
        if (executionPlanRuntime != null) {
            executionPlanRuntime.start();
            executionPlanRuntime.restoreLastRevision();
        }
//        if (haManager != null) {
//            haManager.init();
//        }
//
//        if (persistenceManager != null) {
//            persistenceManager.init();
//        }
    }

    public List<StreamDefinition> getSiddhiStreams(String executionPlan) {
        SiddhiManager siddhiManager = EventProcessorValueHolder.getSiddhiManager();
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        Collection<AbstractDefinition> streamDefinitions = executionPlanRuntime.getStreamDefinitionMap().values();
        List<StreamDefinition> databridgeStreamDefinitions = new ArrayList<StreamDefinition>(streamDefinitions.size());
        for (AbstractDefinition siddhiStreamDef : streamDefinitions) {
            StreamConfiguration streamConfig = new StreamConfiguration(siddhiStreamDef.getId());
            StreamDefinition databridgeStreamDef = EventProcessorUtil.convertToDatabridgeStreamDefinition(
                    (org.wso2.siddhi.query.api.definition.StreamDefinition) siddhiStreamDef, streamConfig);
            databridgeStreamDefinitions.add(databridgeStreamDef);
        }
        executionPlanRuntime.shutdown();
        return databridgeStreamDefinitions;
    }


    public void validateExecutionPlan(String executionPlan)
            throws ExecutionPlanConfigurationException, ExecutionPlanDependencyValidationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventProcessorHelper.validateExecutionPlan(executionPlan, tenantId);
    }

    public void notifyServiceAvailability(String serviceId) {
        for (Integer tenantId : tenantSpecificExecutionPlanFiles.keySet()) {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
                activateInactiveExecutionPlanConfigurations(ExecutionPlanConfigurationFile.Status.WAITING_FOR_OSGI_SERVICE, serviceId, tenantId);
            } catch (ExecutionPlanConfigurationException e) {
                log.error("Error while redeploying distributed execution plans.", e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    private void removeExecutionPlanConfiguration(String name, int tenantId) {
        Map<String, ExecutionPlan> executionPlanMap = tenantSpecificExecutionPlans.get(tenantId);
        if (executionPlanMap != null && executionPlanMap.containsKey(name)) {
            ExecutionPlan executionPlan = executionPlanMap.remove(name);
            executionPlan.shutdown();

            ExecutionPlanConfiguration executionPlanConfiguration = executionPlan.getExecutionPlanConfiguration();

            boolean distributed = false;
            String isDistributedProcessingEnabledString = null;
//            isDistributedProcessingEnabledString = executionPlanConfiguration.getSiddhiConfigurationProperties      //todo
//                    ().get(EventProcessorConstants.SIDDHI_DISTRIBUTED_PROCESSING);
            if (isDistributedProcessingEnabledString != null && isDistributedProcessingEnabledString.equalsIgnoreCase("Distributed")) {
                distributed = true;
            }

            StormDeploymentConfig stormDeploymentConfig = EventProcessorValueHolder.getStormDeploymentConfig();       //todo
            if (distributed && stormDeploymentConfig != null && stormDeploymentConfig.isManagerNode() &&
                    EventProcessorValueHolder.getStormManagerServer().isStormManager()) {
                try {
                    TopologyManager.killTopology(executionPlanConfiguration.getName(), tenantId);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            // releasing junction listeners.
            for (SiddhiEventConsumer eventConsumer : executionPlan.getSiddhiEventConsumers()) {
                EventProcessorValueHolder.getEventStreamService().unsubscribe(eventConsumer);
            }

            for (EventProducer eventProducer : executionPlan.getEventProducers()) {
                EventProcessorValueHolder.getEventStreamService().unsubscribe(eventProducer);
            }

        }

    }

    public void addExecutionPlanConfigurationFile(ExecutionPlanConfigurationFile configurationFile,
                                                  int tenantId) {
        List<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = tenantSpecificExecutionPlanFiles.get(tenantId);
        if (executionPlanConfigurationFiles == null) {
            executionPlanConfigurationFiles = new ArrayList<ExecutionPlanConfigurationFile>();
            tenantSpecificExecutionPlanFiles.put(tenantId, executionPlanConfigurationFiles);
        }
        executionPlanConfigurationFiles.add(configurationFile);
    }

    /**
     * Just removes the configuration file
     *
     * @param fileName the filename of the {@link ExecutionPlanConfigurationFile} to be removed
     * @param tenantId the tenantId of the tenant to which this configuration file belongs
     */
    public void removeExecutionPlanConfigurationFile(String fileName, int tenantId) {
        List<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = tenantSpecificExecutionPlanFiles.get(tenantId);
        for (Iterator<ExecutionPlanConfigurationFile> iterator = executionPlanConfigurationFiles.iterator(); iterator.hasNext(); ) {
            ExecutionPlanConfigurationFile configurationFile = iterator.next();
            if (new File(configurationFile.getFileName()).getName().equals(fileName)) {
                if (configurationFile.getStatus().equals(ExecutionPlanConfigurationFile.Status.DEPLOYED)) {
                    removeExecutionPlanConfiguration(configurationFile.getExecutionPlanName(), tenantId);
                }
                iterator.remove();
                break;
            }
        }
    }

    public String getActiveExecutionPlan(String planName)
            throws ExecutionPlanConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ExecutionPlanConfigurationFile configFile = getExecutionPlanConfigurationFileByPlanName(planName, tenantId);
        if (configFile == null) {
            throw new ExecutionPlanConfigurationException("Configuration file for " + planName + "doesn't exist.");
        }
        return EventProcessorConfigurationFilesystemInvoker.readExecutionPlanConfigFile(configFile.getFileName());
    }

    public String getInactiveExecutionPlan(String filename)
            throws ExecutionPlanConfigurationException {
        return EventProcessorConfigurationFilesystemInvoker.readExecutionPlanConfigFile(filename);
    }

    @Override
    public Map<String, ExecutionPlanConfiguration> getAllActiveExecutionConfigurations(int tenantId) {
        Map<String, ExecutionPlanConfiguration> configurationMap = new HashMap<String, ExecutionPlanConfiguration>();
        Map<String, ExecutionPlan> executionPlanMap = tenantSpecificExecutionPlans.get(tenantId);
        if (executionPlanMap != null) {
            for (Map.Entry<String, ExecutionPlan> entry : executionPlanMap.entrySet()) {
                configurationMap.put(entry.getKey(), entry.getValue().getExecutionPlanConfiguration());
            }
        }
        return configurationMap;
    }

    @Override
    public Map<String, ExecutionPlanConfiguration> getAllExportedStreamSpecificActiveExecutionConfigurations(
            int tenantId, String streamId) {
        Map<String, ExecutionPlanConfiguration> configurationMap = new HashMap<String, ExecutionPlanConfiguration>();
        Map<String, ExecutionPlan> executionPlanMap = tenantSpecificExecutionPlans.get(tenantId);
        if (executionPlanMap != null) {
            for (Map.Entry<String, ExecutionPlan> entry : executionPlanMap.entrySet()) {

                List<StreamConfiguration> streamConfigurationList = entry.getValue().getExecutionPlanConfiguration().getExportedStreams();
                for (StreamConfiguration streamConfiguration : streamConfigurationList) {
                    String streamNameWithVersion = streamConfiguration.getName() + ":" + streamConfiguration.getVersion();
                    if (streamNameWithVersion.equals(streamId)) {
                        configurationMap.put(entry.getKey(), entry.getValue().getExecutionPlanConfiguration());
                    }
                }
            }
        }
        return configurationMap;
    }

    @Override
    public Map<String, ExecutionPlanConfiguration> getAllImportedStreamSpecificActiveExecutionConfigurations(
            int tenantId, String streamId) {
        Map<String, ExecutionPlanConfiguration> configurationMap = new HashMap<String, ExecutionPlanConfiguration>();
        Map<String, ExecutionPlan> executionPlanMap = tenantSpecificExecutionPlans.get(tenantId);
        if (executionPlanMap != null) {
            for (Map.Entry<String, ExecutionPlan> entry : executionPlanMap.entrySet()) {

                List<StreamConfiguration> streamConfigurationList = entry.getValue().getExecutionPlanConfiguration()
                        .getImportedStreams();
                for (StreamConfiguration streamConfiguration : streamConfigurationList) {
                    String streamNameWithVersion = streamConfiguration.getName() + ":" + streamConfiguration.getVersion();
                    if (streamNameWithVersion.equals(streamId)) {
                        configurationMap.put(entry.getKey(), entry.getValue().getExecutionPlanConfiguration());
                    }
                }
            }
        }
        return configurationMap;
    }

    @Override
    public ExecutionPlanConfiguration getActiveExecutionPlanConfiguration(String planName, int tenantId) {
        Map<String, ExecutionPlan> executionPlanMap = tenantSpecificExecutionPlans.get(tenantId);
        if (executionPlanMap != null) {
            ExecutionPlan executionPlan = executionPlanMap.get(planName);
            if (executionPlan != null) {
                return executionPlan.getExecutionPlanConfiguration();
            }
        }
        return null;
    }


    public ExecutionPlan getActiveExecutionPlan(String name, int tenantId) {
        Map<String, ExecutionPlan> executionPlanMap = tenantSpecificExecutionPlans.get(tenantId);
        if (executionPlanMap != null) {
            return executionPlanMap.get(name);
        }
        return null;
    }

    @Override
    public List<ExecutionPlanConfigurationFile> getAllInactiveExecutionPlanConfiguration(
            int tenantId) {
        List<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = this.tenantSpecificExecutionPlanFiles.get(tenantId);

        List<ExecutionPlanConfigurationFile> files = new ArrayList<ExecutionPlanConfigurationFile>();
        if (executionPlanConfigurationFiles != null) {
            for (ExecutionPlanConfigurationFile configFile : executionPlanConfigurationFiles) {
                if (configFile.getStatus() == ExecutionPlanConfigurationFile.Status.ERROR || configFile.getStatus()
                        == ExecutionPlanConfigurationFile.Status.WAITING_FOR_DEPENDENCY || configFile.getStatus() ==
                        ExecutionPlanConfigurationFile.Status.WAITING_FOR_OSGI_SERVICE) {
                    files.add(configFile);
                }
            }
        }
        return files;
    }

    @Override
    public void setTracingEnabled(String executionPlanName, boolean isEnabled)
            throws ExecutionPlanConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, ExecutionPlan> executionPlans = tenantSpecificExecutionPlans.get(tenantId);
        if (executionPlans != null) {
            ExecutionPlan processorExecutionPlan = executionPlans.get(executionPlanName);
            ExecutionPlanConfiguration executionPlanConfiguration = processorExecutionPlan.getExecutionPlanConfiguration();
            executionPlanConfiguration.setTracingEnabled(isEnabled);
            String executionPlan = executionPlanConfiguration.getExecutionPlan();
            String newExecutionPlan = EventProcessorHelper.setExecutionPlanAnnotationName(executionPlan, EventProcessorConstants.ANNOTATION_NAME_TRACE, isEnabled);
            executionPlanConfiguration.setExecutionPlan(newExecutionPlan);
            ExecutionPlanConfigurationFile configFile = getExecutionPlanConfigurationFileByPlanName(executionPlanName, tenantId);
            String fileName = configFile.getFileName();
            EventProcessorConfigurationFilesystemInvoker.delete(fileName);
            EventProcessorConfigurationFilesystemInvoker.save(newExecutionPlan, executionPlanName, fileName);
        }
    }

    @Override
    public void setStatisticsEnabled(String executionPlanName, boolean isEnabled)
            throws ExecutionPlanConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, ExecutionPlan> processorExecutionPlans = tenantSpecificExecutionPlans.get(tenantId);
        if (processorExecutionPlans != null) {
            ExecutionPlan processorExecutionPlan = processorExecutionPlans.get(executionPlanName);
            ExecutionPlanConfiguration executionPlanConfiguration = processorExecutionPlan.getExecutionPlanConfiguration();
            executionPlanConfiguration.setStatisticsEnabled(isEnabled);
            String executionPlan = executionPlanConfiguration.getExecutionPlan();
            String newExecutionPlan = EventProcessorHelper.setExecutionPlanAnnotationName(executionPlan,
                    EventProcessorConstants.ANNOTATION_NAME_STATISTICS, isEnabled);
            executionPlanConfiguration.setExecutionPlan(newExecutionPlan);
            ExecutionPlanConfigurationFile configFile = getExecutionPlanConfigurationFileByPlanName(executionPlanName, tenantId);
            String fileName = configFile.getFileName();
            EventProcessorConfigurationFilesystemInvoker.delete(fileName);
            EventProcessorConfigurationFilesystemInvoker.save(newExecutionPlan, executionPlanName, fileName);
        }
    }

    /**
     * Activate Inactive Execution Plan Configurations
     *
     * @param tenantId             the tenant id of the tenant which triggered this call
     * @param resolvedDependencyId the id of the dependency that was resolved which resulted in triggering this method call
     */
    public void activateInactiveExecutionPlanConfigurations(
            ExecutionPlanConfigurationFile.Status status, String resolvedDependencyId, int tenantId)
            throws ExecutionPlanConfigurationException {

        List<ExecutionPlanConfigurationFile> reloadFileList = new ArrayList<ExecutionPlanConfigurationFile>();

        if (tenantSpecificExecutionPlanFiles != null && tenantSpecificExecutionPlanFiles.size() > 0) {
            List<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = tenantSpecificExecutionPlanFiles.get(tenantId);

            if (executionPlanConfigurationFiles != null) {
                for (ExecutionPlanConfigurationFile executionPlanConfigurationFile : executionPlanConfigurationFiles) {
                    if ((executionPlanConfigurationFile.getStatus().equals(status)) && resolvedDependencyId
                            .equalsIgnoreCase(executionPlanConfigurationFile.getDependency())) {
                        reloadFileList.add(executionPlanConfigurationFile);
                    }
                }
            }
        }
        for (ExecutionPlanConfigurationFile executionPlanConfigurationFile : reloadFileList) {
            try {
                EventProcessorConfigurationFilesystemInvoker.reload(executionPlanConfigurationFile.getFilePath(),
                        executionPlanConfigurationFile.getAxisConfiguration());
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Execution Plan configuration file : " + new
                        File(executionPlanConfigurationFile.getFileName()).getName());
            }
        }

    }

    public void deactivateActiveExecutionPlanConfigurations(String streamId, int tenantId) {

        List<String> toDeactivateExecutionPlan = new ArrayList<String>();
        Map<String, ExecutionPlan> executionPlanMap = tenantSpecificExecutionPlans.get(tenantId);
        if (executionPlanMap != null) {
            for (ExecutionPlan executionPlan : executionPlanMap.values()) {
                boolean done = false;
                for (EventProducer eventProducer : executionPlan.getEventProducers()) {
                    if (eventProducer.getStreamId().equals(streamId)) {
                        toDeactivateExecutionPlan.add(executionPlan.getName());
                        done = true;
                        break;
                    }
                }

                if (!done) {
                    for (SiddhiEventConsumer eventConsumer : executionPlan.getSiddhiEventConsumers()) {
                        if (eventConsumer.getStreamId().equals(streamId)) {
                            toDeactivateExecutionPlan.add(executionPlan.getName());
                            break;
                        }
                    }
                }
            }
        }
        if (toDeactivateExecutionPlan.size() > 0) {
            for (String name : toDeactivateExecutionPlan) {
                ExecutionPlanConfigurationFile executionPlanConfigurationFile = getExecutionPlanConfigurationFileByPlanName(name, tenantId);
                try {
                    EventProcessorConfigurationFilesystemInvoker.reload(executionPlanConfigurationFile.getFilePath(),
                            executionPlanConfigurationFile.getAxisConfiguration());
                } catch (Exception e) {
                    log.error("Exception occurred while trying to deploy the Execution Plan configuration file : " +
                            new File(executionPlanConfigurationFile.getFileName()).getName());
                }
            }
        }
    }

    // gets file by name.
    private ExecutionPlanConfigurationFile getExecutionPlanConfigurationFileByPlanName(String name,
                                                                                       int tenantId) {
        List<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = tenantSpecificExecutionPlanFiles.get(tenantId);
        if (executionPlanConfigurationFiles != null) {
            for (ExecutionPlanConfigurationFile file : executionPlanConfigurationFiles) {
                if (name.equals(file.getExecutionPlanName()) && file.getStatus().equals(ExecutionPlanConfigurationFile.Status.DEPLOYED)) {
                    return file;
                }
            }
        }
        return null;
    }

    private void validateToRemoveInactiveExecutionPlanConfiguration(String executionPlanName)
            throws ExecutionPlanConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        String fileName = executionPlanName + EventProcessorConstants.EP_CONFIG_FILE_EXTENSION_WITH_DOT;
        List<ExecutionPlanConfigurationFile> executionPlanConfigurationFiles = tenantSpecificExecutionPlanFiles.get(tenantId);
        if (executionPlanConfigurationFiles != null) {
            for (ExecutionPlanConfigurationFile executionPlanConfigurationFile : executionPlanConfigurationFiles) {
                if ((executionPlanConfigurationFile.getFileName().equals(fileName))) {
                    if (!(executionPlanConfigurationFile.getStatus().equals(ExecutionPlanConfigurationFile.Status.DEPLOYED))) {
                        EventProcessorConfigurationFilesystemInvoker.delete(fileName);
                        break;
                    }
                }
            }
        }
    }

    private boolean checkExecutionPlanValidity(String executionPlanName, int tenantId)
            throws ExecutionPlanConfigurationException {

        Map<String, ExecutionPlanConfiguration> executionPlanConfigurationMap;
        executionPlanConfigurationMap = getAllActiveExecutionConfigurations(tenantId);
        if (executionPlanConfigurationMap != null) {
            for (String existingExecutionPlanName : executionPlanConfigurationMap.keySet()) {
                if (executionPlanName.equalsIgnoreCase(existingExecutionPlanName)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isExecutionPlanFileAlreadyExist(String executionPlanFileName, int tenantId)
            throws ExecutionPlanConfigurationException {

        if (tenantSpecificExecutionPlanFiles.size() > 0) {
            List<ExecutionPlanConfigurationFile> executionPlanConfigurationFileList = tenantSpecificExecutionPlanFiles.get(tenantId);
            if (executionPlanConfigurationFileList != null) {
                for (ExecutionPlanConfigurationFile executionPlanConfigurationFile : executionPlanConfigurationFileList) {
                    if ((executionPlanConfigurationFile.getExecutionPlanName().equals(executionPlanFileName))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void addCurrentCEPMembership(CEPMembership cepMembership) {
        currentCepMembershipInfo = cepMembership;
    }

    public void shutdown() {
        for (Map<String, ExecutionPlan> executionPlans : tenantSpecificExecutionPlans.values()) {
            for (ExecutionPlan executionPlan : executionPlans.values()) {
                executionPlan.shutdown();
            }
        }
    }

}
