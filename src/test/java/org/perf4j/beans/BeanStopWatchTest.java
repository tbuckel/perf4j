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

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Thomas Buckel
 */
public class BeanStopWatchTest {

    private TimingEventSink timingEventSink;

    @Before
    public void setUp() {
        timingEventSink = mock(TimingEventSinkManager.class);
    }

    @Test
    public void testGetStopWatch() {
        BeanStopWatch stopWatch = new BeanStopWatch(timingEventSink);
        stopWatch.start();
        stopWatch.stop();
        verify(timingEventSink).onTimingEvent(stopWatch, null);
    }

    @Test
    public void testGetStopWatchWithTag() {
        BeanStopWatch stopWatch = new BeanStopWatch(timingEventSink, "tag");
        stopWatch.start();
        stopWatch.stop();
        verify(timingEventSink).onTimingEvent(stopWatch, null);
    }

    @Test
    public void testGetStopWatchWithTagAndMessage() {
        BeanStopWatch stopWatch = new BeanStopWatch(timingEventSink, "tag", "message");
        stopWatch.start();
        stopWatch.stop();
        verify(timingEventSink).onTimingEvent(stopWatch, null);
    }

    @Test
    public void testGetStopWatchWithTimes() {
        BeanStopWatch stopWatch = new BeanStopWatch(timingEventSink, 1l, 1l, "tag", "message");
        stopWatch.start();
        stopWatch.stop();
        verify(timingEventSink).onTimingEvent(stopWatch, null);
    }

}
