package com.e9pay.common.depl.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

public class SystemInfoServiceTest {

    private final SystemInfoService systemInfoService = new SystemInfoService();

    @Test
    public void getSystemInfoReturnsJavaVersionAndOsName() {
        Map<String, Object> systemInfo = systemInfoService.getSystemInfo();

        assertNotNull(systemInfo.get("javaVersion"));
        assertNotNull(systemInfo.get("osName"));
        assertTrue(systemInfo.containsKey("javaVendor"));
        assertTrue(systemInfo.containsKey("osVersion"));
        assertTrue(systemInfo.containsKey("timestamp"));
    }
}
