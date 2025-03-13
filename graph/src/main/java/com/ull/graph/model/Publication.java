package com.ull.graph.model;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Publication
{
    private String key;

    @XmlAttribute
    public String getKey ()
    {
        return key;
    }

    public void setKey (String key)
    {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass() || key == null) {
            return false;
        }
        Publication in = (Publication) o;
        return key.equals(in.getKey());
    }

    @Override
    public String toString()
    {
        return key;
    }
    
}

