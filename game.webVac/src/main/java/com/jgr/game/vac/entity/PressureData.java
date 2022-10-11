package com.jgr.game.vac.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class PressureData {
	
	@EmbeddedId
	private PressureDataPk pk;
	
	@Column(name="reading", nullable = false)
	private Float reading;

	public PressureData() {
		
	}
	
	public PressureData(PressureDataPk pk, Float reading) {
		this.pk = pk;
		this.reading = reading;
	}
	
	public PressureDataPk getPk() {
		return pk;
	}

	public void setId(PressureDataPk pk) {
		this.pk = pk;
	}

	public Float getReading() {
		return reading;
	}

	public void setReading(Float reading) {
		this.reading = reading;
	}
	
	
}
