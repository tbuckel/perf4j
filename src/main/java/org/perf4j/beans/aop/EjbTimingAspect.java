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
package org.perf4j.beans.aop;

import org.perf4j.LoggingStopWatch;
import org.perf4j.aop.AbstractEjbTimingAspect;
import org.perf4j.beans.StopWatchFactory;

/**
 * This TimingAspect implementation uses Log4j to persist StopWatch log messages.
 * To use this interceptor in your code, you should add this class name to the {@link javax.interceptor.Interceptors}
 * annotation on the EJB to be profiled.
 *
 * @author Thomas Buckel
 */
public class EjbTimingAspect extends AbstractEjbTimingAspect {

    protected LoggingStopWatch newStopWatch(String loggerName, String levelName) {
        return StopWatchFactory.get();
    }

}
