/**
 * Bean based StopWatches.
 *
 * In contract to the other packages of perf4j which rely on the individual logging frameworks for their configuration,
 * the bean based StopWatches ({@link BeanStopWatch} configured either by plain Java Code ({@link org.perf4j.beans.StopWatchFactory}
 * or {@link StopWatchConfigBuilder} for a fluent interface) or via a DI (dependency injection) framework like Spring or Guice.
 *
 * The logging framework used by the bean based StopWatches is commons loggings. It is a mandatory dependency as some of the classes
 * use it explicitly and the out-of-the-box {@link org.perf4j.beans.sinks.LoggingTimingEventSink} and {@link org.perf4j.beans.sinks.statistics.LoggingTimingStatisticsSink}
 * use commons logging as well. You could, of course, create your logging framework specific subclasses of these sinks,
 * but the commons-logging dependency remain mandatory.
 *
 * TODO blurb about commons-logging config like spring
 * http://blog.springsource.com/2009/12/04/logging-dependencies-in-spring/
 */
package org.perf4j.beans;