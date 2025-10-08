package com.type_it_backend.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SchedulerProvider {
    public static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(8);
}
