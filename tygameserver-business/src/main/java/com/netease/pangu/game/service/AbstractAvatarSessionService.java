package com.netease.pangu.game.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import com.netease.pangu.game.meta.Avatar;
import io.netty.channel.ChannelId;
import org.springframework.util.Assert;

import com.netease.pangu.game.common.meta.IAvatar;
import com.netease.pangu.game.common.meta.AvatarSession;

import io.netty.channel.Channel;

public abstract class AbstractAvatarSessionService<A extends IAvatar> {
    @Resource
    private UniqueIDGeneratorService uniqueIdGeneratorService;

    private final Map<Long, Map<String, A>> avatarsCache;

    private final Map<Long, Map<ChannelId, Long>> avatarSessions;

    private final ConcurrentMap<Long, AvatarSession<A>> sessions;

    public interface SessionCallable<T, SA extends IAvatar> {
        public T call(AvatarSession<SA> playerSession);
    }

    public AbstractAvatarSessionService() {
        sessions = new ConcurrentHashMap<Long, AvatarSession<A>>();
        avatarsCache = new HashMap<Long, Map<String, A>>();
        avatarSessions = new HashMap<Long, Map<ChannelId, Long>>();
    }

    public Map<Long, AvatarSession<A>> getSessions() {
        return Collections.unmodifiableMap(sessions);
    }

    public Map<Long, A> getAvatars(Set<Long> avatarIds) {
        Map<Long, A> avatarMap = new HashMap<Long, A>();
        for (Long avatarId : avatarIds) {
            AvatarSession<A> session = sessions.get(avatarId);
            avatarMap.put(avatarId, session.getAvatar());
        }
        return Collections.unmodifiableMap(avatarMap);
    }

    public Map<Long, AvatarSession<A>> getAvatarSessions(Set<Long> avatarIds) {
        Map<Long, AvatarSession<A>> avatarMap = new HashMap<Long, AvatarSession<A>>();
        for (Long avatarId : avatarIds) {
            AvatarSession<A> session = sessions.get(avatarId);
            avatarMap.put(avatarId, session);
        }
        return Collections.unmodifiableMap(avatarMap);
    }

    public void put(long avatarId, AvatarSession<A> session) {
        if (sessions.put(avatarId, session) == null) {
            synchronized (avatarsCache) {
                if (!avatarsCache.containsKey(session.getGameId())) {
                    avatarsCache.put(session.getGameId(), new HashMap<String, A>());
                }
                avatarsCache.get(session.getGameId()).put(session.getUuid(), session.getAvatar());
            }
            synchronized (avatarSessions){
                if(!avatarSessions.containsKey(session.getGameId())){
                    avatarSessions.put(session.getGameId(), new HashMap<ChannelId, Long>());
                }
                avatarSessions.get(session.getGameId()).put(session.getChannelId(), session.getAvatarId());
            }
        }
    }

    public A getAvatarFromCache(long gameId, String uuid) {
        if (avatarsCache.containsKey(gameId)) {
            return avatarsCache.get(gameId).get(uuid);
        } else {
            return null;
        }
    }

    public void remove(long avatarId) {
        AvatarSession<A> session = sessions.remove(avatarId);
        if (session != null) {
            synchronized (avatarsCache) {
                if (avatarsCache.containsKey(session.getGameId())) {
                    avatarsCache.get(session.getGameId()).remove(session.getUuid());
                }
            }

            synchronized (avatarSessions){
                if(avatarSessions.containsKey(session.getGameId())){
                    avatarSessions.get(session.getGameId()).remove(session.getChannelId());
                }
            }
        }
    }

    public AvatarSession<A> getSession(long avatarId) {
        return sessions.get(avatarId);
    }

    public AvatarSession<A> createAvatarSession(A avatar, Channel channel) {
        AvatarSession<A> session = new AvatarSession<A>();
        session.setAvatar(avatar);
        session.setAttrs(new HashMap<String, Object>());
        session.setRoomId(0L);
        session.setCreateTime(System.currentTimeMillis());
        session.setChannel(channel);
        put(session.getAvatarId(), session);
        return session;
    }

    public <T> T updateAvatarSession(long avatarId, SessionCallable<T, A> callable) {
        AvatarSession<A> session = getSession(avatarId);
        Assert.notNull(callable);
        if (session != null) {
            synchronized (session) {
                try {
                    return callable.call(session);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            return callable.call(null);
        }
        return null;
    }
}
