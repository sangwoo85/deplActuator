package com.e9pay.common.depl.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

public class MemoryMetricsServiceTest {

    private final MemoryMetricsService memoryMetricsService = new MemoryMetricsService();

    @Test
    public void getMemoryMetricsReturnsHeapNonHeapAndRuntime() {
        Map<String, Object> metrics = memoryMetricsService.getMemoryMetrics();

        assertNotNull(metrics.get("heap"));
        assertNotNull(metrics.get("nonHeap"));
        assertNotNull(metrics.get("runtime"));

        assertMemoryMap(metrics.get("heap"));
        assertMemoryMap(metrics.get("nonHeap"));
        assertMemoryMap(metrics.get("runtime"));
    }

    @SuppressWarnings("unchecked")
    private void assertMemoryMap(Object value) {
        Map<String, Long> memory = (Map<String, Long>) value;
        assertTrue(memory.containsKey("init") || memory.containsKey("total"));
        assertTrue(memory.containsKey("used"));
        assertTrue(memory.containsKey("max"));
    }
}
