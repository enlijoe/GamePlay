package com.jgr.game.vac.service.esp32;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgr.game.vac.service.InputDevice;
import com.jgr.game.vac.service.OutputDevice;
import com.jgr.game.vac.service.PressureDevice;
import com.jgr.game.vac.service.RemoteWatchDog;

class ESP32Device implements RemoteWatchDog {
	private static Logger logger = LoggerFactory.getLogger(GenericEsp32DeviceFactory.class);

	private String UUID;
	private String type;
	private int version;
	private int loggingPort;
	private int controlPort;
	private InetAddress address;
	
	LoggingThread loggingThread;
	ControlThread controlThread;
	HashMap<String, Object> deviceMap = new HashMap<String, Object>();
	
	public HashMap<String, Object> getDeviceMap() {
		return deviceMap;
	}
	
	private class ESP32SubDevice {
		private char type;
		private int id;
		private String name;
		String beanName;
		
		public void setBeanName(String name) {
			beanName = name;
		}
		
		public String getBeanName() {
			return beanName;
		}
		
		void init(char type, int id, String name) {
			this.type = type;
			this.id = id;
			this.name = name;
		}
		
		Map<String, Object> read() {
			return controlThread.sendCommand("R" + type + id);
		}
		
		Map<String, Object> write(int value) {
			return controlThread.sendCommand("W" + type + id + value);
		}
		
		Map<String, Object> status() {
			return controlThread.sendCommand("S" + type + id);
		}
		
		Map<String, Object> info() {
			return controlThread.sendCommand("I" + type + id);
		}
		
		public String getName() {
			return name;
		}
	}
	
	void handleError(String msg, Map<String, Object> data) {
		String errMsg = "";
		if(data.containsKey("error")) {
			msg = data.get("error").toString();
			throw new RuntimeException(msg + " - " + errMsg);
		} else {
			throw new RuntimeException(msg);
		}
	}
	
	class ESP32InputDevice extends ESP32SubDevice implements InputDevice {
		@Override
		public int readValue() {
			Map<String, Object> data = read();
			if(data.containsKey("value")) {
				return Integer.parseInt(data.get("value").toString());
			} else {
				handleError("Unable to read value", data);
			}
			return 0; // dead code
		}
	}
	
	class ESP32OutputDevice extends ESP32SubDevice implements OutputDevice {
		@Override
		public void setValue(int value) {
			Map<String, Object> data = write(value);
			if(!data.containsKey("status")) {
				handleError("Unable to write value", data);
			}
		}

		@Override
		public boolean isWatchDogControled() {
			return true;
		}

		@Override
		public void setDefaultState() {
			// TODO Auto-generated method stub
		}
	}
	
	class ESP32PressureDevice extends ESP32SubDevice implements PressureDevice {
		float minValue;
		float maxValue;

		ESP32PressureDevice(float minValue, float maxValue) {
			this.minValue = minValue;
			this.maxValue = maxValue;
		}
		
		@Override
		public float getMinValue() {
			return minValue;
		}

		@Override
		public float getMaxValue() {
			return maxValue;
		}

		@Override
		public float getValue() {
			Map<String, Object> data = read();
			if(data.containsKey("value")) {
				int value = Integer.parseInt(data.get("value").toString());
				return (((float) value / (float) 0x3fff) * (maxValue - minValue)) + minValue;
			} else {
				handleError("Unable to read value", data);
				return 0;
			}
		}
	}

	public void start() {
		loggingThread = new LoggingThread(this);
		controlThread = new ControlThread(this);
		 
		//loggingThread.start();
		
		try {
			controlThread.open();
		} catch (IOException ex) {
			logger.error("Error opening control thread.", ex);
		}
		
		// get the list of devices
		Map<String, Object> data = controlThread.sendCommand("SC0");
		int numInput = getNumSubDevices("inputs", data);
		// logger.info("There are " + numInput + " input devices");
		for(int pos = 0; pos < numInput; pos++) {
			Map<String, Object> subDevData = controlThread.sendCommand("II"+pos);
			if(subDevData.containsKey("name")) {
				String name = subDevData.get("name").toString();
				ESP32InputDevice subDevice = new ESP32InputDevice();
				subDevice.init('I', pos, name);
				subDevice.setBeanName(name);
				deviceMap.put(name, subDevice);
			}
		}
		
		int numOutput = getNumSubDevices("outputs", data);
		// logger.info("There are " + numOutput + " output devices");
		for(int pos = 0; pos < numOutput; pos++) {
			Map<String, Object> subDevData = controlThread.sendCommand("IO"+pos);
			if(subDevData.containsKey("name")) {
				String name = subDevData.get("name").toString();
				ESP32OutputDevice subDevice = new ESP32OutputDevice();
				subDevice.init('O', pos, name);
				subDevice.setBeanName(name);
				deviceMap.put(name, subDevice);
			}
		}
		
		int numPressure = getNumSubDevices("pressure", data);
		// logger.info("There are " + numPressure + " pressure devices");
		for(int pos = 0; pos < numPressure; pos++) {
			Map<String, Object> subDevData = controlThread.sendCommand("IP"+pos);
			if(subDevData.containsKey("name")) {
				String name = subDevData.get("name").toString();
				float minValue = Float.parseFloat(subDevData.get("min").toString());
				float maxValue = Float.parseFloat(subDevData.get("max").toString());
				ESP32PressureDevice subDevice = new ESP32PressureDevice(minValue, maxValue);
				subDevice.setBeanName(name);
				subDevice.init('P', pos, name);
				deviceMap.put(name, subDevice);
			}
		}
		
	}
	
	
	
	@Override
	public void checkIn() {
		// TODO Auto-generated method stub
	}

	@Override
	public void enable() {
		// TODO Auto-generated method stub
	}

	@Override
	public int getCheckInFrequency() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void disable() {
		// TODO Auto-generated method stub
	}

	@Override
	public void enterErroredState() {
		// TODO Auto-generated method stub
	}

	@Override
	public void resetState() {
		// TODO Auto-generated method stub
	}

	private int getNumSubDevices(String type, Map<String, Object> data) {
		if(data.containsKey(type)) {
			return Integer.parseInt(data.get(type).toString());
		}
		return 0;
	}
	
	public String getUUID() {
		return UUID;
	}
	public void setUUID(String uUID) {
		UUID = uUID;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public int getLoggingPort() {
		return loggingPort;
	}
	public void setLoggingPort(int loggingPort) {
		this.loggingPort = loggingPort;
	}
	public int getControlPort() {
		return controlPort;
	}
	public void setControlPort(int controlPort) {
		this.controlPort = controlPort;
	}
	public InetAddress getAddress() {
		return address;
	}
	public void setAddress(InetAddress address) {
		this.address = address;
	}
	
	
}
