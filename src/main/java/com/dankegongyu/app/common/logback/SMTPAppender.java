package com.dankegongyu.app.common.logback;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import com.dankegongyu.app.common.Current;
import com.dankegongyu.app.common.Mailer;
import com.google.common.base.Charsets;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by ashen on 2017-3-1.
 */
public class SMTPAppender<E> extends OutputStreamAppender<E> {
    private static ThreadLocal<ILoggingEvent> threadLocalEvent = new ThreadLocal<ILoggingEvent>();
    Mailer mailer;
    String projectName = "";

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    public void start() {
        setOutputStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                System.out.write(b);
            }

            @Override
            public void write(byte b[], int off, int len) throws IOException {
                ILoggingEvent event = threadLocalEvent.get();
                if (event.getLevel().levelInt == Level.ERROR.levelInt) {
                    if (event != null && event.getLoggerName().indexOf("Mail") > 0) return;
                    String log = new String(b, Charsets.UTF_8);
                    int i = log.indexOf("\n");
                    String subject = log.substring(0, i);
                    String msg = subject + "\n<br />" + Current.getRequestOtherInfo() + log.substring(i);
                    System.out.println(msg);
                    Mailer mailer = Mailer.getMailer();
                    if (mailer != null)
                        mailer.sendMail(subject, msg);
                }
            }

        });
        super.start();
    }


    protected void writeOut(E event) throws IOException {
        if (event instanceof ILoggingEvent) {
            threadLocalEvent.set((ILoggingEvent) event);
        } else {
            threadLocalEvent.set(null);
        }
        this.encoder.encode(event);
    }
}