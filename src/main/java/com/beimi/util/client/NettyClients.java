package com.beimi.util.client;

import com.beimi.core.engine.game.Message;
import com.beimi.util.server.handler.BeiMiClient;


public class NettyClients {
	
	
	private static NettySystemClient systemClients = new NettySystemClient();
	
	public static NettySystemClient getInstance(){
		return systemClients ;
	}
	
	public void putGameEventClient(String id , BeiMiClient gameClient){
		systemClients.putClient(id, gameClient);
	}
	public void removeGameEventClient(String id){
		systemClients.removeClient(id);
	}
	public void sendGameEventMessage(String id , String event , Message data){
		systemClients.getClient(id).sendEvent(event, data);
	}
}
