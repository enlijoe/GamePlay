package com.jgr.game.vac.controler.model;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.jgr.game.vac.controler.model.views.CalibrateModel;

@Controller
@RequestMapping("/calibrate")
public class CalibrateControler {

	@GetMapping("/")
	public ModelAndView beginCalibration() {
		ModelAndView retVal = new ModelAndView("");
		
		retVal.addObject("model", new CalibrateModel());
		
		return retVal;
	}
	
	@PostMapping("startMonitor")
	public ModelAndView startMonitor() {
		return null;
	}
	
	
	@PostMapping("stopMonitor")
	public ModelAndView stopMonitor() {
		return null;
	}
	
	
	@PostMapping("startFlow")
	public ModelAndView startFlow() {
		return null;
	}
	
	@PostMapping("stopFlow")
	public ModelAndView stopFlow() {
		return null;
	}
	
	@GetMapping("graph.jpg")
	public void displayGraph(HttpServletResponse response, OutputStream out) {
		response.setContentType(MimeTypeUtils.IMAGE_JPEG_VALUE);
	}
	
	@GetMapping("pressure.jpg")
	public void displayPressure(HttpServletResponse response, OutputStream out) {
		response.setContentType(MimeTypeUtils.IMAGE_JPEG_VALUE);
		
	}

	@GetMapping("volume.jpg")
	public void displayVolume(HttpServletResponse response, OutputStream out) {
		response.setContentType(MimeTypeUtils.IMAGE_JPEG_VALUE);
		
	}
}
