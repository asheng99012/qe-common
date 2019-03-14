package com.dankegongyu.app.common;


import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;

/**
 * Created by ashen on 2017-2-8.
 */
public class Mailer extends JavaMailSenderImpl {
    private static final Logger logger = LoggerFactory.getLogger(Mailer.class);
    private String from;
    private String fromName;

    private String errorTo;

    private String domain;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
        initDomain();
    }

    public void setErrorTo(String errorTo) {
        this.errorTo = errorTo;
        initDomain();
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public void setFrom(String from) {
        this.from = from;
        initDomain();
    }

    public String dealAddr(String addr) {
        if (Strings.isNullOrEmpty(domain) || Strings.isNullOrEmpty(addr)) return addr;
        addr = addr.replace("dankegongyu.com", domain);
        addr = addr.replace("danke.com", domain);
        return addr;
    }

    public void initDomain() {
        from = dealAddr(from);
        errorTo = dealAddr(errorTo);
    }

    //    @Async
    public void sendMail(String subject, String content) {
        if (!Strings.isNullOrEmpty(errorTo))
            sendMail(subject, content, errorTo);
    }

    //    @Async
    public void sendMail(String subject, String content, String to) {
        sendMail(subject, content, to.split(","));
    }

    /**
     * @方法名: sendMail
     * @参数名：@param subject  邮件主题
     * @参数名：@param content 邮件主题内容
     * @参数名：@param to         收件人Email地址
     * @描述语: 发送邮件
     */
    public void sendMail(String subject, String content, String[] tos) {
        sendMail(subject, content, null, tos);
    }

    public void sendMail(String subject, String content, Map<String, InputStream> ins, String[] tos) {

        try {
            final Message message = getMessage();
            Address[] addresses = new Address[tos.length];
            for (int i = 0, l = tos.length; i < l; i++) {
                // 创建邮件的接收者地址，并设置到邮件消息中
                addresses[i] = new InternetAddress(dealAddr(tos[i]));
            }
            // Message.RecipientType.TO属性表示接收者的类型为TO
            message.setRecipients(Message.RecipientType.TO, addresses);
            // 设置邮件消息的主题
            message.setSubject(subject);
            // 设置邮件消息发送的时间
            message.setSentDate(new Date());
            // MiniMultipart类是一个容器类，包含MimeBodyPart类型的对象
            Multipart mainPart = new MimeMultipart();
            // 创建一个包含HTML内容的MimeBodyPart
            BodyPart html = new MimeBodyPart();
            // 设置HTML内容
            html.setContent(content.replaceAll("\\n", "<br />"), "text/html; charset=utf-8");
            mainPart.addBodyPart(html);
            // 将MiniMultipart对象设置为邮件内容
            if (ins != null) {
                ins.forEach((key, is) -> {
                    try {
                        MimeBodyPart part = new MimeBodyPart();
                        part.setDataHandler(new DataHandler(new ByteArrayDataSource(is, "application/octet-stream")));
                        part.setFileName(MimeUtility.encodeText(key));
                        mainPart.addBodyPart(part);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                });
            }

            message.setContent(mainPart);
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        // 发送邮件
                        Transport.send(message);
                        logger.info("邮件发送成功：" + message.getSubject());
                    } catch (MessagingException e) {
                        logger.warn("邮件发送失败：" + e.getMessage(), e);
                        e.printStackTrace();
                    }
                }
            };
            new Thread(runnable).start();
//            Transport.send(message);
//            logger.info("邮件发送成功：" + message.getSubject());
            // 发送邮件
//            Transport.send(message);
        } catch (MessagingException e) {
            logger.warn("邮件发送失败：" + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private Message getMessage() throws MessagingException {
        java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", this.getHost());
        if (this.getPort() == 465) {
            props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.smtp.socketFactory.fallback", "false");
            props.setProperty("mail.smtp.port", "465");
            props.setProperty("mail.smtp.socketFactory.port", "465");
        } else {
            props.setProperty("mail.smtp.port", "25");
        }
        props.put("mail.smtp.auth", "true");

        String userName = this.getUsername();
        String password = this.getPassword();
        Session session = Session.getDefaultInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        });
        Message mailMessage = new MimeMessage(session);
        // 创建邮件发送者地址
        Address from = new InternetAddress(this.from);
        // 设置邮件消息的发送者
        mailMessage.setFrom(from);
        return mailMessage;
    }

    public static Mailer getMailer() {
        return AppUtils.getBean(Mailer.class);
    }
}
