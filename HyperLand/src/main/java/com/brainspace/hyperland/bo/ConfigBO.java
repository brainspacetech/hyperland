package com.brainspace.hyperland.bo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "Configuration")
public class ConfigBO {

    private Service serviceObj[];
    @XmlElement(name = "Service")
    public Service[] getServiceObj ()
    {
        return serviceObj;
    }

    public void setServiceObj (Service serviceObj[])
    {
        this.serviceObj = serviceObj;
    }

}
