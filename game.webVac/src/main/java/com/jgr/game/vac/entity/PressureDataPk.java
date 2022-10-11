package com.jgr.game.vac.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class PressureDataPk implements Serializable {
	private static final long serialVersionUID = -6477084795316683567L;

	@Column(name="entryTime", nullable = false)
	private Long entryTime;
	
	@Column(name="sensorId", nullable = false)
	
	private String sensorId;

	public PressureDataPk() {
		
	}
	
	public PressureDataPk(Long entryTime, String sensorId) {
		this.entryTime = entryTime;
		this.sensorId = sensorId;
	}
	
	public Long getEntryTime() {
		return entryTime;
	}

	public void setEntryTime(Long entryTime) {
		this.entryTime = entryTime;
	}

	public String getSensorId() {
		return sensorId;
	}

	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}
}
