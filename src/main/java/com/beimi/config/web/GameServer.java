package com.beimi.config.web;

import java.io.IOException;

import org.tio.server.ServerGroupContext;
import org.tio.websocket.server.WsServerStarter;

import com.beimi.util.server.handler.GameEventHandler;

public class GameServer {
	private ServerGroupContext serverGroupContext;
	private WsServerStarter wsServerStarter;

	/**
	 *
	 * @author tanyaowu
	 */
	public GameServer(int port, GameEventHandler wsMsgHandler) throws IOException {
		wsServerStarter = new WsServerStarter(port, wsMsgHandler);
		serverGroupContext = wsServerStarter.getServerGroupContext();
	}

	/**
	 * @return the serverGroupContext
	 */
	public ServerGroupContext getServerGroupContext() {
		return serverGroupContext;
	}

	public WsServerStarter getWsServerStarter() {
		return wsServerStarter;
	}

	public void start() throws IOException {
		wsServerStarter.start();
	}
}
