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

import org.perf4j.beans.sinks.AsyncCoalescingStatisticsSink;
import org.perf4j.beans.sinks.TimingStatisticsSink;

/**
 * Provides a fluent interface to configure and start the {@link StopWatchFactory}, usually used if no
 * DI framework like Spring or Guice is used.
 *
 * Example:
 * <pre>
 * {@code
 *      new StopWatchConfigBuilder()
 *          .addSink(new LoggingTimingEventSink())
 *          .addStatisticsSink()
 *            .timeSlice(30000L).createRollupStatistics(true)
 *            .addStatisticsSink(new LoggingTimingStatisticsSink())
 *            .endStatisticsSink()
 *          .startUp();
 * }
 * </pre>
 *
 * @author Thomas Buckel
 */
public class StopWatchConfigBuilder {

    private final TimingEventSinkManager rootSinkManager;
    private boolean hasAsyncStatisticsSink;

    public StopWatchConfigBuilder() {
        this.rootSinkManager = new TimingEventSinkManager();
    }

    public StopWatchConfigBuilder addSink(TimingEventSink sink) {
        this.rootSinkManager.addSink(sink);
        return this;
    }

    public void startUp() {
        StopWatchFactory.setRootSink(rootSinkManager);
        StopWatchFactory.startUp();
    }

    public AsyncStatisticsSinkBuilder addStatisticsSink() {
        if (hasAsyncStatisticsSink) {
            throw new IllegalStateException("addStatisticsSink() has already been invoked. It can only be invoked once for the builder.");
        }
        hasAsyncStatisticsSink = true;
        final AsyncCoalescingStatisticsSink asyncCoalescingStatisticsSink = new AsyncCoalescingStatisticsSink();
        this.rootSinkManager.addSink(asyncCoalescingStatisticsSink);

        return new AsyncStatisticsSinkBuilder() {
            public AsyncStatisticsSinkBuilder timeSlice(long timeSlice) {
                asyncCoalescingStatisticsSink.setTimeSlice(timeSlice);
                return this;
            }

            public AsyncStatisticsSinkBuilder createRollupStatistics(boolean createRollupStatistics) {
                asyncCoalescingStatisticsSink.setCreateRollupStatistics(createRollupStatistics);
                return this;
            }

            public AsyncStatisticsSinkBuilder queueSize(int queueSize) {
                asyncCoalescingStatisticsSink.setQueueSize(queueSize);
                return this;
            }

            public AsyncStatisticsSinkBuilder addStasticsSink(TimingStatisticsSink statisticsSink) {
                asyncCoalescingStatisticsSink.addStatisticsSink(statisticsSink);
                return this;
            }

            public StopWatchConfigBuilder endStatisticsSink() {
                return StopWatchConfigBuilder.this;
            }
        };
    }

    public interface AsyncStatisticsSinkBuilder {

        AsyncStatisticsSinkBuilder timeSlice(long timeSlice);

        AsyncStatisticsSinkBuilder createRollupStatistics(boolean createRollupStatistics);

        AsyncStatisticsSinkBuilder queueSize(int queueSize);

        AsyncStatisticsSinkBuilder addStasticsSink(TimingStatisticsSink statisticsSink);

        StopWatchConfigBuilder endStatisticsSink();

    }

}
