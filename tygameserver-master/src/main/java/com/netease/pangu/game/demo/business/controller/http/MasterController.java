package com.netease.pangu.game.demo.business.controller.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;

import com.netease.pangu.game.common.meta.GameConst;
import com.netease.pangu.game.core.service.NodeScheduleService;
import com.netease.pangu.game.distribution.Node;
import com.netease.pangu.game.distribution.NodeManager;
import com.netease.pangu.game.http.annotation.HttpController;
import com.netease.pangu.game.http.annotation.HttpRequestMapping;
import com.netease.pangu.game.meta.Avatar;
import com.netease.pangu.game.meta.GuessQuestion;
import com.netease.pangu.game.rpc.annotation.WsRpcCall;
import com.netease.pangu.game.rpc.annotation.WsRpcController;
import com.netease.pangu.game.service.AvatarService;
import com.netease.pangu.game.service.GuessGameService;
import com.netease.pangu.game.service.RoomAllocationService;
import com.netease.pangu.game.util.JsonUtil;

@WsRpcController(value="/master", gameId=GameConst.GUESSS)
@HttpController(value="/master", gameId=GameConst.GUESSS)
public class MasterController {
	@Resource NodeScheduleService appWorkerScheduleService;
	@Resource AvatarService avatarService;
	@Resource NodeManager nodeManager;
	@Resource RoomAllocationService roomAllocationService;
	@Resource GuessGameService guessGameService;
	@WsRpcCall("/init")
	@HttpRequestMapping("/init")
	public String getNode(String uuid, String roleName, String avatarImg, long gameId, long roomId, String callback){
		Node node = null;
		Avatar avatar = avatarService.getAvatarByUUID(gameId, uuid);
		
		if(avatar != null){			
			avatar.setAvatarImg(avatarImg);
			avatar.setName(roleName);
			avatarService.save(avatar);
			String server = avatar.getServer();
			long roomIdInGame =  roomAllocationService.getRoomByAvatarId(gameId, avatar.getAvatarId());
			if(roomIdInGame > 0){
				node = nodeManager.getNode(roomAllocationService.getServerByRoomId(gameId, roomIdInGame));
			}else if(roomId > 0){
				node = nodeManager.getNode(roomAllocationService.getServerByRoomId(gameId, roomId));
			}else if(StringUtils.isNotEmpty(server)){
				node = nodeManager.getNode(server);
			}else{
				node = appWorkerScheduleService.getNodeByScheduled();
			}
		}else{
			avatar = new Avatar();
			avatar.setAvatarImg(avatarImg);
			avatar.setGameId(gameId);
			avatar.setLastLoginTime(System.currentTimeMillis());
			avatar.setName(roleName);
			node = appWorkerScheduleService.getNodeByScheduled();
			avatar.setServer(node.getName());
			avatar.setUuid(uuid);
			avatar.setWriteToDbTime(System.currentTimeMillis());
			avatar = avatarService.createAvatar(avatar);
			avatarService.insert(avatar);
		}
		if(node != null){
			Map<String, Object> workerInfo = new HashMap<String, Object>();
			workerInfo.put("ip", node.getIp());
			workerInfo.put("port", node.getPort());
			workerInfo.put("name", node.getName());
			workerInfo.put("avatarId", avatar.getAvatarId());
			return callback + "(" + JsonUtil.toJson(workerInfo) + ")";
		}
		return callback + "(" +null + ")";
	}
	
	@WsRpcCall("/avatar")
	@HttpRequestMapping("/avatar")
	public Map<String, Object> getAvatarByUUID(long gameId, String uuid){
		Avatar avatar = avatarService.getAvatarByUUID(gameId, uuid);
		Map<String, Object> playerObj = new HashMap<String, Object>();
		if(avatar != null){
			playerObj.put("name", avatar.getName());
			playerObj.put("uuid", avatar.getUuid());
			playerObj.put("avatarId", avatar.getAvatarId());
			playerObj.put("server", avatar.getServer());
		}
		return playerObj;
	}
	
	@WsRpcCall("/avatar/list")
	@HttpRequestMapping("/avatar/list")
	public List<Map<String, Object>> getAvatarList(long gameId){
		List<Avatar> list = avatarService.getListByGameId(gameId);
		List<Map<String, Object>> avatarObjList = new ArrayList<Map<String, Object>> ();
		for(Avatar avatar: list){
			Map<String, Object> avatarObj = new HashMap<String, Object>();
			avatarObj.put("name", avatar.getName());
			avatarObj.put("uuid", avatar.getUuid());
			avatarObj.put("avatarId", avatar.getAvatarId());
			avatarObj.put("server", avatar.getServer());
			avatarObjList.add(avatarObj);
		}
		return avatarObjList;
	}
	
	@WsRpcCall("/questions")
	@HttpRequestMapping("/questions")
	public List<GuessQuestion> getQuestions(){
		return guessGameService.getQuestions();
	}
}
