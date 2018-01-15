package com.dankegongyu.app.common.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.session.ExpiringSession;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by ashen on 2017-3-14.
 */
class RedisSessionExpirationPolicy {

    private static final Log logger = LogFactory
            .getLog(RedisSessionExpirationPolicy.class);

    private final RedisOperations<Object, Object> redis;

    private final DKRedisOperationsSessionRepository redisSession;

    RedisSessionExpirationPolicy(RedisOperations<Object, Object> sessionRedisOperations,
                                 DKRedisOperationsSessionRepository redisSession) {
        super();
        this.redis = sessionRedisOperations;
        this.redisSession = redisSession;
    }

    public void onDelete(ExpiringSession session) {
        long toExpire = roundUpToNextMinute(expiresInMillis(session));
        String expireKey = getExpirationKey(toExpire);
        this.redis.boundSetOps(expireKey).remove(session.getId());
    }

    public void onExpirationUpdated(Long originalExpirationTimeInMilli,
                                    ExpiringSession session) {
        String keyToExpire = "expires:" + session.getId();
        long toExpire = roundUpToNextMinute(expiresInMillis(session));

        if (originalExpirationTimeInMilli != null) {
            long originalRoundedUp = roundUpToNextMinute(originalExpirationTimeInMilli);
            if (toExpire != originalRoundedUp) {
                String expireKey = getExpirationKey(originalRoundedUp);
                this.redis.boundSetOps(expireKey).remove(keyToExpire);
            }
        }

        long sessionExpireInSeconds = session.getMaxInactiveIntervalInSeconds();
        String sessionKey = getSessionKey(keyToExpire);

        if (sessionExpireInSeconds < 0) {
            this.redis.boundValueOps(sessionKey).append("");
            this.redis.boundValueOps(sessionKey).persist();
            this.redis.boundHashOps(getSessionKey(session.getId())).persist();
            return;
        }

        String expireKey = getExpirationKey(toExpire);
        BoundSetOperations<Object, Object> expireOperations = this.redis
                .boundSetOps(expireKey);
        expireOperations.add(keyToExpire);

        long fiveMinutesAfterExpires = sessionExpireInSeconds
                + TimeUnit.MINUTES.toSeconds(5);

        expireOperations.expire(fiveMinutesAfterExpires, TimeUnit.SECONDS);
        if (sessionExpireInSeconds == 0) {
            this.redis.delete(sessionKey);
        }
        else {
            this.redis.boundValueOps(sessionKey).append("");
            this.redis.boundValueOps(sessionKey).expire(sessionExpireInSeconds,
                    TimeUnit.SECONDS);
        }
        this.redis.boundHashOps(getSessionKey(session.getId()))
                .expire(fiveMinutesAfterExpires, TimeUnit.SECONDS);
    }

    String getExpirationKey(long expires) {
        return this.redisSession.getExpirationsKey(expires);
    }

    String getSessionKey(String sessionId) {
        return this.redisSession.getSessionKey(sessionId);
    }

    public void cleanExpiredSessions() {
        long now = System.currentTimeMillis();
        long prevMin = roundDownMinute(now);

        if (logger.isDebugEnabled()) {
            logger.debug("Cleaning up sessions expiring at " + new Date(prevMin));
        }

        String expirationKey = getExpirationKey(prevMin);
        Set<Object> sessionsToExpire = this.redis.boundSetOps(expirationKey).members();
        this.redis.delete(expirationKey);
        for (Object session : sessionsToExpire) {
            String sessionKey = getSessionKey((String) session);
            touch(sessionKey);
        }
    }

    /**
     * By trying to access the session we only trigger a deletion if it the TTL is
     * expired. This is done to handle
     * https://github.com/spring-projects/spring-session/issues/93
     *
     * @param key the key
     */
    private void touch(String key) {
        this.redis.hasKey(key);
    }

    static long expiresInMillis(ExpiringSession session) {
        int maxInactiveInSeconds = session.getMaxInactiveIntervalInSeconds();
        long lastAccessedTimeInMillis = session.getLastAccessedTime();
        return lastAccessedTimeInMillis + TimeUnit.SECONDS.toMillis(maxInactiveInSeconds);
    }

    static long roundUpToNextMinute(long timeInMs) {

        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeInMs);
        date.add(Calendar.MINUTE, 1);
        date.clear(Calendar.SECOND);
        date.clear(Calendar.MILLISECOND);
        return date.getTimeInMillis();
    }

    static long roundDownMinute(long timeInMs) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeInMs);
        date.clear(Calendar.SECOND);
        date.clear(Calendar.MILLISECOND);
        return date.getTimeInMillis();
    }
}
