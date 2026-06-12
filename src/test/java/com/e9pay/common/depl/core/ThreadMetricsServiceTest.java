package com.e9pay.common.depl.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

public class ThreadMetricsServiceTest {

    private final ThreadMetricsService threadMetricsService = new ThreadMetricsService();

    @Test
    public void getThreadMetricsReturnsCurrentPeakAndState() {
        Map<String, Object> metrics = threadMetricsService.getThreadMetrics();

        assertNotNull(metrics.get("current"));
        assertNotNull(metrics.get("peak"));
        assertNotNull(metrics.get("state"));
        assertTrue(metrics.containsKey("daemon"));
        assertTrue(metrics.containsKey("totalStarted"));
        assertTrue(metrics.containsKey("deadlock"));
        assertTrue(metrics.containsKey("deadlockedThreadCount"));

        @SuppressWarnings("unchecked")
        Map<String, Integer> state = (Map<String, Integer>) metrics.get("state");
        assertTrue(state.containsKey("NEW"));
        assertTrue(state.containsKey("RUNNABLE"));
        assertTrue(state.containsKey("BLOCKED"));
        assertTrue(state.containsKey("WAITING"));
        assertTrue(state.containsKey("TIMED_WAITING"));
        assertTrue(state.containsKey("TERMINATED"));
    }
}
