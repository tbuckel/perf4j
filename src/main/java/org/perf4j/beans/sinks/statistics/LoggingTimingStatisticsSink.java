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
package org.perf4j.beans.sinks.statistics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perf4j.GroupedTimingStatistics;
import org.perf4j.beans.sinks.TimingStatisticsSink;

/**
 * Logs timing statistics to a Java logger.
 *
 * @author Thomas Buckel
 */
public class LoggingTimingStatisticsSink implements TimingStatisticsSink {

    public static final String DEFAULT_LOGGER_NAME = "org.perf4j.Statistics";

    private Log log;

    public LoggingTimingStatisticsSink() {
        this(DEFAULT_LOGGER_NAME);
    }

    public LoggingTimingStatisticsSink(String loggerName) {
        this(LogFactory.getLog(loggerName));
    }

    public LoggingTimingStatisticsSink(Log log) {
        this.log = log;
    }

    public void setLoggerName(String loggerName) {
        this.log = LogFactory.getLog(loggerName);
    }

    public void start() {
    }

    public void stop() {
    }

    public void handle(GroupedTimingStatistics statistics) {
        log.info(statistics.toString());
    }

}
