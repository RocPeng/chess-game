package com.beimi.config.web;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.beimi.core.BMDataContext;
import com.beimi.util.server.handler.GameEventHandler;
  
@org.springframework.context.annotation.Configuration  
public class GameServerConfiguration  
{  	
	@Value("${uk.im.server.host}")  
    private String host;  
  
    @Value("${uk.im.server.port}")  
    private Integer port;
    
    @Value("${web.upload-path}")
    private String path;
    
    @Value("${uk.im.server.threads}")
    private String threads;
    
    private GameEventHandler handler = new GameEventHandler();
    
    @Bean(name="webimport") 
    public Integer getWebIMPort() {   
    	BMDataContext.setWebIMPort(port);
    	return port;   
    }  
    
    @Bean  
    public GameServer socketIOServer() throws NoSuchAlgorithmException, IOException{  
    	GameServer server = new GameServer(port , handler) ;
    	handler.setServer(server);
        return server;  
    }
}  