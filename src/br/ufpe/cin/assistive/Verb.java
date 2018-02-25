package br.ufpe.cin.assistive;

import java.util.ArrayList;
import java.util.List;

public class Verb {
    private int id;
    private List<Classe> classes = new ArrayList<>();

    Verb(int id) {
        this.id = id;
    }

    public int getId() { return this.id; }
    public void setId(int id) { this.id = id; }

    public void setClasses(int id, String className) { this.classes.add(new Classe(id, className)); }
    public List<Classe> getClasses() { return this.classes; }
}
