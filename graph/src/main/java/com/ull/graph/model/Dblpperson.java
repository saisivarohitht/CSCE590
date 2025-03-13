package com.ull.graph.model;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Dblpperson
{
    private R[] r;

    private String name;

    private String pid;

    @XmlElement
    public R[] getR ()
    {
        return r;
    }

    public void setR (R[] r)
    {
        this.r = r;
    }

    @XmlAttribute
    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    @XmlAttribute
    public String getPid ()
    {
        return pid;
    }

    public void setPid (String pid)
    {
        this.pid = pid;
    }

    @Override
    public String toString()
    {
        return "pid";
    }
}

