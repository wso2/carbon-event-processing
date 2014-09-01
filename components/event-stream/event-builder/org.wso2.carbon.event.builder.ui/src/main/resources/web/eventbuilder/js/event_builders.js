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

var advancedMappingCounter = 0;

//Method that used in jsp files

function getWso2EventDataValues(dataTable, inputDataType) {

    var wso2EventDataTable = document.getElementById(dataTable);

    var wso2EventData = "";
    for (var i = 0; i < wso2EventDataTable.rows.length; i++) {

        var column0 = document.getElementById(inputDataType + "EventPropertyName_" + i).value;
        var column2 = document.getElementById(inputDataType + "EventMappedValue_" + i).value;
        var column3 = document.getElementById(inputDataType + "EventType_" + i).value;

        if (column0.trim() == "") {
            return "invalid";
        }

        wso2EventData = wso2EventData + column0 + "^=" + column2 + "^=" + column3 + "$=";
    }
    return wso2EventData;
}

function getJsonDataValues(dataTable) {

    var jsonData = "";
    for (var i = 0; i < dataTable.rows.length; i++) {

        var column0 = document.getElementById("inputPropertyValue_" + i).value;
        var column1 = document.getElementById("inputPropertyName_" + i).value;
        var column2 = document.getElementById("inputPropertyType_" + i).value;
        var column3 = document.getElementById("inputPropertyDefault_" + i).value;

        if (column0.trim() == "") {
            return "invalid";
        }

        // For JSON we use a different terminator (*) since $ is already used in JSONPath
        jsonData = jsonData + column0 + "^=" + column1 + "^=" + column2 + "^=" + column3 + "*=";
    }
    return jsonData;
}

function getXpathDataValues(dataTable) {

    var xpathData = "";
    for (var i = 0; i < dataTable.rows.length; i++) {

        var column0 = document.getElementById("inputPropertyValue_" + i).value;
        var column1 = document.getElementById("inputPropertyName_" + i).value;
        var column2 = document.getElementById("inputPropertyType_" + i).value;
        var column3 = document.getElementById("inputPropertyDefault_" + i).value;


        if (column0 == "") {
            return "invalid";
        }

        xpathData = xpathData + column0 + "^=" + column1 + "^=" + column2 + "^=" + column3 + "$=";
    }
    return xpathData;
}

function getXpathPrefixValues(dataTable) {
    var xpathPrefixes = "";
    for (var i = 1; i < dataTable.rows.length; i++) {

        var row = dataTable.rows[i];
        var column0 = row.cells[0].innerHTML;
        var column1 = row.cells[1].innerHTML;

        xpathPrefixes = xpathPrefixes + column0 + "^=" + column1 + "$=";
    }

    return xpathPrefixes;
}

function getTextDataValues(dataTable) {

    var textData = "";
    for (var i = 1; i < dataTable.rows.length; i++) {

        var row = dataTable.rows[i];
        var column0 = row.cells[0].innerHTML;
        var column1 = row.cells[1].innerHTML;
        var column2 = row.cells[2].innerHTML;
        var column3 = row.cells[3].innerHTML;

        textData = textData + column1 + "^=" + column0 + "^=" + column2 + "^=" + column3 + "$=";
    }
    return textData;
}

function getMapDataValues(dataTable) {

    var mapEventData = "";
    for (var i = 0; i < dataTable.rows.length; i++) {

        var column0 = document.getElementById("inputMapPropName_" + i).value;
        var column1 = document.getElementById("inputMapPropValueOf_" + i).value;
        var column2 = document.getElementById("inputMapPropType_" + i).value;

        if (column0.trim() == "") {
            return "invalid";
        }

        mapEventData = mapEventData + column0 + "^=" + column1 + "^=" + column2 + "^=" + "$=";
    }
    return mapEventData;
}


function addEventBuilderViaPopup(form, toStreamId, redirectPage) {

    var isFieldEmpty = false;

    var eventBuilderName = document.getElementById("eventBuilderNameId").value.trim();
    var toStreamNameAndVersion = toStreamId.split(":");
    if (toStreamNameAndVersion.length != 2) {
        CARBON.showErrorDialog("Could not find a valid To Stream Id, Please check.");
        return;
    }

    var toStreamName = toStreamNameAndVersion[0];
    var toStreamVersion = toStreamNameAndVersion[1];
    var eventAdaptorInfo = document.getElementById("eventAdaptorNameSelect")[document.getElementById("eventAdaptorNameSelect").selectedIndex].value.trim();

    var customMappingValue = "disable";

    if (((advancedMappingCounter % 2) != 0)) {
        customMappingValue = "enable";
    }

    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_\.]+$");
    // Check for white space
    if (!reWhiteSpace.test(eventBuilderName)) {
        CARBON.showErrorDialog("Invalid character found in event builder name.");
        return;
    }
    if (isFieldEmpty || (eventBuilderName == "")) {
        // empty fields are encountered.
        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
        return;
    }
    if (toStreamName != "" && toStreamVersion == "") {
        toStreamVersion = "1.0.0";
    }

    var propertyCount = 0;
    var msgConfigPropertyString = "";

    // all properties, not required and required are checked
    while (document.getElementById("msgConfigProperty_Required_" + propertyCount) != null ||
           document.getElementById("msgConfigProperty_" + propertyCount) != null) {
        // if required fields are empty
        if (document.getElementById("msgConfigProperty_Required_" + propertyCount) != null) {
            if (document.getElementById("msgConfigProperty_Required_" + propertyCount).value.trim() == "") {
                // values are empty in fields
                isFieldEmpty = true;
                msgConfigPropertyString = "";
                break;
            }
            else {
                // values are stored in parameter string to send to backend
                var propertyValue = document.getElementById("msgConfigProperty_Required_" + propertyCount).value.trim();
                var propertyName = document.getElementById("msgConfigProperty_Required_" + propertyCount).name;
                msgConfigPropertyString = msgConfigPropertyString + propertyName + "$=" + propertyValue + "|=";

            }
        } else if (document.getElementById("msgConfigProperty_" + propertyCount) != null) {
            var notRequiredPropertyName = document.getElementById("msgConfigProperty_" + propertyCount).name;
            var notRequiredPropertyValue = document.getElementById("msgConfigProperty_" + propertyCount).value.trim();
            if (notRequiredPropertyValue == "") {
                notRequiredPropertyValue = "  ";
            }
            msgConfigPropertyString = msgConfigPropertyString + notRequiredPropertyName + "$=" + notRequiredPropertyValue + "|=";
        }
        propertyCount++;
    }

    var mappingType = "";
    if (isFieldEmpty) {
        // empty fields are encountered.
        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
        return;
    } else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'wso2event') {

        mappingType = "wso2event";
        var metaData = "";
        var correlationData = "";
        var payloadData = "";

        if (customMappingValue == "enable") {
            metaData = getWso2EventDataValues("addMetaEventDataTable", 'meta');
            correlationData = getWso2EventDataValues("addCorrelationEventDataTable", 'correlation');
            payloadData = getWso2EventDataValues("addPayloadEventDataTable", 'payload');
        }

        if ((metaData == "" && correlationData == "" && payloadData == "" && customMappingValue == "enable") || correlationData == "invalid" || payloadData == "invalid" || metaData == "invalid") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventbuilder/add_eventbuilder_ajaxprocessor.jsp', {
                method:'POST',
                asynchronous:false,
                parameters:{eventBuilderName:eventBuilderName, toStreamName:toStreamName,
                    toStreamVersion:toStreamVersion, eventAdaptorInfo:eventAdaptorInfo, mappingType:mappingType,
                    msgConfigPropertySet:msgConfigPropertyString, customMappingValue:customMappingValue,
                    metaData:metaData, correlationData:correlationData, payloadData:payloadData},
                onSuccess:function (event) {
                    if ("true" == event.responseText.trim()) {
                        CARBON.showInfoDialog("Event builder added successfully!!", function () {
                            if (redirectPage != "none") {
                                window.location.href = "../eventstream/stream_in_flows.jsp?ordinal=1&eventStreamWithVersion=" + toStreamId;
                            }
                        }, null);
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + event.responseText.trim());
                    }
                }
            })
        }
    }

    else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'text') {

        mappingType = "text";
        var textData = "";

        if (customMappingValue == "enable") {
            var textDataTable = document.getElementById("inputTextMappingTable");
            if (textDataTable.rows.length > 1) {
                textData = getTextDataValues(textDataTable);
            }
        }

        if (textData == "" && customMappingValue == "enable") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventbuilder/add_eventbuilder_ajaxprocessor.jsp', {
                method:'POST',
                asynchronous:false,
                parameters:{eventBuilderName:eventBuilderName, toStreamName:toStreamName,
                    toStreamVersion:toStreamVersion, eventAdaptorInfo:eventAdaptorInfo, mappingType:mappingType, msgConfigPropertySet:msgConfigPropertyString,
                    textData:textData, customMappingValue:customMappingValue},
                onSuccess:function (event) {
                    if ("true" == event.responseText.trim()) {
                        CARBON.showInfoDialog("Event builder added successfully!!", function () {
                            if (redirectPage != "none") {
                                window.location.href = "../eventstream/stream_in_flows.jsp?ordinal=1&eventStreamWithVersion=" + toStreamId;
                            }
                        }, null);
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + event.responseText.trim());
                    }
                }
            })
        }
    }

    else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'xml') {
        var parentSelectorXpath = document.getElementById("parentSelectorXpath").value;

        mappingType = "xml";
        var prefixData = "";
        var xpathData = "";


        if (customMappingValue == "enable") {
            var xpathPrefixTable = document.getElementById("inputXpathPrefixTable");
            if (xpathPrefixTable.rows.length > 1) {
                prefixData = getXpathPrefixValues(xpathPrefixTable);
            }

            var xpathExprTable = document.getElementById("addXpathExprTable");
            if (xpathExprTable.rows.length > 0) {
                xpathData = getXpathDataValues(xpathExprTable);
            }
        }

        if ((prefixData == "" && xpathData == "" && customMappingValue == "enable") || xpathData == "invalid") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventbuilder/add_eventbuilder_ajaxprocessor.jsp', {
                method:'POST',
                asynchronous:false,
                parameters:{eventBuilderName:eventBuilderName, toStreamName:toStreamName,
                    toStreamVersion:toStreamVersion, eventAdaptorInfo:eventAdaptorInfo, mappingType:mappingType, msgConfigPropertySet:msgConfigPropertyString,
                    parentSelectorXpath:parentSelectorXpath, prefixData:prefixData, xpathData:xpathData, customMappingValue:customMappingValue},
                onSuccess:function (event) {
                    if ("true" == event.responseText.trim()) {
                        CARBON.showInfoDialog("Event builder added successfully!!", function () {
                            if (redirectPage != "none") {
                                window.location.href = "../eventstream/stream_in_flows.jsp?ordinal=1&eventStreamWithVersion=" + toStreamId;
                            }
                        }, null);
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + event.responseText.trim());
                    }
                }
            });
        }
    }
    else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'map') {

        mappingType = "map";
        var mapData = "";

        var mapDataTable = document.getElementById("addMapDataTable");
        if (mapDataTable.rows.length > 0 && customMappingValue == "enable") {
            mapData = getMapDataValues(mapDataTable);
        }

        if ((mapData == "" && customMappingValue == "enable") || mapData == "invalid") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        }
        else {
            new Ajax.Request('../eventbuilder/add_eventbuilder_ajaxprocessor.jsp', {
                method:'POST',
                asynchronous:false,
                parameters:{eventBuilderName:eventBuilderName, toStreamName:toStreamName,
                    toStreamVersion:toStreamVersion, eventAdaptorInfo:eventAdaptorInfo, mappingType:mappingType, msgConfigPropertySet:msgConfigPropertyString,
                    mapData:mapData, customMappingValue:customMappingValue},
                onSuccess:function (event) {
                    if ("true" == event.responseText.trim()) {
                        CARBON.showInfoDialog("Event builder added successfully!!", function () {
                            if (redirectPage != "none") {
                                window.location.href = "../eventstream/stream_in_flows.jsp?ordinal=1&eventStreamWithVersion=" + toStreamId;
                            }
                        }, null);
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + event.responseText.trim());
                    }
                }
            });
        }
    }
    else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'json') {

        mappingType = "json";
        var jsonData = "";

        var jsonDataTable = document.getElementById("addJsonpathExprTable");
        if (customMappingValue == "enable" && jsonDataTable.rows.length > 0) {
            jsonData = getJsonDataValues(jsonDataTable);
        }

        if ((customMappingValue == "enable" && jsonData == "") || jsonData == "invalid") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventbuilder/add_eventbuilder_ajaxprocessor.jsp', {
                method:'POST',
                asynchronous:false,
                parameters:{eventBuilderName:eventBuilderName, toStreamName:toStreamName,
                    toStreamVersion:toStreamVersion, eventAdaptorInfo:eventAdaptorInfo, mappingType:mappingType, msgConfigPropertySet:msgConfigPropertyString,
                    jsonData:jsonData, customMappingValue:customMappingValue},
                onSuccess:function (event) {
                    if ("true" == event.responseText.trim()) {
                        CARBON.showInfoDialog("Event builder added successfully!!", function () {
                            if (redirectPage != "none") {
                                window.location.href = "../eventstream/stream_in_flows.jsp?ordinal=1&eventStreamWithVersion=" + toStreamId;
                            }
                        }, null);
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + event.responseText.trim());
                    }
                }
            });
        }
    }
}

function addInputMapProperty() {
    var propName = document.getElementById("inputMapPropName");
    var propValueOf = document.getElementById("inputMapPropValueOf");
    var propertyTable = document.getElementById("inputMapPropertiesTable");
    var propertyTableTBody = document.getElementById("inputMapPropertiesTBody");
    var propType = document.getElementById("inputMapPropType");
    var noPropertyDiv = document.getElementById("noInputMapProperties");

    var error = "";
    if (propName.value == "") {
        error = "Name field is empty.\n";
    }
    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Check for white space
    if (!reWhiteSpace.test(propName.value)) {
        error = "Invalid character found in Input Attribute name field.";
    }

    if (propValueOf.value == "") {
        error = "Value Of field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";

    //Check for duplications
//    var topicNamesArr = YAHOO.util.Dom.getElementsByClassName("property-names");
//    var foundDuplication = false;
//    for (var i = 0; i < topicNamesArr.length; i++) {
//        if (topicNamesArr[i].innerHTML == propName.value) {
//            foundDuplication = true;
//            CARBON.showErrorDialog("Duplicated Entry");
//            return;
//        }
//    }

    //add new row
    var newTableRow = propertyTableTBody.insertRow(propertyTableTBody.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = propName.value;
    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = propValueOf.value;

    var newCell2 = newTableRow.insertCell(2);
    newCell2.innerHTML = propType.value;

    var newCell3 = newTableRow.insertCell(3);
    newCell3.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeInputProperty(this,\'' + 'map' + '\')">Delete</a>';

    propName.value = "";
    propValueOf.value = "";
    noPropertyDiv.style.display = "none";
    // propType.value = "";
    // showAddProperty();
}

function addInputXpathDef() {
    var prefixName = document.getElementById("inputPrefixName");
    var xpathNs = document.getElementById("inputXpathNs");
    var propertyTable = document.getElementById("inputXpathPrefixTable");
    var tableTBody = document.getElementById("inputXpathPrefixTBody");
    var noPropertyDiv = document.getElementById("noInputPrefixes");

    var error = "";

    if (prefixName.value == "") {
        error = "Prefix field is empty.\n";
    }
    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Check for white space
    if (!reWhiteSpace.test(prefixName.value)) {
        error = "Invalid character found in prefix field.";
    }

    if (xpathNs.value == "") {
        error = "Namespace field is empty.\n";
    }
//    // Check for white space
//    if (!reWhiteSpace.test(xpathNs.value)) {
//        error = "Invalid character found in XPath namespace.";
//    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";

    //add new row
    var newTableRow = tableTBody.insertRow(tableTBody.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = prefixName.value;
    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = xpathNs.value;

    var newCell2 = newTableRow.insertCell(2);
    newCell2.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeInputProperty(this,\'' + 'xml' + '\')">Delete</a>';

    prefixName.value = "";
    xpathNs.value = "";
    noPropertyDiv.style.display = "none";
}

function addInputXmlProperty() {
    var propName = document.getElementById("inputPropertyName");
    var xpathExpr = document.getElementById("inputPropertyValue");
    var propDefault = document.getElementById("inputPropertyDefault");
    var propertyTable = document.getElementById("inputXpathExprTable");
    var propertyType = document.getElementById("inputPropertyType");
    var tableTBody = document.getElementById("inputXpathExprTBody");
    var noPropertyDiv = document.getElementById("noInputProperties");

    var error = "";

    if (propName.value == "") {
        error = "Name field is empty.\n";
    }
    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Check for white space
    if (!reWhiteSpace.test(propName.value)) {
        error = "Invalid character found in Mapped To field.";
    }

    if (xpathExpr.value == "") {
        error = "XPath field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";

    //Check for duplications
//    var topicNamesArr = YAHOO.util.Dom.getElementsByClassName("property-names");
//    var foundDuplication = false;
//    for (var i = 0; i < topicNamesArr.length; i++) {
//        if (topicNamesArr[i].innerHTML == propName.value) {
//            foundDuplication = true;
//            CARBON.showErrorDialog("Duplicated Entry");
//            return;
//        }
//    }

    //add new row
    var newTableRow = tableTBody.insertRow(tableTBody.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = xpathExpr.value;

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = propName.value;
    YAHOO.util.Dom.addClass(newCell1, "property-names");

    var newCell2 = newTableRow.insertCell(2);
    newCell2.innerHTML = propertyType.value;

    var newCell3 = newTableRow.insertCell(3);
    newCell3.innerHTML = propDefault.value;

    var newCell4 = newTableRow.insertCell(4);
    newCell4.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeInputProperty(this,\'' + 'map' + '\')">Delete</a>';

    propName.value = "";
    xpathExpr.value = "";
    propDefault.value = "";
    noPropertyDiv.style.display = "none";
}

function addInputRegexDef() {
    var regex = document.getElementById("inputRegexDef");
    var propertyTable = document.getElementById("inputRegexDefTable");
    var regexSelect = document.getElementById("inputPropertyValue");
    var tableTBody = document.getElementById("inputRegexDefTBody");
    var noPropertyDiv = document.getElementById("noInputRegex");

    var error = "";


    if (regex.value == "") {
        error = "Regular expression field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";

    //add new row
    var newTableRow = tableTBody.insertRow(tableTBody.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = regex.value;
    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeRegexInputProperty(this,\'' + 'xml' + '\')">Delete</a>';


    if (regexSelect.value == "") {
        regexSelect.remove(regexSelect.selectedIndex);
    }
    var newRegexOption = document.createElement("option");
    newRegexOption.value = regex.value;
    newRegexOption.text = regex.value;
    regexSelect.add(newRegexOption, null);

    regex.value = "";
    noPropertyDiv.style.display = "none";
}

function addInputTextProperty() {
    var propName = document.getElementById("inputPropertyName");
    var regexExpr = document.getElementById("inputPropertyValue");
    var propDefault = document.getElementById("inputPropertyDefault");
    var propertyTable = document.getElementById("inputTextMappingTable");
    var propertyType = document.getElementById("inputPropertyType");
    var tableTBody = document.getElementById("inputTextMappingTBody");
    var noPropertyDiv = document.getElementById("noInputProperties");

    var error = "";

    if (propName.value == "") {
        error = "Name field is empty.\n";
    }
    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Check for white space
    if (!reWhiteSpace.test(propName.value)) {
        error = "Invalid character found in Mapped To field.";
    }


    if (regexExpr.value == "") {
        error = "Regular expression field is empty.\n";
    }

    for (var i = 0; i < tableTBody.rows.length; i++) {

        var row = tableTBody.rows[i];
        var column1 = row.cells[1].innerHTML;

        if(propName.value == column1) {
            error = propName.value +" already defined.\n";
            break;
        }
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";

    //Check for duplications
//    var topicNamesArr = YAHOO.util.Dom.getElementsByClassName("property-names");
//    var foundDuplication = false;
//    for (var i = 0; i < topicNamesArr.length; i++) {
//        if (topicNamesArr[i].innerHTML == propName.value) {
//            foundDuplication = true;
//            CARBON.showErrorDialog("Duplicated Entry");
//            return;
//        }
//    }




    //add new row
    var newTableRow = tableTBody.insertRow(tableTBody.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = regexExpr.value;

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = propName.value;
    YAHOO.util.Dom.addClass(newCell1, "property-names");

    var newCell2 = newTableRow.insertCell(2);
    newCell2.innerHTML = propertyType.value;

    var newCell3 = newTableRow.insertCell(3);
    newCell3.innerHTML = propDefault.value;

    var newCell4 = newTableRow.insertCell(4);
    newCell4.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeInputProperty(this,\'' + 'map' + '\')">Delete</a>';

    propDefault.value = "";
    noPropertyDiv.style.display = "none";
}

function addInputJsonProperty() {
    var propName = document.getElementById("inputPropertyName");
    var propDefault = document.getElementById("inputPropertyDefault");
    var jsonpathExpr = document.getElementById("inputPropertyValue");
    var propertyTable = document.getElementById("inputJsonpathExprTable");
    var propertyType = document.getElementById("inputPropertyType");
    var tableTBody = document.getElementById("inputJsonpathExprTBody");
    var noPropertyDiv = document.getElementById("noInputProperties");

    var error = "";

    if (propName.value == "") {
        error = "Name field is empty.\n";
    }
    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Check for white space
    if (!reWhiteSpace.test(propName.value)) {
        error = "Invalid character found in Mapped To field.";
    }

    if (jsonpathExpr.value == "") {
        error = "JSONPath field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";

    //Check for duplications
//    var topicNamesArr = YAHOO.util.Dom.getElementsByClassName("property-names");
//    var foundDuplication = false;
//    for (var i = 0; i < topicNamesArr.length; i++) {
//        if (topicNamesArr[i].innerHTML == propName.value) {
//            foundDuplication = true;
//            CARBON.showErrorDialog("Duplicated Entry");
//            return;
//        }
//    }

    //add new row
    var newTableRow = tableTBody.insertRow(tableTBody.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = jsonpathExpr.value;

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = propName.value;
    YAHOO.util.Dom.addClass(newCell1, "property-names");

    var newCell2 = newTableRow.insertCell(2);
    newCell2.innerHTML = propertyType.value;

    var newCell3 = newTableRow.insertCell(3);
    newCell3.innerHTML = propDefault.value;

    var newCell4 = newTableRow.insertCell(4);
    newCell4.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeInputProperty(this,\'' + 'map' + '\')">Delete</a>';

    propName.value = "";
    jsonpathExpr.value = "";
    propDefault.value = "";
    noPropertyDiv.style.display = "none";
}

function addInputWso2EventProperty() {
    var propertyName = document.getElementById("inputWso2EventPropertyName");
    var inputPropertyType = document.getElementById("inputWso2EventPropertyInputType");
    var propertyValueOf = document.getElementById("inputWso2EventPropertyValue");
    var propertyType = document.getElementById("inputWso2EventPropertyType");
    var propertyTable = document.getElementById("inputWso2EventDataTable");
    var propertyTableTBody = document.getElementById("inputWso2EventTBody");
    var noPropertyDiv = document.getElementById("noInputWso2EventData");

    var error = "";

    if (propertyName.value == "") {
        error = "Name field is empty.\n";
    }
    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Check for white space
    if (!reWhiteSpace.test(propertyName.value)) {
        error = "Invalid character found in Input Attribute Name field.";
    }

    if (propertyValueOf.value == "") {
        error = "Value Of field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";

    //add new row
    var newTableRow = propertyTableTBody.insertRow(propertyTableTBody.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = propertyName.value;
    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = inputPropertyType.value;

    var newCell2 = newTableRow.insertCell(2);
    newCell2.innerHTML = propertyValueOf.value;

    var newCell3 = newTableRow.insertCell(3);
    newCell3.innerHTML = propertyType.value;

    var newCell4 = newTableRow.insertCell(4);
    newCell4.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeInputProperty(this,\'' + 'map' + '\')">Delete</a>';

    propertyName.value = "";
    propertyValueOf.value = "";
    noPropertyDiv.style.display = "none";

}

function clearInputFields() {
    document.getElementById("queryName").value = "";
    document.getElementById("newTopic").value = "";
    document.getElementById("xmlSourceText").value = "";
    document.getElementById("textSourceText").value = "";
    document.getElementById("querySource").value = "";

    clearDataInTable("inputMetaDataTable");
    clearDataInTable("inputCorrelationDataTable");
    clearDataInTable("inputPayloadDataTable");

    document.getElementById("noInputMetaData").style.display = "";
    document.getElementById("noInputCorrelationData").style.display = "";
    document.getElementById("noInputPayloadData").style.display = "";
}

function removeInputProperty(link, format) {
    var rowToRemove = link.parentNode.parentNode;
    var propertyToRemove = rowToRemove.cells[0].innerHTML.trim();
    rowToRemove.parentNode.removeChild(rowToRemove);
    CARBON.showInfoDialog("Input Property removed successfully!!");
}

function removeRegexInputProperty(link, format) {
    var rowToRemove = link.parentNode.parentNode;
    var propertyToRemove = rowToRemove.cells[0].innerHTML.trim();
    rowToRemove.parentNode.removeChild(rowToRemove);
    CARBON.showInfoDialog("Input Property removed successfully!!");

    var regexSelect = document.getElementById("inputPropertyValue");
    jQuery("#inputPropertyValue option[value='" + propertyToRemove + "']").remove();
    if (regexSelect.length == 0) {
        var newRegexOption = document.createElement("option");
        newRegexOption.value = 'No regular expression defined';
        newRegexOption.text = 'No regular expression defined';
        regexSelect.add(newRegexOption, null);
    }

}

/**
 * Utils
 */
function clearDataInTable(tableName) {
    deleteTableRows(tableName, true);
}

function deleteTableRows(tl, keepHeader) {
    if (typeof(tl) != "object") {
        tl = document.getElementById(tl);

    }
    //debugger;
    for (var i = tl.rows.length; tl.rows.length > 0; i--) {
        if (tl.rows.length > 1) {
            tl.deleteRow(tl.rows.length - 1);
        }
        if (tl.rows.length == 1) {
            if (!keepHeader) {
                tl.deleteRow(0);
            }
            return;
        }
    }

}

function enableMapping(isEnabled) {
    var mappingRow = document.getElementById("mappingRow");
    if (isEnabled) {
        mappingRow.style.display = "";
    } else {
        mappingRow.style.display = "none";
    }
}

function customCarbonWindowClose() {
    jQuery("#custom_dialog").dialog("destroy").remove();
}


var ENABLE = "enable";
var DISABLE = "disable";
var STAT = "statistics";
var TRACE = "Tracing";

function deleteEventBuilder(eventBuilderName) {
    var theform = document.getElementById('deleteForm');
    theform.eventBuilder.value = eventBuilderName;
    theform.submit();
}

function deleteInFlowEventBuilder(eventStreamWithVersion,eventBuilderName) {
    var theform = document.getElementById('deleteForm1');
    theform.eventBuilder.value = eventBuilderName;
    theform.eventStreamWithVersion.value = eventStreamWithVersion;
    theform.submit();
}

function disableBuilderStat(eventBuilderName) {
    jQuery.ajax({
                    type:'POST',
                    url:'../eventbuilder/update_property_ajaxprocessor.jsp',
                    data:'eventBuilderName=' + eventBuilderName + '&attribute=stat' + '&value=false',
                    async:false,
                    success:function (msg) {
                        handleCallback(eventBuilderName, DISABLE, STAT);
                    },
                    error:function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="stat.disable.error"/>' +
                                               ' ' + eventBuilderName);
                    }
                });
}

function enableBuilderStat(eventBuilderName) {
    jQuery.ajax({
                    type:'POST',
                    url:'../eventbuilder/update_property_ajaxprocessor.jsp',
                    data:'eventBuilderName=' + eventBuilderName + '&attribute=stat' + '&value=true',
                    async:false,
                    success:function (msg) {
                        handleCallback(eventBuilderName, ENABLE, STAT);
                    },
                    error:function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="stat.enable.error"/>' +
                                               ' ' + eventBuilderName);
                    }
                });
}

function handleCallback(eventBuilderName, action, type) {
    var element;
    if (action == "enable") {
        if (type == "statistics") {
            element = document.getElementById("disableStat" + eventBuilderName);
            element.style.display = "";
            element = document.getElementById("enableStat" + eventBuilderName);
            element.style.display = "none";
        } else {
            element = document.getElementById("disableTracing" + eventBuilderName);
            element.style.display = "";
            element = document.getElementById("enableTracing" + eventBuilderName);
            element.style.display = "none";
        }
    } else {
        if (type == "statistics") {
            element = document.getElementById("disableStat" + eventBuilderName);
            element.style.display = "none";
            element = document.getElementById("enableStat" + eventBuilderName);
            element.style.display = "";
        } else {
            element = document.getElementById("disableTracing" + eventBuilderName);
            element.style.display = "none";
            element = document.getElementById("enableTracing" + eventBuilderName);
            element.style.display = "";
        }
    }
}

function enableBuilderTracing(eventBuilderName) {
    jQuery.ajax({
                    type:'POST',
                    url:'../eventbuilder/update_property_ajaxprocessor.jsp',
                    data:'eventBuilderName=' + eventBuilderName + '&attribute=trace' + '&value=true',
                    async:false,
                    success:function (msg) {
                        handleCallback(eventBuilderName, ENABLE, TRACE);
                    },
                    error:function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="trace.enable.error"/>' +
                                               ' ' + eventBuilderName);
                    }
                });
}

function disableBuilderTracing(eventBuilderName) {
    jQuery.ajax({
                    type:'POST',
                    url:'../eventbuilder/update_property_ajaxprocessor.jsp',
                    data:'eventBuilderName=' + eventBuilderName + '&attribute=trace' + '&value=false',
                    async:false,
                    success:function (msg) {
                        handleCallback(eventBuilderName, DISABLE, TRACE);
                    },
                    error:function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="trace.disable.error"/>' +
                                               ' ' + eventBuilderName);
                    }
                });
}


function createPopupEventBuilderUI(streamNameWithVersion) {

    new Ajax.Request('../eventbuilder/popup_create_event_builder_ajaxprocessor.jsp', {
        method:'POST',
        asynchronous:false,
        parameters:{streamNameWithVersion:streamNameWithVersion},
        onSuccess:function (data) {
            showCustomEventBuilderPopupDialog(data.responseText, "Create Event Builder", "80%", "", onSuccessCreateEventBuilder, "90%");
        }
    });
}


/**
 * Display the Info Message inside a jQuery UI's dialog widget.
 * @method showPopupDialog
 * @param {String} message to display
 * @return {Boolean}
 */
function showCustomEventBuilderPopupDialog(message, title, windowHight, okButton, callback,
                                           windowWidth) {
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

function handleAdvancedMapping() {
    var outerDiv = document.getElementById("outerDiv");

    if ((advancedMappingCounter % 2) == 0) {
        outerDiv.style.display = "";
    } else {
        outerDiv.style.display = "none";
    }
    advancedMappingCounter = advancedMappingCounter + 1;

}