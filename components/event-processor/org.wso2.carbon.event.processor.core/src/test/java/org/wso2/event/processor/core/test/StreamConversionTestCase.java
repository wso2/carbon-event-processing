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
package org.wso2.event.processor.core.test;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.processor.core.StreamConfiguration;

public class StreamConversionTestCase {
    private static final Log log = LogFactory.getLog(StreamConversionTestCase.class);

    @Test
    public void testFromWso2EventToSiddhi1() throws MalformedStreamDefinitionException {

        StreamConfiguration streamConfiguration=new StreamConfiguration("Foo","1.0.0");
        Assert.assertEquals("Foo",streamConfiguration.getSiddhiStreamName());

    }

    @Test
    public void testFromWso2EventToSiddhi2() throws MalformedStreamDefinitionException {

        StreamConfiguration streamConfiguration=new StreamConfiguration("Foo","1.0.1");
        Assert.assertEquals("Foo_1_0_1",streamConfiguration.getSiddhiStreamName());

    }

    @Test
    public void testFromWso2EventToSiddhi3() throws MalformedStreamDefinitionException {

        StreamConfiguration streamConfiguration=new StreamConfiguration("Foo","1.0.1","Bar");
        Assert.assertEquals("Bar",streamConfiguration.getSiddhiStreamName());

    }

}
