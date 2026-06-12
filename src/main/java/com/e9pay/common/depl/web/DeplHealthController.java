package com.e9pay.common.depl.web;

import java.util.LinkedHashMap;
import java.util.Map;

import com.e9pay.common.depl.core.DbHealthService;
import com.e9pay.common.depl.core.SystemInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/v1/api/actuator")
public class DeplHealthController {

    private final DbHealthService dbHealthService;
    private final SystemInfoService systemInfoService;

    @Autowired
    public DeplHealthController(DbHealthService dbHealthService, SystemInfoService systemInfoService) {
        this.dbHealthService = dbHealthService;
        this.systemInfoService = systemInfoService;
    }

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> health() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("status", "UP");
        result.put("timestamp", System.currentTimeMillis());
        result.put("system", getSystemSummary());
        return result;
    }

    @RequestMapping(value = "/health/db", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> dbHealth() {
        return dbHealthService.getDbHealth();
    }

    private Map<String, Object> getSystemSummary() {
        Map<String, Object> systemInfo = systemInfoService.getSystemInfo();
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("javaVersion", systemInfo.get("javaVersion"));
        result.put("osName", systemInfo.get("osName"));
        result.put("availableProcessors", systemInfo.get("availableProcessors"));
        return result;
    }
}
