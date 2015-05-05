<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<script type="text/javascript" src="../siddhitryit/js/sendInputDetails.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<div id="middle">
    <h2>Siddhi Try It</h2>

    <div id="workArea">
        <table class="styledLeft" id="userTable">
            <thead>
            <tr>
                <th>Event Stream</th>
                <th>Execution Plan</th>
                <th>Result</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>
                    <textarea rows="50" cols="50" name="eventstream"
                              id="eventStreamId">enter stream</textarea>
                </td>

                <td>
                    <textarea rows="50" cols="50" name="executionplan"
                              id="executionPlanId">enter execution plan</textarea>
                    <br>
                    <input type="button" value="Submit" onclick="sendAjaxRequestToSiddhiProcessor()"/>
                </td>

                <td>
                    <textarea rows="50" cols="50" name="result"
                              id="resultId"></textarea>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
