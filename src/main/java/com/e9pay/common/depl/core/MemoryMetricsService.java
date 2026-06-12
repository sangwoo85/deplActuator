package com.e9pay.common.depl.core;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class MemoryMetricsService {

    public Map<String, Object> getMemoryMetrics() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        Runtime runtime = Runtime.getRuntime();

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("heap", toMemoryUsageMap(memoryMXBean.getHeapMemoryUsage()));
        result.put("nonHeap", toMemoryUsageMap(memoryMXBean.getNonHeapMemoryUsage()));
        result.put("runtime", toRuntimeMemoryMap(runtime));
        return result;
    }

    private Map<String, Long> toMemoryUsageMap(MemoryUsage memoryUsage) {
        Map<String, Long> result = new LinkedHashMap<String, Long>();
        result.put("init", memoryUsage.getInit());
        result.put("used", memoryUsage.getUsed());
        result.put("committed", memoryUsage.getCommitted());
        result.put("max", memoryUsage.getMax());
        return result;
    }

    private Map<String, Long> toRuntimeMemoryMap(Runtime runtime) {
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();

        Map<String, Long> result = new LinkedHashMap<String, Long>();
        result.put("max", runtime.maxMemory());
        result.put("total", total);
        result.put("free", free);
        result.put("used", total - free);
        return result;
    }
}
