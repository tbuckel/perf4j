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
 * Like a LogFactory of the various logging frameworks, the {@code StopWatchFactory} is the key
 * user API to obtain StopWatches when using the bean based API.
 *
 * @author Thomas Buckel
 */
public final class StopWatchFactory {

    private static TimingEventSink rootSink = new StopWatchSinkManager();

    private StopWatchFactory() {
    }

    public static LoggingStopWatch get() {
        return new BeanStopWatch(rootSink);
    }

    public static LoggingStopWatch get(String tag) {
        return new BeanStopWatch(rootSink, tag);
    }

    public static LoggingStopWatch get(String tag, String message) {
        return new BeanStopWatch(rootSink, tag, message);
    }

    public static LoggingStopWatch get(long startTime, long elapsedTime, String tag, String message) {
        return new BeanStopWatch(rootSink, startTime, elapsedTime, tag, message);
    }

    public static void setRootSink(TimingEventSink rootSink) {
        if (rootSink == null) {
            throw new IllegalArgumentException("rootSink must not be null");
        }
        StopWatchFactory.rootSink = rootSink;
    }

    public static TimingEventSink getRootSink() {
        return StopWatchFactory.rootSink;
    }

    public static void startUp() {
        rootSink.start();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                rootSink.stop();
            }
        }));
    }

    /**
     * Convenience method to add a {@link TimingEventSink} to the rootSink.
     * Note: This only works if the {@code rootSink} is a {@link StopWatchSinkManager} which is usually the case.
     *
     * @param timingEventSink  Sink to add.
     */
    public static void addTimingEventSink(TimingEventSink timingEventSink) {
        if (rootSink instanceof StopWatchSinkManager) {
            ((StopWatchSinkManager) rootSink).addSink(timingEventSink);
        } else {
            throw new IllegalArgumentException("rootSink is not a StopWatchSinkManager");
        }
    }

}
