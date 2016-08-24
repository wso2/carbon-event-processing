/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var ApplicationOptions = {
    colors: {
        states: {
            NORMAL: 'blue',
            WARNING: 'blue',
            OFFLINE: 'grey',
            ALERTED: 'red',
            UNKNOWN: 'black' // TODO: previous color #19FFFF , change this if black is not user friendly ;)
        },
        application: {
            header: 'grey'
        }
    },
    constance:{
        CEP_WEB_SOCKET_OUTPUT_ADAPTOR_NAME: 'Geo-Publisher-WebSocketLocal-FusedSpacialEvent',
        CEP_ON_ALERT_WEB_SOCKET_OUTPUT_ADAPTOR_NAME: 'Geo-Publisher-WebSocketLocal-GeoAlertNotifications',
        CEP_Traffic_STREAM_WEB_SOCKET_OUTPUT_ADAPTOR_NAME: 'DefaultWebsocketOutputAdaptorOnTrafficStream',
        CEP_WEB_SOCKET_OUTPUT_ADAPTOR_WEBAPP_NAME: 'outputwebsocket',
        TENANT_INDEX: 't',
        COLON : ':',
        PATH_SEPARATOR : '/',

        SPEED_HISTORY_COUNT: 20,
        NOTIFY_INFO_TIMEOUT: 1000,
        NOTIFY_SUCCESS_TIMEOUT: 1000,
        NOTIFY_WARNING_TIMEOUT: 3000,
        NOTIFY_DANGER_TIMEOUT: 5000
    },
    messages:{
        app:{

        }
    },
    leaflet: {
        iconUrls: {
            normalMovingIcon: '/portal/store/carbon.super/fs/gadget/geo-dashboard/img/markers/object-types/default/moving/alerted.png',
            alertedMovingIcon: '/portal/store/carbon.super/fs/gadget/geo-dashboard/img/markers/moving/arrow_alerted.png',
            offlineMovingIcon: '/portal/store/carbon.super/fs/gadget/geo-dashboard/img/markers/moving/arrow_offline.png',
            warningMovingIcon: '/portal/store/carbon.super/fs/gadget/geo-dashboard/img/markers/moving/arrow_warning.png',
            defaultMovingIcon: '/portal/store/carbon.super/fs/gadget/geo-dashboard/img/markers/moving/arrow_normal.png',

            normalNonMovingIcon: '/portal/store/carbon.super/fs/gadget/geo-dashboard/img/markers/non_moving/dot_normal.png',
            alertedNonMovingIcon: '/portal/store/carbon.super/fs/gadget/geo-dashboard/img/markers/non_moving/dot_alerted.png',
            offlineNonMovingIcon: '/portal/store/carbon.super/fs/gadget/geo-dashboard/img/markers/non_moving/dot_offline.png',
            warningNonMovingIcon: '/portal/store/carbon.super/fs/gadget/geo-dashboard/img/markers/non_moving/dot_warning.png',
            defaultNonMovingIcon: '/portal/store/carbon.super/fs/gadget/geo-dashboard/img/markers/non_moving/dot_normal.png',

            normalPlaceIcon: '/portal/store/carbon.super/fs/gadget/geo-dashboard/img/markers/places/marker-icon.png',
            alertedPlaceIcon: '/portal/store/carbon.super/fs/gadget/geo-dashboard/img/markers/places/redMarker.png',
            offlinePlaceIcon: '/portal/store/carbon.super/fs/gadget/geo-dashboard/img/markers/places/ashMarker.png',
            warningPlaceIcon: '/portal/store/carbon.super/fs/gadget/geo-dashboard/img/markers/places/pinkMarker.png',
            defaultPlaceIcon: '/portal/store/carbon.super/fs/gadget/geo-dashboard/img/markers/places/marker-icon.png',

            defaultIcon: '/portal/store/carbon.super/fs/gadget/geo-dashboard/img/markers/moving/default_icons/marker-icon.png',
            resizeIcon: '/portal/store/carbon.super/fs/gadget/geo-dashboard/img/markers/resize.png',
            stopIcon:  '/portal/store/carbon.super/fs/gadget/geo-dashboard/img/markers/stopIcon.png'

        }
    }
};
