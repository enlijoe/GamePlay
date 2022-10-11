package com.jgr.game.vac.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.jgr.game.vac.entity.GameProps;

@Repository
public interface GamePropsDao extends CrudRepository<GameProps, Long> {
	GameProps findByGameIdAndPropId(long gameId, long propId);
}
