package com.e9pay.common.depl.core;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class SystemInfoService {

    public Map<String, Object> getSystemInfo() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("javaVersion", System.getProperty("java.version"));
        result.put("javaVendor", System.getProperty("java.vendor"));
        result.put("osName", System.getProperty("os.name"));
        result.put("osVersion", System.getProperty("os.version"));
        result.put("osArch", System.getProperty("os.arch"));
        result.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        result.put("userTimezone", System.getProperty("user.timezone"));
        result.put("fileEncoding", System.getProperty("file.encoding"));
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}
