/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/* This is used for obtaining any-word hints.
*
* Content within the getAnyWordSuggestions function is extracted from [1].
* Rather than registering a helper in code mirror, here we allow to invoke getAnyWordSuggestions function
* and get any-word suggestions.
*
* [1] http://codemirror.net/addon/hint/anyword-hint.js
* */

function getAnyWordSuggestions(editor, options){
    var WORD = /[\w$]+/, RANGE = 500;

    //making the anyword suggestions as 'anyWordSuggestions'
    var word = options && options.word || WORD;
    var range = options && options.range || RANGE;
    var cur = editor.getCursor(), curLine = editor.getLine(cur.line);
    var end = cur.ch, start = end;
    while (start && word.test(curLine.charAt(start - 1))) --start;
    var curWord = start != end && curLine.slice(start, end);

    var anyWordSuggestions = [], seen = {};
    var re = new RegExp(word.source, "g");
    for (var dir = -1; dir <= 1; dir += 2) {
        var line = cur.line, endLine = Math.min(Math.max(line + dir * range, editor.firstLine()), editor.lastLine()) + dir;
        for (; line != endLine; line += dir) {
            var text = editor.getLine(line), m;
            while (m = re.exec(text)) {
                if (line == cur.line && m[0] === curWord) continue;
                if ((!curWord || m[0].lastIndexOf(curWord, 0) == 0) && !Object.prototype.hasOwnProperty.call(seen, m[0])) {
                    seen[m[0]] = true;
                    anyWordSuggestions.push(m[0]);
                }
            }
        }
    }
    return anyWordSuggestions;
}