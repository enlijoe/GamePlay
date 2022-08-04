package com.jgr.game.vac.controler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.jgr.game.vac.controler.model.EditGameModel;
import com.jgr.game.vac.entity.GameNameProjection;
import com.jgr.game.vac.service.AbstractGameObject;
import com.jgr.game.vac.service.AbstractGameRunnable;
import com.jgr.game.vac.service.GameService;

@Controller
public class HomeControler {
	private static Logger logger = LoggerFactory.getLogger(AbstractGameObject.class);

	@Autowired GameService gameService;
	@Autowired ApplicationContext appContext;
	
	@GetMapping("/")
	public String home(Model model) {
		return "index";
	}
	
	@GetMapping("/modify")
	public String modifyGame(Model model) {
		List<GameNameProjection> gameNames = gameService.getGameNames();
		
		model.addAttribute("selectedUrl", "/editGame");
		model.addAttribute("gameNames", gameNames);
		
		return "selectGame";
	}
	
	@PostMapping("/editGame")
	public String editGame(Model model) {
		return "";
	}
	
	@GetMapping("/resetDB") 
	public String resetDb(Model model) {
		gameService.resetDb();
		return "index";
	}
	
	@GetMapping("/new")
	public String getNewGameName(Model model) {
		@SuppressWarnings("rawtypes")
		Map<String, AbstractGameRunnable> gameBeans = appContext.getBeansOfType(AbstractGameRunnable.class);
		model.addAttribute("gameBeanNames", gameBeans.keySet());
		return "newGame";
	}
	
	@PostMapping("/new")
	public ModelAndView editNewGame(@RequestParam(name="createGame") String button, @RequestParam(required = false, name="gameName") String gameName, @RequestParam(required = false, name="gameBeanName") String gameBeanName, Model model) {
		ArrayList<String> errors = new ArrayList<>();

		if(StringUtils.isEmpty(gameName)) {
			errors.add("Game name can't be blank.");
		} else {
			if(gameService.hasGame(gameName)) {
				errors.add("Game name must be unique.");
			} 
		}
		
		if(StringUtils.isEmpty(gameBeanName)) {
			errors.add("Need to select a starting bean name");
		}
		return null;
		/*
		if(errors.isEmpty()) {
			return new ModelAndView("editGame", "gameModel", new EditGameModel(gameId, gameName, gameBeanName, "", gameService.getGameBeanProps(gameId, gameBeanName)));
		} else {
			logger.warn("Form has errors");
			model.addAttribute("errors", errors);
			return new ModelAndView("index", );
		}
		*/
	}
	
	@GetMapping("/start")
	public String startGame(Model model) {
		
		return "startGame";
	}
	
	@GetMapping("/status")
	public String gameStatus(Model model) {
		return "gameStatus";
	}
}
