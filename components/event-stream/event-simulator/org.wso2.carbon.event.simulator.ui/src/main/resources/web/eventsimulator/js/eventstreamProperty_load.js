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
function showEventProperties() {

    var eventStreamTable = document.getElementById("inputEventDetailTable");

    var selectedIndex = document.getElementById("EventStreamID").selectedIndex;
    var selected_name = document.getElementById("EventStreamID").options[selectedIndex].value;

    for (i = eventStreamTable.rows.length - 1; i > 0; i--) {
        eventStreamTable.deleteRow(i);
    }

    jQuery.ajax({

        type: "POST",
        url: "../eventsimulator/getProperties_ajaxprocessor.jsp?eventName=" + selected_name + "",
        data: {},
        contentType: "application/json; charset=utf-8",
        dataType: "text",
        async: false,

        success: function (msg) {


            if (msg != null) {

                var jsonObject = JSON.parse(msg);
                var eventName = jsonObject.localStreamName;
                var eventVersion = jsonObject.localStreamVersion;
                var eventDef = jsonObject.localStreamDescription;
                var metaData = new Array();
                var correlationData = new Array();
                var payloadData = new Array();

                metaData = jsonObject.localMetaAttributes;
                correlationData = jsonObject.localCorrelationAttributes;
                payloadData = jsonObject.localPayloadAttributes;


                var tableRow1 = eventStreamTable.insertRow(eventStreamTable.rows.length);
                var tableRow2 = eventStreamTable.insertRow(eventStreamTable.rows.length);
                var tableRow3 = eventStreamTable.insertRow(eventStreamTable.rows.length);

                var streamID = eventName + ":" + eventVersion;


                tableRow1.innerHTML = '<tr><td colspan="2"><div id="innerDiv4"><table class="styledLeft noBorders spacer-bot" id="streamAttributeTable" style="width:100%"><tbody><tr name="streamAttributes"><td colspan="2" class="middle-header">Stream Attributes</td></tr></tbody></table></div></td></tr>';


                var streamAttributeTable = document.getElementById("streamAttributeTable");


                var index = 0;
                if (metaData[0] != null) {

                    var tableRow4 = streamAttributeTable.insertRow(streamAttributeTable.rows.length);

                    tableRow4.innerHTML = '<tr><td colspan="2"><h6>Meta Attributes</h6></td></tr>';
                }

                for (var i = 0; i < metaData.length; i++) {
                    if (metaData[i] != null) {

                        var tableRow = streamAttributeTable.insertRow(streamAttributeTable.rows.length);

                        var attributeType = metaData[i].localAttributeType;
                        var attributeName = metaData[i].localAttributeName;

                        var stringNameTyp = attributeName + " (" + attributeType.toLowerCase() + ")";
                        tableRow.innerHTML = '<tr><td class="leftCol-med">' + attributeName + '(<span style="color: grey"><i>' + attributeType.toLowerCase() + '</i></span>) <span class="required">*</span> </td><td><input type="text" name="' + metaData[i].localAttributeName + '" id="' + index + '" attributeType="' + metaData[i].localAttributeType + '" class="initE" style="width:75%"> </td></tr>';
                        index++;
                    }
                }

                if (correlationData[0] != null) {

                    var tableRow5 = streamAttributeTable.insertRow(streamAttributeTable.rows.length);

                    tableRow5.innerHTML = '<tr><td colspan="2"><h6>Correlation Attributes</h6></td></tr>';
                }

                for (var j = 0; j < correlationData.length; j++) {
                    if (correlationData[j] != null) {

                        var tableRow = streamAttributeTable.insertRow(streamAttributeTable.rows.length);

                        var attributeType = correlationData[j].localAttributeType;
                        var attributeName = correlationData[j].localAttributeName;
                        var stringNameTyp = attributeName + " (" + attributeType.toLowerCase() + ")";
                        tableRow.innerHTML = '<tr><td class="leftCol-med">' + attributeName + '(<span style="color: grey"><i>' + attributeType.toLowerCase() + '</i></span>) <span class="required">*</span> </td><td><input type="text" name="' + correlationData[j].localAttributeName + '" id="' + index + '" attributeType="' + correlationData[j].localAttributeType + '" class="initE" style="width:75%"> </td></tr>';
                        index++;

                    }
                }

                if (payloadData[0] != null) {

                    var tableRow6 = streamAttributeTable.insertRow(streamAttributeTable.rows.length);

                    tableRow6.innerHTML = '<tr><td colspan="2"><h6>Payload Attributes</h6></td></tr>';
                }

                for (var k = 0; k < payloadData.length; k++) {
                    if (payloadData[k] != null) {

                        var tableRow = streamAttributeTable.insertRow(streamAttributeTable.rows.length);

                        var attributeType = payloadData[k].localAttributeType;
                        var attributeName = payloadData[k].localAttributeName;
                        var stringNameTyp = attributeName + " (" + attributeType.toLowerCase() + ")";
                        tableRow.innerHTML = '<tr><td class="leftCol-med">' + attributeName + '(<span style="color: grey"><i>' + attributeType.toLowerCase() + '</i></span>) <span class="required">*</span>  </td><td><input type="text" name="' + payloadData[k].localAttributeName + '" id="' + index + '" attributeType="' + payloadData[k].localAttributeType + '" class="initE" style="width:75%"> </td></tr>';
                        index++;
                    }
                }


                var hiddenRow = streamAttributeTable.insertRow(streamAttributeTable.rows.length);
                hiddenRow.innerHTML = '<tr><td colspan="2"><input type="hidden" id="formFields" value="' + index + '"> </td></tr>';


            }
        }


    });
}

function sendEvent(form) {
    if (validate() == true) {
        var selectIndex = document.getElementById("EventStreamID").selectedIndex;
        var eventStreamName = document.getElementById("EventStreamID").options[selectIndex].text;


        var index = document.getElementById("formFields").value;

        var jsonString = "{\"EventStreamName\":\"" + eventStreamName + "\",\"attributes\":[";
        var jsonAttribute = "";

        for (var i = 0; i < index; i++) {
            if (i != index - 1) {
                var fieldInput = document.getElementById(i);

                jsonAttribute = jsonAttribute + "{\"name\":\"" + fieldInput.name + "\",\"value\":\"" + fieldInput.value + "\",\"type\":\"" + fieldInput.getAttribute("attributeType") + "\"},";
            }
            else {
                var fieldInput = document.getElementById(i);

                jsonAttribute = jsonAttribute + "{\"name\":\"" + fieldInput.name + "\",\"value\":\"" + fieldInput.value + "\",\"type\":\"" + fieldInput.getAttribute("attributeType") + "\"}";
            }
        }

        jsonString = jsonString + jsonAttribute + "]}"

        jQuery.ajax({
            type: "POST",
            url: "../eventsimulator/sendEventstreams_ajaxprocessor.jsp?jsonData=" + jsonString + "",
            async: false,

            success: function (msg) {


                if (msg != null && msg.trim() == "Success") {
                    CARBON.showInfoDialog("Event is successfully sent");

                    for (var j = 0; j < index; j++) {
                        var inputField = document.getElementById(j);

                        inputField.value = "";

                    }
                }
                else {
                    CARBON.showErrorDialog("Error sending event -"+"\n\n" + msg);

                }
            }



        });

    }
}


function validate() {
    var index = document.getElementById("formFields").value;

    for (var i = 0; i < index; i++) {

        var val = document.getElementById(i).value;
        var typ = document.getElementById(i).getAttribute("attributeType")

        if (val == undefined || val == "") {

            CARBON.showErrorDialog("Empty input fields are not allowed");
            return;
        }


    }
    return true;
}

function validateUpload() {


    if (document.getElementById('csvFile').value != "") {
        var filename = document.getElementById('csvFile').value;

        if (filename.lastIndexOf(".csv") == -1) {
            CARBON.showWarningDialog('Please select a .csv file');
        } else {
            document.getElementById('csvFileForm').submit();
        }

    } else {
        CARBON.showWarningDialog('Please select required fields to upload a csv file');
    }


}
