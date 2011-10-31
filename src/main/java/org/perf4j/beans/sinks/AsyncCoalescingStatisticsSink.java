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
package org.perf4j.beans.sinks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perf4j.GroupedTimingStatistics;
import org.perf4j.StopWatch;
import org.perf4j.beans.TimingEventSink;
import org.perf4j.helpers.GroupingStatisticsIterator;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * The {@link TimingEventSink} groups individual timing events from {@link org.perf4j.beans.BeanStopWatch}s over a
 * defined time slices and propagates this statistics to its configured {@link TimingStatisticsSink}s, which can,
 * for example, log these statistics, create graphs or expose them via JMX.
 *
 * Technical note:
 * As opposed to the various {@code AsyncCoalescingStatisticsAppenders} for logging frameworks, the bean based
 * version does not need a String conversion and parsing to the individual StopWatch timing events.
 * This class is a combination of {@link org.perf4j.log4j.AsyncCoalescingStatisticsAppender} and
 * {@link org.perf4j.helpers.GenericAsyncCoalescingStatisticsAppender}.
 *
 * @see org.perf4j.log4j.AsyncCoalescingStatisticsAppender
 * @see org.perf4j.helpers.GenericAsyncCoalescingStatisticsAppender
 *
 * @author Thomas Buckel
 */
public class AsyncCoalescingStatisticsSink implements TimingEventSink {

    private final Log log = LogFactory.getLog(getClass());

    private static final StopWatch FINAL_MESSAGE = new StopWatch();

    private Collection<TimingStatisticsSink> statisticsSinks = new ArrayList<TimingStatisticsSink>();

    /** TimeSlice option */
    private long timeSlice = 30000L;

    /** CreateRollupStatistics option */
    private boolean createRollupStatistics = false;

    /** The QueueSize option, used to set the capacity of the loggedStopWatches queue */
    private int queueSize = 1024;

    /**
     * StopWatch log messages are pushed onto this queue, which is initialized in start().
     */
    private BlockingQueue<StopWatch> loggedStopWatches = null;
    /**
     * This thread pumps logs from the loggedStopWatches queue. It is created in start().
     */
    private Thread drainingThread = null;
    /**
     * This int keeps track of the total number of messages that had to be discarded due to the queue being full.
     */
    private volatile int numDiscardedMessages = 0;

    public AsyncCoalescingStatisticsSink() {
        this(30000L);
    }

    public AsyncCoalescingStatisticsSink(long timeSlice) {
        this(timeSlice, false);
    }

    public AsyncCoalescingStatisticsSink(long timeSlice, boolean createRollupStatistics) {
        this(timeSlice, createRollupStatistics, Collections.<TimingStatisticsSink>emptyList());
    }

    public AsyncCoalescingStatisticsSink(long timeSlice, boolean createRollupStatistics, TimingStatisticsSink... statisticsSinks) {
        this(timeSlice, createRollupStatistics, Arrays.asList(statisticsSinks));
    }

    public AsyncCoalescingStatisticsSink(long timeSlice, boolean createRollupStatistics, Collection<TimingStatisticsSink> statisticsSinks) {
        this.statisticsSinks = new ArrayList<TimingStatisticsSink>(statisticsSinks);
        this.timeSlice = timeSlice;
        this.createRollupStatistics = createRollupStatistics;
    }

    public void setStatisticsSinks(List<TimingStatisticsSink> statisticsSinks) {
        if (isStarted()) {
            throw new IllegalArgumentException("Only allowed if not started.");
        }
        this.statisticsSinks = statisticsSinks;
    }

    public void addStatisticsSink(TimingStatisticsSink statisticsSink) {
        this.statisticsSinks.add(statisticsSink);
        if (isStarted()) {
            statisticsSink.start();
        }
    }

    public void removeStatisticsSink(TimingStatisticsSink statisticsSink) {
        if (this.statisticsSinks.remove(statisticsSink)) {
            if (isStarted()) {
                statisticsSink.stop();
            }
        }
    }

    private boolean isStarted() {
        return drainingThread != null;
    }

    // --- options ---

    /**
     * The <b>TimeSlice</b> option represents the length of time, in milliseconds, of the window in which appended
     * log events are coalesced to a single GroupedTimingStatistics and sent to the GroupedTimingStatisticsHandler.
     * Defaults to 30,000 milliseconds.
     *
     * @return the TimeSlice option.
     */
    public long getTimeSlice() {
        return timeSlice;
    }

    /**
     * Sets the value of the <b>TimeSlice</b> option.
     *
     * @param timeSlice The new TimeSlice option, in milliseconds.
     */
    public void setTimeSlice(long timeSlice) {
        this.timeSlice = timeSlice;
    }

    /**
     * The <b>CreateRollupStatistics</b> option is used to determine whether "rollup" statistics should be created.
     * If the tag name of a StopWatch in a log message contains periods, then the GroupedTimingStatistics will be
     * created as if each substring of the tag up to the period was also logged with a separate StopWatch instance.
     * For example, suppose a StopWatch was logged with a tag of "requests.specificReq.PASS". For grouping purposes
     * a StopWatch entry would be logged under each of the following tags:
     * <ul>
     * <li>requests
     * <li>requests.specificReq
     * <li>requests.specificReq.PASS
     * </ul>
     * This allows you to view statistics at both an individual and aggregated level. If there were other StopWatch
     * entries with a tag of "requests.specificReq.FAIL", then the data collected at the "requests.specificReq" level
     * would include BOTH PASS and FAIL events.
     *
     * @return The CreateRollupStatistics option.
     */
    public boolean isCreateRollupStatistics() {
        return createRollupStatistics;
    }

    /**
     * Sets the value of the <b>CreateRollupStatistics</b> option.
     *
     * @param createRollupStatistics The new CreateRollupStatistics option.
     */
    public void setCreateRollupStatistics(boolean createRollupStatistics) {
        this.createRollupStatistics = createRollupStatistics;
    }

    /**
     * The <b>QueueSize</b> option is used to control the size of the internal queue used by this appender to store
     * logged messages before they are sent to downstream appenders. Defaults to 1024. If set too small and the queue
     * fills up, then logged StopWatches will be discarded. The number of discarded messages can be accessed using the
     * {@link #getNumDiscardedMessages()} method.
     *
     * @return The QueueSize option.
     */
    public int getQueueSize() {
        return queueSize;
    }

    /**
     * Sets the value of the <b>QueueSize</b> option.
     *
     * @param queueSize The new QueueSize option.
     */
    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    // --- attributes ---
    /**
     * Returns the number of StopWatch messages that have been discarded due to the queue being full.
     *
     * @return The number of discarded messages.
     */
    public int getNumDiscardedMessages() {
        return numDiscardedMessages;
    }

    // --- main lifecycle methods ---

    public void start() {
        //start should only be called once, but just in case:
        if (drainingThread != null) {
            stopDrainingThread();
        }

        numDiscardedMessages = 0;
        loggedStopWatches = new ArrayBlockingQueue<StopWatch>(getQueueSize());

        drainingThread = new Thread(new Dispatcher(), "perf4j-async-stats-sink");
        drainingThread.setDaemon(true);
        drainingThread.start();

        for (TimingStatisticsSink statisticsSink : statisticsSinks) {
            statisticsSink.start();
        }
    }

    public void onTimingEvent(StopWatch stopWatch, Throwable e) {
        StopWatch clone = stopWatch.clone(); // clone as we're processing async and the stopwatch might be restarted
        if (!loggedStopWatches.offer(clone)) {
            ++numDiscardedMessages;
        }
    }

    /**
     * This method should be called on shutdown to flush any pending messages in the queue and create a final
     * GroupedTimingStatistics instance if necessary.
     */
    public void stop() {
        stopDrainingThread();
        for (TimingStatisticsSink statisticsSink : statisticsSinks) {
            statisticsSink.stop();
        }
    }

    // --- Helper Methods ---
    /**
     * Helper method stops the draining thread and waits for it to finish.
     */
    private void stopDrainingThread() {
        try {
            //pushing an empty string on the queue tells the draining thread that we're closing
            loggedStopWatches.put(FINAL_MESSAGE);
            //wait for the draining thread to finish
            drainingThread.join(10000L);
        } catch (Exception e) {
            log.error("Unexpected error stopping AsyncCoalescingStatisticsSink draining thread", e);
        }
        drainingThread = null;
    }

    // --- Support Classes ---
    /**
     * This Dispatcher Runnable uses a StopWatchesFromQueueIterator to pull StopWatch logging message off the
     * loggedStopWatches queue, which are grouped to create GroupedTimingStatistics by the GroupingStatisticsIterator.
     * The GroupedTimingStatisticsHandler is then called to deal with the created GroupedTimingStatistics.
     */
    private class Dispatcher implements Runnable {
        public void run() {
            GroupingStatisticsIterator statsIterator =
                    new GroupingStatisticsIterator(new StopWatchesFromQueueIterator(),
                                                   timeSlice,
                                                   createRollupStatistics);

            while (statsIterator.hasNext()) {
                GroupedTimingStatistics statistics = statsIterator.next();
                for (TimingStatisticsSink statisticsSink : statisticsSinks) {
                    try {
                        statisticsSink.handle(statistics);
                    } catch (Exception e) {
                        log.error("Error calling the TimingStatisticsSink: " + statisticsSink.toString(), e);
                    }
                }
            }
        }
    }

    /**
     * This helper class pulls StopWatch log messages off the loggedStopWatches queue and exposes them through the
     * Iterator interface.
     */
    private class StopWatchesFromQueueIterator implements Iterator<StopWatch> {
        /**
         * Messages are drained to this list in blocks.
         */
        private LinkedList<StopWatch> drainedStopWatches = new LinkedList<StopWatch>();
        /**
         * Keeps track of the NEXT stop watch we will return.
         */
        private StopWatch nextStopWatch;
        /**
         * State variable keeps track of whether we've already determined that the loggedStopWatches queue has been closed.
         */
        private boolean done;
        /**
         * State variable keeps track of whether we've finished waiting for a timeslice.
         * If true, hasNext will return true and next will return null.
         */
        private boolean timeSliceOver;

        public boolean hasNext() {
            if (nextStopWatch == null) {
                nextStopWatch = getNext(); //then try to get it
            }
            return timeSliceOver || nextStopWatch != null;
        }

        public StopWatch next() {
            if (timeSliceOver) {
                timeSliceOver = false;
                return null;
            } else if (nextStopWatch == null) {
                nextStopWatch = getNext(); //then try to get it, and barf if there is no more
                if (nextStopWatch == null) {
                    throw new NoSuchElementException();
                }
            }

            StopWatch retVal = nextStopWatch;
            nextStopWatch = null;
            return retVal;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private StopWatch getNext() {
            if (done) {
                //if we already found out we're done, short circuit so we won't block
                return null;
            }

            while (true) {
                if (drainedStopWatches.isEmpty()) {
                    loggedStopWatches.drainTo(drainedStopWatches, 64);

                    //drainTo is more efficient but it doesn't block, so if we're still empty call take() to block
                    if (drainedStopWatches.isEmpty()) {
                        //then wait for a message to show up
                        try {
                            StopWatch message = loggedStopWatches.poll(timeSlice, TimeUnit.MILLISECONDS);
                            if (message == null) {
                                // no new messages, but want to indicate to check the timeslice
                                timeSliceOver = true;
                                return null;
                            } else {
                                drainedStopWatches.add(message);
                            }
                        } catch (InterruptedException ie) {
                            //someone interrupted us, we're done
                            done = true;
                            return null;
                        }
                    }
                }

                while (!drainedStopWatches.isEmpty()) {
                    StopWatch stopWatch = drainedStopWatches.removeFirst();
                    if (stopWatch == FINAL_MESSAGE) {
                        //the empty message is pushed onto the queue by the enclosing class' close() method
                        //to indicate that we're done
                        done = true;
                        return null;
                    }

                    return stopWatch;
                }
            }
        }
    }


}
