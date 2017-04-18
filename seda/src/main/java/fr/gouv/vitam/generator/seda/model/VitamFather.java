package fr.gouv.vitam.generator.seda.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;

import fr.gouv.culture.archivesdefrance.seda.v2.LevelType;
import fr.gouv.culture.archivesdefrance.seda.v2.TextType;

public class VitamFather {

    @XmlElement(name = "id")
    private String id;

    @XmlElement(name = "Title")
    private List<TextType> titles;

    @XmlElement(name = "DescriptionLevel", required = true)
    @XmlSchemaType(name = "token")
    private LevelType descriptionLevel;

    public VitamFather() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<TextType> getTitles() {
        return titles;
    }

    public void setTitles(List<TextType> titles) {
        this.titles = titles;
    }

    public LevelType getDescriptionLevel() {
        return descriptionLevel;
    }

    public void setDescriptionLevel(LevelType descriptionLevel) {
        this.descriptionLevel = descriptionLevel;
    }
}
