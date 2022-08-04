package com.jgr.game.vac.controler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jgr.game.vac.service.PressureDevice;

@Controller
@DependsOn("GenericEsp32DeviceFactory")
public class PressureControler {
	@Autowired List<PressureDevice> devices;
	
	@GetMapping(path="/pressure")
	public String pressurePage(Model model) {
		model.addAttribute("devices", devices);
		return "pressure";
	}
	
	@GetMapping(path="/pressure/update")
	@ResponseBody
	public String updatePressure() {
		return null;
	}
}
