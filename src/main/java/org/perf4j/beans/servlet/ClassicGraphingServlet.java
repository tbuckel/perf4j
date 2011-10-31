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
package org.perf4j.beans.servlet;

import org.perf4j.chart.StatisticsChartGenerator;
import org.perf4j.beans.sinks.statistics.GraphingStatisticsSink;
import org.perf4j.servlet.AbstractGraphingServlet;

import java.util.ArrayList;
import java.util.List;

/**
 * GraphingServlet for bean based StopWatches in a plain servlet environment.
 * It looks for graphs from {@link GraphingStatisticsSink}s that are configured for your {@link org.perf4j.beans.StopWatchFactory}.
 * These {@link GraphingStatisticsSink}s must have a name and create an instance of this servlet in your web.xml file and
 * set a "graphNames" init parameter to be a comma-separated list of the appender names whose graphs you wish to display.
 *
 * For Spring Web Applications, check {@link SpringGraphingServlet}.
 *
 * @author Thomas Buckel
 * @author Alex Devine
 */
public class ClassicGraphingServlet extends AbstractGraphingServlet {

    /**
     * Finds the specified graph by using the
     * {@link org.perf4j.log4j.GraphingStatisticsAppender#getAppenderByName(String)} method to find the appender with
     * the specified name.
     *
     * @param name the name of the GraphingStatisticsAppender whose chart generator should be returned.
     * @return The specified chart generator, or null if no appender with the specified name was found.
     */
    protected StatisticsChartGenerator getGraphByName(String name) {
        GraphingStatisticsSink sink = GraphingStatisticsSink.getSinkByName(name);
        return (sink == null) ? null : sink.getChartGenerator();
    }

    /**
     * This method looks for all known GraphingStatisticsAppenders and returns their names.
     *
     * @return The list of known GraphingStatisticsAppender names.
     */
    protected List<String> getAllKnownGraphNames() {
        List<String> retVal = new ArrayList<String>();
        for (GraphingStatisticsSink appender : GraphingStatisticsSink.getAllGraphingStatisticsSinks()) {
            retVal.add(appender.getName());
        }
        return retVal;
    }
}
