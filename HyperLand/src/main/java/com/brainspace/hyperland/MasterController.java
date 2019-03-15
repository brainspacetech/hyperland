package com.brainspace.hyperland;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/firm")
public class MasterController {
 
	@RequestMapping(value = "/add" , produces = MediaType.APPLICATION_JSON_VALUE,method = RequestMethod.GET)

	public String addMaster()
	{
		return "{'name':'pankaj'}";
	}
}
