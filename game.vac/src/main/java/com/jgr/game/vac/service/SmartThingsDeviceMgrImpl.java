package com.jgr.game.vac.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.jgr.game.vac.interfaces.InputDevice;
import com.jgr.game.vac.interfaces.OutputDevice;

public class SmartThingsDeviceMgrImpl implements DeviceManager, BeanNameAware {
	@Value("${smartthings.auth}") private String authToken;
	@Value("${smartthings.url}") private String smartUrl;
	@Value("${smartthings.maxRetrys}") private int maxRetrys;
	
	private String onBody;
	private String offBody;
	private String beanName;

	private Logger logger = LoggerFactory.getLogger(SmartThingsDeviceMgrImpl.class);
	
	public SmartThingsDeviceMgrImpl() throws IOException {
		onBody = StreamUtils.copyToString(this.getClass().getClassLoader().getResourceAsStream("onCommand.json"), Charset.defaultCharset());;
		offBody = StreamUtils.copyToString(this.getClass().getClassLoader().getResourceAsStream("offCommand.json"), Charset.defaultCharset());;
	}
	
	HashMap<String, SmartThingsDevice> deviceMap = new HashMap<>();
	
	private class SmartThingsDevice implements InputDevice, OutputDevice {
		String deviceId;
		
		@Override
		public DeviceManager getOwner() {
			return SmartThingsDeviceMgrImpl.this;
		}
		
		SmartThingsDevice(String deviceId) {
			this.deviceId = deviceId;
		}
		
		@Override
		public int getValue() {
			logger.info("Get Value called for " + deviceId);
			return getSwitchState(deviceId);
		}
		
		@Override
		public boolean isOff() {
			return getSwitchState(deviceId) == 0;
		}
		
		@Override
		public boolean isOn() {
			return getSwitchState(deviceId) != 0;
		}
		
		@Override
		public void setOff() {
			logger.info("set off called for " + deviceId);
			setDeviceState(deviceId, false);
		}
		
		@Override
		public void setOn() {
			logger.info("set on called for " + deviceId);
			setDeviceState(deviceId, true);
		}
		
		@Override
		public void setValue(int value) {
			logger.info("set value " + value + " called for " + deviceId);
			setDeviceState(deviceId, value!=0);
		}
	}
	
	@Override
	public boolean isDeviceLogging() {
		return false;
	}
	
	@Override
	public void setDeviceLogging(boolean value) {
		logger.info("The Smart things device does not support logging");
	}
	
	@Override
	public void setBeanName(String name) {
		this.beanName = name; 
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <DeviceType> DeviceType getDevice(DeviceUrl deviceUrl) {
		SmartThingsDevice device = null;
		if(beanName.equalsIgnoreCase(deviceUrl.getDeviceManagerClass())) {
			String deviceId = deviceUrl.getDeviceId();
			if(StringUtils.isEmpty(deviceId)) { 
				logger.error("Device url error (" + deviceUrl.toString() + ") device id is required");
				return null;
			}
			device = deviceMap.get(deviceId);
			if(device == null) {
				synchronized (deviceMap) {
					device = deviceMap.get(deviceId);
					if(device == null) {
						device = new SmartThingsDevice(deviceId);
						deviceMap.put(deviceId, device);
					}
				}
			}
		}
		return (DeviceType) device;
	}
	
	public void listAllDevices() {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(authToken);
		
		HttpEntity<String> entity = new HttpEntity<>("body", headers);
		String url = smartUrl + "/devices/";
		logger.debug("Calling Url " + url);
		ResponseEntity<String> responce = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
		
		logger.error("Device List " + responce.getBody());
	}
	

	boolean fake = false;
	
	@SuppressWarnings("rawtypes")
	public int getSwitchState(String id) {
		Exception lastEx;
		int retrys = 0;
		do {
			try {
				Gson gson = new Gson();
			
				if(fake) throw new RuntimeException();
				
				RestTemplate restTemplate = new RestTemplate();
				HttpHeaders headers = new HttpHeaders();
				headers.setBearerAuth(authToken);
				
				HttpEntity<String> entity = new HttpEntity<>("body", headers);
				String url = smartUrl + "/devices/" + id + "/status";
				logger.debug("Calling Url " + url);
				ResponseEntity<String> responce = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
				
				Map data = gson.fromJson(responce.getBody(), Map.class);
				Map components = getAsMap(data,"components");
				Map main = getAsMap(components, "main");
				Map switch1Map = getAsMap(main, "switch");
				if(switch1Map == null) {
					listAllDevices();
					logger.error("(" + id + ")Unable to get switch entry in " + responce.getBody());
				}
				Map switch2Map = getAsMap(switch1Map, "switch");
				
				String value = (String) switch2Map.get("value");
				Double level;
				// logger.info("The Switch is " + value);

				if(main.containsKey("switchLevel")) {
					Map switchLevel = getAsMap(main, "switchLevel");
					Map levelMap = getAsMap(switchLevel, "level");
					level = (Double) levelMap.get("value");
					// logger.info("Switch has level of " + level);
					return level.intValue();
				} else if(value.equalsIgnoreCase("on")) {
					return 100;
				} else {
					return 0;
				}

			} catch(Exception ex) {
				lastEx = ex;
				retrys++;
			}
		} while(retrys < maxRetrys);
		logger.error("Max retries reached", lastEx);
		throw new RuntimeException(lastEx);
	}

	@SuppressWarnings("rawtypes")
	private Map getAsMap(Map data, String index) {
    	return (Map) data.get(index);
    }
	
	public void setDeviceState(String id, boolean state) {
		
		Exception lastEx;
		int retrys = 0;
		do {
			try {
				RestTemplate restTemplate = new RestTemplate();
				HttpHeaders headers = new HttpHeaders();
				headers.setBearerAuth(authToken);
				HttpEntity<String> entity = new HttpEntity<>(state?onBody:offBody, headers);
				String url = smartUrl + "/devices/" + id + "/commands";
				logger.debug("Calling url " + url);
				
				ResponseEntity<String> responce = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
				displayRateLimit("Command", responce);
				return;
			} catch(Exception ex) {
				lastEx = ex;
				retrys++;
			}
		} while(retrys < maxRetrys);
		if(lastEx != null) {
			logger.error("Max retries reached", lastEx);
			throw new RuntimeException(lastEx); 
		}
	}
	
	public boolean isOn(int value) {
		// logger.info("is on level " + value + " state " + (value != 0));
		return value != 0;
	}

	public boolean isOff(int value) {
		// logger.info("is on level " + value + " state " + (value != 0));
		return value == 0;
	}

	void displayRateLimit(String desc , ResponseEntity<String> responce) {
//		int xRateMax = Integer.parseInt(responce.getHeaders().get("X-RateLimit-Limit").get(0));
//		int xRateLimit = Integer.parseInt(responce.getHeaders().get("x-ratelimit-remaining").get(0));
//		int xRateReset = Integer.parseInt(responce.getHeaders().get("x-ratelimit-reset").get(0));
		// logger.info(desc + " max " + xRateMax + " remaining " + xRateLimit + " reset "+ xRateReset);
	}
}
