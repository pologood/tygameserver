package com.netease.pangu.game.service;

import com.netease.pangu.game.common.meta.AvatarSession;
import com.netease.pangu.game.common.meta.ConnectionStatus;
import com.netease.pangu.game.common.meta.IAvatar;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractAvatarSessionService<A extends IAvatar> {
    @Resource
    private UniqueIDGeneratorService uniqueIdGeneratorService;

    private final Map<Long, Map<String, A>> avatarsCache;

    private final ConcurrentMap<ChannelId, Long> channelIdAvatarIdMap;

    private final ConcurrentMap<Long, AvatarSession<A>> sessions;

    public interface SessionCallable<T, SA extends IAvatar> {
        public T call(AvatarSession<SA> playerSession);
    }

    public AbstractAvatarSessionService() {
        sessions = new ConcurrentHashMap<Long, AvatarSession<A>>();
        avatarsCache = new HashMap<Long, Map<String, A>>();
        channelIdAvatarIdMap = new ConcurrentHashMap<ChannelId, Long>();
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

    public Map<Long, AvatarSession<A>> getAvatarSessions(List<Long> avatarIds) {
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
            synchronized (channelIdAvatarIdMap) {
                channelIdAvatarIdMap.put(session.getChannelId(), session.getAvatarId());
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

            synchronized (channelIdAvatarIdMap) {
                channelIdAvatarIdMap.remove(session.getChannelId());
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
        session.setState(ConnectionStatus.CONNECTED);
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

    public <T> T updateAvatarSessionByChannelId(ChannelId channelId, SessionCallable<T, A> callable) {
       if(channelIdAvatarIdMap.containsKey(channelId)) {
           long avatarId = channelIdAvatarIdMap.get(channelId);
           return updateAvatarSession(avatarId, callable);
       }else{
           return null;
       }
    }
}
