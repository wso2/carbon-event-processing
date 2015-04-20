/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
function deleteConfiguration(domainName, configurationName, row, tableId) {

    CARBON.showConfirmationDialog(
        "Are you sure want to delete?", function () {
            $.ajax({
                type: "POST",
                url: "save_configurations_ajaxprocessor.jsp",
                data: "domainName=" + domainName + "&configurationName=" + configurationName + "&saveType=delete"
            })
                .error(function () {
                    CARBON.showErrorDialog("Error occurred when deleting configurations");
                })
                .then(function () {
                    document.getElementById(tableId).deleteRow(row.parentNode.parentNode.rowIndex);
                    CARBON.showInfoDialog("Configurations deleted successfully");
                });
        }, null, null);
}

function saveConfiguration(domainName, templateType, configurationName, description, redirectURL, parameters) {
    $.ajax({
        type: "POST",
        url: "save_configurations_ajaxprocessor.jsp",
        data: "domainName=" + domainName + "&configurationName=" + configurationName + "&templateType=" + templateType
            + "&description=" + description + "&saveType=save" + "&parameters=" + parameters
    })
        .error(function () {
            CARBON.showErrorDialog("Error occurred when saving configurations");
        })
        .then(function () {
            CARBON.showInfoDialog("Configurations saved successfully",
                function () {
                    document.location.href = redirectURL;
                });

        });

}