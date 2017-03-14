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
function createPopupStreamConfigUI(filename){

    new Ajax.Request("../eventsimulator/popup_create_stream_configuration_ajaxprocessor.jsp",{

        method:'POST',
        parameters:{fileName:filename},
        asynchronous:false,
        onSuccess:function(data){
            showCustomConfigurationPopupDialog(data.responseText, "Event Mapping Configuration", "30%", "", null, "50%");
        }
    });


}

function showCustomConfigurationPopupDialog(message, title, windowHight, okButton, callback,
                                            windowWidth){
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

function sendConfiguration(form){

    var selectedIndex=document.getElementById("eventStreamSelect").selectedIndex;
    var streamName=document.getElementById("eventStreamSelect").options[selectedIndex].text;
    var seperateChar=document.getElementById("seperateChar").value;
    var fileName=document.getElementById("filename").textContent;
    var delayBetweenEventsInMilies = document.getElementById("eventSendingDelay").value;

    if(seperateChar==""){
        CARBON.showErrorDialog("Error - Fill the necessary fields");
        return;
    }

    var jsonString="{\"FileName\":\""+fileName+"\",\"streamID\":\""+streamName+"\",\"seperateChar\":\""+seperateChar+"\"" +
        ",\"delayBetweenEventsInMilies\":"+delayBetweenEventsInMilies+"}";

    var xhr = window.XMLHttpRequest ? new window.XMLHttpRequest : new window.ActiveXObject("Microsoft.XMLHTTP");
    xhr.open("POST", "/carbon/admin/js/csrfPrevention.js", false);
    xhr.setRequestHeader("FETCH-CSRF-TOKEN", "1");
    xhr.send(null);

    var token_pair = xhr.responseText;
    token_pair = token_pair.split(":");
    var token_name = token_pair[0];
    var token_value = token_pair[1];

    jQuery.ajax({
        type: "POST",
        url: "../eventsimulator/sendConfigValues_ajaxprocessor.jsp",
        beforeSend: function(xhr){xhr.setRequestHeader(token_name, token_value);},
        data:'jsonData=' + jsonString,
        async: false,
        success:function(msg){
            if (msg != null && msg.trim() == "Sent"){
                CARBON.showInfoDialog(" File-stream mapping saved successfully",function(){
                    window.location.href="../eventsimulator/index.jsp";

                });

            }else{
                CARBON.showErrorDialog("ERROR - "+msg,function(){
                    window.location.href="../eventsimulator/index.jsp";
                });
            }
        }

    });


}