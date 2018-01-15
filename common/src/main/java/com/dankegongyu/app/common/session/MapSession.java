package com.dankegongyu.app.common.session;

import com.dankegongyu.app.common.Current;
import org.springframework.session.ExpiringSession;
import org.springframework.session.Session;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by ashen on 2017-3-14.
 */
public class MapSession implements ExpiringSession, Serializable {
    /**
     * Default {@link #setMaxInactiveIntervalInSeconds(int)} (30 minutes).
     */
    public static final int DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS = 1800;

    private String id;
    private Map<String, Object> sessionAttrs = new HashMap<String, Object>();
    private long creationTime = System.currentTimeMillis();
    private long lastAccessedTime = this.creationTime;

    /**
     * Defaults to 30 minutes.
     */
    private int maxInactiveInterval = DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS;

    /**
     * Creates a new instance with a secure randomly generated identifier.
     */
    public MapSession() {
        this(Current.getUUID());
    }

    /**
     * Creates a new instance with the specified id. This is preferred to the default
     * constructor when the id is known to prevent unnecessary consumption on entropy
     * which can be slow.
     *
     * @param id the identifier to use
     */
    public MapSession(String id) {
        this.id = id;
    }


    public MapSession(ExpiringSession session) {
        if (session == null) {
            throw new IllegalArgumentException("session cannot be null");
        }
        this.id = session.getId();
        this.sessionAttrs = new HashMap<String, Object>(
                session.getAttributeNames().size());
        for (String attrName : session.getAttributeNames()) {
            Object attrValue = session.getAttribute(attrName);
            this.sessionAttrs.put(attrName, attrValue);
        }
        this.lastAccessedTime = session.getLastAccessedTime();
        this.creationTime = session.getCreationTime();
        this.maxInactiveInterval = session.getMaxInactiveIntervalInSeconds();
    }

    public void setLastAccessedTime(long lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    public String getId() {
        return this.id;
    }

    public long getLastAccessedTime() {
        return this.lastAccessedTime;
    }

    public void setMaxInactiveIntervalInSeconds(int interval) {
        this.maxInactiveInterval = interval;
    }

    public int getMaxInactiveIntervalInSeconds() {
        return this.maxInactiveInterval;
    }

    public boolean isExpired() {
        return isExpired(System.currentTimeMillis());
    }

    boolean isExpired(long now) {
        if (this.maxInactiveInterval < 0) {
            return false;
        }
        return now - TimeUnit.SECONDS
                .toMillis(this.maxInactiveInterval) >= this.lastAccessedTime;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String attributeName) {
        return (T) this.sessionAttrs.get(attributeName);
    }

    public Set<String> getAttributeNames() {
        return this.sessionAttrs.keySet();
    }

    public void setAttribute(String attributeName, Object attributeValue) {
        if (attributeValue == null) {
            removeAttribute(attributeName);
        } else {
            this.sessionAttrs.put(attributeName, attributeValue);
        }
    }

    public void removeAttribute(String attributeName) {
        this.sessionAttrs.remove(attributeName);
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean equals(Object obj) {
        return obj instanceof Session && this.id.equals(((Session) obj).getId());
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    private static final long serialVersionUID = 7160779239673823561L;
}