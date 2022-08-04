package com.jgr.game.vac.service.smartthings;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.jgr.game.vac.service.InputDevice;
import com.jgr.game.vac.service.OutputDevice;

@Service
public class GenericSmartThingsDeviceFactory implements ApplicationContextAware {
	private static Logger logger = LoggerFactory.getLogger(GenericSmartThingsDeviceFactory.class);

	@Value("${smartthings.auth}") private String authToken;
	@Value("${smartthings.url}") private String smartUrl;
	@Value("${smartthings.maxRetrys}") private int maxRetrys;
	@Value("${smartthings.deviceId}") private String[] deviceIds;
	
	private String onBody = "{\"commands\": [{\"component\": \"main\",\"capability\": \"switch\",\"command\": \"on\",\"arguments\": []}]}";
	private String offBody = "{\"commands\": [{\"component\": \"main\",\"capability\": \"switch\",\"command\": \"off\",\"arguments\": []}]}";
	private boolean fake = false;
	
	ConfigurableListableBeanFactory  beanFactory;
	
	HashMap<String, Object> deviceList = new HashMap<>();
	
	private class SmartDevice implements InputDevice, OutputDevice {
		String deviceId;
		String name;
		String beanName;
		
		@Override
		public void setBeanName(String name) {
			beanName = name;
		}
		
		@Override
		public String getBeanName() {
			return beanName;
		}
		
		SmartDevice(String deviceId, String name) {
			this.deviceId = deviceId;
			this.name = name;
		}
		
		@Override
		public void setValue(int value) {
			setDeviceState(deviceId, value != 0);			
		}

		@Override
		public int readValue() {
			return getSwitchState(deviceId);
		}

		@Override
		public boolean isWatchDogControled() {
			return false;
		}

		@Override
		public void setDefaultState() {
			setValue(0);
		}
		
		@Override
		public String getName() {
			return name;
		}
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if(applicationContext instanceof GenericApplicationContext) {
			beanFactory = ((GenericApplicationContext)applicationContext).getBeanFactory();
		}
	}

	@PostConstruct
	public void init() {
		for(String deviceId:deviceIds) {
			// TODO query the device to get it's name to use for the bean name
			SmartDevice device = new SmartDevice(deviceId, deviceId);
			deviceList.put(device.name, device);
			beanFactory.registerSingleton(device.name, device);
			
		}
		logger.info("Smart Things init complete");
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
				
				restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
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
	
}
