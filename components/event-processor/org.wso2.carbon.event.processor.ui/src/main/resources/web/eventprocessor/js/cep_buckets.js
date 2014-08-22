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
//Method that used in jsp files

function goBack() {
    var callback =
    {
        success:function (o) {
            location.href = 'cep_buckets.jsp';
            if (o.responseText !== undefined) {
            }
        },
        failure:function (o) {
            if (o.responseText !== undefined) {
                alert("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
            }
        }
    };
    var request = YAHOO.util.Connect.asyncRequest('POST', "cep_clear_property_sessions_ajaxprocessor.jsp", callback, "");

}

/**
 * Utils
 */
function clearDataInTable(tableName) {
    deleteTableRows(tableName, true);
    document.getElementById(tableName).style.display = "none";
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


function populateElementDisplay(elements, display) {
    for (var i = 0; i < elements.length; i++) {
        elements[i].style.display = display;
    }
}

