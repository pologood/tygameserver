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
	
	public void addPlayer(Player player){
		this.addObject(player);
	}
	
	public void updatePlayer(Player player){
		Query query = new Query(Criteria.where("pId").is(player.getPid()));
		Update update = Update.update("name", player.getName());
		this.update(query, update, Player.class);
	}
}
