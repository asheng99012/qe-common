package com.dankegongyu.app.common.xxl;

import ch.qos.logback.core.OutputStreamAppender;
import com.xxl.job.core.log.XxlJobFileAppender;
import com.xxl.job.core.thread.JobThread;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public class XxlLogAppender<E> extends OutputStreamAppender<E> {
    public static ThreadLocal<Boolean> LOCAL = new ThreadLocal<>();

    public static void startXxlJob() {
        LOCAL.set(true);
    }

    public static void stopXxlJob() {
        LOCAL.set(false);
    }

    public static boolean isXxlRunning() {
        return LOCAL.get() != null && LOCAL.get();
    }

    @Override
    public void start() {
        setOutputStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }

            @Override
            public void write(byte b[], int off, int len) throws IOException {
                if (isXxlRunning()) {
                    String log = new String(b);
                    String logFileName = XxlJobFileAppender.contextHolder.get();
                    if (logFileName != null && logFileName.trim().length() > 0) {
                        XxlJobFileAppender.appendLog(logFileName, log);
                    }
                }
            }

        });
        super.start();
    }
}
