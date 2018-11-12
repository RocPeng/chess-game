package com.beimi.util.server.handler;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.tio.core.Aio;
import org.tio.utils.json.Json;
import org.tio.websocket.common.Opcode;
import org.tio.websocket.common.WsResponse;

import com.beimi.config.web.GameServer;
import com.beimi.core.engine.game.Message;

public class BeiMiClient{
	
	protected GameServer server;
	
	private String token ;
	private String playway ;
	private String orgi ;
	private String room ;
	
	private String command ;
	
	private String data ;
	
	private long time ;
	
	private String userid ;
	
	private String session ;
	
	
	private Map<String,  String> extparams ;
	
	public BeiMiClient(){
		
	}
	
	public String getSession() {
		return session;
	}


	public void setSession(String session) {
		this.session = session;
	}


	public String getPlayway() {
		return playway;
	}

	public void setPlayway(String playway) {
		this.playway = playway;
	}

	public String getOrgi() {
		return orgi;
	}

	public void setOrgi(String orgi) {
		this.orgi = orgi;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public Map<String, String> getExtparams() {
		return extparams;
	}

	public void setExtparams(Map<String, String> extparams) {
		this.extparams = extparams;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}
	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
	public void sendEvent(String event, Message msg){
		try {
			msg.setEvent(event);
			Aio.sendToUser(this.server.getServerGroupContext(), this.userid , convertToTextResponse(msg)) ;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public GameServer getServer() {
		return server;
	}

	public void setServer(GameServer server) {
		this.server = server;
	}
	/**
	 * 
	 * @param body
	 * @return
	 * @throws IOException
	 */
	public WsResponse convertToTextResponse(Serializable body) throws IOException{
        WsResponse response = new WsResponse();
        if(body != null) {
            String json = Json.toJson(body);
            response.setBody(json.getBytes("UTF-8"));
            response.setWsBodyText(json);
            response.setWsBodyLength(response.getWsBodyText().length());
            //返回text类型消息（如果这里设置成 BINARY,那么客户端就需要进行解析了）
            response.setWsOpcode(Opcode.TEXT);
        }
        return response;
    }
}
