package com.e9pay.common.depl.web;

import java.util.LinkedHashMap;
import java.util.Map;

import com.e9pay.common.depl.core.MemoryMetricsService;
import com.e9pay.common.depl.core.SystemInfoService;
import com.e9pay.common.depl.core.ThreadMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/v1/api/actuator/metrics")
public class DeplMetricsController {

    private final ThreadMetricsService threadMetricsService;
    private final MemoryMetricsService memoryMetricsService;
    private final SystemInfoService systemInfoService;

    @Autowired
    public DeplMetricsController(ThreadMetricsService threadMetricsService,
                                 MemoryMetricsService memoryMetricsService,
                                 SystemInfoService systemInfoService) {
        this.threadMetricsService = threadMetricsService;
        this.memoryMetricsService = memoryMetricsService;
        this.systemInfoService = systemInfoService;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> metrics() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("status", "UP");
        result.put("thread", threadMetricsService.getThreadMetrics());
        result.put("memory", memoryMetricsService.getMemoryMetrics());
        result.put("system", systemInfoService.getSystemInfo());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    @RequestMapping(value = "/thread", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> thread() {
        return threadMetricsService.getThreadMetrics();
    }

    @RequestMapping(value = "/memory", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> memory() {
        return memoryMetricsService.getMemoryMetrics();
    }

    @RequestMapping(value = "/system", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> system() {
        return systemInfoService.getSystemInfo();
    }
}
