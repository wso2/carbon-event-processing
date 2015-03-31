/*
 *
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 *
 */

var CONSTANTS = {
    webAppName: 'outputui',
    urlSeperator: '/',
    urlGetParameter : '?lastUpdatedTime=',
    tenantUrlAttribute: 't',
    urlTransportHttp : 'http://',
    urlTransportWebsocket : 'ws://',
    colon : ':',
    defaultIntervalTime : 10 * 1000,
    defaultUserDomain : 'carbon.super',
    defaultHostName : 'localhost',
    defaultPortNumber : '9763',
    defaultMode : 'AUTO',
    processModeHTTP : 'HTTP',
    processModeWebSocket : 'WEBSOCKET',
    processModeAuto : 'AUTO',
    superTenantId : 'carbon.super',
    websocketWaitTime : 1000,
    websocketTimeAppender : 400
};


var websocket = null;
var webSocketUrl;
var httpUrl;
var cepHostName;
var cepPortNumber;
var isErrorOccured = false;
var lastUpdatedtime = -1;
var polingInterval;
var stream;
var streamVersion;
var firstPollingAttempt;
var processMode;
var onSuccessFunction;
var onErrorFunction;
var userDomainUrl = "";

function subscribe(streamName,version,intervalTime,domain,
                   listningFuncSuccessData,listningFuncErrorData,cepHost,cepPort,mode){

    stream = streamName;
    streamVersion = version;
    onSuccessFunction = listningFuncSuccessData;
    onErrorFunction = listningFuncErrorData;

    if(intervalTime == null || intervalTime == ""){
        polingInterval = CONSTANTS.defaultIntervalTime;
    } else{
        polingInterval = intervalTime * 1000;
    }

    if(domain == null || domain == ""){
        domain = CONSTANTS.defaultUserDomain;
    }

    if(cepHost == null || cepHost == ""){
        cepHostName = CONSTANTS.defaultHostName;
    } else{
        cepHostName = cepHost;
    }

    if(cepPort == null || cepPort == ""){
        cepPortNumber = CONSTANTS.defaultPortNumber;
    } else{
        cepPortNumber = cepPort;
    }

    if(mode == null || mode == ""){
        processMode = CONSTANTS.defaultMode;
    } else{
        processMode = mode;
    }

    if(domain != CONSTANTS.superTenantId){
        userDomainUrl = CONSTANTS.tenantUrlAttribute + CONSTANTS.urlSeperator + domain + CONSTANTS.urlSeperator;

    }
    webSocketUrl = CONSTANTS.urlTransportWebsocket + cepHostName + CONSTANTS.colon + cepPortNumber +
        CONSTANTS.urlSeperator + CONSTANTS.webAppName+ CONSTANTS.urlSeperator + userDomainUrl + stream +
        CONSTANTS.urlSeperator + streamVersion;

    if(processMode == CONSTANTS.processModeHTTP){
        firstPollingAttempt = true;
        startPoll();
    } else{
        initializeWebSocket(webSocketUrl);
    }
}


/**
 * Initializing Web Socket
 */
function initializeWebSocket(webSocketUrl){
    websocket = new WebSocket(webSocketUrl);
    websocket.onopen = webSocketOnOpen;
    websocket.onmessage = webSocketOnMessage;
    websocket.onclose = webSocketOnClose;
    websocket.onerror = webSocketOnError;
}

/**
 * Web socket On Open
 */

var webSocketOnOpen = function () {
    //onErrorFunction("Successfully connected to URL:" + webSocketUrl + "\n");
};


/**
 * On server sends a message
 */
var webSocketOnMessage = function (evt) {
    var event = evt.data;
    constructPayload(event);
};

/**
 * On server close
 */
var webSocketOnClose =function (e) {

    if(isErrorOccured){
        if(processMode != CONSTANTS.processModeWebSocket){
            firstPollingAttempt = true;
            startPoll();
        }
    } else{
        waitForSocketConnection(websocket);
    }
};

/**
 * On server Error
 */
var webSocketOnError = function (err) {
    var error = "Error: Cannot connect to Websocket URL:" + webSocketUrl + " .Hence closing the connection!";

    onErrorFunction(error);
    isErrorOccured = true;

};

/**
 * Gracefully increments the connection retry
 */
var waitTime = CONSTANTS.websocketWaitTime;
function waitForSocketConnection(socket, callback){
    setTimeout(
        function () {
            if (socket.readyState === 1) {
                initializeWebSocket(webSocketUrl);
                console.log("Connection is made");
                if(callback != null){
                    callback();
                }
                return;
            } else {
                websocket = new WebSocket(webSocketUrl);
                waitTime += CONSTANTS.websocketTimeAppender;
                waitForSocketConnection(websocket, callback);
            }
        }, waitTime);
}

/**
 * Polling to retrieve events from http request periodically
 */
function startPoll(){

    (function poll(){
        setTimeout(function(){
            httpUrl = CONSTANTS.urlTransportHttp + cepHostName + CONSTANTS.colon + cepPortNumber + CONSTANTS.urlSeperator
                + CONSTANTS.webAppName + CONSTANTS.urlSeperator + userDomainUrl + stream + CONSTANTS.urlSeperator +
                streamVersion + CONSTANTS.urlGetParameter + lastUpdatedtime;

            $.getJSON(httpUrl, function(responseText) {
                if(firstPollingAttempt){
                    /*var data = $("textarea#idConsole").val();
                     $("textarea#idConsole").val(data + "Successfully connected to HTTP.");*/
                    firstPollingAttempt = false;
                }
                if($.parseJSON(responseText.eventsExists)){
                    lastUpdatedtime = responseText.lastEventTime;

                    var eventList = (responseText.events);
                    constructPayload(eventList);
                }
                startPoll();
            })
                .fail(function(errorData) {
                    var errorData = JSON.parse(errorData.responseText);
                    onErrorFunction(errorData.error);
                });
        }, polingInterval);
    })()
}

function constructPayload(eventsArray){

    var streamId = stream + CONSTANTS.colon + streamVersion;
    var eventsData = {};
    var jsonData = [];

    eventsData ["source"] = streamId;
    eventsData ["data"] = eventsArray;
    jsonData.push(eventsData);
    console.log(jsonData);
    onSuccessFunction(jsonData);

}