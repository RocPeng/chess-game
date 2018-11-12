package com.beimi.util.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.beimi.config.web.GameServer;
import com.beimi.core.BMDataContext;
  
@Component  
public class ServerRunner implements CommandLineRunner {  
    private final GameServer server;
    
    @Autowired  
    public ServerRunner(GameServer server) {  
        this.server = server;  
    }
    
    public void run(String... args) throws Exception { 
        server.start();  
        BMDataContext.setIMServerStatus(true);	//IMServer 启动成功
    }  
}  