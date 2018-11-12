
package com.beimi.util.server.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.tio.core.Aio;
import org.tio.core.ChannelContext;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.websocket.common.WsRequest;
import org.tio.websocket.server.handler.IWsMsgHandler;

import com.alibaba.fastjson.JSON;
import com.beimi.config.web.GameServer;
import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.ActionTaskUtils;
import com.beimi.util.GameUtils;
import com.beimi.util.UKTools;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.client.NettyClients;
import com.beimi.util.rules.model.GameStatus;
import com.beimi.util.rules.model.SearchRoom;
import com.beimi.util.rules.model.SearchRoomResult;
import com.beimi.web.model.GamePlayway;
import com.beimi.web.model.GameRoom;
import com.beimi.web.model.PlayUserClient;
import com.beimi.web.model.Token;
import com.beimi.web.service.repository.es.PlayUserClientESRepository;
import com.beimi.web.service.repository.jpa.GameRoomRepository;
import com.beimi.web.service.repository.jpa.PlayUserClientRepository;

public class GameEventHandler implements IWsMsgHandler
{  
	protected GameServer server;
    /**
	 * 握手时走这个方法，业务可以在这里获取cookie，request参数等
	 */
	@Override
	public HttpResponse handshake(HttpRequest request, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
		String userid = request.getParam("userid") ;
		if(!StringUtils.isBlank(userid)) {
			channelContext.setAttribute(userid, userid);
            //绑定用户ID
            Aio.bindUser(channelContext, userid);
		}
		return httpResponse;
	}

	/**
	 * 字节消息（binaryType = arraybuffer）过来后会走这个方法
	 */
	@Override
	public Object onBytes(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
		channelContext.getClientNode().getIp();
		ByteBuffer buffer = ByteBuffer.allocate(1);
		return buffer;
	}
	
	/**
	 * 字符消息（binaryType = blob）过来后会走这个方法
	 */
	@Override
	public Object onText(WsRequest wsRequest, String text, ChannelContext channelContext) throws Exception {
		if(text!=null) {
			BeiMiClient beiMiClient = JSON.parseObject(text , BeiMiClient.class) ;
			if(!StringUtils.isBlank(beiMiClient.getCommand())) {
				beiMiClient.setServer(this.server);
				switch(beiMiClient.getCommand()) {
					case "joinroom" : this.onJoinRoom(beiMiClient); break;
					case "gamestatus":this.onGameStatus(beiMiClient); break;
					case "docatch":this.onCatch(beiMiClient); break;
					case "giveup":this.onGiveup(beiMiClient); break;
					case "cardtips":this.onCardTips(beiMiClient); break;
					case "doplaycards":this.onPlayCards(beiMiClient); break;
					case "nocards":this.onNoCards(beiMiClient); break;
					case "selectcolor":this.onSelectColor(beiMiClient); break;
					case "selectaction":this.onActionEvent(beiMiClient); break;
					case "restart":this.onRestart(beiMiClient); break;
					case "start":this.onStart(beiMiClient); break;
					case "recovery":this.onRecovery(beiMiClient); break;
					case "leave":this.onLeave(beiMiClient); break;
					case "command":this.onCommand(beiMiClient); break;
					case "searchroom":this.onSearchRoom(beiMiClient); break;
					case "message":this.onMessage(beiMiClient); break;
					
				}
			}
		}
		return null;
	}
	/**
	 * 当客户端发close flag时，会走这个方法
	 */
	@Override
	public Object onClose(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
		Aio.remove(channelContext, "receive close flag");
		BeiMiClient beiMiClient = NettyClients.getInstance().getClient(channelContext.getUserid()) ;
    	if(beiMiClient!=null){
    		/**
    		 * 玩家离线
    		 */
    		PlayUserClient playUserClient = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(beiMiClient.getUserid(), beiMiClient.getOrgi()) ;
    		if(playUserClient!=null){
    			if(BMDataContext.GameStatusEnum.PLAYING.toString().equals(playUserClient.getGamestatus())){
    				GameUtils.updatePlayerClientStatus(beiMiClient.getUserid(), beiMiClient.getOrgi(), BMDataContext.PlayerTypeEnum.OFFLINE.toString());
    			}else{
    				CacheHelper.getApiUserCacheBean().delete(beiMiClient.getUserid(), beiMiClient.getOrgi()) ;
    				if(CacheHelper.getGamePlayerCacheBean().getPlayer(beiMiClient.getUserid(), beiMiClient.getOrgi())!=null){
    					CacheHelper.getGamePlayerCacheBean().delete(beiMiClient.getUserid(), beiMiClient.getOrgi()) ;
    				}
    				CacheHelper.getRoomMappingCacheBean().delete(beiMiClient.getUserid(), beiMiClient.getOrgi()) ;
    				/**
    				 * 玩家退出游戏，需要发送事件给所有玩家，如果房主退出，则房间解散
    				 */
    			}
    			/**
    			 * 退出房间，房卡模式下如果房间还有剩余局数 ， 则不做任何操作，如果无剩余或未开始扣卡，则删除房间
    			 */
    		}
    	}
		
		return null;
	}

    
  //抢地主事件
    public void onJoinRoom(BeiMiClient beiMiClient)  
    {  
    	String token = beiMiClient.getToken();
		if(!StringUtils.isBlank(token)){
			/**
			 * Token不为空，并且，验证Token有效，验证完毕即开始进行游戏撮合，房卡类型的
			 * 1、大厅房间处理
			 *    a、从房间队列里获取最近一条房间信息
			 *    b、将token对应玩家加入到房间
			 *    c、如果房间凑齐了玩家，则将房间从等待撮合队列中移除，放置到游戏中的房间信息，如果未凑齐玩家，继续扔到队列
			 *    d、通知房间的所有人，有新玩家加入
			 *    e、超时处理，增加AI进入房价
			 *    f、事件驱动
			 *    g、定时器处理
			 * 2、房卡房间处理
			 * 	  a、创建房间
			 * 	  b、加入到等待中队列
			 */
			Token userToken ;
			if(beiMiClient!=null && !StringUtils.isBlank(token) && (userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, beiMiClient.getOrgi()))!=null){
				//鉴权完毕
				PlayUserClient userClient = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi()) ;
				beiMiClient.setUserid(userClient.getId());
				/**
				 * 心跳时间
				 */
				beiMiClient.setTime(System.currentTimeMillis());
				NettyClients.getInstance().putClient(userClient.getId(), beiMiClient);
				
				/**
				 * 更新当前玩家状态，在线|离线
				 */
				userClient.setOnline(true);
				
				/**
				 * 更新状态
				 */
				ActionTaskUtils.updatePlayerClientStatus(userClient, BMDataContext.PlayerTypeEnum.NORMAL.toString());
				
				UKTools.published(userClient,BMDataContext.getContext().getBean(PlayUserClientESRepository.class), BMDataContext.getContext().getBean(PlayUserClientRepository.class));
				
				BMDataContext.getGameEngine().gameRequest(userToken.getUserid(), beiMiClient.getPlayway(), beiMiClient.getRoom(), beiMiClient.getOrgi(), userClient , beiMiClient) ;
			}
		}
    }
    
  //抢地主事件
    public void onGameStatus(BeiMiClient beiMiClient) throws IOException  
    {  
    	Token userToken ;
    	GameStatus gameStatus = new GameStatus() ;
    	gameStatus.setGamestatus(BMDataContext.GameStatusEnum.NOTREADY.toString());
		if(beiMiClient!=null && !StringUtils.isBlank(beiMiClient.getToken()) && (userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(beiMiClient.getToken(), beiMiClient.getOrgi()))!=null){
			//鉴权完毕
			PlayUserClient userClient = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi()) ;
			if(userClient!=null){
				gameStatus.setGamestatus(BMDataContext.GameStatusEnum.READY.toString());
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(userClient.getId(), userClient.getOrgi()) ;
				if(!StringUtils.isBlank(roomid) && CacheHelper.getBoardCacheBean().getCacheObject(roomid, userClient.getId())!=null){
					gameStatus.setUserid(userClient.getId());
					gameStatus.setOrgi(userClient.getOrgi());

					GameRoom gameRoom = (GameRoom)CacheHelper.getGameRoomCacheBean().getCacheObject(roomid , userClient.getOrgi()) ;
                    GamePlayway gamePlayway = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(gameRoom.getPlayway(), userClient.getOrgi()) ;
					gameStatus.setGametype(gamePlayway.getCode());
					gameStatus.setPlayway(gamePlayway.getId());
					gameStatus.setGamestatus(BMDataContext.GameStatusEnum.PLAYING.toString());
					if(gameRoom.isCardroom()){
						gameStatus.setCardroom(true);
					}
				}
			}
		}else{
			gameStatus.setGamestatus(BMDataContext.GameStatusEnum.TIMEOUT.toString());
		}
		beiMiClient.sendEvent(BMDataContext.BEIMI_GAMESTATUS_EVENT, gameStatus);
    }
      
    //抢地主事件
    public void onCatch(BeiMiClient beiMiClient)  
    {  
    	String token = beiMiClient.getToken();
		if(!StringUtils.isBlank(token)){
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI) ;
			if(userToken!=null){
				PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi()) ;
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(playUser.getId(), playUser.getOrgi()) ;
				BMDataContext.getGameEngine().actionRequest(roomid, playUser, playUser.getOrgi(), true);
			}
		}
    }
    
    //不抢/叫地主事件
    public void onGiveup(BeiMiClient beiMiClient)  
    {  
    	String token = beiMiClient.getToken();
		if(!StringUtils.isBlank(token)){
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI) ;
			if(userToken!=null){
				PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi()) ;
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(playUser.getId(), playUser.getOrgi()) ;
				BMDataContext.getGameEngine().actionRequest(roomid, playUser, playUser.getOrgi(), false);
			}
		}
    }
    
  //不抢/叫地主事件
    public void onCardTips(BeiMiClient beiMiClient)  
    {  
    	String token = beiMiClient.getToken();
		if(!StringUtils.isBlank(token)){
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI) ;
			if(userToken!=null){
				PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi()) ;
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(playUser.getId(), playUser.getOrgi()) ;
				BMDataContext.getGameEngine().cardTips(roomid, playUser, playUser.getOrgi(), beiMiClient.getData());
			}
		}
    }
    
    
    //出牌
    public void onPlayCards(BeiMiClient beiMiClient)  
    {  
    	String token = beiMiClient.getToken();
		if(!StringUtils.isBlank(token) && !StringUtils.isBlank(beiMiClient.getData())){
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI) ;
			if(userToken!=null){
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi()) ;
				String[] cards = beiMiClient.getData().split(",") ;
				
				byte[] playCards = new byte[cards.length] ;
				for(int i= 0 ; i<cards.length ; i++){
					playCards[i] = Byte.parseByte(cards[i]) ;
				}
				BMDataContext.getGameEngine().takeCardsRequest(roomid, userToken.getUserid(), userToken.getOrgi() , false , playCards);
			}
		}
    }
    
    //出牌
    public void onNoCards(BeiMiClient beiMiClient)  
    {  
    	String token = beiMiClient.getToken();
		if(!StringUtils.isBlank(token)){
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI) ;
			if(userToken!=null){
				PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi()) ;
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(playUser.getId(), playUser.getOrgi()) ;
				BMDataContext.getGameEngine().takeCardsRequest(roomid, userToken.getUserid(), userToken.getOrgi() , false , null);
			}
		}
    }
    
    //出牌
    public void onSelectColor(BeiMiClient beiMiClient)  
    {  
    	String token = beiMiClient.getToken();
		if(!StringUtils.isBlank(token)){
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI) ;
			if(userToken!=null){
				PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi()) ;
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(playUser.getId(), playUser.getOrgi()) ;
				BMDataContext.getGameEngine().selectColorRequest(roomid, playUser.getId(), userToken.getOrgi() , beiMiClient.getData());
			}
		}
    }
    
    //出牌
    public void onActionEvent(BeiMiClient beiMiClient)  
    {  
    	String token = beiMiClient.getToken();
		if(!StringUtils.isBlank(token)){
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI) ;
			if(userToken!=null){
				PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi()) ;
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(playUser.getId(), playUser.getOrgi()) ;
				BMDataContext.getGameEngine().actionEventRequest(roomid, playUser.getId(), userToken.getOrgi() , beiMiClient.getData());
			}
		}
    }
    
    //抢地主事件
    public void onRestart(BeiMiClient beiMiClient)  
    {  
    	String token = beiMiClient.getToken();
		if(!StringUtils.isBlank(token)){
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI) ;
			if(userToken!=null){
				PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi()) ;
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(playUser.getId(), playUser.getOrgi()) ;
				BMDataContext.getGameEngine().restartRequest(roomid, playUser , beiMiClient , "true".equals(beiMiClient.getData()));
			}
		}
    }
    
  //抢地主事件
    public void onStart(BeiMiClient beiMiClient)  
    {  
    	String token = beiMiClient.getToken();
		if(!StringUtils.isBlank(token)){
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI) ;
			if(userToken!=null){
				
				PlayUserClient playUser = (PlayUserClient) CacheHelper.getGamePlayerCacheBean().getPlayer(userToken.getUserid(), userToken.getOrgi()) ;
				if(playUser!=null){
					BMDataContext.getGameEngine().startGameRequest(playUser.getRoomid(), playUser , userToken.getOrgi() , "true".equals(beiMiClient.getData())) ;
				}
			}
		}
    }
    
    //抢地主事件
    public void onRecovery(BeiMiClient beiMiClient)  
    {  
    	String token = beiMiClient.getToken();
		if(!StringUtils.isBlank(token)){
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI) ;
			if(userToken!=null){
				PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi()) ;
				BMDataContext.getGameEngine().gameRequest(playUser.getId(), beiMiClient.getPlayway(), beiMiClient.getRoom(), beiMiClient.getOrgi(), playUser , beiMiClient) ;
			}
		}
    }
    
    //玩家离开
    public void onLeave(BeiMiClient beiMiClient)  
    {  
    	String token = beiMiClient.getToken();
		if(!StringUtils.isBlank(token)){
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI) ;
			if(userToken!=null){
				GameUtils.updatePlayerClientStatus(beiMiClient.getUserid(), beiMiClient.getOrgi(), BMDataContext.PlayerTypeEnum.LEAVE.toString());
			}
		}
    }
    
    //杂七杂八的指令，混合到一起
    public void onCommand(BeiMiClient beiMiClient)  
    {  
    	Command command = JSON.parseObject(beiMiClient.getData() , Command.class) ;
		if(command!=null && !StringUtils.isBlank(command.getToken())){
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(command.getToken(), BMDataContext.SYSTEM_ORGI) ;
			PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi()) ;
			if(userToken!=null){
				switch(command.getCommand()){
					case "subsidy" : GameUtils.subsidyPlayerClient(beiMiClient , playUser, userToken.getOrgi()) ; break ;
				}
			}
		}
    }
    
    //聊天
    public void onMessage(BeiMiClient beiMiClient)  
    {  
    	String token = beiMiClient.getToken();
		if(!StringUtils.isBlank(token)){
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI) ;
			if(userToken!=null){
				GameUtils.updatePlayerClientStatus(beiMiClient.getUserid(), beiMiClient.getOrgi(), BMDataContext.PlayerTypeEnum.LEAVE.toString());
			}
		}
    }
    
    
  //抢地主事件
    public void onSearchRoom(BeiMiClient beiMiClient) throws IOException  
    {  
    	SearchRoom searchRoom = JSON.parseObject(beiMiClient.getData() , SearchRoom.class) ;
    	GamePlayway gamePlayway = null ;
    	SearchRoomResult searchRoomResult = null ;
    	boolean joinRoom = false;
    	if(searchRoom!=null && !StringUtils.isBlank(searchRoom.getUserid())){
    		GameRoomRepository gameRoomRepository = BMDataContext.getContext().getBean(GameRoomRepository.class);
    		PlayUserClient playUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(searchRoom.getUserid(), searchRoom.getOrgi()) ;
			if(playUser!=null){
				GameRoom gameRoom = null ;
				String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(playUser.getId(), playUser.getOrgi()) ;
				if(!StringUtils.isBlank(roomid)){
					gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, playUser.getOrgi()) ;
				}else{
					List<GameRoom> gameRoomList = gameRoomRepository.findByRoomidAndOrgi(searchRoom.getRoomid(), playUser.getOrgi()) ;
					if(gameRoomList!=null && gameRoomList.size() > 0){
						GameRoom tempGameRoom = gameRoomList.get(0) ;
						gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(tempGameRoom.getId(), playUser.getOrgi()) ;
					}
				}
				if(gameRoom!=null){
					/**
					 * 将玩家加入到 房间 中来 ， 加入的时候需要处理当前的 房间 已满员或未满员，如果满员，需要检查是否允许围观
					 */
					gamePlayway = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(gameRoom.getPlayway(), gameRoom.getOrgi()) ;
					List<PlayUserClient> playerList = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi()) ;
					if(playerList.size() < gamePlayway.getPlayers()){
						BMDataContext.getGameEngine().joinRoom(gameRoom, playUser, playerList);
						joinRoom = true ;
					}
					/**
					 * 获取的玩法，将玩法数据发送给当前请求的玩家
					 */
				}
			}
    	}
    	if(gamePlayway!=null){
    		//通知客户端
    		if(joinRoom == true){		//加入成功 ， 是否需要输入加入密码？
    			searchRoomResult = new SearchRoomResult(gamePlayway.getId() , gamePlayway.getCode() , BMDataContext.SearchRoomResultType.OK.toString());
    		}else{						//加入失败
    			searchRoomResult = new SearchRoomResult(BMDataContext.SearchRoomResultType.FULL.toString());
    		}
    	}else{ //房间不存在
    		searchRoomResult = new SearchRoomResult(BMDataContext.SearchRoomResultType.NOTEXIST.toString());
    	}
		
    	beiMiClient.sendEvent(BMDataContext.BEIMI_SEARCHROOM_EVENT, searchRoomResult);
    }

	public GameServer getServer() {
		return server;
	}

	public void setServer(GameServer server) {
		this.server = server;
	}
}  