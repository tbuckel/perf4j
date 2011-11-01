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

import org.junit.After;
import org.junit.Test;
import org.mockito.Matchers;
import org.perf4j.GroupedTimingStatistics;
import org.perf4j.StopWatch;
import org.perf4j.beans.sinks.AsyncCoalescingStatisticsSink;
import org.perf4j.beans.sinks.TimingStatisticsSink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for StopWatchConfigBuilder
 *
 * @author Thomas Buckel
 */
public class StopWatchConfigBuilderTest {

    @After
    public void tearDown() {
        StopWatchFactory.shutdown();
    }

    @Test
    public void testWithNoSink() {
        // TODO Does this make sense?
        new StopWatchConfigBuilder().startUp();
        assertTrue(StopWatchFactory.isRunning());
    }

    @Test
    public void testWithOneTimingEventSink() {
        TimingEventSink timingEventSink = mock(TimingEventSink.class);
        new StopWatchConfigBuilder()
                .addSink(timingEventSink)
                .startUp();
        assertTrue(StopWatchFactory.isRunning());
        verify(timingEventSink).start();

        StopWatch stopWatch = StopWatchFactory.get();
        stopWatch.start();
        stopWatch.stop();
        verify(timingEventSink).onTimingEvent(stopWatch, null);

        StopWatchFactory.shutdown();
        verify(timingEventSink).stop();
    }

    @Test
    public void testWithMultipleTimingEventSinks() {
        TimingEventSink timingEventSink1 = mock(TimingEventSink.class);
        TimingEventSink timingEventSink2 = mock(TimingEventSink.class);
        new StopWatchConfigBuilder()
                .addSink(timingEventSink1)
                .addSink(timingEventSink2)
                .startUp();
        assertTrue(StopWatchFactory.isRunning());
        verify(timingEventSink1).start();
        verify(timingEventSink2).start();

        StopWatch stopWatch = StopWatchFactory.get();
        stopWatch.start();
        stopWatch.stop();
        verify(timingEventSink1).onTimingEvent(stopWatch, null);
        verify(timingEventSink2).onTimingEvent(stopWatch, null);

        StopWatchFactory.shutdown();
        verify(timingEventSink1).stop();
        verify(timingEventSink2).stop();
    }

    @Test
    public void testWithAsyncCoalescingStatisticsSink() {
        TimingStatisticsSink timingStatisticsSink = mock(TimingStatisticsSink.class);
        new StopWatchConfigBuilder()
                .addStatisticsSink()
                    .timeSlice(1234L)
                    .queueSize(2)
                    .addStasticsSink(timingStatisticsSink)
                    .endStatisticsSink()
                .startUp();
        assertTrue(StopWatchFactory.isRunning());
        verify(timingStatisticsSink).start();

        TimingEventSinkManager rootSink = (TimingEventSinkManager) StopWatchFactory.getRootSink();
        AsyncCoalescingStatisticsSink statisticsSink = (AsyncCoalescingStatisticsSink) rootSink.getSinks().get(0);

        assertEquals(1234L, statisticsSink.getTimeSlice());
        assertEquals(2, statisticsSink.getQueueSize());

        StopWatch stopWatch = StopWatchFactory.get();
        stopWatch.start();
        stopWatch.stop();

        StopWatchFactory.shutdown();
        verify(timingStatisticsSink).stop();
        verify(timingStatisticsSink).handle(Matchers.<GroupedTimingStatistics>any());
    }

}
