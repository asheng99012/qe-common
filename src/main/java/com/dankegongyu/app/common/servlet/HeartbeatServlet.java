package com.dankegongyu.app.common.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 心跳servlet 路径配置成 http://IP:PORT/context/heartbeat
 * 
 * @author jerry Servlet implementation class CurrentPriceServlet
 *
 */
public class HeartbeatServlet extends HttpServlet {

    private static final long         serialVersionUID = 1432976803859041402L;
    private static Logger             logger           = LoggerFactory.getLogger(HeartbeatServlet.class);
    public static String              startup;
    public static String              version;
    
    
    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            startup = sdf.format(new Date());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        StringBuilder result = new StringBuilder();
        result.append("startup=");
        result.append(startup);
//        result.append(" | version=");
//        result.append(version);
        response.getWriter().write(result.toString());
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
