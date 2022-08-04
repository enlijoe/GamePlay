package com.jgr.game.vac.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.jgr.game.vac.entity.Property;

@Repository
public interface PropertyDao extends CrudRepository<Property, Long>{
	public Property getByName(String name);
	public boolean existsByName(String name);
	
}
