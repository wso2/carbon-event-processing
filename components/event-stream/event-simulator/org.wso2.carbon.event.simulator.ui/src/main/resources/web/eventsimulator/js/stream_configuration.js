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

    if(seperateChar==""){
        CARBON.showErrorDialog("Error - Fill the necessary fields");
        return;
    }

    var jsonString="{\"FileName\":\""+fileName+"\",\"streamID\":\""+streamName+"\",\"seperateChar\":\""+seperateChar+"\"}";

    jQuery.ajax({
        type: "POST",
        url: "../eventsimulator/sendConfigValues_ajaxprocessor.jsp?jsonData=" + jsonString + "",
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