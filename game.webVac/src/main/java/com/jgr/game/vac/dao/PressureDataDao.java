package com.jgr.game.vac.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.jgr.game.vac.entity.PressureData;
import com.jgr.game.vac.entity.PressureDataPk;

@Repository

public interface PressureDataDao extends CrudRepository<PressureData, PressureDataPk> {

}
