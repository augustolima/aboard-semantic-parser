package br.ufpe.cin.assistive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Frame {
    private int id;
    private String name;
    private Map<String, List<String>> relations = new HashMap<>();
    private Boolean active;

    public Frame(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Frame(String name) {
        this.name = name;
    }

    public void setId(int id) { this.id = id ; }

    public int getId() { return this.id; }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

//    public void setRelations(Map relations) {
//        this.relations = relations;
//    }

    public Map<String, List<String>> getRelations() {
        return this.relations;
    }

    public void addRelation(String name, List<String> relations) {
        this.relations.put(name, relations);
    }

    public void setActive(Boolean flag) { this.active = flag; }
    public Boolean isActive() { return this.active; }
}
