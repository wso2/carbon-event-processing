/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wso2.carbon.event.output.adaptor.kafka.internal.util;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;

public class SimplePartitioner implements Partitioner {
    public SimplePartitioner (VerifiableProperties props) {
 
    }

    @Override
    public int partition(Object o, int i) {
        String key = (String) o;
        int partition = 0;
        int offset = key.lastIndexOf('.');
        if (offset > 0) {
            partition = Integer.parseInt( key.substring(offset+1)) % i;
        }
        return partition;
    }
}
