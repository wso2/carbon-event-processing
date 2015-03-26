/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.event.execution.manager.core.internal.processing;

/**
 * build a tree and derives the final condition by processing each
 * the condition node of the tree
 */
public class ConditionTree {
    //keeps track of the current processed condition
    private String finalCondition = "";
    //root node of the tree
    private ConditionNode root;

    /**
     * constructor
     */
    public ConditionTree() {
        this.root = null;
    }

    /**
     * get the root object
     *
     * @return root condition node
     */
    public ConditionNode getRoot() {
        return this.root;
    }

    /**
     * insert node to the tree
     *
     * @param node condition node object
     */
    public void insertNode(ConditionNode node) {
        //set the root of the tree
        if (node.getParent() == null) {
            root = node;
        } else {
            //set the currently inserted node as the left child according to template config
            ConditionNode temp;
            if (node.getOrder().equals("left")) {
                temp = node.getParent();
                temp.setLeft(node);
                //set the currently inserted node as the right child according to template config
            } else if (node.getOrder().equals("right")) {
                temp = node.getParent();
                temp.setRight(node);
            }
        }
    }

    /**
     * traverse the tree recursively using in-order traversal and process the current condition
     *
     * @param root root condition node
     */
    public void traverse(ConditionNode root) {
        if (root.getLeft() != null) {
            traverse(root.getLeft());
        }
        processCondition(root);
        if (root.getRight() != null) {
            traverse(root.getRight());
        }
    }

    /**
     * process parameter condition and assign it to the
     * current processed condition and set the query expression by
     * setting parenthesis
     *
     * @param root root condition node
     */
    public void processCondition(ConditionNode root) {
        //check if the node is a type of PARAMETER
        if (root.getType().equals("PARAMETER")) {
            finalCondition = finalCondition + "(" + root.getCurrentCondition() + ")";
            //setting parenthesis according to (AND/OR) cases
        } else {
            if ((finalCondition.split("and").length > 1) || finalCondition.split("or").length > 1) {
                finalCondition = "(" + finalCondition + ")";
                finalCondition = finalCondition + root.getType().toLowerCase();
            } else {
                finalCondition = finalCondition + root.getType().toLowerCase();
            }


        }
    }

    /**
     * get final string content
     *
     * @return string content
     */
    public String getFinalString() {
        return this.finalCondition;
    }
}