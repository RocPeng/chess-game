package com.beimi.core.engine.game.state;

import com.beimi.web.model.GameRoom;

public class GameEvent implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1606276594008150495L;
	
	public GameEvent(int players , int cardsnum , String orgi){
		this.players = players ;
		this.time = System.currentTimeMillis() ;
		this.orgi = orgi ;
		this.cardsnum = cardsnum ;
	}
	
	public GameEvent(String roomid , String event , int players , int cardsnum , long time , String orgi){
		this.roomid = roomid ;
		this.event = event ;
		this.players = players ;
		this.time = time ;
		this.orgi = orgi ;
		this.cardsnum = cardsnum ;
		
	}
	public String roomid ;
	private String event ;
	private String orgi ;
	private int cardsnum ;
	private GameRoom gameRoom ;
	
	
	private int index ; 	//当前玩家 顺序号
	
	private int players ;
	private long time ;
	public String getRoomid() {
		return roomid;
	}
	public void setRoomid(String roomid) {
		this.roomid = roomid;
	}
	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public int getPlayers() {
		return players;
	}
	public void setPlayers(int players) {
		this.players = players;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public String getOrgi() {
		return orgi;
	}
	public void setOrgi(String orgi) {
		this.orgi = orgi;
	}
	public int getCardsnum() {
		return cardsnum;
	}
	public void setCardsnum(int cardsnum) {
		this.cardsnum = cardsnum;
	}

	public GameRoom getGameRoom() {
		return gameRoom;
	}

	public void setGameRoom(GameRoom gameRoom) {
		this.gameRoom = gameRoom;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
