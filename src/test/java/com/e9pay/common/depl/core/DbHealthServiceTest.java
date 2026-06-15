package com.e9pay.common.depl.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;

public class DbHealthServiceTest {

    @Test
    public void usesConfiguredHealthDataSourceAliasWhenMultipleDataSourcesExist() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.getBeanFactory().registerSingleton("projectMainDataSource", new FailingDataSource("selected"));
        context.getBeanFactory().registerSingleton("batchDataSource", new FailingDataSource("batch"));
        context.getBeanFactory().registerAlias(
                "projectMainDataSource", DbHealthService.HEALTH_DATASOURCE_BEAN_NAME);
        context.refresh();

        try {
            DbHealthService service = new DbHealthService();
            service.setApplicationContext(context);

            Map<String, Object> result = service.getDbHealth();

            assertEquals("DOWN", result.get("status"));
            assertEquals("DOWN", result.get("db"));
            assertEquals(DbHealthService.HEALTH_DATASOURCE_BEAN_NAME, result.get("dataSource"));
            assertTrue(String.valueOf(result.get("message")).contains("selected"));
        } finally {
            context.close();
        }
    }

    @Test
    public void usesSingleDataSourceWhenOnlyOneDataSourceExists() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.getBeanFactory().registerSingleton("mainDataSource", new FailingDataSource("single"));
        context.refresh();

        try {
            DbHealthService service = new DbHealthService();
            service.setApplicationContext(context);

            Map<String, Object> result = service.getDbHealth();

            assertEquals("DOWN", result.get("status"));
            assertEquals("DOWN", result.get("db"));
            assertEquals("mainDataSource", result.get("dataSource"));
            assertTrue(String.valueOf(result.get("message")).contains("single"));
        } finally {
            context.close();
        }
    }

    @Test
    public void returnsNotSelectedWhenMultipleDataSourcesExistWithoutAlias() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.getBeanFactory().registerSingleton("mainDataSource", new FailingDataSource("main"));
        context.getBeanFactory().registerSingleton("batchDataSource", new FailingDataSource("batch"));
        context.refresh();

        try {
            DbHealthService service = new DbHealthService();
            service.setApplicationContext(context);

            Map<String, Object> result = service.getDbHealth();

            assertEquals("UNKNOWN", result.get("status"));
            assertEquals("DATASOURCE_NOT_SELECTED", result.get("db"));
            assertTrue(String.valueOf(result.get("message")).contains("deplHealthDataSource"));
            assertTrue(String.valueOf(result.get("message")).contains("mainDataSource"));
            assertTrue(String.valueOf(result.get("message")).contains("batchDataSource"));
        } finally {
            context.close();
        }
    }

    @Test
    public void returnsNoDataSourceWhenNoDataSourceExists() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();

        try {
            DbHealthService service = new DbHealthService();
            service.setApplicationContext(context);

            Map<String, Object> result = service.getDbHealth();

            assertEquals("UNKNOWN", result.get("status"));
            assertEquals("NO_DATASOURCE", result.get("db"));
        } finally {
            context.close();
        }
    }

    private static class FailingDataSource implements DataSource {

        private final String message;

        private FailingDataSource(String message) {
            this.message = message;
        }

        @Override
        public Connection getConnection() throws SQLException {
            throw new SQLException(message);
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            throw new SQLException(message);
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {
        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            return 0;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new SQLException("Not a wrapper");
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }
}
