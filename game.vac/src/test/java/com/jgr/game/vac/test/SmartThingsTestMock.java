package com.jgr.game.vac.test;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import com.jgr.game.vac.interfaces.SmartThings;

public class SmartThingsTestMock implements SmartThings {
	
	HashMap<String, Integer> states = new HashMap<String, Integer>(); 
	
	Integer on = 100;
	Integer off = 0;
	
	public void reset() {
		states.clear();
	}
	
	@Override
	public void listAllDevices() {
		// TODO Auto-generated method stub
		
	}
	
	public void setDeviceValue(String id, int value) {
		states.put(id, value);
	}
	
	public int getSwitchState(String id) {
		return states.get(id);
	}

	public void setDeviceState(String id, boolean state) {
		assertTrue(states.containsKey(id));
		if(state) {
			states.put(id, on);
		} else {
			states.put(id, off);
		}
	}

	public boolean isOn(int value) {
		return value != 0;
	}

	public boolean isOff(int value) {
		return value == 0;
	}
}