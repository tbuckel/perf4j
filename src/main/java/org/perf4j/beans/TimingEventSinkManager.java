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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perf4j.StopWatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Delegates to StopWatchSinks for immediate processing.
 * Any async processing or cloning is responsibility of the Sinks.
 *
 * @author Thomas Buckel
 */
public class TimingEventSinkManager implements TimingEventSink {

    private final Log log = LogFactory.getLog(getClass());

    private List<TimingEventSink> sinks = new ArrayList<TimingEventSink>();
    private boolean running;

    public void setSinks(List<TimingEventSink> sinks) {
        this.sinks = sinks;
    }

    public void addSink(TimingEventSink sink) {
        sinks.add(sink);
    }

    public void removeSink(TimingEventSink sink) {
        if (sinks.remove(sink) && running) {
            sink.stop();
        }
    }

    public List<TimingEventSink> getSinks() {
        return Collections.unmodifiableList(sinks);
    }

    // @PostConstruct
    public void start() {
        log.debug("Starting TimingEventSinks");
        for (TimingEventSink sink : sinks) {
            try {
                sink.start();
            } catch (RuntimeException ex) {
                log.error("Error starting TimingEventSink " + sink.getClass() + "/" + sink.toString(), ex);
            }
        }
        running = true;
    }

    public void onTimingEvent(StopWatch stopWatch, Throwable e) {
        for (TimingEventSink sink : sinks) {
            try {
                sink.onTimingEvent(stopWatch, e);
            } catch (RuntimeException ex) {
                log.error("Error handling timingEvent " + stopWatch.toString(), ex);
            }
        }
    }

    // @PreDestroy
    public void stop() {
        log.debug("Stopping TimingEventSinks");
        for (TimingEventSink sink : sinks) {
            try {
                sink.stop();
            } catch (RuntimeException ex) {
                log.error("Error stopping TimingEventSink " + sink.getClass() + "/" + sink.toString(), ex);
            }
        }
        running = false;
    }

}
