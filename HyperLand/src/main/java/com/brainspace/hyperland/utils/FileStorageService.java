package com.brainspace.hyperland.utils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileStorageService {
    public Resource loadFileAsResource()  throws FileNotFoundException{
        Resource resource = null;
        try {
            ClassLoader classLoader = FileStorageService.class.getClassLoader();
            Path filePath =  Paths.get(classLoader.getResource("./hyperland-plots.xls").toURI());
            resource = new UrlResource(filePath.toUri());
            System.out.println(resource.getFilename()) ;
            if(resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found " );
            }
        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("File not found");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return resource;
    }



}
