package org.wso2.carbon.event.processing.application.deployer;

import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.application.deployer.AppDeployerConstants;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.event.processing.application.deployer.internal.EventProcessingAppDeployerDS;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class EventProcessingAppDeployer implements AppDeploymentHandler {

    private static final Log log = LogFactory.getLog(EventProcessingAppDeployer.class);

    private Map<String, Boolean> acceptanceList = null;

    List<Artifact> eventBuilders = new ArrayList<Artifact>();
    List<Artifact> eventFormatters = new ArrayList<Artifact>();
    List<Artifact> executionPlans = new ArrayList<Artifact>();
    List<Artifact> inputEventAdaptors = new ArrayList<Artifact>();
    List<Artifact> outputEventAdaptors = new ArrayList<Artifact>();
    List<Artifact> eventStreams = new ArrayList<Artifact>();

    /**
     * @param carbonApp  - CarbonApplication instance to check for Event Processing artifacts
     * @param axisConfig - AxisConfiguration of the current tenant
     */
    public void deployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig)
            throws DeploymentException {

        List<Artifact.Dependency> artifacts =
                carbonApp.getAppConfig().getApplicationArtifact().getDependencies();

        for (Artifact.Dependency dep : artifacts) {
            Artifact artifact = dep.getArtifact();
            if (!validateArtifact(artifact)) {
                continue;
            }
            addArtifact(artifact);
        }

        try {
            //deployEventStreams();
            deployTypeSpecifiedArtifacts(eventStreams, axisConfig,
                    EventProcessingAppDeployerConstants.CEP_EVENT_STREAM_DIR, EventProcessingAppDeployerConstants.FILE_TYPE_JSON);
            deployTypeSpecifiedArtifacts(inputEventAdaptors, axisConfig,
                    EventProcessingAppDeployerConstants.CEP_INPUT_EVENT_ADAPTOR_DIR, EventProcessingAppDeployerConstants.FILE_TYPE_XML);
            deployTypeSpecifiedArtifacts(eventBuilders, axisConfig, EventProcessingAppDeployerConstants.CEP_EVENT_BUILDER_DIR,
                    EventProcessingAppDeployerConstants.FILE_TYPE_XML);
            deployTypeSpecifiedArtifacts(outputEventAdaptors, axisConfig,
                    EventProcessingAppDeployerConstants.CEP_OUTPUT_EVENT_ADAPTOR_DIR, EventProcessingAppDeployerConstants.FILE_TYPE_XML);
            deployTypeSpecifiedArtifacts(eventFormatters, axisConfig, EventProcessingAppDeployerConstants.CEP_EVENT_FORMATTER_DIR,
                    EventProcessingAppDeployerConstants.FILE_TYPE_XML);
            deployTypeSpecifiedArtifacts(executionPlans, axisConfig, EventProcessingAppDeployerConstants.CEP_EXECUTION_PLAN_DIR,
                    EventProcessingAppDeployerConstants.FILE_TYPE_XML);
        } finally {
            eventBuilders.clear();
            eventFormatters.clear();
            executionPlans.clear();
            inputEventAdaptors.clear();
            outputEventAdaptors.clear();
            eventStreams.clear();
        }
    }

    /*private void deployEventStreams() throws DeploymentException {
        for(Artifact artifact: eventStreams) {
            String path = artifact.getExtractedPath() + File.separator + artifact.getFiles().get(0).getName();
            try {
                String content = new Scanner(new File(path)).useDelimiter("\\Z").next();
                StreamDefinition streamDefinition = EventDefinitionConverterUtils.convertFromJson(content);
                ServiceHolder.getEventStreamStoreService().addEventStreamDefinition(streamDefinition,
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            } catch (Exception e) {
                artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                log.error("Deployment is failed due to " + e.getMessage(), e);
                throw new DeploymentException("Failed deploying event stream file "
                        + artifact.getName() + " due to " + e.getMessage(), e);
            }
        }
    }*/

    private void deployTypeSpecifiedArtifacts(List<Artifact> artifacts, AxisConfiguration axisConfig, String directory,
                                              String fileType) throws DeploymentException {
        for(Artifact artifact: artifacts) {
            EventProcessingDeployer deployer;

            deployer = (EventProcessingDeployer)AppDeployerUtils.getArtifactDeployer(axisConfig,
                    directory, fileType);
            if(deployer!=null) {
                deploy(deployer,artifact);
            }
        }
    }

    void deploy(EventProcessingDeployer deployer, Artifact artifact) throws DeploymentException {
        List<CappFile> files = artifact.getFiles();
        String fileName = artifact.getFiles().get(0).getName();
        String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
        try {
            deployer.processDeployment(new DeploymentFileData(new File(artifactPath), deployer));
            artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED);
        } catch (Exception e) {
            artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
            log.error("Deployment is failed due to " + e.getMessage(), e);
            throw new DeploymentException(e.getMessage(), e);
        }
    }

    private void addArtifact(Artifact artifact) {
        if (EventProcessingAppDeployerConstants.CEP_EVENT_BUILDER_TYPE.equals(artifact.getType())) {
            eventBuilders.add(artifact);
        } else if (EventProcessingAppDeployerConstants.CEP_EVENT_FORMATTER_TYPE.equals(artifact.getType())) {
            eventFormatters.add(artifact);
        } else if (EventProcessingAppDeployerConstants.CEP_EXECUTION_PLAN_TYPE.equals(artifact.getType())) {
            executionPlans.add(artifact);
        } else if (EventProcessingAppDeployerConstants.CEP_INPUT_EVENT_ADAPTOR_TYPE.equals(artifact.getType())) {
            inputEventAdaptors.add(artifact);
        } else if (EventProcessingAppDeployerConstants.CEP_OUTPUT_EVENT_ADAPTOR_TYPE.equals(artifact.getType())) {
            outputEventAdaptors.add(artifact);
        } else if (EventProcessingAppDeployerConstants.CEP_EVENT_STREAM_TYPE.equals(artifact.getType())) {
            eventStreams.add(artifact);
        }
    }

    public void undeployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig) {
        List<Artifact.Dependency> artifacts = carbonApp.getAppConfig().getApplicationArtifact()
                .getDependencies();

        for (Artifact.Dependency dep : artifacts) {
            EventProcessingDeployer deployer;
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }

            deployer = getDeployer(artifact,axisConfig);

            List<CappFile> files = artifact.getFiles();
            if (files.size() != 1) {
                log.error("Artifact must have only a single file. But " +
                        files.size() + " files found.");
                continue;
            }

            if (deployer != null && AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED.
                    equals(artifact.getDeploymentStatus())) {
                String fileName = artifact.getFiles().get(0).getName();
                String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
                try {
                    deployer.processUndeployment(artifactPath);
                    artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_PENDING);
                    File artifactFile = new File(artifactPath);
                    if (artifactFile.exists() && !artifactFile.delete()) {
                        log.warn("Couldn't delete App artifact file : " + artifactPath);
                    }
                } catch (Exception e) {
                    artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                    log.error("Error occured while trying to undeploy : " + artifact.getName() + " due to " + e.getMessage(), e);
                }
            }
        }
    }

    private EventProcessingDeployer getDeployer(Artifact artifact, AxisConfiguration axisConfig) {
        Deployer deployer;
        if (EventProcessingAppDeployerConstants.CEP_EVENT_BUILDER_TYPE.equals(artifact.getType())) {
            deployer =  AppDeployerUtils.getArtifactDeployer(axisConfig, EventProcessingAppDeployerConstants.CEP_EVENT_BUILDER_DIR, "xml");
        } else if(EventProcessingAppDeployerConstants.CEP_EVENT_FORMATTER_TYPE.equals(artifact.getType())) {
            deployer =  AppDeployerUtils.getArtifactDeployer(axisConfig, EventProcessingAppDeployerConstants.CEP_EVENT_FORMATTER_DIR, "xml");
        } else if(EventProcessingAppDeployerConstants.CEP_EXECUTION_PLAN_TYPE.equals(artifact.getType())) {
            deployer = AppDeployerUtils.getArtifactDeployer(axisConfig, EventProcessingAppDeployerConstants.CEP_EXECUTION_PLAN_DIR, "xml");
        } else if(EventProcessingAppDeployerConstants.CEP_INPUT_EVENT_ADAPTOR_TYPE.equals(artifact.getType())) {
            deployer =  AppDeployerUtils.getArtifactDeployer(axisConfig, EventProcessingAppDeployerConstants.CEP_INPUT_EVENT_ADAPTOR_DIR, "xml");
        } else if(EventProcessingAppDeployerConstants.CEP_OUTPUT_EVENT_ADAPTOR_TYPE.equals(artifact.getType())) {
            deployer =  AppDeployerUtils.getArtifactDeployer(axisConfig, EventProcessingAppDeployerConstants.CEP_OUTPUT_EVENT_ADAPTOR_DIR, "xml");
        } else if (EventProcessingAppDeployerConstants.CEP_EVENT_STREAM_TYPE.equals(artifact.getType())) {
            deployer =  AppDeployerUtils.getArtifactDeployer(axisConfig, EventProcessingAppDeployerConstants.CEP_EVENT_STREAM_DIR, "json");
        } else {
            deployer = null;
        }
        return (EventProcessingDeployer)deployer;
    }

    private boolean isAccepted(String serviceType) {
        if (acceptanceList == null) {
            acceptanceList = AppDeployerUtils.buildAcceptanceList(EventProcessingAppDeployerDS
                    .getRequiredFeatures());
        }
        Boolean acceptance = acceptanceList.get(serviceType);
        return (acceptance == null || acceptance);
    }

    private boolean validateArtifact(Artifact artifact) {
        if (artifact == null) {
            return false;
        }

        if (!isAccepted(artifact.getType())) {
            log.warn("Can't deploy artifact : " + artifact.getName() + " of type : " +
                    artifact.getType() + ". Required features are not installed in the system");
            return false;
        }

        List<CappFile> files = artifact.getFiles();
        if (files.size() != 1) {
            log.error("Synapse artifact types must have a single file to " +
                    "be deployed. But " + files.size() + " files found.");
            return false;
        }

        return true;
    }
}
