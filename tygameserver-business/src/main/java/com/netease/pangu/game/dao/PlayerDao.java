package com.netease.pangu.game.dao;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.netease.pangu.game.meta.Player;

@Component
public class PlayerDao extends AbstractMongoDao<Player> {
	public Player getPlayer(long id){
		Query query = new Query(Criteria.where("pId").is(id));
		return this.findOne(query, Player.class);
	}
	
	public Player getPlayerByUUID(String uuid){
		Query query = new Query(Criteria.where("uuid").is(uuid));
		return this.findOne(query, Player.class);
	}
	
	public Player getPlayerByName(String name){
		Query query = new Query(Criteria.where("name").is(name));
		return this.findOne(query, Player.class);
	}
	
	public void addPlayer(Player player){
		this.addObject(player);
	}
	
	public void updatePlayer(Player player){
		Query query = new Query(Criteria.where("pId").is(player.getPid()));
		Update update = Update.update("name", player.getName());
		update.addToSet("writeToDbTime", player.getWriteToDbTime());
		update.addToSet("server", player.getServer());
		update.addToSet("lastLoginTime", player.getLastLoginTime());
		update.addToSet("uuid", player.getUuid());
		this.update(query, update, Player.class);
	}
}
