package com.beimi.util.rules.model;

import com.beimi.core.engine.game.Message;

public class BoardRatio implements Message{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean bomb ;
	private int ratio ;
	private boolean king ;//王炸
	
	private String command ;
	private String event ;
	
	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public BoardRatio(boolean bomb ,boolean king , int ratio){
		this.bomb = bomb ;
		this.ratio = ratio ;
		this.king = king ;
	}
	
	public BoardRatio(int ratio){
		this.ratio = ratio ;
	}

	public boolean isBomb() {
		return bomb;
	}

	public void setBomb(boolean bomb) {
		this.bomb = bomb;
	}

	public int getRatio() {
		return ratio;
	}

	public void setRatio(int ratio) {
		this.ratio = ratio;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public boolean isKing() {
		return king;
	}

	public void setKing(boolean king) {
		this.king = king;
	}
	
}
