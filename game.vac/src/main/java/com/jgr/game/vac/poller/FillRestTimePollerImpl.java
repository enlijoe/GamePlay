package com.jgr.game.vac.poller;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jgr.game.vac.interfaces.FillRestTimePoller;
import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.OutputDevice;
import com.jgr.game.vac.interfaces.SystemTime;
import com.jgr.game.vac.service.DeviceMapperService;
import com.jgr.game.vac.service.DeviceUrl;

public class FillRestTimePollerImpl extends Poller implements FillRestTimePoller {
	@Autowired private SystemTime systemTime;
	@Autowired private DeviceMapperService deviceMapperService;
	
	
	@Value("${deviceUrl.status}") private String statusLightUrl;
	@Value("${deviceUrl.nipplesSwitch}") private String nipplesSwitchUrl;

	private InputDevice statusLight;
	private OutputDevice nipplesSwitch;

	private long startTime;
	private boolean stimOn;
	
	@Value("${game.waterRestHalfStim}") boolean halfTimeAsStim;
	@Value("${game.waterFillRest}") long fillRestTime;
	@Value("${game.timeMutiple}") long timeMutiple;
	
	private Logger logger = LoggerFactory.getLogger(FillRestTimePollerImpl.class);

	@PostConstruct
	public void afterPropSet() {
		statusLight = deviceMapperService.getDevice(new DeviceUrl(statusLightUrl));
		nipplesSwitch = deviceMapperService.getDevice(new DeviceUrl(nipplesSwitchUrl));
	}
	
	@Override
	public void init() {
		startTime = systemTime.currentTime();
		stimOn = false;
	}

	@Override
	public boolean doCheck() {
		if(halfTimeAsStim && !stimOn &&  systemTime.currentTime() - startTime > fillRestTime*timeMutiple/2) {
			logger.info("Turned on Stim A");
			stimOn = true;
			nipplesSwitch.setOn();
		}
		return systemTime.currentTime() - startTime > fillRestTime*timeMutiple || statusLight.isOn();
	}

}
