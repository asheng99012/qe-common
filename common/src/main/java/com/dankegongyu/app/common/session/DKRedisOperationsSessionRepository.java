package com.dankegongyu.app.common.session;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.session.ExpiringSession;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisFlushMode;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.session.events.SessionDeletedEvent;
import org.springframework.session.events.SessionDestroyedEvent;
import org.springframework.session.events.SessionExpiredEvent;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by ashen on 2017-3-14.
 */
public class DKRedisOperationsSessionRepository implements
        FindByIndexNameSessionRepository<DKRedisOperationsSessionRepository.RedisSession>,
        MessageListener {
    private static final Log logger = LogFactory
            .getLog(RedisOperationsSessionRepository.class);

    private static final String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";

    static PrincipalNameResolver PRINCIPAL_NAME_RESOLVER = new PrincipalNameResolver();

    /**
     * The default prefix for each key and channel in Redis used by Spring Session.
     */
    static final String DEFAULT_SPRING_SESSION_REDIS_PREFIX = "spring:session:";

    /**
     * The key in the Hash representing
     * {@link org.springframework.session.ExpiringSession#getCreationTime()}.
     */
    static final String CREATION_TIME_ATTR = "creationTime";

    /**
     * The key in the Hash representing
     * {@link org.springframework.session.ExpiringSession#getMaxInactiveIntervalInSeconds()}
     * .
     */
    static final String MAX_INACTIVE_ATTR = "maxInactiveInterval";

    /**
     * The key in the Hash representing
     * {@link org.springframework.session.ExpiringSession#getLastAccessedTime()}.
     */
    static final String LAST_ACCESSED_ATTR = "lastAccessedTime";

    /**
     * The prefix of the key for used for session attributes. The suffix is the name of
     * the session attribute. For example, if the session contained an attribute named
     * attributeName, then there would be an entry in the hash named
     * sessionAttr:attributeName that mapped to its value.
     */
    static final String SESSION_ATTR_PREFIX = "sessionAttr:";

    /**
     * The prefix for every key used by Spring Session in Redis.
     */
    private String keyPrefix = DEFAULT_SPRING_SESSION_REDIS_PREFIX;

    private final RedisOperations<Object, Object> sessionRedisOperations;

    private final RedisSessionExpirationPolicy expirationPolicy;

    private ApplicationEventPublisher eventPublisher = new ApplicationEventPublisher() {
        public void publishEvent(ApplicationEvent event) {
        }

        public void publishEvent(Object event) {
        }
    };

    /**
     * If non-null, this value is used to override the default value for
     * {@link RedisSession#setMaxInactiveIntervalInSeconds(int)}.
     */
    private Integer defaultMaxInactiveInterval;

    private RedisSerializer<Object> defaultSerializer = new JdkSerializationRedisSerializer();

    private RedisFlushMode redisFlushMode = RedisFlushMode.ON_SAVE;

    /**
     * Allows creating an instance and uses a default {@link RedisOperations} for both
     * managing the session and the expirations.
     *
     * @param redisConnectionFactory the {@link RedisConnectionFactory} to use.
     */
    public DKRedisOperationsSessionRepository(
            RedisConnectionFactory redisConnectionFactory) {
        this(createDefaultTemplate(redisConnectionFactory));
    }

    /**
     * Creates a new instance. For an example, refer to the class level javadoc.
     *
     * @param sessionRedisOperations The {@link RedisOperations} to use for managing the
     *                               sessions. Cannot be null.
     */
    public DKRedisOperationsSessionRepository(
            RedisOperations<Object, Object> sessionRedisOperations) {
        Assert.notNull(sessionRedisOperations, "sessionRedisOperations cannot be null");
        this.sessionRedisOperations = sessionRedisOperations;
        this.expirationPolicy = new RedisSessionExpirationPolicy(sessionRedisOperations, this);
    }

    /**
     * Sets the {@link ApplicationEventPublisher} that is used to publish
     * {@link SessionDestroyedEvent}. The default is to not publish a
     * {@link SessionDestroyedEvent}.
     *
     * @param applicationEventPublisher the {@link ApplicationEventPublisher} that is used
     *                                  to publish {@link SessionDestroyedEvent}. Cannot be null.
     */
    public void setApplicationEventPublisher(
            ApplicationEventPublisher applicationEventPublisher) {
        Assert.notNull(applicationEventPublisher,
                "applicationEventPublisher cannot be null");
        this.eventPublisher = applicationEventPublisher;
    }

    /**
     * Sets the maximum inactive interval in seconds between requests before newly created
     * sessions will be invalidated. A negative time indicates that the session will never
     * timeout. The default is 1800 (30 minutes).
     *
     * @param defaultMaxInactiveInterval the number of seconds that the {@link Session}
     *                                   should be kept alive between client requests.
     */
    public void setDefaultMaxInactiveInterval(int defaultMaxInactiveInterval) {
        this.defaultMaxInactiveInterval = defaultMaxInactiveInterval;
    }

    /**
     * Sets the default redis serializer. Replaces default serializer which is based on
     * {@link JdkSerializationRedisSerializer}.
     *
     * @param defaultSerializer the new default redis serializer
     */
    public void setDefaultSerializer(RedisSerializer<Object> defaultSerializer) {
        Assert.notNull(defaultSerializer, "defaultSerializer cannot be null");
        this.defaultSerializer = defaultSerializer;
    }

    /**
     * Sets the redis flush mode. Default flush mode is {@link RedisFlushMode#ON_SAVE}.
     *
     * @param redisFlushMode the new redis flush mode
     */
    public void setRedisFlushMode(RedisFlushMode redisFlushMode) {
        Assert.notNull(redisFlushMode, "redisFlushMode cannot be null");
        this.redisFlushMode = redisFlushMode;
    }

    public void save(RedisSession session) {
        session.saveDelta();
        if (session.isNew()) {
            String sessionCreatedKey = getSessionCreatedChannel(session.getId());
            this.sessionRedisOperations.convertAndSend(sessionCreatedKey, session.delta);
            session.setNew(false);
        }
    }

    @Scheduled(cron = "${spring.session.cleanup.cron.expression:0 * * * * *}")
    public void cleanupExpiredSessions() {
        this.expirationPolicy.cleanExpiredSessions();
    }

    public RedisSession getSession(String id) {
        return getSession(id, false);
    }

    public Map<String, RedisSession> findByIndexNameAndIndexValue(String indexName,
                                                                  String indexValue) {
        if (!PRINCIPAL_NAME_INDEX_NAME.equals(indexName)) {
            return Collections.emptyMap();
        }
        String principalKey = getPrincipalKey(indexValue);
        Set<Object> sessionIds = this.sessionRedisOperations.boundSetOps(principalKey)
                .members();
        Map<String, RedisSession> sessions = new HashMap<String, RedisSession>(
                sessionIds.size());
        for (Object id : sessionIds) {
            RedisSession session = getSession((String) id);
            if (session != null) {
                sessions.put(session.getId(), session);
            }
        }
        return sessions;
    }

    /**
     * Gets the session.
     *
     * @param id           the session id
     * @param allowExpired if true, will also include expired sessions that have not been
     *                     deleted. If false, will ensure expired sessions are not returned.
     * @return the Redis session
     */
    private RedisSession getSession(String id, boolean allowExpired) {
        Map<Object, Object> entries = getSessionBoundHashOperations(id).entries();
        if (entries.isEmpty()) {
            return null;
        }
        MapSession loaded = loadSession(id, entries);
        if (!allowExpired && loaded.isExpired()) {
            return null;
        }
        RedisSession result = new RedisSession(loaded);
        result.originalLastAccessTime = loaded.getLastAccessedTime();
        return result;
    }

    private MapSession loadSession(String id, Map<Object, Object> entries) {
        MapSession loaded = new MapSession(id);
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            String key = (String) entry.getKey();
            if (CREATION_TIME_ATTR.equals(key)) {
                loaded.setCreationTime((Long) entry.getValue());
            } else if (MAX_INACTIVE_ATTR.equals(key)) {
                loaded.setMaxInactiveIntervalInSeconds((Integer) entry.getValue());
            } else if (LAST_ACCESSED_ATTR.equals(key)) {
                loaded.setLastAccessedTime((Long) entry.getValue());
            } else if (key.startsWith(SESSION_ATTR_PREFIX)) {
                loaded.setAttribute(key.substring(SESSION_ATTR_PREFIX.length()),
                        entry.getValue());
            }
        }
        return loaded;
    }

    public void delete(String sessionId) {
        RedisSession session = getSession(sessionId, true);
        if (session == null) {
            return;
        }

        cleanupPrincipalIndex(session);
        this.expirationPolicy.onDelete(session);

        String expireKey = getExpiredKey(session.getId());
        this.sessionRedisOperations.delete(expireKey);

        session.setMaxInactiveIntervalInSeconds(0);
        save(session);
    }

    public RedisSession createSession() {
        RedisSession redisSession = new RedisSession();
        if (this.defaultMaxInactiveInterval != null) {
            redisSession.setMaxInactiveIntervalInSeconds(this.defaultMaxInactiveInterval);
        }
        return redisSession;
    }

    @SuppressWarnings("unchecked")
    public void onMessage(Message message, byte[] pattern) {
        byte[] messageChannel = message.getChannel();
        byte[] messageBody = message.getBody();
        if (messageChannel == null || messageBody == null) {
            return;
        }

        String channel = new String(messageChannel);

        if (channel.startsWith(getSessionCreatedChannelPrefix())) {
            // TODO: is this thread safe?
            Map<Object, Object> loaded = (Map<Object, Object>) this.defaultSerializer
                    .deserialize(message.getBody());
            handleCreated(loaded, channel);
            return;
        }

        String body = new String(messageBody);
        if (!body.startsWith(getExpiredKeyPrefix())) {
            return;
        }

        boolean isDeleted = channel.endsWith(":del");
        if (isDeleted || channel.endsWith(":expired")) {
            int beginIndex = body.lastIndexOf(":") + 1;
            int endIndex = body.length();
            String sessionId = body.substring(beginIndex, endIndex);

            RedisSession session = getSession(sessionId, true);

            if (logger.isDebugEnabled()) {
                logger.debug("Publishing SessionDestroyedEvent for session " + sessionId);
            }

            cleanupPrincipalIndex(session);

            if (isDeleted) {
                handleDeleted(sessionId, session);
            } else {
                handleExpired(sessionId, session);
            }

            return;
        }
    }

    private void cleanupPrincipalIndex(RedisSession session) {
        if (session == null) {
            return;
        }
        String sessionId = session.getId();
        String principal = PRINCIPAL_NAME_RESOLVER.resolvePrincipal(session);
        if (principal != null) {
            this.sessionRedisOperations.boundSetOps(getPrincipalKey(principal))
                    .remove(sessionId);
        }
    }

    public void handleCreated(Map<Object, Object> loaded, String channel) {
        String id = channel.substring(channel.lastIndexOf(":") + 1);
        ExpiringSession session = loadSession(id, loaded);
        publishEvent(new SessionCreatedEvent(this, session));
    }

    private void handleDeleted(String sessionId, RedisSession session) {
        if (session == null) {
            publishEvent(new SessionDeletedEvent(this, sessionId));
        } else {
            publishEvent(new SessionDeletedEvent(this, session));
        }
    }

    private void handleExpired(String sessionId, RedisSession session) {
        if (session == null) {
            publishEvent(new SessionExpiredEvent(this, sessionId));
        } else {
            publishEvent(new SessionExpiredEvent(this, session));
        }
    }

    private void publishEvent(ApplicationEvent event) {
        try {
            this.eventPublisher.publishEvent(event);
        } catch (Throwable ex) {
            logger.error("Error publishing " + event + ".", ex);
        }
    }

    public void setRedisKeyNamespace(String namespace) {
        this.keyPrefix = DEFAULT_SPRING_SESSION_REDIS_PREFIX + namespace + ":";
    }

    /**
     * Gets the Hash key for this session by prefixing it appropriately.
     *
     * @param sessionId the session id
     * @return the Hash key for this session by prefixing it appropriately.
     */
    String getSessionKey(String sessionId) {
        return this.keyPrefix + "sessions:" + sessionId;
    }

    String getPrincipalKey(String principalName) {
        return this.keyPrefix + "index:"
                + FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME + ":"
                + principalName;
    }

    String getExpirationsKey(long expiration) {
        return this.keyPrefix + "expirations:" + expiration;
    }

    private String getExpiredKey(String sessionId) {
        return getExpiredKeyPrefix() + sessionId;
    }

    private String getSessionCreatedChannel(String sessionId) {
        return getSessionCreatedChannelPrefix() + sessionId;
    }

    private String getExpiredKeyPrefix() {
        return this.keyPrefix + "sessions:" + "expires:";
    }

    /**
     * Gets the prefix for the channel that SessionCreatedEvent are published to. The
     * suffix is the session id of the session that was created.
     *
     * @return the prefix for the channel that SessionCreatedEvent are published to
     */
    public String getSessionCreatedChannelPrefix() {
        return this.keyPrefix + "event:created:";
    }

    /**
     * Gets the {@link BoundHashOperations} to operate on a {@link Session}.
     *
     * @param sessionId the id of the {@link Session} to work with
     * @return the {@link BoundHashOperations} to operate on a {@link Session}
     */
    private BoundHashOperations<Object, Object, Object> getSessionBoundHashOperations(
            String sessionId) {
        String key = getSessionKey(sessionId);
        return this.sessionRedisOperations.boundHashOps(key);
    }

    /**
     * Gets the key for the specified session attribute.
     *
     * @param attributeName the attribute name
     * @return the attribute key name
     */
    static String getSessionAttrNameKey(String attributeName) {
        return SESSION_ATTR_PREFIX + attributeName;
    }

    private static RedisTemplate<Object, Object> createDefaultTemplate(
            RedisConnectionFactory connectionFactory) {
        Assert.notNull(connectionFactory, "connectionFactory cannot be null");
        RedisTemplate<Object, Object> template = new RedisTemplate<Object, Object>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setConnectionFactory(connectionFactory);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * A custom implementation of {@link Session} that uses a {@link MapSession} as the
     * basis for its mapping. It keeps track of any attributes that have changed. When
     * {@link org.springframework.session.data.redis.RedisOperationsSessionRepository.RedisSession#saveDelta()}
     * is invoked all the attributes that have been changed will be persisted.
     *
     * @author Rob Winch
     * @since 1.0
     */
    final class RedisSession implements ExpiringSession {
        private final MapSession cached;
        private Long originalLastAccessTime;
        private Map<String, Object> delta = new HashMap<String, Object>();
        private boolean isNew;
        private String originalPrincipalName;

        /**
         * Creates a new instance ensuring to mark all of the new attributes to be
         * persisted in the next save operation.
         */
        RedisSession() {
            this(new MapSession());
            this.delta.put(CREATION_TIME_ATTR, getCreationTime());
            this.delta.put(MAX_INACTIVE_ATTR, getMaxInactiveIntervalInSeconds());
            this.delta.put(LAST_ACCESSED_ATTR, getLastAccessedTime());
            this.isNew = true;
            this.flushImmediateIfNecessary();
        }

        /**
         * Creates a new instance from the provided {@link MapSession}.
         *
         * @param cached the {@link MapSession} that represents the persisted session that
         *               was retrieved. Cannot be null.
         */
        RedisSession(MapSession cached) {
            Assert.notNull("MapSession cannot be null");
            this.cached = cached;
            this.originalPrincipalName = PRINCIPAL_NAME_RESOLVER.resolvePrincipal(this);
        }

        public void setNew(boolean isNew) {
            this.isNew = isNew;
        }

        public void setLastAccessedTime(long lastAccessedTime) {
            this.cached.setLastAccessedTime(lastAccessedTime);
            this.putAndFlush(LAST_ACCESSED_ATTR, getLastAccessedTime());
        }

        public boolean isExpired() {
            return this.cached.isExpired();
        }

        public boolean isNew() {
            return this.isNew;
        }

        public long getCreationTime() {
            return this.cached.getCreationTime();
        }

        public String getId() {
            return this.cached.getId();
        }

        public long getLastAccessedTime() {
            return this.cached.getLastAccessedTime();
        }

        public void setMaxInactiveIntervalInSeconds(int interval) {
            this.cached.setMaxInactiveIntervalInSeconds(interval);
            this.putAndFlush(MAX_INACTIVE_ATTR, getMaxInactiveIntervalInSeconds());
        }

        public int getMaxInactiveIntervalInSeconds() {
            return this.cached.getMaxInactiveIntervalInSeconds();
        }

        public <T> T getAttribute(String attributeName) {
            return this.cached.getAttribute(attributeName);
        }

        public Set<String> getAttributeNames() {
            return this.cached.getAttributeNames();
        }

        public void setAttribute(String attributeName, Object attributeValue) {
            this.cached.setAttribute(attributeName, attributeValue);
            this.putAndFlush(getSessionAttrNameKey(attributeName), attributeValue);
        }

        public void removeAttribute(String attributeName) {
            this.cached.removeAttribute(attributeName);
            this.putAndFlush(getSessionAttrNameKey(attributeName), null);
        }

        private void flushImmediateIfNecessary() {
            if (DKRedisOperationsSessionRepository.this.redisFlushMode == RedisFlushMode.IMMEDIATE) {
                saveDelta();
            }
        }

        private void putAndFlush(String a, Object v) {
            this.delta.put(a, v);
            this.flushImmediateIfNecessary();
        }

        /**
         * Saves any attributes that have been changed and updates the expiration of this
         * session.
         */
        private void saveDelta() {
            if (this.delta.isEmpty()) {
                return;
            }
            String sessionId = getId();
            getSessionBoundHashOperations(sessionId).putAll(this.delta);
            String principalSessionKey = getSessionAttrNameKey(
                    FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME);
            String securityPrincipalSessionKey = getSessionAttrNameKey(
                    SPRING_SECURITY_CONTEXT);
            if (this.delta.containsKey(principalSessionKey)
                    || this.delta.containsKey(securityPrincipalSessionKey)) {
                if (this.originalPrincipalName != null) {
                    String originalPrincipalRedisKey = getPrincipalKey(
                            this.originalPrincipalName);
                    DKRedisOperationsSessionRepository.this.sessionRedisOperations
                            .boundSetOps(originalPrincipalRedisKey).remove(sessionId);
                }
                String principal = PRINCIPAL_NAME_RESOLVER.resolvePrincipal(this);
                this.originalPrincipalName = principal;
                if (principal != null) {
                    String principalRedisKey = getPrincipalKey(principal);
                    DKRedisOperationsSessionRepository.this.sessionRedisOperations
                            .boundSetOps(principalRedisKey).add(sessionId);
                }
            }

            this.delta = new HashMap<String, Object>(this.delta.size());

            Long originalExpiration = this.originalLastAccessTime == null ? null
                    : this.originalLastAccessTime + TimeUnit.SECONDS
                    .toMillis(getMaxInactiveIntervalInSeconds());
            DKRedisOperationsSessionRepository.this.expirationPolicy
                    .onExpirationUpdated(originalExpiration, this);
        }
    }

    /**
     * Principal name resolver helper class.
     */
    static class PrincipalNameResolver {
        private SpelExpressionParser parser = new SpelExpressionParser();

        public String resolvePrincipal(Session session) {
            String principalName = session.getAttribute(PRINCIPAL_NAME_INDEX_NAME);
            if (principalName != null) {
                return principalName;
            }
            Object authentication = session.getAttribute(SPRING_SECURITY_CONTEXT);
            if (authentication != null) {
                Expression expression = this.parser
                        .parseExpression("authentication?.name");
                return expression.getValue(authentication, String.class);
            }
            return null;
        }

    }
}