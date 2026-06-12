package com.e9pay.common.depl.web;

import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class ActiveRequestInterceptor extends HandlerInterceptorAdapter {

    private static final AtomicLong ACTIVE_REQUEST_COUNT = new AtomicLong(0);
    private static final AtomicLong TOTAL_REQUEST_COUNT = new AtomicLong(0);
    private static final AtomicLong MAX_ACTIVE_REQUEST_COUNT = new AtomicLong(0);

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        long active = ACTIVE_REQUEST_COUNT.incrementAndGet();
        TOTAL_REQUEST_COUNT.incrementAndGet();
        updateMax(active);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        long active = ACTIVE_REQUEST_COUNT.decrementAndGet();
        if (active < 0) {
            resetActiveCountIfNegative();
        }
    }

    public static long getActiveRequestCount() {
        return ACTIVE_REQUEST_COUNT.get();
    }

    public static long getTotalRequestCount() {
        return TOTAL_REQUEST_COUNT.get();
    }

    public static long getMaxActiveRequestCount() {
        return MAX_ACTIVE_REQUEST_COUNT.get();
    }

    static void resetForTest() {
        ACTIVE_REQUEST_COUNT.set(0);
        TOTAL_REQUEST_COUNT.set(0);
        MAX_ACTIVE_REQUEST_COUNT.set(0);
    }

    private static void updateMax(long active) {
        long currentMax;
        do {
            currentMax = MAX_ACTIVE_REQUEST_COUNT.get();
            if (active <= currentMax) {
                return;
            }
        } while (!MAX_ACTIVE_REQUEST_COUNT.compareAndSet(currentMax, active));
    }

    private static void resetActiveCountIfNegative() {
        long active;
        do {
            active = ACTIVE_REQUEST_COUNT.get();
            if (active >= 0) {
                return;
            }
        } while (!ACTIVE_REQUEST_COUNT.compareAndSet(active, 0));
    }
}
