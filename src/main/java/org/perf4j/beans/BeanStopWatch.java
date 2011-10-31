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

import org.perf4j.LoggingStopWatch;

/**
 * A LoggingStopWatch publishing its log calls to a {@link TimingEventSink}.
 *
 * Usually this type of StopWatch not instantiated directly, but created by a {@link StopWatchFactory}.
 *
 * @author Thomas Buckel
 */
public class BeanStopWatch extends LoggingStopWatch {

    private final TimingEventSink sink;

    public BeanStopWatch(TimingEventSink sink) {
        this.sink = sink;
    }

    public BeanStopWatch(TimingEventSink sink, String tag) {
        super(tag);
        this.sink = sink;
    }

    public BeanStopWatch(TimingEventSink sink, String tag, String message) {
        super(tag, message);
        this.sink = sink;
    }

    public BeanStopWatch(TimingEventSink sink, long startTime, long elapsedTime, String tag, String message) {
        super(startTime, elapsedTime, tag, message);
        this.sink = sink;
    }

    @Override
    protected final void log(String stopWatchAsString, Throwable exception) {
        sink.onTimingEvent(this, exception);
    }

}
