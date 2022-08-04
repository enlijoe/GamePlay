package com.jgr.game.vac.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.jgr.game.vac.dao.GamePropsDao;
import com.jgr.game.vac.dao.GameSetupDao;
import com.jgr.game.vac.dao.PropertyDao;
import com.jgr.game.vac.entity.GameNameProjection;
import com.jgr.game.vac.entity.GameProps;
import com.jgr.game.vac.entity.GameSetup;
import com.jgr.game.vac.entity.Property;
import com.jgr.game.vac.service.AbstractGameObject;
import com.jgr.game.vac.service.AbstractGameObject.GameSettingInfo;
import com.jgr.game.vac.service.GameService;
import com.jgr.game.vac.service.dto.GameSettingInfoDto;
import com.jgr.game.vac.service.game.GameManagerImpl;

@Service
@Transactional
public class GameServiceImpl implements GameService {
	private static Logger logger = LoggerFactory.getLogger(AbstractGameObject.class);

	@Autowired GameSetupDao gameSetupDao;
	@Autowired PropertyDao propertyDao;
	@Autowired GamePropsDao gamePropsDao;
	@Autowired ApplicationContext context;
	
	
	private static List<String> premitiveList = Arrays.asList( new String[] {"int", "long", "double", "flot", "boolean", "char", "byte"});
		
	
	public void resetDb() {
		gameSetupDao.deleteAll();
		propertyDao.deleteAll();
	}
	
	@Override
	public boolean hasGame(String name) {
		return gameSetupDao.getByName(name) != null;
	}
	
	@Override
	public List<GameSettingInfoDto> getGameBeanProps(Long gameId, String beanName) {
		List<GameSettingInfoDto> dtos = new ArrayList<>();
		
		AbstractGameObject<?> gameObject = context.getBean(beanName, AbstractGameObject.class);
		if(gameObject == null) {
			throw new RuntimeException("Game Object not found " + beanName);
		}
		
		List<GameSettingInfo> settings = gameObject.getDefinedProps();
		for(GameSettingInfo setting:settings) {
			Property property = propertyDao.getByName(setting.name);
			GameProps gameProp = gamePropsDao.findByGameIdAndPropId(gameId, property.getId());
			String[] selectList = null;
			
			if(!premitiveList.contains(property.getTypeClassName())) {
				try {
					Class<?> typeClass = Class.forName(property.getTypeClassName());
					Class<?> mappedClass = GameManagerImpl.getMappedClass(typeClass);
					if(mappedClass != null) {
						selectList = context.getBeanNamesForType(mappedClass);
					} else {
						selectList = context.getBeanNamesForType(typeClass);
					}
				} catch (ClassNotFoundException e) {
					logger.error("Unable to find java type for beanType " + property.getTypeClassName(), e);
					throw new RuntimeException("Error");
				}
				 
			}
			dtos.add(new GameSettingInfoDto(setting.name, property.getTypeClassName(), selectList, gameProp.getValue()));
		}
		return dtos;
	}
	
	
	@Override
	public Long createNewGame(String name, String mainBeanName) {
		GameSetup gameSetup = new GameSetup(name, mainBeanName);
		if(context.containsBean(gameSetup.getBeanName())) {
			AbstractGameObject<?> gameBean = context.getBean(gameSetup.getBeanName(), AbstractGameObject.class);
			
			for(GameSettingInfo property:gameBean.getDefinedProps()) {
				Property gameProp = propertyDao.getByName(property.name);
				GameProps gameProps = new GameProps(gameSetup, gameProp, gameProp.getInitalValue());
				gameSetup.addProperty(gameProps);
			}
			
			gameSetupDao.save(gameSetup);
		} else {
			throw new RuntimeException("Game bean is undefined " + gameSetup.getBeanName());
		}
		
		return gameSetup.getId();
	}
	
	@Override
	public List<GameNameProjection> getGameNames() {
		return gameSetupDao.findByNameNotNull();
	}
}
