package com.brainspace.hyperland.bo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

public class PropertyMapping
{
    private Property[] Property;

    @XmlElement(name="Property")
    public Property[] getProperty ()
    {
        return Property;
    }

    public void setProperty (Property[] Property)
    {
        this.Property = Property;
    }

}