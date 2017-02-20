package com.netease.pangu.game.demo.business.controller.http;

import com.netease.pangu.game.common.meta.GameConst;
import com.netease.pangu.game.core.service.NodeScheduleService;
import com.netease.pangu.game.distribution.Node;
import com.netease.pangu.game.distribution.NodeManager;
import com.netease.pangu.game.http.annotation.Anonymous;
import com.netease.pangu.game.http.annotation.HttpController;
import com.netease.pangu.game.http.annotation.HttpRequestMapping;
import com.netease.pangu.game.meta.Avatar;
import com.netease.pangu.game.meta.DataCenterSimpleRoleInfo;
import com.netease.pangu.game.service.AvatarService;
import com.netease.pangu.game.service.DataCenterApiService;
import com.netease.pangu.game.service.GuessGameService;
import com.netease.pangu.game.service.RoomAllocationService;
import com.netease.pangu.game.util.HttpClientUtils;
import com.netease.pangu.game.util.JSONPUtil;
import com.netease.pangu.game.util.ReturnUtils;
import com.netease.pangu.game.util.UrsAuthUtils;
import io.netty.handler.codec.http.FullHttpRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@HttpController(value = "/master", gameId = GameConst.GUESSS)
public class MasterController {
    @Resource
    private NodeScheduleService appWorkerScheduleService;
    @Resource
    private AvatarService avatarService;
    @Resource
    private NodeManager nodeManager;
    @Resource
    private RoomAllocationService roomAllocationService;
    @Resource
    private GuessGameService guessGameService;
    @Resource
    private DataCenterApiService dataCenterApiService;

    @Value("${auth.url}")
    private String authBaseUrl;
    private final String GENERATE_URL = "/auth/generate";

    @HttpRequestMapping("/init")
    public String getNode(String uuid, String roleName, String avatarImg, long gameId, long roomId, String callback, FullHttpRequest request) {
        String urs = UrsAuthUtils.getLoginedUserName(request);
        if (StringUtils.isNotEmpty(urs)) {
            Node node = null;
            Avatar avatar = avatarService.getAvatarByUUID(gameId, uuid);

            if (avatar != null) {
                avatar.setAvatarImg(avatarImg);
                avatar.setName(roleName);
                avatarService.save(avatar);
                String server = avatar.getServer();
                long roomIdInGame = roomAllocationService.getRoomByAvatarId(gameId, avatar.getAvatarId());
                if (roomIdInGame > 0) {
                    node = nodeManager.getNode(roomAllocationService.getServerByRoomId(gameId, roomIdInGame));
                } else if (roomId > 0) {
                    node = nodeManager.getNode(roomAllocationService.getServerByRoomId(gameId, roomId));
                } else if (StringUtils.isNotEmpty(server)) {
                    node = nodeManager.getNode(server);
                } else {
                    node = appWorkerScheduleService.getNodeByScheduled();
                }
            } else {
                avatar = new Avatar();
                avatar.setAvatarImg(avatarImg);
                avatar.setGameId(gameId);
                avatar.setLastLoginTime(System.currentTimeMillis());
                avatar.setName(roleName);
                node = appWorkerScheduleService.getNodeByScheduled();
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
            return JSONPUtil.getJSONP(callback, ReturnUtils.failed());
        }else{
            return JSONPUtil.getJSONP(callback, ReturnUtils.failed("not logined"));
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
    public String getRolesByUrs(String callback, FullHttpRequest request){
        String urs = UrsAuthUtils.getLoginedUserName(request);
        if(StringUtils.isNotEmpty(urs)) {
            Map<String, List<DataCenterSimpleRoleInfo>> roles = dataCenterApiService.getSimpleAvatarsInfoByUrs(urs);
            Map<String, List<Map<String, Object>>> rolesData = new HashMap<String, List<Map<String, Object>>>();
            for(String server : roles.keySet()){
                List<DataCenterSimpleRoleInfo> rolesInfo = roles.get(server);
                if(rolesInfo != null){
                    List<Map<String, Object>> roleList = rolesData.get(server);
                    if(roleList == null) {
                        roleList = new ArrayList<Map<String, Object>>();
                        rolesData.put(server, roleList);
                    }
                    for(DataCenterSimpleRoleInfo info : rolesInfo){
                        Map<String, Object> roleMap = new HashMap<String, Object>();
                        roleMap.put("playerName", info.getPlayerName());
                        roleMap.put("level", info.getLevel());
                        roleMap.put("gbId", String.valueOf(info.getGbId()));
                        roleMap.put("school", info.getSchool());
                        roleList.add(roleMap);
                    }
                }
            }
            return JSONPUtil.getJSONP(callback , ReturnUtils.succ(rolesData));
        }else{
            return JSONPUtil.getJSONP(callback, ReturnUtils.failed());
        }
    }

    @Anonymous
    @HttpRequestMapping("/isLogin")
    public String isLogin(String callback, FullHttpRequest request){
        String urs = UrsAuthUtils.getLoginedUserName(request);
        if(StringUtils.isNotEmpty(urs)) {
            return JSONPUtil.getJSONP(callback, ReturnUtils.succ());
        }else{
            return JSONPUtil.getJSONP(callback, ReturnUtils.failed());
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
