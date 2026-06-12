package com.e9pay.common.depl.core;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.LinkedHashMap;
import java.util.Map;

import com.e9pay.common.depl.web.ActiveRequestInterceptor;
import org.springframework.stereotype.Service;

@Service
public class ThreadMetricsService {

    public Map<String, Object> getThreadMetrics() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("current", threadMXBean.getThreadCount());
        result.put("peak", threadMXBean.getPeakThreadCount());
        result.put("daemon", threadMXBean.getDaemonThreadCount());
        result.put("totalStarted", threadMXBean.getTotalStartedThreadCount());
        result.put("activeHttpRequestCount", ActiveRequestInterceptor.getActiveRequestCount());
        result.put("totalHttpRequestCount", ActiveRequestInterceptor.getTotalRequestCount());
        result.put("maxActiveHttpRequestCount", ActiveRequestInterceptor.getMaxActiveRequestCount());
        result.put("state", getThreadStateCounts(threadMXBean));

        long[] deadlockedThreadIds = findDeadlockedThreads(threadMXBean);
        int deadlockedThreadCount = deadlockedThreadIds == null ? 0 : deadlockedThreadIds.length;
        result.put("deadlock", deadlockedThreadCount > 0);
        result.put("deadlockedThreadCount", deadlockedThreadCount);

        return result;
    }

    private Map<String, Integer> getThreadStateCounts(ThreadMXBean threadMXBean) {
        Map<String, Integer> stateCounts = createEmptyStateCounts();
        long[] threadIds = threadMXBean.getAllThreadIds();
        ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadIds);

        if (threadInfos == null) {
            return stateCounts;
        }

        for (ThreadInfo threadInfo : threadInfos) {
            if (threadInfo == null || threadInfo.getThreadState() == null) {
                continue;
            }
            String stateName = threadInfo.getThreadState().name();
            Integer currentCount = stateCounts.get(stateName);
            stateCounts.put(stateName, currentCount == null ? 1 : currentCount + 1);
        }

        return stateCounts;
    }

    private Map<String, Integer> createEmptyStateCounts() {
        Map<String, Integer> stateCounts = new LinkedHashMap<String, Integer>();
        for (Thread.State state : Thread.State.values()) {
            stateCounts.put(state.name(), 0);
        }
        return stateCounts;
    }

    private long[] findDeadlockedThreads(ThreadMXBean threadMXBean) {
        long[] deadlockedThreadIds = threadMXBean.findDeadlockedThreads();
        if (deadlockedThreadIds != null) {
            return deadlockedThreadIds;
        }
        return threadMXBean.findMonitorDeadlockedThreads();
    }
}
