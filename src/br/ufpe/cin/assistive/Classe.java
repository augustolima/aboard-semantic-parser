package br.ufpe.cin.assistive;

public class Classe {
    private int id;
    private String name;

    Classe(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return this.id; }
    public void setId(int id) { this.id = id; }

    public void setName(String name) { this.name = name; }
    public String getName() { return this.name; }
}
