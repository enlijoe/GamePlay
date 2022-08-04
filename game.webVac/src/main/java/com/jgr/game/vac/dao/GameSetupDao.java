package com.jgr.game.vac.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.jgr.game.vac.entity.GameNameProjection;
import com.jgr.game.vac.entity.GameSetup;

@Repository
public interface GameSetupDao extends CrudRepository<GameSetup, Long> {
	public GameSetup getByName(String name);
	public List<GameNameProjection> findByNameNotNull();

}
