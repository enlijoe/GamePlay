package com.jgr.game.vac.service.dto;

public class GameSettingInfoDto {
	private String name;
	private String type;
	private String[] listValues;
	private String currentValue;
	
	
	public GameSettingInfoDto(String name, String type, String[] listValues, String currentValue) {
		this.name = name;
		this.type = type;
		this.listValues = listValues;
		this.currentValue = currentValue;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String[] getListValues() {
		return listValues;
	}
	public void setListValues(String[] listValues) {
		this.listValues = listValues;
	}

	public String getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(String currentValue) {
		this.currentValue = currentValue;
	}
	
	
}
