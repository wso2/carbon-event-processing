/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

function removeExportedStream(link) {
    var rowToRemove = link.parentNode.parentNode;
    rowToRemove.parentNode.removeChild(rowToRemove);
    CARBON.showInfoDialog("Exported stream removed successfully!!");
    return;
}


function removeImportedStreamDefinition(link) {
    var rowToRemove = link.parentNode.parentNode;
    rowToRemove.parentNode.removeChild(rowToRemove);
    CARBON.showInfoDialog("Imported stream removed successfully!!");
    return;
}

function createEventBuilder(streamNameWithVersion) {
    if (streamNameWithVersion == '' || streamNameWithVersion == null) {
        CARBON.showErrorDialog("No stream definition selected.");
        return;
    }
    new Ajax.Request('../eventbuilder/popup_create_event_builder_ajaxprocessor.jsp', {
        method:'POST',
        asynchronous:false,
        parameters:{streamNameWithVersion:streamNameWithVersion, redirectPage:"none"},
        onSuccess:function (data) {
            showCustomPopupDialog(data.responseText, "Create Event Builder", "80%", "", onSuccessCreateEventBuilder, "90%");
        }
    });
}

function createEventFormatter(streamId) {
    if (streamId != '' && streamId != null) {
        new Ajax.Request('../eventformatter/popup_create_event_formatter_ajaxprocessor.jsp', {
            method:'POST',
            parameters:{streamId:streamId, redirectPage:"none"},
            asynchronous:false,
            onSuccess:function (data) {
                showCustomPopupDialog(data.responseText, "Create Event Formatter", "80%", "", onSuccessCreateEventFormatter, "90%");
            }
        });
    }
}

/**
 * Display the Info Message inside a jQuery UI's dialog widget.
 * @method showPopupDialog
 * @param {String} message to display
 * @return {Boolean}
 */
function showCustomPopupDialog(message, title, windowHight, okButton, callback, windowWidth) {
    var strDialog = "<div id='custom_dialog' title='" + title + "'><div id='popupDialog'></div>" + message + "</div>";
    var requiredWidth = 750;
    if (windowWidth) {
        requiredWidth = windowWidth;
    }
    var func = function () {
        jQuery("#custom_dcontainer").hide();
        jQuery("#custom_dcontainer").html(strDialog);
        if (okButton) {
            jQuery("#custom_dialog").dialog({
                                                close:function () {
                                                    jQuery(this).dialog('destroy').remove();
                                                    jQuery("#custom_dcontainer").empty();
                                                    return false;
                                                },
                                                buttons:{
                                                    "OK":function () {
                                                        if (callback && typeof callback == "function") {
                                                            callback();
                                                        }
                                                        jQuery(this).dialog("destroy").remove();
                                                        jQuery("#custom_dcontainer").empty();
                                                        return false;
                                                    }
                                                },
                                                autoOpen:false,
                                                height:windowHight,
                                                width:requiredWidth,
                                                minHeight:windowHight,
                                                minWidth:requiredWidth,
                                                modal:true
                                            });
        } else {
            jQuery("#custom_dialog").dialog({
                                                close:function () {
                                                    if (callback && typeof callback == "function") {
                                                        callback();
                                                    }
                                                    jQuery(this).dialog('destroy').remove();
                                                    jQuery("#custom_dcontainer").empty();
                                                    return false;
                                                },
                                                autoOpen:false,
                                                height:windowHight,
                                                width:requiredWidth,
                                                minHeight:windowHight,
                                                minWidth:requiredWidth,
                                                modal:true
                                            });
        }

        jQuery('.ui-dialog-titlebar-close').click(function () {
            jQuery('#custom_dialog').dialog("destroy").remove();
            jQuery("#custom_dcontainer").empty();
            jQuery("#custom_dcontainer").html('');
        });
        jQuery("#custom_dcontainer").show();
        jQuery("#custom_dialog").dialog("open");
    };
    if (!pageLoaded) {
        jQuery(document).ready(func);
    } else {
        func();
    }

}


function onSuccessCreateEventFormatter() {
    refreshEventFormatterInfo("eventFormatter");
}

function onSuccessCreateEventBuilder() {
    refreshEventBuilderInfo("eventBuilder");
}

function onSuccessCreateInflowStreamDefinition(streamId) {
    refreshStreamDefInfo("importedStreamId");
    refreshStreamDefInfo("exportedStreamId");
    CARBON.customConfirmDialogBox("The defined event stream can be populated with in flow of events using an event builder.\nDo you want to create an event builder now? ", "Default WSO2Event Builder", "Custom Event Builder", function (option) {
        if (option == "custom") {
            createEventBuilder(streamId);
        } else {
            new Ajax.Request('../eventbuilder/add_default_event_builder_ajaxprocessor.jsp', {
                method:'POST',
                asynchronous:false,
                parameters:{eventStreamId:streamId},
                onSuccess:function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showInfoDialog("Default Event Builder added successfully!!");
                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + response.responseText.trim());
                    }
                }
            });
        }

    }, null);
}

function onSuccessCreateOutflowStreamDefinition(streamId) {
    refreshStreamDefInfo("importedStreamId");
    refreshStreamDefInfo("exportedStreamId");

    CARBON.customConfirmDialogBox("Defined event streams can publish out flow of events using event formatter.\nDo you want to  create an event formatter now? ", "Default LoggerAdaptor Event Formatter", "Custom Event Formatter", function (option) {
        if (option == "custom") {
            createEventFormatter(streamId);
        } else {
            new Ajax.Request('../eventformatter/add_default_event_formatter_ajaxprocessor.jsp', {
                method:'POST',
                asynchronous:false,
                parameters:{eventStreamId:streamId},
                onSuccess:function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showInfoDialog("Default Event formatter added successfully!!");
                    } else {
                        CARBON.showErrorDialog("Failed to add event formatter, Exception: " + response.responseText.trim());
                    }
                }
            });
        }

    }, null);
}


CARBON.customConfirmDialogBox = function (message, option1, option2, callback, closeCallback) {
    var strDialog = "<div id='dialog' title='WSO2 Carbon'><div id='messagebox-info' style='height:90px'><p>" +
                    message + "</p> <br/><input id='dialogRadio1' name='dialogRadio' type='radio' value='default' checked />" + option1 + "<br/> <input id='dialogRadio2' name='dialogRadio' type='radio' value='custom' />" + option2 + "</div></div>";
    var func = function () {
        jQuery("#dcontainer").html(strDialog);

        jQuery("#dialog").dialog({
                                     close:function () {
                                         jQuery(this).dialog('destroy').remove();
                                         jQuery("#dcontainer").empty();
                                         if (closeCallback && typeof closeCallback == "function") {
                                             closeCallback();
                                         }
                                         return false;
                                     },

                                     buttons:{
                                         "OK":function () {
                                             var value = jQuery('input[name=dialogRadio]:checked').val();
                                             jQuery(this).dialog("destroy").remove();
                                             jQuery("#dcontainer").empty();
                                             if (callback && typeof callback == "function") {
                                                 callback(value);
                                             }
                                             return false;
                                         },
                                         "Create Later":function () {
                                             jQuery(this).dialog('destroy').remove();
                                             jQuery("#dcontainer").empty();
                                             if (closeCallback && typeof closeCallback == "function") {
                                                 closeCallback();
                                             }
                                             return false;
                                         }
                                     },

                                     height:200,
                                     width:500,
                                     minHeight:200,
                                     minWidth:330,
                                     modal:true
                                 });
    };
    if (!pageLoaded) {
        jQuery(document).ready(func);
    } else {
        func();
    }

};


function refreshEventFormatterInfo(eventFormatterSelectId) {
    var eventFormatterSelect = document.getElementById(eventFormatterSelectId);
    new Ajax.Request('../eventformatter/get_active_event_formatters_ajaxprocessor.jsp', {
        method:'POST',
        asynchronous:false,
        onSuccess:function (event) {
            eventFormatterSelect.length = 0;
            // for each property, add a text and input field in a row
            var jsonArrEventFormatterNames = JSON.parse(event.responseText);
            for (i = 0; i < jsonArrEventFormatterNames.length; i++) {
                var eventFormatterName = jsonArrEventFormatterNames[i];
                if (eventFormatterName != undefined && eventFormatterName != "") {
                    eventFormatterName = eventFormatterName.trim();
                    eventFormatterSelect.add(new Option(eventFormatterName, eventFormatterName), null);
                }
            }

        }
    });
}

function refreshEventBuilderInfo(eventBuilderSelectId) {
    var eventBuilderSelect = document.getElementById(eventBuilderSelectId);
    new Ajax.Request('../eventbuilder/get_active_event_builders_ajaxprocessor.jsp', {
        method:'POST',
        asynchronous:false,
        onSuccess:function (event) {
            eventBuilderSelect.length = 0;
            // for each property, add a text and input field in a row
            var jsonArrEventBuilderNames = JSON.parse(event.responseText);
            for (i = 0; i < jsonArrEventBuilderNames.length; i++) {
                var eventBuilderName = jsonArrEventBuilderNames[i];
                if (eventBuilderName != undefined && eventBuilderName != "") {
                    eventBuilderName = eventBuilderName.trim();
                    eventBuilderSelect.add(new Option(eventBuilderName, eventBuilderName), null);
                }
            }

        }
    });
}

function refreshStreamDefInfo(streamDefSelectId) {
    var streamDefSelect = document.getElementById(streamDefSelectId);
    new Ajax.Request('../eventstream/get_stream_definitions_ajaxprocessor.jsp', {
        method:'POST',
        asynchronous:false,
        onSuccess:function (event) {
            streamDefSelect.length = 0;
            // for each property, add a text and input field in a row
            var jsonArrStreamDefIds = JSON.parse(event.responseText);
            for (i = 0; i < jsonArrStreamDefIds.length; i++) {
                var streamDefId = jsonArrStreamDefIds[i];
                if (streamDefId != undefined && streamDefId != "") {
                    streamDefId = streamDefId.trim();
                    streamDefSelect.add(new Option(streamDefId, streamDefId), null);
                }
            }
            streamDefSelect.add(new Option("-- Create Stream Definition --", "createStreamDef"), null);
        }
    });
}

function customCarbonWindowClose() {
    jQuery("#custom_dialog").dialog("destroy").remove();
}


var ENABLE = "enable";
var DISABLE = "disable";
var STAT = "statistics";
var TRACE = "Tracing";

function doDelete(executionPlanName) {
    var theform = document.getElementById('deleteForm');
    theform.executionPlan.value = executionPlanName;
    theform.submit();
}

function disableStat(execPlanName) {
    jQuery.ajax({
                    type:'POST',
                    url:'../eventprocessor/stats_tracing_ajaxprocessor.jsp',
                    data:'execPlanName=' + execPlanName + '&action=disableStat',
                    async:false,
                    success:function (msg) {
                        handleCallback(execPlanName, DISABLE, STAT);
                    },
                    error:function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="stat.disable.error"/>' +
                                               ' ' + execPlanName);
                    }
                });
}

function enableStat(execPlanName) {
    jQuery.ajax({
                    type:'POST',
                    url:'../eventprocessor/stats_tracing_ajaxprocessor.jsp',
                    data:'execPlanName=' + execPlanName + '&action=enableStat',
                    async:false,
                    success:function (msg) {
                        handleCallback(execPlanName, ENABLE, STAT);
                    },
                    error:function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="stat.enable.error"/>' +
                                               ' ' + execPlanName);
                    }
                });
}

function handleCallback(execPlanName, action, type) {
    var element;
    if (action == "enable") {
        if (type == "statistics") {
            element = document.getElementById("disableStat" + execPlanName);
            element.style.display = "";
            element = document.getElementById("enableStat" + execPlanName);
            element.style.display = "none";
        } else {
            element = document.getElementById("disableTracing" + execPlanName);
            element.style.display = "";
            element = document.getElementById("enableTracing" + execPlanName);
            element.style.display = "none";
        }
    } else {
        if (type == "statistics") {
            element = document.getElementById("disableStat" + execPlanName);
            element.style.display = "none";
            element = document.getElementById("enableStat" + execPlanName);
            element.style.display = "";
        } else {
            element = document.getElementById("disableTracing" + execPlanName);
            element.style.display = "none";
            element = document.getElementById("enableTracing" + execPlanName);
            element.style.display = "";
        }
    }
}

function enableTracing(execPlanName) {
    jQuery.ajax({
                    type:'POST',
                    url:'../eventprocessor/stats_tracing_ajaxprocessor.jsp',
                    data:'execPlanName=' + execPlanName + '&action=enableTracing',
                    async:false,
                    success:function (msg) {
                        handleCallback(execPlanName, ENABLE, TRACE);
                    },
                    error:function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="trace.enable.error"/>' +
                                               ' ' + execPlanName);
                    }
                });
}

function disableTracing(execPlanName) {
    jQuery.ajax({
                    type:'POST',
                    url:'../eventprocessor/stats_tracing_ajaxprocessor.jsp',
                    data:'execPlanName=' + execPlanName + '&action=disableTracing',
                    async:false,
                    success:function (msg) {
                        handleCallback(execPlanName, DISABLE, TRACE);
                    },
                    error:function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="trace.disable.error"/>' +
                                               ' ' + execPlanName);
                    }
                });
}

