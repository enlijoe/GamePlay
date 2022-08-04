package com.jgr.game.vac.service.esp32;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;


@Service("GenericEsp32DeviceFactory")
public class GenericEsp32DeviceFactory implements ApplicationContextAware {
	private static Logger logger = LoggerFactory.getLogger(GenericEsp32DeviceFactory.class);
	private static final String UDP_QUERY_MSG = "Device Query";
	private static final int NumExpectedDevices = 1;
	private static final int queryWaitTime = 1000;

	HashMap<String, ESP32Device> deviceList;
	ConfigurableListableBeanFactory  beanFactory;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if(applicationContext instanceof GenericApplicationContext) {
			beanFactory = ((GenericApplicationContext)applicationContext).getBeanFactory();
		}
	}


	@PostConstruct
	public void init() {
		deviceList = new HashMap<>();
		Gson gson = new Gson();
		byte[] buffer = new byte[1024];
		DatagramSocket querySocket = null;
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			querySocket = new DatagramSocket(3358);

			// search on each network interface
			while(interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();
				
				if(networkInterface.isLoopback()) {
					// logger.info("Skipping interface " + networkInterface.getDisplayName() + " because it is a loop back device.");
					continue;
				}

				if(!networkInterface.isUp()) {
					// logger.info("Skipping interface " + networkInterface.getDisplayName() + " because it is not currently up.");
					continue;
				}
				
				logger.info("Searching network interface " + networkInterface.getDisplayName());
				
				for(InterfaceAddress networkInterfaceAddr:networkInterface.getInterfaceAddresses()) {
					InetAddress broadcastAddr = networkInterfaceAddr.getBroadcast();
					InetAddress interfaceAddr = networkInterfaceAddr.getAddress();
					
					querySocket.setSoTimeout(queryWaitTime);
					
					if(broadcastAddr == null || interfaceAddr == null) {
						// There is not either a broadcast address or interface address so nothing to do here
						continue;
					}
					
					logger.info("Sending " + UDP_QUERY_MSG + " on " + broadcastAddr);
					
					DatagramPacket searchPacket = new DatagramPacket(UDP_QUERY_MSG.getBytes(), UDP_QUERY_MSG.length(), broadcastAddr, 3358);
					DatagramPacket responcePacket = new DatagramPacket(buffer, buffer.length);
					
					for(int cnt = 0; cnt < 3; cnt++) {
						if(deviceList.size() == NumExpectedDevices) break;
						querySocket.send(searchPacket);
						try {
							while(true) {
								querySocket.receive(responcePacket);
								if(responcePacket.getAddress().equals(networkInterfaceAddr.getAddress())) {
									continue; // ignore our message
								}
								//logger.info("Mesage from " + responcePacket.getSocketAddress());
								@SuppressWarnings("unchecked")
								Map<String, ?> deviceData = gson.fromJson(new String(responcePacket.getData(), 0, responcePacket.getLength()), Map.class);
								if(deviceData.containsKey("DeviceType") && deviceData.containsKey("UUID")) {
									if(!deviceList.containsKey(deviceData.get("UUID").toString())) {
										logger.info("Found " + deviceData.get("DeviceType") + " UUID of " + deviceData.get("UUID") + " on " + responcePacket.getAddress());
										ESP32Device device = new ESP32Device();
										device.setVersion(Integer.parseInt(deviceData.get("Version").toString()));
										device.setAddress(responcePacket.getAddress());
										device.setUUID(deviceData.get("UUID").toString());
										device.setControlPort(Integer.parseInt(deviceData.get("controlPort").toString()));
										device.setLoggingPort(Integer.parseInt(deviceData.get("loggingPort").toString()));
										device.setType(deviceData.get("DeviceType").toString());
										device.start();
										deviceList.put(deviceData.get("UUID").toString(), device);
										// TODO need to handle more then one of the same type somehow
										//logger.info("Adding " + deviceData.get("DeviceType").toString() + " bean");
										beanFactory.registerSingleton(deviceData.get("DeviceType").toString(), device);
										for(Map.Entry<String, Object> subDevice: device.deviceMap.entrySet()) {
											//logger.info("Adding " + subDevice.getKey() + " bean");
											beanFactory.registerSingleton(subDevice.getKey(), subDevice.getValue());
										}
									}
								}
							}
						} catch(SocketTimeoutException timeoutEx) {
							// we can ignore this
						}
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
