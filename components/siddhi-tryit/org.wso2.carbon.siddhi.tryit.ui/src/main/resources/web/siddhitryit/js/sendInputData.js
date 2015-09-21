/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

function sendAjaxRequestToSiddhiProcessor(sendInputData) {
    new Ajax.Request("../siddhitryit/siddhiProcessor_ajaxprocessor.jsp", {
        method: 'POST',
        asynchronous: false,
        parameters: {
            eventstream: document.getElementById("eventStreamId").value,
            executionplan: window.queryEditor.getValue(),
            datetime: document.getElementById("dateTimeID").value
        },
        onSuccess: function (data) {
            if (data != null) {
                var jsonObject = JSON.parse(data.responseText);

                if (jsonObject != undefined) {
                    if (jsonObject.success.localeCompare("true") == 0) {
                        jQuery('.js_resultCol').show();
                        var jsonArray = JSON.parse(jsonObject.jsonValue);
                        var tabContent = document.getElementById('resultsId');
                        tabContent.innerHTML = '<div id="tabs"><ul id="tabHeaders"></ul></div>';
                        for (i = 0; i < jsonArray.length; i++) {
                            var tabHeader = document.getElementById('tabHeaders');
                            var jsonArrayKey = jsonArray[i].key;
                            var jsonArrayValue = jsonArray[i].jsonValue;
                            if (jsonArrayKey.indexOf(':') == 0) {
                                jsonArrayKey = jsonArrayKey.replace(':', ' ');
                                tabHeader.innerHTML = tabHeader.innerHTML + '<li><a class="query-tab" href="#tabs-' + i + '">' + jsonArrayKey + '</a></li>';
                            }
                            else {
                                tabHeader.innerHTML = tabHeader.innerHTML + '<li><a class="stream-tab" href="#tabs-' + i + '">' + jsonArrayKey + '</a></li>';
                            }
                            tabContent = document.getElementById('tabs');
                            if (jsonArrayValue != null) {
                                tabContent.innerHTML = tabContent.innerHTML + '<div id="tabs-' + i + '"><textarea rows="50" cols="50" readonly>' + jsonArrayValue + '</textarea></div>';
                            } else {
                                tabContent.innerHTML = tabContent.innerHTML + '<div id="tabs-' + i + '"><textarea rows="50" cols="50" readonly></textarea></div>';
                            }
                        }
                        jQuery("#tabs").tabs();
                    }
                    else if (jsonObject.success.localeCompare("false") == 0) {
                        CARBON.showErrorDialog(jsonObject.jsonValue);
                        jQuery('.js_resultCol').hide();
                    }
                }
            }
        }

    });
}

