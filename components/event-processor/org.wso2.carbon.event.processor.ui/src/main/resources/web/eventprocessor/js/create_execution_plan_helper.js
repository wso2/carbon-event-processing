/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

function validateQueries() {
    var executionPlan = window.queryEditor.getValue();

    if (executionPlan == "") {
        CARBON.showErrorDialog("Query expressions cannot be empty.");
        return;
    }

    new Ajax.Request('../eventprocessor/validate_siddhi_queries_ajaxprocessor.jsp', {
        method:'POST',
        asynchronous:false,
        parameters:{executionPlan:executionPlan },
        onSuccess:function (callbackMessage) {
            var resultText = callbackMessage.responseText.trim();
            if (resultText == "success") {
                CARBON.showInfoDialog("Queries are valid!");
                return;
            } else {
                CARBON.showErrorDialog(resultText);
                return;
            }
        }
    });
}

function createImportedStreamDefinition(element) {
    var selectedVal = element.options[element.selectedIndex].value;
    if (selectedVal == 'createStreamDef') {
        new Ajax.Request('../eventstream/popup_create_event_stream_ajaxprocessor.jsp', {
            method:'POST',
            asynchronous:false,
            parameters:{callback:"inflow"},
            onSuccess:function (data) {
                showCustomPopupDialog(data.responseText, "Create Stream Definition", "80%", "", onSuccessCreateInflowStreamDefinition, "90%");
            }
        });
    }
}

function importedStreamDefSelectClick(element) {
    if (element.length <= 1) {
        createImportedStreamDefinition(element);
    }
}

function exportedStreamDefSelectClick(element) {
    if (element.length <= 1) {
        createExportedStreamDefinition(element);
    }
}

function createExportedStreamDefinition(element) {

    var selectedVal = element.options[element.selectedIndex].value;
    if (selectedVal == 'createStreamDef') {

        var executionPlan = window.queryEditor.getValue();

        if (executionPlan == "") {
            CARBON.showErrorDialog("Execution plan cannot be empty.");
            return;
        }

        var valueOf = document.getElementById("exportedStreamValueOf").value;

        if (valueOf == "") {
            CARBON.showErrorDialog("Please fill the 'Value of' field with a valid stream name. It cannot be empty.");
            return;
        }

        var jsonStreamDefinition = "";

        new Ajax.Request('../eventprocessor/export_siddhi_stream_ajaxprocessor.jsp', {
            method:'POST',
            asynchronous:false,
            parameters:{executionPlan:executionPlan, targetStream:valueOf },
            onSuccess:function (data) {
                jsonStreamDefinition = data.responseText;
            }
        });

        if (jsonStreamDefinition) {
            jsonStreamDefinition = jsonStreamDefinition.replace(/^\s+|\s+$/g, '');
            if (jsonStreamDefinition == "") {
                CARBON.showErrorDialog("No matching stream definition can be found for: " + valueOf + ". " +
                                       "Please fill the 'Value of' field with a valid stream name.");
                return;
            }
        } else {
            CARBON.showErrorDialog("No matching stream definition can be found for: " + valueOf + ". " +
                                   "Please fill the 'Value of' field with a valid stream name.");
            return;
        }


        new Ajax.Request('../eventstream/popup_create_event_stream_ajaxprocessor.jsp', {
            method:'POST',
            asynchronous:false,
            parameters:{streamDef:jsonStreamDefinition, callback:"outflow"},
            onSuccess:function (data) {
                showCustomPopupDialog(data.responseText, "Create Stream Definition", "80%", "", onSuccessCreateOutflowStreamDefinition, "90%");
            }
        });
    }
}


function addImportedStreamDefinition() {
    var propStreamId = document.getElementById("importedStreamId");
    var propAs = document.getElementById("importedStreamAs");

    var error = "";
    if (propStreamId.value == "" || propStreamId.value == "createStreamDef") {
        error = "Invalid Stream ID selected.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }

    if (propAs.value.trim() == "") {
        propAs.value = propStreamId.value.trim().split(':')[0];
        propAs.value = propAs.value.replace(/\./g, '_');
    }

    new Ajax.Request('../eventprocessor/get_stream_definition_ajaxprocessor.jsp', {
        method:'POST',
        asynchronous:false,
        parameters:{streamId:propStreamId.value, streamAs:propAs.value },
        onSuccess:function (eventStreamDefinition) {
            var definitions = eventStreamDefinition.responseText.trim().split("|=");
            var streamId = definitions[0].trim();
            var streamAs = definitions[1].trim();
            var streamDefinition = definitions[3].trim();

            var currentExecutionPlan = window.queryEditor.getValue();
            var executionPlanHeader = "";
            var executionPlanBody = "";
            if(currentExecutionPlan != "") {
                var linesArray = currentExecutionPlan.split(SIDDHI_LINE_BREAK_CHARACTER);
                var bodyBeginIndex = -1;
                for (var i = 0; i < linesArray.length ; i++){
                    var line = linesArray[i];
                    if(line.match(REGEX_LINE_STARTING_WITH_PLAN) != null
                        || line.match(REGEX_LINE_STARTING_WITH_SINGLE_LINE_COMMENT) != null
                        || line.match(REGEX_LINE_STARTING_WITH_MULTI_LINE_COMMENT) != null
                        || line == ""){
                        executionPlanHeader += line + SIDDHI_LINE_BREAK;
                    } else {
                        bodyBeginIndex = i;
                        break;
                    }
                }
                if(bodyBeginIndex != -1){
                    for (var i = bodyBeginIndex; i < linesArray.length ; i++){
                        var line = linesArray[i];
                        executionPlanBody += SIDDHI_LINE_BREAK + line;
                    }
                }
            }
            var importStatement = ANNOTATION_TOKEN_AT +
                ANNOTATION_IMPORT +
                ANNOTATION_TOKEN_OPENING_BRACKET +
                SIDDHI_SINGLE_QUOTE + streamId + SIDDHI_SINGLE_QUOTE +
                ANNOTATION_TOKEN_CLOSING_BRACKET +
                SIDDHI_LINE_BREAK +
                SIDDHI_LITERAL_DEFINE_STREAM + SIDDHI_SPACE_LITERAL + streamAs + SIDDHI_SPACE_LITERAL +
                ANNOTATION_TOKEN_OPENING_BRACKET +
                streamDefinition +
                ANNOTATION_TOKEN_CLOSING_BRACKET +
                SIDDHI_STATEMENT_DELIMETER;
            window.queryEditor.setValue(executionPlanHeader + importStatement + SIDDHI_LINE_BREAK + executionPlanBody);
            window.queryEditor.save();
        }
    });
    propAs.value = "";
}


function addExportedStreamDefinition() {
    var propStreamId = document.getElementById("exportedStreamId");
    var propValueOf = document.getElementById("exportedStreamValueOf");
    var streamDefinitionsTable = document.getElementById("streamDefinitionsTable");

    var error = "";

    if (propStreamId.value == "" || propStreamId.value == "createStreamDef") {
        error = "Invalid Stream ID selected.\n";
    }

    if (propValueOf.value == "") {
        error = "Value Of field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }

    new Ajax.Request('../eventprocessor/get_stream_definition_ajaxprocessor.jsp', {
        method:'POST',
        asynchronous:false,
        parameters:{streamId:propStreamId.value, streamAs:propValueOf.value },
        onSuccess:function (eventStreamDefinition) {
            var definitions = eventStreamDefinition.responseText.trim().split("|=");
            var streamId = definitions[0].trim();
            var streamAs = definitions[1].trim();
            var streamDefinition = definitions[3].trim();

            var currentExecutionPlan = window.queryEditor.getValue();
            var executionPlanHeader = "";
            var executionPlanBody = "";

            if(currentExecutionPlan != "") {
                var linesArray = currentExecutionPlan.split(SIDDHI_LINE_BREAK_CHARACTER);
                var bodyBeginIndex = -1;
                for (var i = 0; i < linesArray.length ; i++){
                    var line = linesArray[i];
                    if(line.match(REGEX_LINE_STARTING_WITH_PLAN) != null
                        || line.match(REGEX_LINE_STARTING_WITH_SINGLE_LINE_COMMENT) != null
                        || line.match(REGEX_LINE_STARTING_WITH_MULTI_LINE_COMMENT) != null
                        || line == ""
                        || line.match(REGEX_LINE_STARTING_WITH_IMPORT_STATEMENT) != null){
                        if(line.match(REGEX_LINE_STARTING_WITH_IMPORT_STATEMENT) != null){
                            executionPlanHeader += line + SIDDHI_LINE_BREAK;
                            executionPlanHeader += linesArray[++i] + SIDDHI_LINE_BREAK;
                        } else {
                            executionPlanHeader += line + SIDDHI_LINE_BREAK;
                        }
                    } else {
                        bodyBeginIndex = i;
                        break;
                    }
                }
                if(bodyBeginIndex != -1){
                    for (var i = bodyBeginIndex; i < linesArray.length ; i++){
                        var line = linesArray[i];
                        executionPlanBody += SIDDHI_LINE_BREAK + line;
                    }
                }
            }
            var exportStatement = ANNOTATION_TOKEN_AT + ANNOTATION_EXPORT + ANNOTATION_TOKEN_OPENING_BRACKET +
                SIDDHI_SINGLE_QUOTE + streamId + SIDDHI_SINGLE_QUOTE + ANNOTATION_TOKEN_CLOSING_BRACKET +
                SIDDHI_LINE_BREAK + SIDDHI_LITERAL_DEFINE_STREAM + SIDDHI_SPACE_LITERAL + streamAs + SIDDHI_SPACE_LITERAL +
                ANNOTATION_TOKEN_OPENING_BRACKET + streamDefinition + ANNOTATION_TOKEN_CLOSING_BRACKET + SIDDHI_STATEMENT_DELIMETER;
            window.queryEditor.setValue(executionPlanHeader + exportStatement + SIDDHI_LINE_BREAK + executionPlanBody);
            window.queryEditor.save();
        }
    });
    propValueOf.value = "";
}


function addExecutionPlan(form) {
    // query expressions can be empty for pass thru execution plans...?
    var executionPlan = window.queryEditor.getValue();
    if (executionPlan == "") {
        CARBON.showErrorDialog("Query expressions cannot be empty.");
        return;
    }

    new Ajax.Request('../eventprocessor/validate_siddhi_queries_ajaxprocessor.jsp', {
        method:'POST',
        asynchronous:false,
        parameters:{executionPlan:executionPlan },
        onSuccess:function (callbackMessage) {
            var resultText = callbackMessage.responseText.trim();
            if (resultText == "success") {
                makeAsyncCallToCreateExecutionPlan(form, executionPlan);
            } else {
                CARBON.showErrorDialog(resultText);
                return;
            }
        }
    });
}

function getDuplicatedStreams() {
    var exportedStreamsTable = document.getElementById("exportedStreamsTable");
    var StreamTable = document.getElementById("streamDefinitionsTable");

    var importedStreamCount = 0;
    var exportedStreamCount = 0;
    var importedStreamArray = new Array();
    var exportedStreamArray = new Array();

    for (var i = 0; i < StreamTable.rows.length; i++) {
        var row = StreamTable.rows[i];
        if (row.getAttribute("streamType") == "imported") {
            importedStreamArray[importedStreamCount] = row.getAttribute("streamId");
            importedStreamCount++;
        } else if (row.getAttribute("streamType") == "exported") {
            exportedStreamArray[exportedStreamCount] = row.getAttribute("streamId");
            exportedStreamCount++;
        }
    }

    var reusedStreams = "";

    if (exportedStreamCount == 0) {
        return "no-export-streams";
    }
    else if (importedStreamCount > 0 && exportedStreamCount > 0) {
        for (var i = 0; i < exportedStreamCount; i++) {
            var exportedStreamId = exportedStreamArray[i];
            for (var j = 0; j < importedStreamCount; j++) {
                var importedStreamId = importedStreamArray[j];
                if (importedStreamId == exportedStreamId) {
                    if (reusedStreams != "") {
                        reusedStreams = reusedStreams + "\n"
                    }
                    reusedStreams = reusedStreams + importedStreamId;
                }
            }
        }
    }
    return reusedStreams;

}


function makeAsyncCallToCreateExecutionPlan(form, queryExpressions) {
    new Ajax.Request('../eventprocessor/add_execution_plan_ajaxprocessor.jsp', {
        method:'POST',
        asynchronous:false,
        parameters:{queryExpressions:queryExpressions },
        onSuccess:function (transport) {
            if ("true" == transport.responseText.trim()) {
                form.submit();
            } else {
                CARBON.showErrorDialog("Failed to add execution plan, Exception: " + transport.responseText.trim());
            }
        }
    });
}

function loadUIElements(uiId) {
    var uiElementTd = document.getElementById("uiElement");
    uiElementTd.innerHTML = "";

    jQuery.ajax({
        type:"POST",
        url:"../eventexecutionplanwizard/get_ui_ajaxprocessor.jsp?uiId=" + uiId + "",
        data:{},
        contentType:"text/html; charset=utf-8",
        dataType:"text",
        async:false,
        success:function (ui_content) {
            if (ui_content != null) {
                jQuery(uiElementTd).html(ui_content);

                if (uiId == 'builder') {
                    var innertTD = document.getElementById('addEventAdaptorTD');
                    jQuery(innertTD).html('<a onclick=\'loadUIElements("inputAdaptor") \'style=\'background-image:url(images/add.gif);\' class="icon-link" href="#" \'> Add New Input Event Adaptor </a>')
                }

                else if (uiId == 'formatter') {
                    var innertTD = document.getElementById('addOutputEventAdaptorTD');
                    jQuery(innertTD).html('<a onclick=\'loadUIElements("outputAdaptor") \'style=\'background-image:url(images/add.gif);\' class="icon-link" href="#" \'> Add New Out Event Adaptor </a>')
                }

                else if (uiId == 'processor') {
                    var innertTD = document.getElementById('addEventStreamTD');
                    jQuery(innertTD).html('<a onclick=\'loadUIElements("builder") \'style=\'background-image:url(images/add.gif);\' class="icon-link" href="#" \'> Add New Event Stream </a>')
                }

            }
        }
    });
}


