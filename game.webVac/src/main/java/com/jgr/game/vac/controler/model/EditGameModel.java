package com.jgr.game.vac.controler.model;

import java.util.List;

import com.jgr.game.vac.service.dto.GameSettingInfoDto;

public class EditGameModel {
	Long gameId;
	String gameName;
	String gameBeanName;
	String parentBeanName;
	List<GameSettingInfoDto> gameSetup;
	
	public EditGameModel(Long gameId, String gameName, String gameBeanName, String parentBeanName, List<GameSettingInfoDto> gameSetup) {
		this.gameId = gameId;
		this.gameName = gameName;
		this.gameBeanName = gameBeanName;
		this.parentBeanName = parentBeanName;
		this.gameSetup = gameSetup;
	}
	public Long getGameId() {
		return gameId;
	}
	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}
	public String getGameName() {
		return gameName;
	}
	public void setGameName(String gameName) {
		this.gameName = gameName;
	}
	public String getGameBeanName() {
		return gameBeanName;
	}
	public void setGameBeanName(String gameBeanName) {
		this.gameBeanName = gameBeanName;
	}
	public String getParentBeanName() {
		return parentBeanName;
	}
	public void setParentBeanName(String parentBeanName) {
		this.parentBeanName = parentBeanName;
	}
	public List<GameSettingInfoDto> getGameSetup() {
		return gameSetup;
	}
	public void setGameSetup(List<GameSettingInfoDto> gameSetup) {
		this.gameSetup = gameSetup;
	}
}
