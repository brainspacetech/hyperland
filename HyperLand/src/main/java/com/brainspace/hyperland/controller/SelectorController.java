package com.brainspace.hyperland.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brainspace.hyperland.bo.RestResponse;
import com.brainspace.hyperland.service.ISelectorService;

@RestController
@RequestMapping("/selector")
public class SelectorController {
	@Autowired
	private ISelectorService selectorService;
	@GetMapping(value = "/get/{type}")
	public ResponseEntity<RestResponse> getSelector(@PathVariable("type") String type)
	{
		RestResponse response = selectorService.getSelector(type);
		return  new ResponseEntity<RestResponse>(response, HttpStatus.OK);
	}
}
