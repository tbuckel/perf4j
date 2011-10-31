/* Copyright (c) 2008-2009 HomeAway, Inc.
 * Copyright (c) 2011 Thomas Buckel
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
package org.perf4j.beans.sinks.statistics;

import org.perf4j.GroupedTimingStatistics;
import org.perf4j.beans.sinks.TimingStatisticsSink;
import org.perf4j.helpers.AcceptableRangeConfiguration;
import org.perf4j.helpers.StatisticsExposingMBean;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * A {@code TimingStatisticsSink} that exposes the statistics as values of a JMX Bean.
 * The attributes on this MBean can then be monitored by external tools. In addition, this class allows you to specify
 * notification thresholds so that a JMX notification is sent if one of the attributes falls outside an acceptable range
 * (for example, if the mean time for a specific value is too high).
 *
 * @author Alex Devine
 * @author Thomas Buckel
 */
public class JmxAttributeStatisticsSink implements TimingStatisticsSink {

    /** The object name of the MBean exposed through the JMX server. */
    private String mBeanName;

    /** A set of the tag names to be exposed as JMX attributes. */
    private Set<String> tagNamesToExpose;

    /**
     * A collection of the notification thresholds, which controls whether JMX notifications are sent
     * when attribute values fall outside acceptable ranges.
     */
    private Collection<AcceptableRangeConfiguration> notificationThresholds;

    /** Whether stats for tags are automatically exposed */
    private boolean exposeTagsAutomatically = false;

    /** This is the MBean that is registered with the MBeanServer */
    protected StatisticsExposingMBean mBean;

    public JmxAttributeStatisticsSink() {
        this(null, false);
    }

    public JmxAttributeStatisticsSink(boolean exposeTagsAutomatically, String... tagNamesToExpose) {
        this(null, exposeTagsAutomatically, tagNamesToExpose);
    }

    public JmxAttributeStatisticsSink(String mBeanName) {
        this(mBeanName, false);
    }

    public JmxAttributeStatisticsSink(String mBeanName, String... tagNamesToExpose) {
        this(mBeanName, false, tagNamesToExpose);
    }

    public JmxAttributeStatisticsSink(String mBeanName, boolean exposeTagsAutomatically, String... tagNamesToExpose) {
        this.mBeanName = (mBeanName != null) ? mBeanName : StatisticsExposingMBean.DEFAULT_MBEAN_NAME;
        this.exposeTagsAutomatically = exposeTagsAutomatically;
        this.tagNamesToExpose = new HashSet<String>(Arrays.asList(tagNamesToExpose));
    }

    /**
     * The <b>MBeanName</b> option is used to specify the ObjectName under which the StatisticsExposingMBean in the
     * MBeanServer. If not specified, defaults to org.perf4j:type=StatisticsExposingMBean,name=Perf4J.
     *
     * @return The value of the MBeanName option
     */
    public String getMBeanName() {
        return mBeanName;
    }

    /**
     * Sets the value of the <b>MBeanName</b> option. This must be a valid JMX ObjectName.
     *
     * @param mBeanName The new value for the MBeanName option.
     */
    public void setMBeanName(String mBeanName) {
        this.mBeanName = mBeanName;
    }

    /**
     * The <b>TagNamesToExpose</b> option is a comma-separated list of the tag names whose statistics values (e.g.
     * mean, min, max, etc.) should be exposed as MBeanAttributes. See the
     * {@link StatisticsExposingMBean} for more details.
     *
     * @return The value of the TagNamesToExpose expose
     */
    public Set<String> getTagNamesToExpose() {
        return tagNamesToExpose;
    }

    /**
     * Sets the value of the TagNamesToExpose option.
     *
     * @param tagNamesToExpose The new value for the TagNamesToExpose option.
     */
    public void setTagNamesToExpose(Set<String> tagNamesToExpose) {
        this.tagNamesToExpose = tagNamesToExpose;
    }

    public void addTagNameToExpose(String tagNameToExpose) {
        if (this.tagNamesToExpose == null) {
            this.tagNamesToExpose = new HashSet<String>();
        }
        this.tagNamesToExpose.add(tagNameToExpose);
    }

    /**
     * Sets the value of the NotificationThresholds option.
     *
     * @param notificationThresholds The new value for the NotificationThresholds option.
     */
    public void setNotificationThresholds(Collection<AcceptableRangeConfiguration> notificationThresholds) {
        this.notificationThresholds = notificationThresholds;
    }

    public void addNotificationThreshold(AcceptableRangeConfiguration notificationThreshold) {
        if (this.notificationThresholds == null) {
            this.notificationThresholds = new ArrayList<AcceptableRangeConfiguration>();
        }
        this.notificationThresholds.add(notificationThreshold);
    }

    public boolean isExposeTagsAutomatically() {
        return exposeTagsAutomatically;
    }

    public void setExposeTagsAutomatically(boolean exposeTagsAutomatically) {
        this.exposeTagsAutomatically = exposeTagsAutomatically;
    }


    // --- implements TimingStatisticsSink

    public void start() {
        if (tagNamesToExpose == null) {
            throw new RuntimeException("You must set the TagNamesToExpose option before activating this appender");
        }

        mBean = new StatisticsExposingMBean(mBeanName, tagNamesToExpose, notificationThresholds, exposeTagsAutomatically);

        try {
            MBeanServer mBeanServer = getMBeanServer();
            mBeanServer.registerMBean(mBean, new ObjectName(mBeanName));
        } catch (Exception e) {
            throw new RuntimeException("Error registering statistics MBean: " + e.getMessage(), e);
        }
    }

    public void handle(GroupedTimingStatistics statistics) {
        if (mBean != null) {
            mBean.updateCurrentTimingStatistics(statistics);
        }
    }

    public void stop() {
        try {
            MBeanServer mBeanServer = getMBeanServer();
            mBeanServer.unregisterMBean(new ObjectName(mBeanName));
        } catch (Exception e) {
            //fine, if we can't unregister it's not a big deal
        }
    }

    // --- helper methods ---

    /**
     * Gets the MBeanServer that should be used to register the StatisticsExposingMBean. Defaults to the Java Platform
     * MBeanServer. Subclasses could override this to use a different server.
     *
     * @return The MBeanServer to use for registrations.
     */
    protected MBeanServer getMBeanServer() {
        return ManagementFactory.getPlatformMBeanServer();
    }
}
