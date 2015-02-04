package org.wso2.carbon.event.processing.application.deployer;

import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;

public interface EventProcessingDeployer extends Deployer {
    public void processDeployment(DeploymentFileData deploymentFileData) throws Exception;
    public void processUndeployment(String filePath) throws Exception;
}
