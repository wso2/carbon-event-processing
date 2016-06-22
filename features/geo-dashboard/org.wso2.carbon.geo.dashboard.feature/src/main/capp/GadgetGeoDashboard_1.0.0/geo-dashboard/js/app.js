/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


$(".modal").draggable({
    handle: ".modal-header"
});

//Clear modal content for reuse the wrapper by other functions
$('body').on('hidden.bs.modal', '.modal', function () {
    $(this).removeData('bs.modal');
});

/*Map layer configurations*/
var map;

initialLoad();

function initialLoad() {
    if (document.getElementById('map') == null) {
        setTimeout(initialLoad, 500); // give everything some time to render
    } else {
        initializeMap();
        getTileServers();
        loadWms();
        processAfterInitializationMap();
        //Access gps and make zoom to server location as map center
        //navigator.geolocation.getCurrentPosition(success, error);
        $("#loading").hide();
    }
}


//function success(position) {
//    var browserLatitude = position.coords.latitude;
//    var browserLongitude = position.coords.longitude;
//    map.setView([browserLatitude, browserLongitude]);
//    map.setZoom(14);
//    $.UIkit.notify({
//        message: "Map view set to browser's location",
//        status: 'info',
//        timeout: ApplicationOptions.constance.NOTIFY_INFO_TIMEOUT,
//        pos: 'top-center'
//    });
//}
//
//function error() {
//    $.UIkit.notify({
//        message: "Unable to find browser location!",
//        status: 'warning',
//        timeout: ApplicationOptions.constance.NOTIFY_WARNING_TIMEOUT,
//        pos: 'top-center'
//    });
//}


function initializeMap() {
    if (typeof(map) !== 'undefined') {
        map.remove();
    }


    if (document.getElementById('map') == null) {
        console.log("no map");
    } else {
    }

    map = L.map("map", {
        zoom: 14,
        center:[51.548525, 0.111749],
        layers: [defaultOSM, defaultTFL],
        zoomControl: false,
        attributionControl: false,
        maxZoom: 20,
        maxNativeZoom: 18
    });

    map.on('click', function (e) {
        $.UIkit.offcanvas.hide();//[force = false] no animation
    });

    map.on('zoomend', function () {
        if (map.getZoom() < 14) {
            // remove busStops
            var layer;
            for (var key in currentSpatialObjects) {
                if (currentSpatialObjects.hasOwnProperty(key)) {
                    object = currentSpatialObjects[key];
                    if (object.type == "STOP")
                        map.removeLayer(object.geoJson);
                }
            }
            console.log("removed busStops from map");
        } else {

            var layer;
            for (var key in currentSpatialObjects) {
                if (currentSpatialObjects.hasOwnProperty(key)) {
                    object = currentSpatialObjects[key];
                    if (object.type == "STOP")
                        map.addLayer(object.geoJson);
                }
            }
            console.log("added busStops to map");
        }

    });


}

/* Attribution control */
function updateAttribution(e) {
    $.each(map._layers, function (index, layer) {
        if (layer.getAttribution) {
            $("#attribution").html((layer.getAttribution()));
        }
    });
}

var attributionControl;
var groupedOverlays;
var layerControl;

function processAfterInitializationMap(){
    attributionControl = L.control({
        position: "bottomright"
    });
    attributionControl.onAdd = function (map) {
        var div = L.DomUtil.create("div", "leaflet-control-attribution");
        div.innerHTML = "<a href='#' onclick='$(\"#attributionModal\").modal(\"show\"); return false;'>Attribution</a>";
        return div;
    };
    //map.addControl(attributionControl);

    //L.control.fullscreen({
    //    position: 'bottomright'
    //}).addTo(map);
    L.control.zoom({
        position: "bottomright"
    }).addTo(map);

    groupedOverlays = {
        "Web Map Service layers": {}
    };

    layerControl = L.control.groupedLayers(baseLayers, groupedOverlays, {
        collapsed: true
    }).addTo(map);

    //L.control.layers(baseLayers).addTo(map);
    //map.addLayer(defaultTFL);
}

/* Highlight search box text on click */
$("#searchbox").click(function () {
    $(this).select();
});

/* TypeAhead search functionality */

var substringMatcher = function () {
    return function findMatches(q, cb) {
        var matches, substrRegex;
        matches = [];
        substrRegex = new RegExp(q, 'i');
        $.each(currentSpatialObjects, function (i, str) {
            if (substrRegex.test(i)) {
                matches.push({value: i});
            }
        });

        cb(matches);
    };
};

var chart;
function createChart() {
    chart = c3.generate({
        bindto: '#chart_div',
        data: {
            columns: [
                ['speed']
            ]
        },
        subchart: {
            show: true
        },
        axis: {
            y: {
                label: {
                    text: 'Speed',
                    position: 'outer-middle'
                }
            }
        },
        legend: {
            show: false
        }
    });
}

var predictionChart;
function createPredictionChart() {
    predictionChart = c3.generate({
        bindto: '#prediction_chart_div',
        data: {
            x: 'x',
            columns: [
                ['traffic']
            ]
        },
        subchart: {
            show: true
        },
        axis: {
            y: {
                label: {
                    text: 'Traffic',
                    position: 'outer-middle'
                }
            },
            x: {
                label: {
                    text: 'UTC hour for today',
                    position: 'outer-middle'
                }
            }

        },
        legend: {
            show: false
        }
    });
}

$('#searchbox').typeahead({
        hint: true,
        highlight: true,
        minLength: 1
    },
    {
        name: 'speed',
        displayKey: 'value',
        source: substringMatcher()
    }).on('typeahead:selected', function ($e, datum) {
        objectId = datum['value'];
        focusOnSpatialObject(objectId)
    });

var toggled = false;
function focusOnSpatialObject(objectId) {
    console.log("Selecting" + objectId);
    var spatialObject = currentSpatialObjects[objectId];// (local)
    if (!spatialObject) {
        $.UIkit.notify({
            message: "Spatial Object <span style='color:red'>" + objectId + "</span> not in the Map!!",
            status: 'warning',
            timeout: ApplicationOptions.constance.NOTIFY_WARNING_TIMEOUT,
            pos: 'top-center'
        });
        return false;
    }
    clearFocus(); // Clear current focus if any
    selectedSpatialObject = objectId; // (global) Why not use 'var' other than implicit declaration http://stackoverflow.com/questions/1470488/what-is-the-function-of-the-var-keyword-and-when-to-use-it-or-omit-it#answer-1471738

    console.log("Selected " + objectId + " type " + spatialObject.type);
    if (spatialObject.type == "area") {
        spatialObject.focusOn(map);
        return true;
    }

    map.setView(spatialObject.marker.getLatLng(), 15, {animate: true}); // TODO: check the map._layersMaxZoom and set the zoom level accordingly

    $('#objectInfo').find('#objectInfoId').html(selectedSpatialObject);
    spatialObject.marker.openPopup();
    if (!toggled) {
        $('#objectInfo').animate({width: 'toggle'}, 100);
        toggled = true;
    }
    getAlertsHistory(objectId);
    spatialObject.drawPath();
    setTimeout(function () {
        createChart();
        chart.load({columns: [spatialObject.speedHistory.getArray()]});
    }, 100);
}


// Unfocused on current searched spatial object
function clearFocus() {
    if (selectedSpatialObject) {
        spatialObject = currentSpatialObjects[selectedSpatialObject];
        spatialObject.removeFromMap();
        selectedSpatialObject = null;
    }
}