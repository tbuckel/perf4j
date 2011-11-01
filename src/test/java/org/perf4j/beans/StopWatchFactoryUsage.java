package org.perf4j.beans;

import org.perf4j.LoggingStopWatch;
import org.perf4j.beans.sinks.AsyncCoalescingStatisticsSink;
import org.perf4j.beans.sinks.LoggingTimingEventSink;
import org.perf4j.beans.sinks.statistics.GraphingStatisticsSink;
import org.perf4j.beans.sinks.statistics.JmxAttributeStatisticsSink;
import org.perf4j.beans.sinks.statistics.LoggingTimingStatisticsSink;
import org.perf4j.helpers.StatsValueRetriever;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 6/10/11
 * Time: 9:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class StopWatchFactoryUsage {

    public static void main(String[] args) throws InterruptedException {
        // Configure
        LoggingTimingEventSink individualStopWatchLogSink = new LoggingTimingEventSink();

        // Builder?
        AsyncCoalescingStatisticsSink statisticsSink = new AsyncCoalescingStatisticsSink(10000L, false,
                new LoggingTimingStatisticsSink(),
                new JmxAttributeStatisticsSink(true),
                new GraphingStatisticsSink(StatsValueRetriever.TPS, "test"));

        StopWatchFactory.addTimingEventSink(individualStopWatchLogSink);
        StopWatchFactory.addTimingEventSink(statisticsSink);
        StopWatchFactory.startUp();

        while (true) {
            LoggingStopWatch stopWatch = StopWatchFactory.get("test");
            stopWatch.start();
            Thread.sleep((long)(Math.random() * 1000));
            stopWatch.stop();
        }
    }

}
