package com.jgr.game.vac.service.esp32;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Value;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.jgr.game.vac.service.DeviceManager;
import com.jgr.game.vac.service.DeviceUrl;
import com.jgr.game.vac.service.RemoteWatchDog;


public class GenericEsp32DeviceFactory implements DeviceManager, BeanNameAware, RemoteWatchDog {
	private static Logger logger = LoggerFactory.getLogger(GenericEsp32DeviceFactory.class);
	private static final String UDP_QUERY_MSG = "Device Query";

	@Value("${esp32.udp.socket}") private int updSocket;
	@Value("${esp32.udp.timeout}") private int udpTimeout;
	@Value("${esp32.udp.interface:}") private InetAddress[] broadcastInterfaces;
	@Value("${esp32.udp.retries:3}") private int udpRetries;
	
	HashMap<String, ESP32Device> deviceList;
	private String beanName;
	
	
	@Override
	public void checkIn() {
		for(ESP32Device device:deviceList.values()) {
			if(device.getClass().isInstance(RemoteWatchDog.class)) {
				RemoteWatchDog remoteWatchDog = (RemoteWatchDog) device;
				remoteWatchDog.checkIn();
			}
		}
	}
	
	@Override
	public String getDescription() {
		return "GenericEsp32DeviceFactory";
	}
	
	@Override
	public boolean getStatus() {
		for(ESP32Device device:deviceList.values()) {
			if(device.getClass().isInstance(RemoteWatchDog.class)) {
				RemoteWatchDog remoteWatchDog = (RemoteWatchDog) device;
				if(remoteWatchDog.getStatus()) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void reset() {
		for(ESP32Device device:deviceList.values()) {
			if(device.getClass().isInstance(RemoteWatchDog.class)) {
				RemoteWatchDog remoteWatchDog = (RemoteWatchDog) device;
				remoteWatchDog.reset();
			}
		}
	}
	
	@Override
	public void disable() {
		for(ESP32Device device:deviceList.values()) {
			if(device.getClass().isInstance(RemoteWatchDog.class)) {
				RemoteWatchDog remoteWatchDog = (RemoteWatchDog) device;
				remoteWatchDog.disable();
			}
		}
	}
	
	@Override
	public void enable() {
		for(ESP32Device device:deviceList.values()) {
			if(device.getClass().isInstance(RemoteWatchDog.class)) {
				RemoteWatchDog remoteWatchDog = (RemoteWatchDog) device;
				remoteWatchDog.enable();
			}
		}
	}
	
	@Override
	public void errorState() {
		for(ESP32Device device:deviceList.values()) {
			if(device.getClass().isInstance(RemoteWatchDog.class)) {
				RemoteWatchDog remoteWatchDog = (RemoteWatchDog) device;
				remoteWatchDog.errorState();
			}
		}
	}
	
	
	/**
	 * Url is beanName://esp32-UUID/device-UUID
	 */
	@Override
	public <DeviceType> DeviceType getDevice(DeviceUrl deviceUrl) {
		if(beanName.equalsIgnoreCase(deviceUrl.getDeviceManagerClass())) {
			ESP32Device subDeviceManager = deviceList.get(deviceUrl.getDeviceId());
			if(subDeviceManager == null) {
				logger.error("unable to find device manager for " + deviceUrl.getDeviceId());
			} else {
				return subDeviceManager.getDevice(deviceUrl);
			}
		}
		return null;
	}
	
	@Override
	public void setBeanName(String name) {
		beanName = name;
	}
	
	@PostConstruct
	public void init() {
		deviceList = new HashMap<>();
		Gson gson = new Gson();
		byte[] buffer = new byte[1024];
		DatagramSocket querySocket = null;
		try {
			List<InterfaceAddress> networkList = new ArrayList<>();
			querySocket = new DatagramSocket(updSocket);

			if(broadcastInterfaces != null && broadcastInterfaces.length != 0) {
				for(InetAddress addr:broadcastInterfaces) {
					NetworkInterface networkInterface = NetworkInterface.getByInetAddress(addr);

					if(networkInterface == null) {
						logger.warn("Ignoring interface on " + addr);
						continue;
					}
					
					if(networkInterface.isLoopback()) {
						// logger.info("Skipping interface " + networkInterface.getDisplayName() + " because it is a loop back device.");
						continue;
					}
	
					if(!networkInterface.isUp()) {
						// logger.info("Skipping interface " + networkInterface.getDisplayName() + " because it is not currently up.");
						continue;
					}
					
					logger.debug("Searching network interface " + networkInterface.getDisplayName());
					
					for(InterfaceAddress networkInterfaceAddr:networkInterface.getInterfaceAddresses()) {
						if(networkInterfaceAddr.getBroadcast() != null && networkInterfaceAddr.getAddress() != null) {
							networkList.add(networkInterfaceAddr);	
						}
					}					
				}
			} 
			if(networkList.isEmpty()) {
				logger.info("Searchin all interfaces since none given in config.");
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				// search on each network interface
				while(interfaces.hasMoreElements()) {
					NetworkInterface networkInterface = interfaces.nextElement();
					
					if(networkInterface.isLoopback()) {
						continue;
					}
	
					if(!networkInterface.isUp()) {
						continue;
					}
					
					logger.debug("Searching network interface " + networkInterface.getDisplayName());
					
					for(InterfaceAddress networkInterfaceAddr:networkInterface.getInterfaceAddresses()) {
						if(networkInterfaceAddr.getBroadcast() != null && networkInterfaceAddr.getAddress() != null) {
							networkList.add(networkInterfaceAddr);	
						}
					}
				}
			}
			
			for(InterfaceAddress networkInterfaceAddr:networkList) {
				InetAddress broadcastAddr = networkInterfaceAddr.getBroadcast();
				InetAddress interfaceAddr = networkInterfaceAddr.getAddress();
				querySocket.setSoTimeout(udpTimeout);
				
				if(broadcastAddr == null || interfaceAddr == null) {
					// There is not either a broadcast address or interface address so nothing to do here
					continue;
				}
				
				logger.debug("Sending " + UDP_QUERY_MSG + " on " + broadcastAddr + " for interface on " + interfaceAddr);
				
				DatagramPacket searchPacket = new DatagramPacket(UDP_QUERY_MSG.getBytes(), UDP_QUERY_MSG.length(), broadcastAddr, updSocket);
				DatagramPacket responcePacket = new DatagramPacket(buffer, buffer.length);
				
				for(int cnt = 0; cnt < udpRetries; cnt++) {
					querySocket.send(searchPacket);
					try {
						while(true) {
							querySocket.receive(responcePacket);
							if(responcePacket.getAddress().equals(interfaceAddr)) {
								continue; // ignore our message
							}
							try {
								@SuppressWarnings("unchecked")
								Map<String, ?> deviceData = gson.fromJson(new String(responcePacket.getData(), 0, responcePacket.getLength()), Map.class);
								if(deviceData.containsKey("DeviceType") && deviceData.containsKey("UUID")) {
									if(!deviceList.containsKey(deviceData.get("UUID").toString())) {
										logger.info("Found " + deviceData.get("DeviceType") + " esp32://" + deviceData.get("UUID") + " on " + responcePacket.getAddress());

										ESP32Device device = new ESP32Device();
										device.setVersion(Integer.parseInt(deviceData.get("Version").toString()));
										device.setAddress(responcePacket.getAddress());
										device.setUUID(deviceData.get("UUID").toString());
										device.setControlPort(Integer.parseInt(deviceData.get("controlPort").toString()));
										device.setLoggingPort(Integer.parseInt(deviceData.get("loggingPort").toString()));
										device.setType(deviceData.get("DeviceType").toString());
										device.start();
										deviceList.put(deviceData.get("UUID").toString(), device);
									}
								} else {
									logger.debug("Bad data");
								}
							} catch(JsonSyntaxException ex) {
								logger.debug("Error reading JSON data", ex);
							}
						}
					} catch(SocketTimeoutException timeoutEx) {
						logger.debug("Timed out.");
					}
				}
			}
		} catch(Exception ex) {
			logger.error("Error while searching for devices", ex);
		} finally {
			//logger.info("Done searching");
			if(querySocket != null) {
				querySocket.close();
			}
		}
	}
	
}
