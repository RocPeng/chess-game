package com.beimi.core.engine.game;

public interface Message extends java.io.Serializable{
	/**
	 * 发送到客户端的指令
	 * @return
	 */
	public String getCommand() ; 
	
	/**
	 * 指令
	 * @param command
	 */
	public void setCommand(String command) ;
	
	
	/**
	 * 发送到客户端的指令
	 * @return
	 */
	public String getEvent() ; 
	
	/**
	 * 指令
	 * @param command
	 */
	public void setEvent(String event) ;
}
