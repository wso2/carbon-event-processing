/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.processor.core.internal.persistence;

import com.google.common.io.Files;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;

import java.io.File;
import java.io.IOException;

public class FileSystemPersistenceStore implements PersistenceStore {
    private static final Log log = LogFactory.getLog(FileSystemPersistenceStore.class);
    private static final String folderSeparator = File.separator;
    private static final String folder = "repository" + folderSeparator + "cep_persistence";
    private static final String fileNameSeparator = "_";

    @Override
    public void save(String queryPlanIdentifier, String revision, byte[] bytes) {
        String tenantId = getTenantId();
        File file = new File(folder + folderSeparator + tenantId + folderSeparator +
                queryPlanIdentifier + folderSeparator + revision);
        try {
            Files.createParentDirs(file);
            Files.write(bytes, file);
            if (log.isDebugEnabled()) {
                log.debug("Saved revision " + revision + " of ExecutionPlan:" + queryPlanIdentifier + " to the file system.");
            }
        } catch (IOException e) {
            log.error("Cannot load the revision " + revision + " of ExecutionPlan:" + queryPlanIdentifier +
                    "from file system.", e);
        }
    }

    @Override
    public byte[] load(String queryPlanIdentifier, String revision) {
        String tenantId = getTenantId();
        File file = new File(folder + folderSeparator + tenantId + folderSeparator +
                queryPlanIdentifier + folderSeparator + revision);
        try {
            byte[] bytes = Files.toByteArray(file);
            if (log.isDebugEnabled()) {
                log.debug("Loaded revision " + revision + " of ExecutionPlan:" + queryPlanIdentifier + " from the file system.");
            }
            return bytes;
        } catch (IOException e) {
            log.error("Cannot load the revision " + revision + " of ExecutionPlan:" + queryPlanIdentifier +
                    "from file system.", e);
        }
        return null;
    }

    @Override
    public String getLastRevision(String executionPlanIdentifier) {

        String tenantId = getTenantId();

        File dir = new File(folder + folderSeparator + tenantId + folderSeparator + executionPlanIdentifier);
        File[] files = dir.listFiles();

        if (files == null || files.length == 0) {
            return null;
        }

        String lastRevision = null;
        for (File file : files) {
            String fileName = file.getName();
            if (lastRevision == null || fileName.compareTo(lastRevision) > 0) {
                lastRevision = fileName;
            }
        }
        return lastRevision;
    }

    private String getTenantId() {
        return String.valueOf(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId()).replaceAll("-", "M");
    }
}

