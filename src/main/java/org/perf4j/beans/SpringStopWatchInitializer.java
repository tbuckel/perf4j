/* Copyright (c) 2011 Thomas Buckel
 * All rights reserved.  http://www.perf4j.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.perf4j.beans;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import java.util.ArrayList;
import java.util.List;

/**
 * A Spring bean that initializes the static {@link StopWatchFactory}.
 *
 * It takes care of the lifecycle of the attached {@link TimingEventSink}s and with being a
 * priority {@code BeanFactoryPostProcessor} it configures the {@link StopWatchFactory} very early in the
 * application context's lifecycle.
 *
 * The priority can be overriden.
 *
 * Typical usage in Spring application context:
 * <pre>
 * {@code
 * <bean id="perf4j" class="org.perf4j.beans.SpringStopWatchInitializer">
 *      <property name="sinks">
 *          <list>
 *              <bean class="org.perf4j.beans.sinks.Slf4jTimingEventSink" />
 *              <bean class="org.perf4j.beans.sinks.AsyncCoalescingStatisticsSink">
 *                  <property name="statisticsSinks">
 *                      <list>
 *                          <bean class="org.perf4j.beans.sinks.statistics.GraphingStatisticsSink">
 *                              <property name="name" value="test" />
 *                              <property name="tagNamesToGraph" value="test1,test2" />
 *                              <property name="graphType" value="COUNT" />
 *                          </bean>
 *                          <bean class="org.perf4j.beans.sinks.statistics.Slf4jTimingStatisticsSink" />
 *                          <bean class="org.perf4j.beans.sinks.statistics.JmxAttributeStatisticsSink">
 *                              <property name="exposeTagsAutomatically" value="true" />
 *                          </bean>
 *                      </list>
 *                  </property>
 *              </bean>
 *          </list>
 *      </property>
 *  </bean>
 *  }
 *  </pre>
 *
 * @author Thomas Buckel
 */
public class SpringStopWatchInitializer implements InitializingBean, DisposableBean, BeanFactoryPostProcessor, PriorityOrdered {

    private List<TimingEventSink> sinks = new ArrayList<TimingEventSink>();
    private StopWatchSinkManager manager;
    private int order = Ordered.LOWEST_PRECEDENCE;

    public void setSinks(List<TimingEventSink> sinks) {
        this.sinks = sinks;
    }

    public void afterPropertiesSet() {
        manager = new StopWatchSinkManager();
        manager.setSinks(sinks);
        manager.start();
    }

    public void destroy() {
        manager.stop();
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        StopWatchFactory.setRootSink(manager);
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

}
