package com.jgr.game.vac.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.jgr.game.vac.dao.PropertyDao;
import com.jgr.game.vac.entity.Property;
import com.jgr.game.vac.service.stereotype.GameSetting;

public abstract class AbstractGameObject<T extends AbstractGameState> implements BeanNameAware {
	String beanName;
	@Autowired private PropertyDao propertyDao;
	@Autowired private ApplicationContext context;
	
	private Logger logger = LoggerFactory.getLogger(AbstractGameObject.class);

	abstract public Class<T> getStateClass();
	
	public static class GameSettingInfo {
		public final String name;
		public final String defaultValue;
		public final String type;

		private GameSettingInfo(String name, String defaultValue, String type) {
			this.name = name;
			this.defaultValue = defaultValue;
			this.type = type;
		}
	}
	
	
	public List<GameSettingInfo> getDefinedProps() {
		List<GameSettingInfo> retVal = new ArrayList<>();
		
		Class<T> stateClass = getStateClass();
		if(stateClass != null) {
			Field[] fields = stateClass.getDeclaredFields();
			for(Field field:fields) {
				GameSetting gameSetting = field.getAnnotation(GameSetting.class);
				if(gameSetting != null) {
					retVal.add(new GameSettingInfo(gameSetting.name(), gameSetting.defaultValue(), field.getType().getCanonicalName()));
				}
			}
		}
		
		return retVal;
	}
	
	@PostConstruct
	@Transactional
	private void verifyDbAndProps() {
		List<GameSettingInfo> gameProps = getDefinedProps();
		for(GameSettingInfo entry:gameProps) {
			String propName = entry.name;
			if(propertyDao.existsByName(propName)) {
				// verify the type is the same or assignable either way and fix if needed
			} else {
				Property property = new Property(propName, entry.defaultValue, entry.type);
				propertyDao.save(property);
			}
		}
	}
	
	@Override
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	
}
