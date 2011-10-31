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
import org.perf4j.StopWatch;
import org.perf4j.beans.TimingEventSink;

/**
 * Logs TimingEvents (individual StopWatches) to a Java.Commons.Logging logger.
 *
 * @author Thomas Buckel
 */
public class LoggingTimingEventSink implements TimingEventSink {

    public static final String DEFAULT_LOGGER_NAME = "org.perf4j.StopWatch";

    // TODO Add log level?

    private Log log;

    public LoggingTimingEventSink() {
        this(DEFAULT_LOGGER_NAME);
    }

    public LoggingTimingEventSink(String loggerName) {
        this(LogFactory.getLog(loggerName));
    }

    public LoggingTimingEventSink(Log log) {
        this.log = log;
    }

    public void setLoggerName(String loggerName) {
        this.log = LogFactory.getLog(loggerName);
    }

    public void start() {
    }

    public void stop() {
    }

    public void onTimingEvent(StopWatch stopWatch, Throwable e) {
        log.info(stopWatch.toString(), e);
    }

}
