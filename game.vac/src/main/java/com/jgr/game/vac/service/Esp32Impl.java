package com.jgr.game.vac.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.jgr.game.vac.interfaces.Esp32;

public class Esp32Impl implements Esp32 {
	@Value("${esp32.uri}") private String esp32Uri;

	private Logger logger = LoggerFactory.getLogger(Esp32Impl.class);
	
	@Override
	public void turnOnValve() {
		try {
			synchronized(this) {
				logger.info("Safty Valve on");
				RestTemplate restTemplate = new RestTemplate();
				HttpHeaders headers = new HttpHeaders();
				HttpEntity<String> entity = new HttpEntity<>("{ \"value\":\"on\"}", headers);
				restTemplate.exchange(esp32Uri + "/update", HttpMethod.POST, entity, String.class);
			}
		} catch (Exception ex) {
			logErrMsg("Unable to contact the valve to turn on", ex);
		}
	}
	
	@Override
	public void turnOffValve() {
		try {
			synchronized(this) {
				logger.info("Safty Valve off");
				RestTemplate restTemplate = new RestTemplate();
				HttpHeaders headers = new HttpHeaders();
				HttpEntity<String> entity = new HttpEntity<>("{ \"value\":\"off\"}", headers);
				restTemplate.exchange(esp32Uri + "/update", HttpMethod.POST, entity, String.class);
			}
		} catch (Exception ex) {
			logErrMsg("Unable to contact the valve to turn off", ex);
		}
	}
	
	@Override
	public boolean getValveStatus() {
		try {
			synchronized(this) {
				Gson gson = new Gson();
				RestTemplate restTemplate = new RestTemplate();
				HttpHeaders headers = new HttpHeaders();
				HttpEntity<String> entity = new HttpEntity<>("body", headers);
				ResponseEntity<String> responce = restTemplate.exchange(esp32Uri + "/status", HttpMethod.GET, entity, String.class);
				@SuppressWarnings("rawtypes")
				Map data = gson.fromJson(responce.getBody(), Map.class);
				String value = (String) data.get("value");
				return "on".equalsIgnoreCase(value); 
			}
		} catch(Exception ex) {
			logErrMsg("Unable to contact valve to check status", ex);
			return false;
		}
	}
	
	private void logErrMsg(String message, Exception ex) {
		if(ex instanceof ResourceAccessException && ex.getMessage().contains("timed out")) {
			logger.warn("***************** " + message + " timed out *****************");
			
		} else {
			logger.warn(message, ex);
		}
		
	}
	
}
