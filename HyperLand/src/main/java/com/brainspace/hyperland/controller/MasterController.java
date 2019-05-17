package com.brainspace.hyperland.controller;

import com.brainspace.hyperland.service.IMasterService;
import com.brainspace.hyperland.utils.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import com.brainspace.hyperland.bo.RestResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;


@RestController
@RequestMapping("/master")
public class MasterController {
    @Autowired
    private IMasterService masterService;

    @RequestMapping(value = "/getAll/{type}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public ResponseEntity<RestResponse> getAll(@PathVariable(name = "type") String type) {
        RestResponse response = null;
        try {
            response = masterService.getAllData(type);
        } catch (Exception e) {
            e.printStackTrace();
        }
       if(response.getStatusCode().equalsIgnoreCase("1"))
          return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
       else
           return new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @PostMapping(value = "/add/{type}")
    public ResponseEntity<RestResponse> add(@PathVariable(name = "type") String type, @RequestBody Object request) {
        RestResponse response = masterService.addData(type, request, "");
        if(response.getStatusCode().equalsIgnoreCase("1"))
        {
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping(value = "/create/Firm")
    public ResponseEntity<RestResponse> createFirm(@RequestParam("logoFile") MultipartFile logoFile, @RequestParam("firmName") String firmName ) {
        RestResponse response = null;
        try {
            response =  masterService.createFirm(logoFile.getInputStream(), firmName);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        if(response.getStatusCode().equalsIgnoreCase("1"))
        {
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/addLand")
    public ResponseEntity<RestResponse> addLand(@RequestBody Object request) {
        RestResponse response = masterService.addLandData(request, "");
        if(response.getStatusCode().equalsIgnoreCase("1"))
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        else
            return new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping(value = "/add/menuConfig")
    public ResponseEntity<RestResponse> addMenuConfig(@RequestBody Object request) {
        RestResponse response = masterService.addMenuConfig((String) ((Map) request).get("menuConfig"));
        if(response.getStatusCode().equalsIgnoreCase("1"))
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        else
            return new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @GetMapping(value = "/get/{type}/{id}")
    public ResponseEntity<RestResponse> get(@PathVariable(name = "type") String type, @PathVariable("id") int id) {
        RestResponse response = masterService.getDataById(type, id);
        if(response.getStatusCode().equalsIgnoreCase("1"))
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        else
            return new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping(value = "/update/{type}/{id}")
    public ResponseEntity<RestResponse> update(@PathVariable(name = "type") String type, @PathVariable("id") int id, @RequestBody Object request) {
        RestResponse response = masterService.updateData(type, id, request);
        if(response.getStatusCode().equalsIgnoreCase("1"))
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        else
            return new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping(value = "/delete/{type}/{id}")
    public ResponseEntity<RestResponse> delete(@PathVariable(name = "type") String type, @PathVariable("id") int id) {
        RestResponse response = masterService.deleteData(type, id);
        if(response.getStatusCode().equalsIgnoreCase("1"))
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        else
            return new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(value = "/getLand/{id}")
    public ResponseEntity<RestResponse> getLand(@PathVariable("id") int id) {
        RestResponse response = masterService.getLandDataById("land", id);
        if(response.getStatusCode().equalsIgnoreCase("1"))
          return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        else
           return new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping(value = "/add/roleMenuConfig")

    public ResponseEntity<RestResponse> addRoleMenuConfig(@RequestBody Object request) {
        RestResponse response = masterService.addRoleMenuConfig((String) ((Map) request).get("roleMenuConfig"), (String) ((Map) request).get("role"));
        if(response.getStatusCode().equalsIgnoreCase("1"))
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        else
            return new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/downloadPlots")
    public ResponseEntity<?> downloadFile(HttpServletRequest request) {
        // Load file as Resource
        Resource resource = null;
        String contentType = null;
        byte content[] = null;
        try {
            resource = new FileStorageService().loadFileAsResource();
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            content = FileCopyUtils.copyToByteArray(resource.getInputStream());
        } catch (IOException ex) {

        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }


        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(content);
    }


    @PostMapping("/uploadPlots")
    public ResponseEntity<RestResponse> uploadPlot(@RequestParam("file") MultipartFile file) {
        RestResponse response = null;
        System.out.println(file);
        try {
            response  = masterService.createPlots(file.getInputStream());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        if(response.getStatusCode().equalsIgnoreCase("1"))
            return new ResponseEntity<RestResponse>(response, HttpStatus.OK);
        else
            return new ResponseEntity<RestResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
