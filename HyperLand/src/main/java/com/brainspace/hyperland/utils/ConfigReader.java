package com.brainspace.hyperland.utils;

import com.brainspace.hyperland.bo.ConfigBO;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.InputStream;

public class ConfigReader {
    private static ConfigBO configBO = null;

    private ConfigReader() {
    }

    public static ConfigBO getConfig() {
        if (configBO == null) {
            try {

                ClassLoader classLoader = ConfigReader.class.getClassLoader();
                InputStream inputStream = classLoader.getResourceAsStream("./config.xml");
                JAXBContext jaxbcontext = JAXBContext.newInstance(ConfigBO.class);
                Unmarshaller unmarshaller = jaxbcontext.createUnmarshaller();
                configBO = (ConfigBO) unmarshaller.unmarshal(inputStream);
                System.out.println("configBO " + configBO.getServiceObj());

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return configBO;
    }

    public static void main(String s[])
    {
        try {
            Class dao = Class.forName("com.brainspace.hyperland.dao.MasterDAO");
            Object obj = dao.newInstance();
            System.out.println(obj.getClass());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
