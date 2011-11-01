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
import org.junit.Before;
import org.junit.Test;
import org.perf4j.StopWatch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for StopWatchFactory.
 *
 * @author Thomas Buckel
 */
public class StopWatchFactoryTest {

    private TimingEventSinkManager sinkManager;

    @Before
    public void setUp() {
        sinkManager = mock(TimingEventSinkManager.class);
        StopWatchFactory.setRootSink(sinkManager);
    }

    @After
    public void tearDown() {
        // As StopWatchFactory is static, this ensures that we have a plain RootSinkManager again
        StopWatchFactory.setRootSink(new TimingEventSinkManager());
        StopWatchFactory.shutdown();
    }

    @Test
    public void testRootSinkManagerLifeCycle() {
        StopWatchFactory.startUp();
        verify(sinkManager).start();
        assertTrue(StopWatchFactory.isRunning());

        StopWatchFactory.shutdown();
        verify(sinkManager).stop();
        assertFalse(StopWatchFactory.isRunning());
    }

    @Test
    public void testAddTimingEventSink() {
        TimingEventSink testSink = mock(TimingEventSink.class);
        StopWatchFactory.addTimingEventSink(testSink);
        verify(sinkManager).addSink(testSink);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTimingEventSinkNoTimingEventSinkManager() {
        TimingEventSink testSink = mock(TimingEventSink.class);
        StopWatchFactory.setRootSink(testSink);
        StopWatchFactory.addTimingEventSink(mock(TimingEventSink.class));
    }

    @Test
    public void testSetRootSink() {
        TimingEventSink testSink = mock(TimingEventSink.class);
        StopWatchFactory.setRootSink(testSink);
        assertSame(testSink, StopWatchFactory.getRootSink());
    }

    @Test
    public void testGetStopWatch() {
        StopWatch stopWatch = StopWatchFactory.get();
        stopWatch.start();
        stopWatch.stop();
        verify(sinkManager).onTimingEvent(stopWatch, null);
    }

    @Test
    public void testGetStopWatchWithTag() {
        StopWatch stopWatch = StopWatchFactory.get("tag");
        stopWatch.start();
        stopWatch.stop();
        verify(sinkManager).onTimingEvent(stopWatch, null);
    }

    @Test
    public void testGetStopWatchWithTagAndMessage() {
        StopWatch stopWatch = StopWatchFactory.get("tag", "message");
        stopWatch.start();
        stopWatch.stop();
        verify(sinkManager).onTimingEvent(stopWatch, null);
    }

    @Test
    public void testGetStopWatchWithTimes() {
        StopWatch stopWatch = StopWatchFactory.get(1l, 1l, "tag", "message");
        stopWatch.start();
        stopWatch.stop();
        verify(sinkManager).onTimingEvent(stopWatch, null);
    }

}
