package com.jgr.game.vac.service;

import java.util.List;

import com.jgr.game.vac.entity.GameNameProjection;
import com.jgr.game.vac.service.dto.GameSettingInfoDto;

public interface GameService {
	public Long createNewGame(String name, String mainBeanName);
	public boolean hasGame(String name);
	public void resetDb();
	public List<GameNameProjection> getGameNames();
	public List<GameSettingInfoDto> getGameBeanProps(Long gameId, String beanName);
}
