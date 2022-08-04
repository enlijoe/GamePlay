package com.jgr.game.vac.service;

import org.springframework.beans.factory.annotation.Value;

public class DeviceNames {
	@Value("${smartthings.switch.nipples}") private String nipplesSwitch;
	@Value("${smartthings.switch.probe}") private String probeSwitch;
	@Value("${smartthings.switch.vibe}") private String vibeSwitch;
	@Value ("${smartthings.switch.vibe2}") private String vibe2Switch;
	@Value("${smartthings.switch.pump}") private String pumpSwitch;
	@Value("${smartthings.switch.waterValve}") private String waterValve;
	@Value("${smartthings.switch.status}") private String statusLight;
	@Value("${smartthings.switch.pumpCheck}") private String pumpCheck;
	@Value("${smartthings.switch.waterHeater}") private String waterHeater;
	@Value ("${smartthings.bedroom.light}") private String bedroomLight;
	
	public DeviceNames() {
	}

	public String getNipplesSwitch() {
		return nipplesSwitch;
	}

	public void setNipplesSwitch(String nipplesSwitch) {
		this.nipplesSwitch = nipplesSwitch;
	}

	public String getProbeSwitch() {
		return probeSwitch;
	}

	public void setProbeSwitch(String probeSwitch) {
		this.probeSwitch = probeSwitch;
	}

	public String getVibeSwitch() {
		return vibeSwitch;
	}

	public void setVibeSwitch(String vibeSwitch) {
		this.vibeSwitch = vibeSwitch;
	}

	public String getPumpSwitch() {
		return pumpSwitch;
	}

	public void setPumpSwitch(String pumpSwitch) {
		this.pumpSwitch = pumpSwitch;
	}

	public String getWaterValve() {
		return waterValve;
	}

	public void setWaterValve(String waterValve) {
		this.waterValve = waterValve;
	}

	public String getStatusLight() {
		return statusLight;
	}

	public void setStatusLight(String statusLight) {
		this.statusLight = statusLight;
	}

	public String getPumpCheck() {
		return pumpCheck;
	}

	public void setPumpCheck(String pumpCheck) {
		this.pumpCheck = pumpCheck;
	}

	public String getWaterHeater() {
		return waterHeater;
	}

	public void setWaterHeater(String waterHeater) {
		this.waterHeater = waterHeater;
	}

	public String getVibe2Switch() {
		return vibe2Switch;
	}

	public void setVibe2Switch(String vibe2Switch) {
		this.vibe2Switch = vibe2Switch;
	}

	public String getBedroomLight() {
		return bedroomLight;
	}

	public void setBedroomLight(String bedroomLight) {
		this.bedroomLight = bedroomLight;
	}
	
	
}