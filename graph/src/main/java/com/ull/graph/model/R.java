package com.ull.graph.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class R
{
    private Incollection incollection;

    private Book book;

    private Proceedings proceedings;

    private Inproceedings inproceedings;

    private Article article;

    @XmlElement
    public Incollection getIncollection ()
    {
        return incollection;
    }

    public void setIncollection (Incollection incollection)
    {
        this.incollection = incollection;
    }

    @XmlElement
    public Book getBook ()
    {
        return book;
    }

    public void setBook (Book book)
    {
        this.book = book;
    }
    
    @XmlElement
    public Proceedings getProceedings ()
    {
        return proceedings;
    }

    public void setProceedings (Proceedings proceedings)
    {
        this.proceedings = proceedings;
    }
    
    @XmlElement
    public Inproceedings getInproceedings ()
    {
        return inproceedings;
    }

    public void setInproceedings (Inproceedings inproceedings)
    {
        this.inproceedings = inproceedings;
    }

    @XmlElement
    public Article getArticle ()
    {
        return article;
    }

    public void setArticle (Article article)
    {
        this.article = article;
    }
    
    public Publication getPublication() 
    {
    	if (article != null) {
    		return article;
    	}
    	else if (inproceedings != null) {
    		return inproceedings;
    	}
    	else if (incollection != null) {
    		return incollection;
    	}
    	else if (book != null) {
    		return book;
    	}
    	else if (proceedings != null) {
    		return proceedings;
    	}
    	else {
    		return null;
    	}
    }

    @Override
    public String toString()
    {
        return "[incollection = "+incollection+", inproceedings = "+inproceedings+", article = "+article+"]";
    }
    
}
