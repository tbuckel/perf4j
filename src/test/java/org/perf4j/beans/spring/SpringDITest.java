package org.perf4j.beans.spring;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.perf4j.LoggingStopWatch;
import org.perf4j.beans.StopWatchFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 19/10/11
 * Time: 7:52 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="testContext.xml")
@Ignore
public class SpringDITest {

    public void test1() throws Exception {
        LoggingStopWatch stopWatch = StopWatchFactory.get("test");
        stopWatch.start();
        Thread.sleep(995);
        stopWatch.stop();
    }
}
