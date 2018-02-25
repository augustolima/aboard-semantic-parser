package br.ufpe.cin.assistive;

import java.util.ArrayList;
import java.util.List;

public class Option {
    private String name;
    private String color;
    private Boolean selected;
    private String value;
    private String csPart;
    private ArrayList<String> ontology;
    private List<Frame> relations;
    private String pos;

    public Option(String name, String color, String pos, String csPart) {
        this.name = name;
        this.color = color;
        this.selected = false;
        this.value = "";
        this.ontology = new ArrayList<>();
        this.csPart = csPart;
        this.relations = new ArrayList<>();
    }

    public Option(String name) {
        this.name = name;
        this.color = "";
        this.selected = false;
        this.value = "";
        this.ontology = null;
        this.csPart = "";
        this.relations = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return this.color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public void setCsPart(String csPart) {
        this.csPart = csPart;
    }

    public String getPos() {
        return this.pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getCsPart() {
        return this.csPart;
    }

    public ArrayList<String> getOntology() {
        return this.ontology;
    }

    public void setOntology(ArrayList<String> ontology) {
        this.ontology = ontology;
    }

    public void setRelations(List<String> relations) {
        for (String relation : relations) {
            Frame frame = new Frame(relation);
            frame.setActive(true);
            this.relations.add(frame);
        }
    }

    public List<Frame> getRelations() { return this.relations; }
}
