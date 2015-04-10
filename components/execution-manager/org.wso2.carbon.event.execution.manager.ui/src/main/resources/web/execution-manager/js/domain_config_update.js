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
function deleteConfiguration(domainName, configurationName) {

    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open("POST", "save_configurations_ajaxprocessor.jsp");
    xmlHttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    xmlHttp.send("domainName=" + domainName + "&configurationName=" + configurationName + "&saveType=delete");

}


function saveConfiguration(domainName, configurationName, description, parameters) {

    /* var xmlHttp = new XMLHttpRequest();
     xmlHttp.open("POST", "save_configurations_ajaxprocessor.jsp");
     xmlHttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
     xmlHttp.send("domainName=" + domainName + "&configurationName=" + configurationName
     + "&description=" + description + "&saveType=save" + "&parameters=" + parameters);
     */

    $.ajax({
        type: "POST",
        url: "save_configurations_ajaxprocessor.jsp",
        data: "domainName=" + domainName + "&configurationName=" + configurationName
            + "&description=" + description + "&saveType=save" + "&parameters=" + parameters
        //,
        //dataType: "application/x-www-form-urlencoded"
    })
        .error(function (xhr, err, status) {
            alert("Error Occurred");
        })
        .then(function (data) {
            alert("Success");
        });

}