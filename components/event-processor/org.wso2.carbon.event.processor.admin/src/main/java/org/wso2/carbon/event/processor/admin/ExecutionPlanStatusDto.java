package org.wso2.carbon.event.processor.admin;

public class ExecutionPlanStatusDto {
    private String executionPlanName;
    private String statusInStorm;

    public ExecutionPlanStatusDto(String executionPlanName, String statusInStorm){
        this.executionPlanName = executionPlanName;
        this.statusInStorm = statusInStorm;
    }

    public String getStatusInStorm() {
        return statusInStorm;
    }

    public void setStatusInStorm(String statusInStorm) {
        this.statusInStorm = statusInStorm;
    }

    public String getExecutionPlanName() {
        return executionPlanName;
    }

    public void setExecutionPlanName(String executionPlanName) {
        this.executionPlanName = executionPlanName;
    }

}
