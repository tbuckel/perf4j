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
package org.perf4j.beans.servlet;

import org.perf4j.chart.StatisticsChartGenerator;
import org.perf4j.beans.sinks.statistics.GraphingStatisticsSink;
import org.perf4j.servlet.AbstractGraphingServlet;

import java.util.*;

/**
 * GraphingServlet for bean based StopWatches in a Spring servlet environment.
 *
 * Rather than configuring the names of {@link GraphingStatisticsSink}s that should be exposed, just configure the reference
 * to the {@link GraphingStatisticsSink}s that should be exposed.
 *
 * TODO Code sample
 *
 * @author Thomas Buckel
 * @author Alex Devine
 */
public class SpringGraphingServlet extends AbstractGraphingServlet {

    private Map<String, GraphingStatisticsSink> graphingStatisticsSinks = new HashMap<String, GraphingStatisticsSink>();

    public void setGraphingStatisticsSinks(Collection<GraphingStatisticsSink> graphingStatisticsSinks) {
        for (GraphingStatisticsSink sink : graphingStatisticsSinks) {
            this.graphingStatisticsSinks.put(sink.getName(), sink);
        }
    }

    /**
     * Finds the specified graph by using the
     * {@link org.perf4j.log4j.GraphingStatisticsAppender#getAppenderByName(String)} method to find the appender with
     * the specified name.
     *
     * @param name the name of the GraphingStatisticsAppender whose chart generator should be returned.
     * @return The specified chart generator, or null if no appender with the specified name was found.
     */
    protected StatisticsChartGenerator getGraphByName(String name) {
        GraphingStatisticsSink sink = graphingStatisticsSinks.get(name);
        return (sink != null) ? sink.getChartGenerator() : null;
    }

    /**
     * This method looks for all known GraphingStatisticsAppenders and returns their names.
     *
     * @return The list of known GraphingStatisticsAppender names.
     */
    protected List<String> getAllKnownGraphNames() {
        return new ArrayList<String>(graphingStatisticsSinks.keySet());
    }

}
