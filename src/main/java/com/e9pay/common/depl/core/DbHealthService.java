package com.e9pay.common.depl.core;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DbHealthService {

    private DataSource dataSource;

    @Autowired(required = false)
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, Object> getDbHealth() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("timestamp", System.currentTimeMillis());

        if (dataSource == null) {
            result.put("status", "UNKNOWN");
            result.put("db", "NO_DATASOURCE");
            return result;
        }

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            result.put("status", "UP");
            result.put("db", "UP");
        } catch (Exception ex) {
            result.put("status", "DOWN");
            result.put("db", "DOWN");
            result.put("message", ex.getMessage());
        }

        return result;
    }
}
