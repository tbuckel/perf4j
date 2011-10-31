/* Copyright (c) 2008-2009 HomeAway, Inc.
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
package org.perf4j.helpers;

import org.perf4j.TimingStatistics;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The StatsValueRetriever is used to enable retrieval of any of the statistics on the TimingStatistics object
 * by name. In addition, retrieval of a transactions per second statistic is supported.
 *
 * @author Alex Devine
 */
public enum StatsValueRetriever {

    MEAN() {
        public Number getStatsValue(TimingStatistics timingStats, long windowLength) {
            return (timingStats == null) ? 0.0 : timingStats.getMean();
        }

        public Class getValueClass() { return Double.class; }

        public String getValueName() { return "Mean"; }
    },
    STD_DEV() {
        public Number getStatsValue(TimingStatistics timingStats, long windowLength) {
            return (timingStats == null) ? 0.0 : timingStats.getStandardDeviation();
        }

        public Class getValueClass() { return Double.class; }

        public String getValueName() { return "StdDev"; }
    },
    MIN() {
        public Number getStatsValue(TimingStatistics timingStats, long windowLength) {
            return (timingStats == null) ? 0L : timingStats.getMin();
        }

        public Class getValueClass() { return Long.class; }

        public String getValueName() { return "Min"; }
    },
    MAX() {
        public Number getStatsValue(TimingStatistics timingStats, long windowLength) {
            return (timingStats == null) ? 0L : timingStats.getMax();
        }

        public Class getValueClass() { return Long.class; }

        public String getValueName() { return "Max"; }
    },
    COUNT() {
        public Number getStatsValue(TimingStatistics timingStats, long windowLength) {
            return (timingStats == null) ? 0 : timingStats.getCount();
        }

        public Class getValueClass() { return Integer.class; }

        public String getValueName() { return "Count"; }
    },
    TPS() {
        public Number getStatsValue(TimingStatistics timingStats, long windowLength) {
            return (timingStats == null || windowLength == 0) ?
                   0.0 :
                   ((double) timingStats.getCount()) / (((double) windowLength) / 1000.0);
        }

        public Class getValueClass() { return Double.class; }

        public String getValueName() { return "TPS"; }
    };

    /**
     * Default unmodifiable Map of statistic name to the corresponding StatsValueRetriever object that retrieves that
     * statistic. Statistic names are Mean, StdDev, Min, Max, Count and TPS.
     */
    public static final Map<String, StatsValueRetriever> DEFAULT_RETRIEVERS;
    static {
        Map<String, StatsValueRetriever> defaultRetrievers = new LinkedHashMap<String, StatsValueRetriever>();
        for (StatsValueRetriever statsValueRetriever : StatsValueRetriever.values()) {
            defaultRetrievers.put(statsValueRetriever.getValueName(), statsValueRetriever);
        }
        DEFAULT_RETRIEVERS = Collections.unmodifiableMap(defaultRetrievers);
    }

    /**
     * Retrieves a single statistic value from the specified TimingStatistics object.
     *
     * @param timingStats  The TimingStatistics object containing the data to be retrieved.
     *                     May be null, if so 0 is returned.
     * @param windowLength The length of time, in milliseconds, of the data window represented by the TimingStatistics.
     * @return The value requested.
     */
    public abstract Number getStatsValue(TimingStatistics timingStats, long windowLength);

    /**
     * Gets the class of the object returned by {@link #getStatsValue(org.perf4j.TimingStatistics, long)}.
     *
     * @return The value class.
     */
    public abstract Class getValueClass();

    /**
     * Returns the name of the value, such as "Mean" or "Max".
     *
     * @return The name of the value retrieved.
     */
    public abstract String getValueName();
}
