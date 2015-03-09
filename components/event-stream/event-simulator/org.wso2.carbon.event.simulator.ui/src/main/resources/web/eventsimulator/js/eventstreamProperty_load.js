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

    if(selected_name.localeCompare("select")){
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


}

function showEventPropertiesForSimulator() {

    var eventStreamTable = document.getElementById("inputEventDetailTable2");

    var selectedIndex = document.getElementById("EventStreamID2").selectedIndex;
    var selected_name = document.getElementById("EventStreamID2").options[selectedIndex].value;


    for (i = eventStreamTable.rows.length - 1; i > 0; i--) {
        eventStreamTable.deleteRow(i);
    }

    if(selected_name.localeCompare("select")){
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


                    tableRow1.innerHTML = '<tr><td colspan="2"><div id="innerDiv4">' +
                        '<table class="styledLeft noBorders spacer-bot" id="streamAttributeTable2" style="width:100%">' +
                        '<tbody>' +
                        '<tr name="streamAttributes"><td colspan="2" class="middle-header">Map Stream Attributes with DataBase Fields</td></tr>' +
                        '<tr><td><h6></h6></td><td><h6>Table column name</h6></td></tr>' +
                        '</tbody>' +
                        '</table></div></td></tr>';


                    var streamAttributeTable2 = document.getElementById("streamAttributeTable2");

                    var index = 0;

                    if (metaData[0] != null) {

                        var tableRow4 = streamAttributeTable2.insertRow(streamAttributeTable2.rows.length);

                        tableRow4.innerHTML = '<tr><td colspan="2"><h6>Meta Attributes</h6></td></tr>';
                    }

                    for (var i = 0; i < metaData.length; i++) {
                        if (metaData[i] != null) {

                            var tableRow = streamAttributeTable2.insertRow(streamAttributeTable2.rows.length);

                            var attributeType = metaData[i].localAttributeType;
                            var attributeName = metaData[i].localAttributeName;

                            var stringNameTyp = attributeName + " (" + attributeType.toLowerCase() + ")";
                            var newIndex = "DB"+index;
                            tableRow.innerHTML = '<tr><td class="leftCol-med">' + attributeName + '(<span style="color: grey"><i>' + attributeType.toLowerCase() + '</i></span>) <span class="required">*</span> </td><td><input type="text" id = "'+newIndex+'" name="' + metaData[i].localAttributeName + '" attributeType="' + metaData[i].localAttributeType + '" class="initE" style="width:75%"> </td></tr>';
                            index++;
                        }
                    }

                    if (correlationData[0] != null) {

                        var tableRow5 = streamAttributeTable2.insertRow(streamAttributeTable2.rows.length);

                        tableRow5.innerHTML = '<tr><td colspan="2"><h6>Correlation Attributes</h6></td></tr>';
                    }

                    for (var j = 0; j < correlationData.length; j++) {
                        if (correlationData[j] != null) {

                            var tableRow = streamAttributeTable2.insertRow(streamAttributeTable2.rows.length);

                            var attributeType = correlationData[j].localAttributeType;
                            var attributeName = correlationData[j].localAttributeName;
                            var stringNameTyp = attributeName + " (" + attributeType.toLowerCase() + ")";
                            var newIndex = "DB"+index;
                            tableRow.innerHTML = '<tr><td class="leftCol-med">' + attributeName + '(<span style="color: grey"><i>' + attributeType.toLowerCase() + '</i></span>) <span class="required">*</span> </td><td><input type="text" id = "'+newIndex+ '"name="' + correlationData[j].localAttributeName + '" attributeType="' + correlationData[j].localAttributeType + '" class="initE" style="width:75%"> </td></tr>';
                            index++;

                        }
                    }

                    if (payloadData[0] != null) {

                        var tableRow6 = streamAttributeTable2.insertRow(streamAttributeTable2.rows.length);

                        tableRow6.innerHTML = '<tr><td colspan="2"><h6>Payload Attributes</h6></td></tr>';
                    }

                    for (var k = 0;k < payloadData.length; k++) {
                        if (payloadData[k] != null) {

                            var tableRow = streamAttributeTable2.insertRow(streamAttributeTable2.rows.length);

                            var attributeType = payloadData[k].localAttributeType;
                            var attributeName = payloadData[k].localAttributeName;
                            var stringNameTyp = attributeName + " (" + attributeType.toLowerCase() + ")";
                            var newIndex = "DB"+index;
                            tableRow.innerHTML = '<tr><td class="leftCol-med">' + attributeName + '(<span style="color: grey"><i>' + attributeType.toLowerCase() + '</i></span>) <span class="required">*</span>  </td><td><input type="text" id = "'+newIndex+ '"name="' + payloadData[k].localAttributeName + '" attributeType="' + payloadData[k].localAttributeType + '" class="initE" style="width:75%"> </td></tr>';
                            index++;
                        }
                    }

                    var hiddenRow = streamAttributeTable2.insertRow(streamAttributeTable2.rows.length);
                    hiddenRow.innerHTML = '<tr><td colspan="2"><input type="hidden" id="formFields2" value="' + index + '"> </td></tr>';


                }
            }


        });
    }


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
                    CARBON.showInfoDialog("Events is successfully sent");

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

function validateForDBSimulator() {
    var index = document.getElementById("formFields2").value;


    for (var i = 0; i < index; i++) {

        var val = document.getElementById("DB"+i).value;

        var typ = document.getElementById("DB"+i).getAttribute("attributeType");

        if (val == undefined || val == "") {

            CARBON.showErrorDialog("Empty input fields are not allowed");
            return false;
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

function testRDBMConnection(databaseType){
    if(databaseType.localeCompare("RDBMS")==0){

        var jsonString = getEventStreamAndDataSourceInfo();
        if(jsonString!=null){
            new Ajax.Request('../eventsimulator/TestRDBMSConnection_ajaxprocessor.jsp',{
                method: 'POST',
                asynchronous: false,
                dataType: "text",
                parameters: {
                    eventStreamAndDataSourceColumnNamesAndTypes: jsonString
                },onSuccess: function (data) {

                    var result = JSON.parse(data.responseText.trim());

                    if (result.success.localeCompare("fail") == 0) {
                        CARBON.showErrorDialog(result.message);
                    }else {
                        CARBON.showInfoDialog("Database connection is successful!");
                    }
                }
            });
        }

    }else{

    }
}

function getEventStreamAndDataSourceInfo(){

    var dataSourceName = document.getElementById("dataSourceNameId2").value;

    var tableName = document.getElementById("tableNameId2").value;

    var configurationName = document.getElementById("configurationNameId2").value;

    if (dataSourceName == null || dataSourceName == "") {
        CARBON.showErrorDialog("Data Source name cannot be empty");
        return null;
    }else if(tableName == null || tableName == ""){
        CARBON.showErrorDialog("Table name cannot be empty.");
        return null;
    }else if(configurationName == null || configurationName == ""){
        CARBON.showErrorDialog("Configuration name cannot be empty.");
        return null;
    }else if(!validateForDBSimulator()){
        return null;
    } else {
        var selectIndex = document.getElementById("EventStreamID2").selectedIndex;
        var eventStreamID = document.getElementById("EventStreamID2").options[selectIndex].text;
        var eventStreamName = document.getElementById("EventStreamID2").options[selectIndex].value;


        var index = document.getElementById("formFields2").value;

        var jsonString = "{\"streamID\":\"" + eventStreamID + "\",\"eventStreamName\":\"" + eventStreamName + "\",\"dataSource\":\"" + dataSourceName + "\",\"tableName\":\"" + tableName + "\", \"name\":\"" + configurationName + "\",\"dataSourceColumnsAndTypes\":[";
        var jsonAttribute = "";

        for (var i = 0; i < index; i++) {
            if (i != index - 1) {
                var fieldInput = document.getElementById("DB" + i);

                jsonAttribute = jsonAttribute + "{\"streamAttribute\":\"" + fieldInput.name + "\",\"columnName\":\"" + fieldInput.value + "\",\"columnType\":\"" + fieldInput.getAttribute("attributeType") + "\"},";
            }
            else {
                var fieldInput = document.getElementById("DB" + i);

                jsonAttribute = jsonAttribute + "{\"streamAttribute\":\"" + fieldInput.name + "\",\"columnName\":\"" + fieldInput.value + "\",\"columnType\":\"" + fieldInput.getAttribute("attributeType") + "\"}";
            }
        }
        jsonString = jsonString + jsonAttribute + "]}";
        return jsonString;
    }
}


function saveDBConfiguration(databaseType){


    if(databaseType.localeCompare("RDBMS")==0){
        var jsonString = getEventStreamAndDataSourceInfo();
        if(jsonString!=null){
            new Ajax.Request('../eventsimulator/TestRDBMSConnection_ajaxprocessor.jsp',{
                method: 'POST',
                asynchronous: false,
                dataType: "text",
                parameters: {
                    eventStreamAndDataSourceColumnNamesAndTypes: jsonString
                },
                onSuccess: function (data) {

                    var result = JSON.parse(data.responseText.trim());

                    if (result.success.localeCompare("fail") == 0) {
                        CARBON.showErrorDialog(result.message);
                    }else {
                        CARBON.showConfirmationDialog("Database connection is successful! Save data source info?",
                            function () {
                                new Ajax.Request('../eventsimulator/sendDataSourceConfigValues_ajaxprocessor.jsp',{
                                    method: 'POST',
                                    asynchronous: false,
                                    parameters: {
                                        dataSourceConfigAndEventStreamInfo: result.message
                                    },
                                    onSuccess: function (msg) {
                                        msg = msg.responseText.trim();
                                        if(msg.localeCompare("Sent")==0){
                                            CARBON.showInfoDialog("Database configuration saved successfully.");

                                            document.getElementById("dataSourceNameId2").value = "";
                                            document.getElementById("tableNameId2").value = "";
                                            document.getElementById("configurationNameId2").value = "";

                                            var index = document.getElementById("formFields2").value;
                                            for (var i = 0; i < index; i++) {
                                                if (i != index - 1) {
                                                    document.getElementById("DB" + i).value = "";
                                                }
                                                else {
                                                    document.getElementById("DB" + i).value = "";
                                                }
                                            }

                                        }else{
                                            CARBON.showErrorDialog(msg);
                                        }
                                    }
                                })
                            }, null, null);
                    }
                }
            });
        }

    }else{
        //if cassandra or other database types
    }
}