package com.e9pay.common.depl.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DbHealthService implements ApplicationContextAware {

    public static final String HEALTH_DATASOURCE_BEAN_NAME = "deplHealthDataSource";

    private DataSource dataSource;
    private String dataSourceBeanName;
    private String dataSourceResolveMessage;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.dataSourceBeanName = dataSource == null ? null : "providedDataSource";
        this.dataSourceResolveMessage = null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        resolveDataSource(applicationContext);
    }

    public Map<String, Object> getDbHealth() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("timestamp", System.currentTimeMillis());

        if (dataSource == null) {
            result.put("status", "UNKNOWN");
            if (dataSourceResolveMessage == null) {
                result.put("db", "NO_DATASOURCE");
            } else {
                result.put("db", "DATASOURCE_NOT_SELECTED");
                result.put("message", dataSourceResolveMessage);
            }
            return result;
        }

        if (dataSourceBeanName != null) {
            result.put("dataSource", dataSourceBeanName);
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

    private void resolveDataSource(ApplicationContext applicationContext) {
        dataSource = null;
        dataSourceBeanName = null;
        dataSourceResolveMessage = null;

        if (applicationContext == null) {
            return;
        }

        if (applicationContext.containsBean(HEALTH_DATASOURCE_BEAN_NAME)) {
            resolveNamedDataSource(applicationContext);
            return;
        }

        resolveSingleDataSource(applicationContext);
    }

    private void resolveNamedDataSource(ApplicationContext applicationContext) {
        try {
            Object bean = applicationContext.getBean(HEALTH_DATASOURCE_BEAN_NAME);
            if (bean instanceof DataSource) {
                dataSource = (DataSource) bean;
                dataSourceBeanName = HEALTH_DATASOURCE_BEAN_NAME;
                return;
            }

            dataSourceResolveMessage = "Bean named '" + HEALTH_DATASOURCE_BEAN_NAME
                    + "' is not a javax.sql.DataSource.";
        } catch (BeansException ex) {
            dataSourceResolveMessage = "Bean named '" + HEALTH_DATASOURCE_BEAN_NAME
                    + "' could not be resolved as a DataSource: " + ex.getMessage();
        }
    }

    private void resolveSingleDataSource(ApplicationContext applicationContext) {
        Map<String, DataSource> dataSources;
        try {
            dataSources = BeanFactoryUtils.beansOfTypeIncludingAncestors(
                    applicationContext, DataSource.class, true, false);
        } catch (BeansException ex) {
            dataSourceResolveMessage = "DataSource beans could not be resolved: " + ex.getMessage();
            return;
        }

        if (dataSources.isEmpty()) {
            return;
        }

        if (dataSources.size() == 1) {
            Entry<String, DataSource> entry = dataSources.entrySet().iterator().next();
            dataSource = entry.getValue();
            dataSourceBeanName = entry.getKey();
            return;
        }

        List<String> beanNames = new ArrayList<String>(dataSources.keySet());
        Collections.sort(beanNames);
        dataSourceResolveMessage = "Multiple DataSource beans found " + beanNames
                + ". Define alias '" + HEALTH_DATASOURCE_BEAN_NAME
                + "' for the DataSource used by DB health checks.";
    }
}
