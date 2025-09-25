/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.simulator.core.internal.util;

import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.simulator.core.exception.EventSimulatorRuntimeException;
import java.io.File;
import java.io.IOException;

public class EventSimulatorUtil {

    public static Event getWso2Event(StreamDefinition streamDefinition, long timestamp, Object[] data) {
        int metaAttrCount = streamDefinition.getMetaData() != null ? streamDefinition.getMetaData().size() : 0;
        int correlationAttrCount = streamDefinition.getCorrelationData() != null ? streamDefinition.getCorrelationData().size() : 0;
        int payloadAttrCount = streamDefinition.getPayloadData() != null ? streamDefinition.getPayloadData().size() : 0;
        Object[] metaAttrArray = new Object[metaAttrCount];
        Object[] correlationAttrArray = new Object[correlationAttrCount];
        Object[] payloadAttrArray = new Object[payloadAttrCount];
        for (int i = 0; i < data.length; i++) {
            if (i < metaAttrCount) {
                metaAttrArray[i] = data[i];
            } else if (i < metaAttrCount + correlationAttrCount) {
                correlationAttrArray[i - metaAttrCount] = data[i];
            } else {
                payloadAttrArray[i - (metaAttrCount + correlationAttrCount)] = data[i];
            }
        }
        return new Event(streamDefinition.getStreamId(), timestamp, metaAttrArray, correlationAttrArray, payloadAttrArray);
    }


    /**
     * Validate the given fileName path is in the parent directory itself.
     *
     * @param fileName File name which needs to be validated.
     */
    public static void validatePath(String fileName) {
        if (fileName.contains("../") || fileName.contains("..\\")) {
            throw new EventSimulatorRuntimeException("File name contains restricted path elements. " + fileName);
        }
    }


    /**
     * Generic file validation method that combines all validation logic.
     * Validates file name, extension, and path security.
     *
     * @param fileName File name to validate
     * @param baseDirectory Base directory where files should be contained  
     * @param allowedExtensions Array of allowed file extensions (without dot), e.g., {"csv", "xml"}
     * @throws EventSimulatorRuntimeException if validation fails
     */
    public static void validateFile(String fileName, String baseDirectory, String[] allowedExtensions) {
        // 1. Basic null/empty validation
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new EventSimulatorRuntimeException("Invalid file name. File name is not available");
        }

        fileName = fileName.trim();

        // 2. File extension validation
        if (allowedExtensions != null && allowedExtensions.length > 0) {
            boolean validExtension = false;
            String lowerFileName = fileName.toLowerCase();
            
            for (String extension : allowedExtensions) {
                if (lowerFileName.endsWith("." + extension.toLowerCase())) {
                    validExtension = true;
                    
                    // Ensure filename has content before the extension
                    int minLength = extension.length() + 1; // extension + dot
                    if (fileName.length() <= minLength) {
                        throw new EventSimulatorRuntimeException(
                            "Invalid file name: filename cannot be just an extension. File: " + fileName);
                    }
                    break;
                }
            }
            
            if (!validExtension) {
                String allowedExts = String.join(", ", allowedExtensions);
                throw new EventSimulatorRuntimeException(
                    "Invalid file type. Only " + allowedExts + " files are allowed. File: " + fileName);
            }
        }

        // 3. Path validation
        if (baseDirectory != null) {
            try {
                // Canonical path validation
                File baseDir = new File(baseDirectory);
                File targetFile = new File(baseDir, fileName);
                
                String baseDirCanonical = baseDir.getCanonicalPath();
                String targetFileCanonical = targetFile.getCanonicalPath();
                
                // Ensure resolved path stays within base directory
                if (!targetFileCanonical.startsWith(baseDirCanonical + File.separator) && 
                    !targetFileCanonical.equals(baseDirCanonical)) {
                    throw new EventSimulatorRuntimeException("File validation failed: Invalid file name: " + fileName);
                }
                
            } catch (IOException e) {
                throw new EventSimulatorRuntimeException("File validation failed: Invalid file name: " + fileName);
            }
        }
    }

    /**
     * Convenience method for CSV file validation.
     * 
     * @param fileName CSV file name to validate
     * @param baseDirectory Base directory where CSV files should be contained
     * @throws EventSimulatorRuntimeException if validation fails
     */
    public static void validateCSVFile(String fileName, String baseDirectory) {
        validateFile(fileName, baseDirectory, new String[]{"csv"});
    }

    /**
     * Validate the given fileName and path.
     *
     * @param fileName File name which needs to be validated.
     * @param baseDirectory Base directory where files should be contained
     * @throws EventSimulatorRuntimeException if validation fails
     */
    public static void validatePath(String fileName, String baseDirectory) {
        validateFile(fileName, baseDirectory, null);
    }
}
