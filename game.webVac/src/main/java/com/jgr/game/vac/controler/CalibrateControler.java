package com.jgr.game.vac.controler;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.ModelAndView;

import com.jgr.game.vac.controler.model.CalibrateModel;
import com.jgr.game.vac.service.PressureDevice;

@Controller
@RequestMapping("/calibrate")
@DependsOn("GenericEsp32DeviceFactory")
public class CalibrateControler {
	@Autowired List<PressureDevice> deviceList;
	
	private static class SessionData implements Serializable {
		private static final long serialVersionUID = -7015229330784831612L;

		private void destroy() {
			
		}
	}
	

	@ModelAttribute("deviceNameList")
	public List<String> deviceNameList() {
		ArrayList<String> retVal = new ArrayList<String>();
		for(PressureDevice device:deviceList) {
			retVal.add(device.getBeanName());
		}
		
		return retVal;
	}
	
	
	@GetMapping("")
	public ModelAndView beginCalibration(HttpSession session) {
		ModelAndView forward = new ModelAndView("calibrate");
		createSessionObjectIfNeeded(session);
		
		forward.addObject("data", new CalibrateModel());
		
		return forward;
	}
	
	@PostMapping(name="update", params="update")
	public ModelAndView update(@Valid @ModelAttribute("data") CalibrateModel data, @SessionAttribute SessionData sessionData) {
		ModelAndView forward = new ModelAndView("calibrate");
		
		List<String> errors = validateCalibrateModel(data);
		if(errors != null) {
			forward.addObject("errors", errors);
		} else {
		}
		return forward;
	}
	
	private SessionData createSessionObjectIfNeeded(HttpSession session) {
		SessionData data = (SessionData) session.getAttribute("CalibrateControlerSessionData");
		return data;
	}
	
	@PostMapping(name="update", params = "startMonitor")
	public ModelAndView startMonitor(@Valid @ModelAttribute("data") CalibrateModel data, @SessionAttribute SessionData sessionData) {
		ModelAndView forward = new ModelAndView("calibrate");
		List<String> errors = validateCalibrateModel(data);
		if(errors != null) {
			forward.addObject("errors", errors);
		} else {
			
		}
		return forward;
	}
	
	
	@PostMapping(name="update", params = "stopMonitor")
	public ModelAndView stopMonitor(@Valid @ModelAttribute("data") CalibrateModel data, @SessionAttribute SessionData sessionData) {
		ModelAndView forward = new ModelAndView("calibrate");
		List<String> errors = validateCalibrateModel(data);
		if(errors != null) {
			forward.addObject("errors", errors);
		} else {
			
		}
		return forward;
	}
	
	
	@PostMapping(name="update", params = "startFlow")
	public ModelAndView startFlow(@Valid @ModelAttribute("data") CalibrateModel data, @SessionAttribute SessionData sessionData) {
		ModelAndView forward = new ModelAndView("calibrate");
		List<String> errors = validateCalibrateModel(data);
		if(errors != null) {
			forward.addObject("errors", errors);
		} else {
			
		}
		return forward;
	}
	
	@PostMapping(name="update", params = "stopFlow")
	public ModelAndView stopFlow(@Valid @ModelAttribute("data") CalibrateModel data, @SessionAttribute SessionData sessionData) {
		ModelAndView forward = new ModelAndView("calibrate");
		List<String> errors = validateCalibrateModel(data);
		if(errors != null) {
			forward.addObject("errors", errors);
		} else {
			
		}
		return forward;
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
	
	private List<String> validateCalibrateModel(CalibrateModel data) {
		return null;
		
	}
}
