/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function sendFileDetails(fileName) {

    jQuery.ajax({
        type: "POST",
        url: "../eventsimulator/sendFileDetail_ajaxprocessor.jsp?fileName=" + fileName + "&mode=send",
        async: true,
        success: function (msg) {
            handleFileCallback(fileName, "play");
            CARBON.showInfoDialog("Events sending using file is started .......");
        }

    });
}

function pauseEventsViaFile(fileName) {

    jQuery.ajax({
        type: "POST",
        url: "../eventsimulator/sendFileDetail_ajaxprocessor.jsp?fileName=" + fileName + "&mode=pause",
        async: true,
        success: function (msg) {
            handleFileCallback(fileName, "pause");
            CARBON.showInfoDialog("Pausing events sending process .......");
        }

    });
}

function resumeEventsViaFile(fileName) {

    jQuery.ajax({
        type: "POST",
        url: "../eventsimulator/sendFileDetail_ajaxprocessor.jsp?fileName=" + fileName + "&mode=resume",
        async: true,
        success: function (msg) {
            handleFileCallback(fileName, "resume");
            CARBON.showInfoDialog("Resuming events sending process .......");
        }

    });
}

function stopEventsViaFile(fileName) {

    jQuery.ajax({
        type: "POST",
        url: "../eventsimulator/sendFileDetail_ajaxprocessor.jsp?fileName=" + fileName + "&mode=stop",
        async: true,
        success: function (msg) {
            handleFileCallback(fileName, "stop");
            CARBON.showInfoDialog("Stopping events sending process .......");
        }

    });
}

function deleteFile(fileName) {

    jQuery.ajax({
        type: "POST",
        url: "../eventsimulator/deleteFile_ajaxprocessor.jsp?fileName=" + fileName + "",
        async: false,
        success: function (msg) {
            if (msg != null && msg.trim() == "deleted") {
                CARBON.showInfoDialog("File is successfully deleted, please refresh the page to see the changes");
            } else {
                CARBON.showErrorDialog(msg);
            }
        }
    });
}

function handleFileCallback(fileName, action) {
    var element;
    if (action == "play") {
        element = document.getElementById("fileSimulationPlay" + fileName);
        element.style.display = "none";
        element = document.getElementById("fileSimulationResume" + fileName);
        element.style.display = "none";
        element = document.getElementById("fileSimulationPause" + fileName);
        element.style.display = "";
        element = document.getElementById("fileSimulationStop" + fileName);
        element.style.display = "";
    } else if(action == "pause"){
        element = document.getElementById("fileSimulationPause" + fileName);
        element.style.display = "none";
        element = document.getElementById("fileSimulationPlay" + fileName);
        element.style.display = "none";
        element = document.getElementById("fileSimulationResume" + fileName);
        element.style.display = "";
        element = document.getElementById("fileSimulationStop" + fileName);
        element.style.display = "";
    } else if(action == "resume"){
        element = document.getElementById("fileSimulationPlay" + fileName);
        element.style.display = "none";
        element = document.getElementById("fileSimulationResume" + fileName);
        element.style.display = "none";
        element = document.getElementById("fileSimulationPause" + fileName);
        element.style.display = "";
        element = document.getElementById("fileSimulationStop" + fileName);
        element.style.display = "";
    } else{
        element = document.getElementById("fileSimulationStop" + fileName);
        element.style.display = "none";
        element = document.getElementById("fileSimulationPause" + fileName);
        element.style.display = "none";
        element = document.getElementById("fileSimulationResume" + fileName);
        element.style.display = "none";
        element = document.getElementById("fileSimulationPlay" + fileName);
        element.style.display = "";
    }
}