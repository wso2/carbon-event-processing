/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.metrics.core;

import org.wso2.carbon.metrics.manager.Gauge;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.siddhi.metrics.core.util.SiddhiMetricsConstants;
import org.wso2.siddhi.core.util.statistics.MemoryUsageTracker;
import org.wso2.siddhi.core.util.statistics.memory.ObjectSizeCalculator;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SiddhiMemoryUsageMetric implements MemoryUsageTracker {
    private ConcurrentMap<Object, ObjectMetric> registeredObjects = new ConcurrentHashMap<Object, ObjectMetric>();
    private boolean enabled;

    public SiddhiMemoryUsageMetric(boolean isEnabled) {
        enabled = isEnabled;
    }

    /**
     * Register the object that needs to be measured the memory usage.
     *
     * @param object Object.
     * @param name   An unique value to identify the object.
     */
    @Override
    public void registerObject(Object object, String name) {
        if (enabled) {
            if (registeredObjects.get(object) == null) {
                String metricId = MetricManager.name(name, SiddhiMetricsConstants.METRIC_SUFFIX_MEMORY);
                registeredObjects.put(object, new ObjectMetric(object, metricId));
            }
        }
    }

    /**
     * @return Name of the memory usage tracker.
     */
    @Override
    public String getName(Object object) {
        if (enabled) {
            if (registeredObjects.get(object) != null) {
                return registeredObjects.get(object).getName();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    class ObjectMetric {
        private final Object object;
        private String name;

        public ObjectMetric(final Object object, String name) {
            this.object = object;
            this.name = name;
            initMetric();
        }

        public String getName() {
            return name;
        }

        private void initMetric() {
            MetricManager.gauge(
                    name,
                    Level.DEBUG,
                    new Gauge<Long>() {
                        @Override
                        public Long getValue() {
                            try {
                                return ObjectSizeCalculator.getObjectSize(object);
                            } catch (UnsupportedOperationException e) {
                                return 0l;
                            }
                        }
                    });
        }
    }
}
