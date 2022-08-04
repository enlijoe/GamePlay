package com.jgr.game.vac.service;

import org.springframework.beans.factory.annotation.Value;

public class PropertyService {
	// from DoStimThreadImpl
	@Value ("${game.timeMutiple}") private long timeMutiple;
	@Value ("${game.nippleOnTime}") private int nippleOnTime;
	@Value ("${game.probeOnTime}") private int probeOnTime;
	@Value ("${game.vibeOnTime}") private int vibeOnTime;
	@Value ("${game.vibe2OnTime}") private int vibe2OnTime;
	@Value ("${game.stimRestTime}") private int stimRestTime;
	@Value ("${game.numCycles}") private int numCycles;
	@Value ("${game.additionalCycles}") private int additionalCycles;

	// from MaintainVacuumThreadImpl
	@Value ("${game.pumpRunTime}") private long pumpRunTime;
	@Value ("${game.pumpRestTime}") private int pumpRestTime;
	private long minVacuumPressure;
	@Value("${pressure.maxVacuumPressure}") private long maxVacuumPressure;
	@Value("${pressure.vacuumPoolTime}") private long vacuumPoolTime;

	// from RandomStimThreadImpl
	@Value ("${game.randomStimRunTime}") private int stimRunTime;
	@Value ("${game.abortWindows}") private String abortWindowsString;
	
	// from TheTeaseThread
	@Value ("${game.simulate}") private boolean simulate;
	
	// from MainLine
	@Value ("${game.runTime}") private int runTime;
	@Value ("${game.randomTime}") private boolean randomTime;
	@Value ("${game.useVaccume}") private boolean useVaccume;
	@Value ("${game.waterFillRest}") private int fillRestTime;
	@Value ("${game.delayed}") private boolean delayedStart;
	@Value ("${game.waterFill.controled}") private boolean controledFill;
	@Value ("${game.waterFill.onTime}") private int flowOnTime;
	@Value ("${game.waterFill.offTIme}") private int flowOffTime;
	@Value ("${game.waterFill.flowTime}") private int flowTime;
	@Value ("${game.waterFill.initalOnTime}") private int initalOnTime;
	@Value ("${game.waterFillTime}") private int totalOnTime;
	@Value ("${game.fillRestFullFlow}") private int fillRestFullFlow;
	@Value ("${game.doSelfTest}") private boolean doSelfTest;
	@Value("${game.water.preHeatTime}") private int preHeatTime;
	@Value("${game.use.waterHeater}") private boolean userWaterHeater;
	
	// from FillRestTimePollerImpl
	@Value("${game.waterRestHalfStim}") private boolean halfTimeAsStim;
	
	// from SealCompletePollerImpl
	@Value("${game.pumpAutoseal}") private int autoSealTime;
	
	// from StartTimePollerImpl
	@Value("${game.startHour}") private int startHour;
	@Value("${game.smartMin}") private int smartMin;
	@Value("${game.delaySeconds}") private int delaySeconds;
	@Value("${game.allowEarlyStart}") private boolean allowEarlyStart;
	
	// from VaccumPollerImpl

	// from WaterFillPollerImpl
	@Value ("${game.waterFillTime}") private int waterFillTime;
	@Value ("${game.manualControl}") private boolean manualControl;
	@Value ("${pressure.bowelIn.empty}") private long emptyPressure;
	
	@Value("${pressureSensor.maxReadTime}") private long maxPresureReadTime;
	
	public int getNippleOnTime() {
		return nippleOnTime;
	}

	public void setNippleOnTime(int nippleOnTime) {
		this.nippleOnTime = nippleOnTime;
	}

	public int getProbeOnTime() {
		return probeOnTime;
	}

	public void setProbeOnTime(int probeOnTime) {
		this.probeOnTime = probeOnTime;
	}

	public int getVibeOnTime() {
		return vibeOnTime;
	}

	public void setVibeOnTime(int vibeOnTime) {
		this.vibeOnTime = vibeOnTime;
	}

	public int getVibe2OnTime() {
		return vibe2OnTime;
	}

	public void setVibe2OnTime(int vibe2OnTime) {
		this.vibe2OnTime = vibe2OnTime;
	}

	public int getStimRestTime() {
		return stimRestTime;
	}

	public void setStimRestTime(int stimRestTime) {
		this.stimRestTime = stimRestTime;
	}

	public int getNumCycles() {
		return numCycles;
	}

	public void setNumCycles(int numCycles) {
		this.numCycles = numCycles;
	}

	public int getAdditionalCycles() {
		return additionalCycles;
	}

	public void setAdditionalCycles(int additionalCycles) {
		this.additionalCycles = additionalCycles;
	}

	public long getPumpRunTime() {
		return pumpRunTime;
	}

	public void setPumpRunTime(long pumpRunTime) {
		this.pumpRunTime = pumpRunTime;
	}

	public int getPumpRestTime() {
		return pumpRestTime;
	}

	public void setPumpRestTime(int pumpRestTime) {
		this.pumpRestTime = pumpRestTime;
	}

	public long getMinVacuumPressure() {
		return minVacuumPressure;
	}

	public void setMinVacuumPressure(long minVacuumPressure) {
		this.minVacuumPressure = minVacuumPressure;
	}

	public long getMaxVacuumPressure() {
		return maxVacuumPressure;
	}

	public void setMaxVacuumPressure(long maxVacuumPressure) {
		this.maxVacuumPressure = maxVacuumPressure;
	}

	public long getVacuumPoolTime() {
		return vacuumPoolTime;
	}

	public void setVacuumPoolTime(long vacuumPoolTime) {
		this.vacuumPoolTime = vacuumPoolTime;
	}

	public int getStimRunTime() {
		return stimRunTime;
	}

	public void setStimRunTime(int stimRunTime) {
		this.stimRunTime = stimRunTime;
	}

	public String getAbortWindowsString() {
		return abortWindowsString;
	}

	public void setAbortWindowsString(String abortWindowsString) {
		this.abortWindowsString = abortWindowsString;
	}

	public long getTimeMutiple() {
		return timeMutiple;
	}

	public void setTimeMutiple(long timeMutiple) {
		this.timeMutiple = timeMutiple;
	}

	public int getRunTime() {
		return runTime;
	}

	public void setRunTime(int runTime) {
		this.runTime = runTime;
	}

	public boolean isRandomTime() {
		return randomTime;
	}

	public void setRandomTime(boolean randomTime) {
		this.randomTime = randomTime;
	}

	public boolean isUseVaccume() {
		return useVaccume;
	}

	public void setUseVaccume(boolean useVaccume) {
		this.useVaccume = useVaccume;
	}

	public int getFillRestTime() {
		return fillRestTime;
	}

	public void setFillRestTime(int fillRestTime) {
		this.fillRestTime = fillRestTime;
	}

	public boolean isDelayedStart() {
		return delayedStart;
	}

	public void setDelayedStart(boolean delayedStart) {
		this.delayedStart = delayedStart;
	}

	public boolean isControledFill() {
		return controledFill;
	}

	public void setControledFill(boolean controledFill) {
		this.controledFill = controledFill;
	}

	public int getFlowOnTime() {
		return flowOnTime;
	}

	public void setFlowOnTime(int flowOnTime) {
		this.flowOnTime = flowOnTime;
	}

	public int getFlowOffTime() {
		return flowOffTime;
	}

	public void setFlowOffTime(int flowOffTime) {
		this.flowOffTime = flowOffTime;
	}

	public int getFlowTime() {
		return flowTime;
	}

	public void setFlowTime(int flowTime) {
		this.flowTime = flowTime;
	}

	public int getInitalOnTime() {
		return initalOnTime;
	}

	public void setInitalOnTime(int initalOnTime) {
		this.initalOnTime = initalOnTime;
	}

	public int getTotalOnTime() {
		return totalOnTime;
	}

	public void setTotalOnTime(int totalOnTime) {
		this.totalOnTime = totalOnTime;
	}

	public int getFillRestFullFlow() {
		return fillRestFullFlow;
	}

	public void setFillRestFullFlow(int fillRestFullFlow) {
		this.fillRestFullFlow = fillRestFullFlow;
	}

	public boolean isDoSelfTest() {
		return doSelfTest;
	}

	public void setDoSelfTest(boolean doSelfTest) {
		this.doSelfTest = doSelfTest;
	}

	public int getPreHeatTime() {
		return preHeatTime;
	}

	public void setPreHeatTime(int preHeatTime) {
		this.preHeatTime = preHeatTime;
	}

	public boolean isUserWaterHeater() {
		return userWaterHeater;
	}

	public void setUserWaterHeater(boolean userWaterHeater) {
		this.userWaterHeater = userWaterHeater;
	}

	public boolean isSimulate() {
		return simulate;
	}

	public void setSimulate(boolean simulate) {
		this.simulate = simulate;
	}
	
	public boolean isHalfTimeAsStim() {
		return halfTimeAsStim;
	}

	public void setHalfTimeAsStim(boolean halfTimeAsStim) {
		this.halfTimeAsStim = halfTimeAsStim;
	}

	public int getAutoSealTime() {
		return autoSealTime;
	}

	public void setAutoSealTime(int autoSealTime) {
		this.autoSealTime = autoSealTime;
	}

	public int getStartHour() {
		return startHour;
	}

	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}

	public int getSmartMin() {
		return smartMin;
	}

	public void setSmartMin(int smartMin) {
		this.smartMin = smartMin;
	}

	public int getDelaySeconds() {
		return delaySeconds;
	}

	public void setDelaySeconds(int delaySeconds) {
		this.delaySeconds = delaySeconds;
	}

	public boolean isAllowEarlyStart() {
		return allowEarlyStart;
	}

	public void setAllowEarlyStart(boolean allowEarlyStart) {
		this.allowEarlyStart = allowEarlyStart;
	}

	public int getWaterFillTime() {
		return waterFillTime;
	}

	public void setWaterFillTime(int waterFillTime) {
		this.waterFillTime = waterFillTime;
	}

	public boolean isManualControl() {
		return manualControl;
	}

	public void setManualControl(boolean manualControl) {
		this.manualControl = manualControl;
	}

	public long getEmptyPressure() {
		return emptyPressure;
	}

	public void setEmptyPressure(long emptyPressure) {
		this.emptyPressure = emptyPressure;
	}
	
	public long getMaxPresureReadTime() {
		return maxPresureReadTime;
	}

	public void setMaxPresureReadTime(long maxPresureReadTime) {
		this.maxPresureReadTime = maxPresureReadTime;
	}
}
