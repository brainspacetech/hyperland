package com.brainspace.hyperland.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.brainspace.hyperland.bo.Firm;
import com.brainspace.hyperland.bo.RestResponse;
import com.brainspace.hyperland.service.IFirmService;

@RestController
@RequestMapping("/firm")
public class MasterController { 
	@Autowired
	private IFirmService firmService;

	@RequestMapping(value = "/getAll" , produces = MediaType.APPLICATION_JSON_VALUE,method = RequestMethod.GET)
	public ResponseEntity<RestResponse> getAll()
	{
		RestResponse response = firmService.getAllFirms();
		return  new ResponseEntity<RestResponse>(response, HttpStatus.OK);
	}
	@PostMapping(value = "/add")
	public ResponseEntity<RestResponse> add(@RequestBody Firm firm)
	{
		RestResponse response  = firmService.addFirm(firm);		
		return  new ResponseEntity<RestResponse>(response,HttpStatus.OK);
	}
	@GetMapping(value = "/get/{id}")
	public ResponseEntity<RestResponse> get(@PathVariable("id") int id)
	{
		RestResponse response = firmService.getFirmById(id);
		return  new ResponseEntity<RestResponse>(response, HttpStatus.OK);
	}
	
}
