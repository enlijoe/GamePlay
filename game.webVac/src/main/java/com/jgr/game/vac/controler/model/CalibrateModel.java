package com.jgr.game.vac.controler.model;

public class CalibrateModel {
	private String insideDevice;
	private String outsideDevice;
	private int startingVolume;
	private int volumeFlowed;
	private int tubeDiameter;
	private int graphWidth;
	private float averageFlowRate;
	
	private float waterInsideStoppingPressure;
	private float waterInsideStartingPressure;
	private float waterInsideMaxPressure;
	private float waterInsideMinPressure;
	private float waterInsideCurrentPressure;
	private float waterOutsideMaxPressure;
	private float waterOutsideMinPressure;
	private float waterOutsideCurrentPressure;
	private float waterOutsideStartPressure;

	/* these are not connected right now
	private float insideMaxPressure;
	private float insideMinPressure;
	private float insideCurrentPressure;
	private float outsideMaxPressure;
	private float outsideMinPressure;
	private float outsideCurrentPressure;
	*/
	private int timeRunning;

	public int getStartingVolume() {
		return startingVolume;
	}

	public void setStartingVolume(int startingVolume) {
		this.startingVolume = startingVolume;
	}

	public int getVolumeFlowed() {
		return volumeFlowed;
	}

	public void setVolumeFlowed(int volumeFlowed) {
		this.volumeFlowed = volumeFlowed;
	}

	public int getTubeDiameter() {
		return tubeDiameter;
	}

	public void setTubeDiameter(int tubeDiameter) {
		this.tubeDiameter = tubeDiameter;
	}

	public float getAverageFlowRate() {
		return averageFlowRate;
	}

	public void setAverageFlowRate(float averageFlowRate) {
		this.averageFlowRate = averageFlowRate;
	}

	public float getWaterInsideStoppingPressure() {
		return waterInsideStoppingPressure;
	}

	public void setWaterInsideStoppingPressure(float waterInsideStoppingPressure) {
		this.waterInsideStoppingPressure = waterInsideStoppingPressure;
	}

	public float getWaterInsideStartingPressure() {
		return waterInsideStartingPressure;
	}

	public void setWaterInsideStartingPressure(float waterInsideStartingPressure) {
		this.waterInsideStartingPressure = waterInsideStartingPressure;
	}

	public float getWaterInsideMaxPressure() {
		return waterInsideMaxPressure;
	}

	public void setWaterInsideMaxPressure(float waterInsideMaxPressure) {
		this.waterInsideMaxPressure = waterInsideMaxPressure;
	}

	public float getWaterInsideMinPressure() {
		return waterInsideMinPressure;
	}

	public void setWaterInsideMinPressure(float waterInsideMinPressure) {
		this.waterInsideMinPressure = waterInsideMinPressure;
	}

	public float getWaterInsideCurrentPressure() {
		return waterInsideCurrentPressure;
	}

	public void setWaterInsideCurrentPressure(float waterInsideCurrentPressure) {
		this.waterInsideCurrentPressure = waterInsideCurrentPressure;
	}

	public float getWaterOutsideMaxPressure() {
		return waterOutsideMaxPressure;
	}

	public void setWaterOutsideMaxPressure(float waterOutsideMaxPressure) {
		this.waterOutsideMaxPressure = waterOutsideMaxPressure;
	}

	public float getWaterOutsideMinPressure() {
		return waterOutsideMinPressure;
	}

	public void setWaterOutsideMinPressure(float waterOutsideMinPressure) {
		this.waterOutsideMinPressure = waterOutsideMinPressure;
	}

	public float getWaterOutsideCurrentPressure() {
		return waterOutsideCurrentPressure;
	}

	public void setWaterOutsideCurrentPressure(float waterOutsideCurrentPressure) {
		this.waterOutsideCurrentPressure = waterOutsideCurrentPressure;
	}

	public float getWaterOutsideStartPressure() {
		return waterOutsideStartPressure;
	}

	public void setWaterOutsideStartPressure(float waterOutsideStartPressure) {
		this.waterOutsideStartPressure = waterOutsideStartPressure;
	}

	public int getTimeRunning() {
		return timeRunning;
	}

	public void setTimeRunning(int timeRunning) {
		this.timeRunning = timeRunning;
	}

	public int getGraphWidth() {
		return graphWidth;
	}

	public void setGraphWidth(int graphWidth) {
		this.graphWidth = graphWidth;
	}

	public String getInsideDevice() {
		return insideDevice;
	}

	public void setInsideDevice(String insideDevice) {
		this.insideDevice = insideDevice;
	}

	public String getOutsideDevice() {
		return outsideDevice;
	}

	public void setOutsideDevice(String outsideDevice) {
		this.outsideDevice = outsideDevice;
	}
	
	
	
}
