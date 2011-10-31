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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perf4j.GroupedTimingStatistics;
import org.perf4j.beans.sinks.TimingStatisticsSink;
import org.perf4j.chart.GoogleChartGenerator;
import org.perf4j.chart.StatisticsChartGenerator;
import org.perf4j.helpers.StatsValueRetriever;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@code TimingStatisticsSink} that takes the incoming GroupedTimingStatistics event and uses this data to update
 * a graphical view of the logged statistics.
 *
 * TODO Clarify on this: If ANOTHER appender is then attached to this appender then the graph URLs will be written to the appender on a scheduled basis.
 *
 * Alternatively, the graph can be viewed by setting up a {@link org.perf4j.beans.servlet.ClassicGraphingServlet} or
 * {@link org.perf4j.beans.servlet.SpringGraphingServlet} to expose the graph images.
 *
 * @author Alex Devine
 * @author Thomas Buckel
 */
public class GraphingStatisticsSink implements TimingStatisticsSink {

    private final Log log = LogFactory.getLog(getClass());

    /**
     * This class keeps track of all appenders of this type that have been created. This allows static access to
     * the appenders from the org.perf4j.log4j.servlet.GraphingServlet class.
     */
    protected final static Map<String, GraphingStatisticsSink> SINKS_BY_NAME =
            Collections.synchronizedMap(new LinkedHashMap<String, GraphingStatisticsSink>());

    // --- configuration options ---
    /**
     * The type of data to display on the graph. Defaults to "Mean" to display mean values. Acceptable values are any
     * constant name from the {@link org.perf4j.helpers.StatsValueRetriever} class, such as Mean, Min, Max, Count,
     * StdDev or TPS.
     */
    private StatsValueRetriever graphType = StatsValueRetriever.MEAN;
    /**
     * A set of the tag names that should be graphed. If not set then a separate series will be
     * displayed on the graph for each tag name logged.
     */
    private Set<String> tagNamesToGraph = null;
    /**
     * Gets the number of data points that will be written on each graph before the graph URL is written to any
     * attached appenders. Thus, this option is only relevant if there are attached appenders.
     * Defaults to <tt>StatisticsChartGenerator.DEFAULT_MAX_DATA_POINTS</tt>.
     */
    private int dataPointsPerGraph = StatisticsChartGenerator.DEFAULT_MAX_DATA_POINTS;

    /** Name of the Graph */
    private String name;

    // --- contained objects/state variables ---
    /**
     * The chart genertor, initialized in the <tt>activateOptions</tt> method, that stores the data for the chart.
     */
    private StatisticsChartGenerator chartGenerator;

    /**
     * Keeps track of the number of logged GroupedTimingStatistics, which is used to determine when a graph should
     * be written to any attached appenders.
     */
    private AtomicLong numLoggedStatistics = new AtomicLong();

    /**
     * Keeps track of whether there is existing data that hasn't yet been flushed to downstream appenders.
     */
    private volatile boolean hasUnflushedData = false;

    private boolean logGraphUrls = true;

    public GraphingStatisticsSink() {
    }

    public GraphingStatisticsSink(StatsValueRetriever graphType, String... tagNamesToGraph) {
        this(null, graphType, StatisticsChartGenerator.DEFAULT_MAX_DATA_POINTS, tagNamesToGraph);
    }

    public GraphingStatisticsSink(StatsValueRetriever graphType, int dataPointsPerGraph, String... tagNamesToGraph) {
        this(null, graphType, dataPointsPerGraph, tagNamesToGraph);
    }

    public GraphingStatisticsSink(StatsValueRetriever graphType, boolean logGraphUrls, String... tagNamesToGraph) {
        this(null, graphType, StatisticsChartGenerator.DEFAULT_MAX_DATA_POINTS, logGraphUrls, tagNamesToGraph);
    }

    public GraphingStatisticsSink(StatsValueRetriever graphType, int dataPointsPerGraph, boolean logGraphUrls, String... tagNamesToGraph) {
        this(null, graphType, dataPointsPerGraph, tagNamesToGraph);
    }

    public GraphingStatisticsSink(String name, StatsValueRetriever graphType, String... tagNamesToGraph) {
        this(name, graphType, StatisticsChartGenerator.DEFAULT_MAX_DATA_POINTS, tagNamesToGraph);
    }

    public GraphingStatisticsSink(String name, StatsValueRetriever graphType, int dataPointsPerGraph, String... tagNamesToGraph) {
        this(name, graphType, dataPointsPerGraph, true, tagNamesToGraph);
    }

    public GraphingStatisticsSink(String name, StatsValueRetriever graphType, boolean logGraphUrls, String... tagNamesToGraph) {
        this(name, graphType, StatisticsChartGenerator.DEFAULT_MAX_DATA_POINTS, logGraphUrls, tagNamesToGraph);
    }

    public GraphingStatisticsSink(String name, StatsValueRetriever graphType, int dataPointsPerGraph, boolean logGraphUrls, String... tagNamesToGraph) {
        this.name = name;
        this.graphType = graphType;
        this.dataPointsPerGraph = dataPointsPerGraph;
        this.logGraphUrls = logGraphUrls;
        this.tagNamesToGraph = new HashSet<String>(Arrays.asList(tagNamesToGraph));
    }

    // --- options ---

    /**
     * The <b>GraphType</b> option is used to specify the data that should be displayed on the graph. Acceptable
     * values are Mean, Min, Max, Count, StdDev and TPS (for transactions per second). Defaults to Mean if not
     * explicitly set.
     *
     * @return The value of the GraphType option
     */
    public StatsValueRetriever getGraphType() {
        return graphType;
    }

    /**
     * Sets the value of the <b>GraphType</b> option. This must be a valid type, one of
     * Mean, Min, Max, Count, StdDev or TPS (for transactions per second).
     *
     * @param graphType The new value for the GraphType option.
     */
    public void setGraphType(StatsValueRetriever graphType) {
        this.graphType = graphType;
    }

    /**
     * The <b>TagNamesToGraph</b> option is used to specify which tags should be logged as a data series on the
     * graph. If not specified ALL tags will be drawn on the graph, one series for each tag.
     *
     * @return The value of the TagNamesToGraph option
     */
    public Set<String> getTagNamesToGraph() {
        return tagNamesToGraph;
    }

    /**
     * Sets the value of the <b>TagNamesToGraph</b> option.
     *
     * @param tagNamesToGraph The new value for the TagNamesToGraph option.
     */
    public void setTagNamesToGraph(Set<String> tagNamesToGraph) {
        this.tagNamesToGraph = tagNamesToGraph;
    }

    /**
     * The <b>DataPointsPerGraph</b> option is used to specify how much data should be displayed on each graph before
     * it is written to any attached appenders. Defaults to <tt>StatisticsChartGenerator.DEFAULT_MAX_DATA_POINTS</tt>.
     *
     * @return The value of the DataPointsPerGraph option
     */
    public int getDataPointsPerGraph() {
        return dataPointsPerGraph;
    }

    /**
     * Sets the value of the <b>DataPointsPerGraph</b> option.
     *
     * @param dataPointsPerGraph The new value for the DataPointsPerGraph option.
     */
    public void setDataPointsPerGraph(int dataPointsPerGraph) {
        if (dataPointsPerGraph <= 0) {
            throw new IllegalArgumentException("The DataPointsPerGraph option must be positive");
        }
        this.dataPointsPerGraph = dataPointsPerGraph;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLogGraphUrls() {
        return logGraphUrls;
    }

    public void setLogGraphUrls(boolean logGraphUrls) {
        this.logGraphUrls = logGraphUrls;
    }

    public void start() {
        chartGenerator = createChartGenerator();

        //update the static SINKS_BY_NAME object
        if (getName() != null) {
            SINKS_BY_NAME.put(getName(), this);
        }
    }

    /**
     * Helper method creates a new StatisticsChartGenerator based on the options set on this appender. By default
     * a GoogleChartGenerator is created, though subclasses may override this method to create a different type of
     * chart generator.
     *
     * @return A newly created StatisticsChartGenerator.
     */
    protected StatisticsChartGenerator createChartGenerator() {
        if (graphType == null) {
            throw new RuntimeException("graphType is not set.");
        }

        //create the chart generator and set the enabled tags
        GoogleChartGenerator retVal = new GoogleChartGenerator(graphType);
        if (tagNamesToGraph != null) {
            retVal.setEnabledTags(tagNamesToGraph);
        }

        return retVal;
    }

    // --- exposed objects ---

    /**
     * Gets the contained StatisticsChartGenerator that is used to generate the graphs.
     *
     * @return The StatisticsChartGenerator used by this appender.
     */
    public StatisticsChartGenerator getChartGenerator() {
        return chartGenerator;
    }

    /**
     * This static method returns any created GraphingStatisticsAppender by its name.
     *
     * @param appenderName the name of the GraphingStatisticsAppender to return
     * @return the specified GraphingStatisticsAppender, or null if not found
     */
    public static GraphingStatisticsSink getSinkByName(String appenderName) {
        return SINKS_BY_NAME.get(appenderName);
    }

    /**
     * This static method returns an unmodifiable collection of all GraphingStatisticsAppenders that have been created.
     *
     * @return The collection of GraphingStatisticsAppenders created in this VM.
     */
    public static Collection<GraphingStatisticsSink> getAllGraphingStatisticsSinks() {
        return Collections.unmodifiableCollection(SINKS_BY_NAME.values());
    }

    public void handle(GroupedTimingStatistics event) {
        if (chartGenerator != null) {
            chartGenerator.appendData(event);
            hasUnflushedData = true;

            //output the graph if necessary to any attached appenders
            if ((numLoggedStatistics.incrementAndGet() % getDataPointsPerGraph()) == 0) {
                flush();
            }
        }
    }

    public void stop() {
        flush();
    }

    /**
     * This flush method writes the graph, with the data that exists at the time it is called, to any attached appenders.
     */
    public void flush() {
        if (hasUnflushedData) {
            hasUnflushedData = false;
            log.info(chartGenerator.getChartUrl());
        }
    }

}
