package com.jgr.game.vac.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jgr.game.vac.dao.PressureDataDao;
import com.jgr.game.vac.service.PressureService;

@Service("pressureService")
public class PressureServiceImpl implements PressureService {
	private static final Logger logger = LoggerFactory.getLogger(PressureServiceImpl.class);
	@Autowired private PressureDataDao pressureDataDao;

	
	

}
