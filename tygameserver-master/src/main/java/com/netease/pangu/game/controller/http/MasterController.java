package com.netease.pangu.game.controller.http;

import com.netease.pangu.game.common.meta.GameConst;
import com.netease.pangu.game.core.service.SlaveScheduleService;
import com.netease.pangu.game.distribution.Slave;
import com.netease.pangu.game.distribution.SlaveManager;
import com.netease.pangu.game.http.annotation.Anonymous;
import com.netease.pangu.game.http.annotation.HttpController;
import com.netease.pangu.game.http.annotation.HttpRequestMapping;
import com.netease.pangu.game.meta.Avatar;
import com.netease.pangu.game.meta.DataCenterSimpleRoleInfo;
import com.netease.pangu.game.service.AvatarService;
import com.netease.pangu.game.service.DataCenterApiService;
import com.netease.pangu.game.service.RoomAllocationService;
import com.netease.pangu.game.util.BusinessCode;
import com.netease.pangu.game.util.HttpClientUtils;
import com.netease.pangu.game.util.JSONPUtil;
import com.netease.pangu.game.util.ReturnUtils;
import com.netease.pangu.game.util.UrsAuthUtils;
import io.netty.handler.codec.http.FullHttpRequest;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@HttpController(value = "/master", moduleId = GameConst.SYSTEM)
public class MasterController {
    @Resource
    private SlaveScheduleService slaveScheduleService;
    @Resource
    private AvatarService avatarService;
    @Resource
    private SlaveManager slaveManager;
    @Resource
    private RoomAllocationService roomAllocationService;
    @Resource
    private DataCenterApiService dataCenterApiService;

    @Value("${auth.url}")
    private String authBaseUrl;
    private final String GENERATE_URL = "/auth/generate";

    @HttpRequestMapping("/init")
    public String getSlave(String uuid, String roleName, String avatarImg, long moduleId, long roomId, String callback, FullHttpRequest request) {
        String urs = UrsAuthUtils.getLoginedUserName(request);
        if (StringUtils.isNotEmpty(urs)) {
            Slave node = null;
            Avatar avatar = avatarService.getAvatarByUUID(moduleId, uuid);

            if (avatar != null) {
                avatar.setAvatarImg(avatarImg);
                avatar.setName(roleName);
                avatarService.save(avatar);
                String server = avatar.getServer();
                long roomIdInGame = roomAllocationService.getRoomByAvatarId(moduleId, avatar.getAvatarId());
                if (roomIdInGame > 0) {
                    node = slaveManager.get(roomAllocationService.getServerByRoomId(moduleId, roomIdInGame));
                } else if (roomId > 0) {
                    String tServer = roomAllocationService.getServerByRoomId(moduleId, roomId);
                    if (StringUtils.isNotEmpty(tServer)) {
                        node = slaveManager.get(tServer);
                    }
                } else if (StringUtils.isNotEmpty(server)) {
                    node = slaveManager.get(server);
                } else {
                    node = slaveScheduleService.getSlaveByScheduled();
                }
            } else {
                avatar = new Avatar();
                avatar.setAvatarImg(avatarImg);
                avatar.setGameId(moduleId);
                avatar.setLastLoginTime(System.currentTimeMillis());
                avatar.setName(roleName);
                node = slaveScheduleService.getSlaveByScheduled();
                if (node != null) {
                    avatar.setServer(node.getName());
                    avatar.setUuid(uuid);
                    avatar.setWriteToDbTime(System.currentTimeMillis());
                    avatar = avatarService.createAvatar(avatar);
                    avatarService.insert(avatar);
                }
            }
            if (node != null) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("uuid", uuid);
                params.put("gameId", GameConst.SYSTEM);
                HttpClientUtils.HttpResult result = HttpClientUtils.get(authBaseUrl + GENERATE_URL, params);
                if (result.getStatusCode() == HttpStatus.SC_OK) {
                    Map<String, Object> workerInfo = new HashMap<String, Object>();
                    workerInfo.put("token", result.getContentAsString());
                    workerInfo.put("ip", node.getIp());
                    workerInfo.put("port", node.getPort());
                    workerInfo.put("name", node.getName());
                    workerInfo.put("avatarId", avatar.getAvatarId());
                    return JSONPUtil.getJSONP(callback, ReturnUtils.succ(workerInfo));
                }
            }
            return JSONPUtil.getJSONP(callback, BusinessCode.failed());
        } else {
            return JSONPUtil.getJSONP(callback, BusinessCode.failed(BusinessCode.NOT_LOGINED));
        }
    }

    @HttpRequestMapping("/avatar")
    public Map<String, Object> getAvatarByUUID(long gameId, String uuid) {
        Avatar avatar = avatarService.getAvatarByUUID(gameId, uuid);
        Map<String, Object> playerObj = new HashMap<String, Object>();
        if (avatar != null) {
            playerObj.put("name", avatar.getName());
            playerObj.put("uuid", avatar.getUuid());
            playerObj.put("avatarId", avatar.getAvatarId());
            playerObj.put("server", avatar.getServer());
        }
        return playerObj;
    }

    @HttpRequestMapping("/avatar/roles")
    public String getRolesByUrs(String callback, FullHttpRequest request) {
        String urs = UrsAuthUtils.getLoginedUserName(request);
        if (StringUtils.isNotEmpty(urs)) {
            Map<String, Object> serversMap = MapUtils.getMap(dataCenterApiService.getGameServers(), "all", new HashMap<String, Object>());
            Map<String, List<DataCenterSimpleRoleInfo>> roles = dataCenterApiService.getSimpleAvatarsInfoByUrs(urs);
            Map<String, List<Map<String, Object>>> rolesData = new HashMap<String, List<Map<String, Object>>>();
            for (String server : roles.keySet()) {
                String displayName = server;
                Map<String, Object> serverInfo = (Map<String, Object>) serversMap.get(server);
                if (serverInfo != null && serverInfo.containsKey("displayName")) {
                    displayName = (String) serverInfo.get("displayName");
                }
                List<DataCenterSimpleRoleInfo> rolesInfo = roles.get(server);
                if (rolesInfo != null) {
                    List<Map<String, Object>> roleList = rolesData.get(displayName);
                    if (roleList == null) {
                        roleList = new ArrayList<Map<String, Object>>();
                        rolesData.put(displayName, roleList);
                    }
                    for (DataCenterSimpleRoleInfo info : rolesInfo) {
                        Map<String, Object> roleMap = new HashMap<String, Object>();
                        roleMap.put("playerName", info.getPlayerName());
                        roleMap.put("level", info.getLevel());
                        roleMap.put("gbId", String.valueOf(info.getGbId()));
                        roleMap.put("school", info.getSchool());
                        roleMap.put("serverId", info.getServerName());
                        roleList.add(roleMap);
                    }
                }
            }
            return JSONPUtil.getJSONP(callback, ReturnUtils.succ(rolesData));
        } else {
            return JSONPUtil.getJSONP(callback, BusinessCode.failed(BusinessCode.NOT_LOGINED));
        }
    }

    @Anonymous
    @HttpRequestMapping("/isLogin")
    public String isLogin(String callback, FullHttpRequest request) {
        String urs = UrsAuthUtils.getLoginedUserName(request);
        if (StringUtils.isNotEmpty(urs)) {
            return JSONPUtil.getJSONP(callback, ReturnUtils.succ());
        } else {
            return JSONPUtil.getJSONP(callback, BusinessCode.failed(BusinessCode.NOT_LOGINED));
        }
    }

    @HttpRequestMapping("/avatar/list")
    public List<Map<String, Object>> getAvatarList(long gameId) {
        List<Avatar> list = avatarService.getListByGameId(gameId);
        Map<Long, Long> roomInfoMap = roomAllocationService.getAvatarIdsByRoom(gameId);
        List<Map<String, Object>> avatarObjList = new ArrayList<Map<String, Object>>();
        for (Avatar avatar : list) {
            Map<String, Object> avatarObj = new HashMap<String, Object>();
            avatarObj.put("name", avatar.getName());
            avatarObj.put("uuid", avatar.getUuid());
            avatarObj.put("avatarId", avatar.getAvatarId());
            avatarObj.put("server", avatar.getServer());
            avatarObj.put("roomId", roomInfoMap.get(avatar.getAvatarId()));
            avatarObjList.add(avatarObj);
        }
        return avatarObjList;
    }
}
