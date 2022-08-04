package com.jgr.game.vac.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertiesDAO extends CrudRepository<Property, Long> {
	Property findByName(String name);
	Property findById(long id);
}
