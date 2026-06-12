package com.e9pay.common.depl.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ActiveRequestInterceptorTest {

    private final ActiveRequestInterceptor interceptor = new ActiveRequestInterceptor();

    @Before
    public void setUp() {
        ActiveRequestInterceptor.resetForTest();
    }

    @After
    public void tearDown() {
        ActiveRequestInterceptor.resetForTest();
    }

    @Test
    public void preHandleIncreasesActiveHttpRequestCount() throws Exception {
        interceptor.preHandle(null, null, null);

        assertEquals(1L, ActiveRequestInterceptor.getActiveRequestCount());
        assertEquals(1L, ActiveRequestInterceptor.getTotalRequestCount());
    }

    @Test
    public void afterCompletionDecreasesActiveHttpRequestCount() throws Exception {
        interceptor.preHandle(null, null, null);
        interceptor.afterCompletion(null, null, null, null);

        assertEquals(0L, ActiveRequestInterceptor.getActiveRequestCount());
    }

    @Test
    public void afterCompletionDoesNotLeaveNegativeActiveHttpRequestCount() throws Exception {
        interceptor.afterCompletion(null, null, null, null);

        assertTrue(ActiveRequestInterceptor.getActiveRequestCount() >= 0L);
    }

    @Test
    public void multiplePreHandleCallsUpdateMaxActiveHttpRequestCount() throws Exception {
        interceptor.preHandle(null, null, null);
        interceptor.preHandle(null, null, null);
        interceptor.preHandle(null, null, null);

        assertEquals(3L, ActiveRequestInterceptor.getActiveRequestCount());
        assertEquals(3L, ActiveRequestInterceptor.getTotalRequestCount());
        assertEquals(3L, ActiveRequestInterceptor.getMaxActiveRequestCount());
    }
}
