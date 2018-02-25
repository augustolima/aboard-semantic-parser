package br.ufpe.cin.assistive;

import java.util.List;

public class FrameRelation {
    private String core;
    private List<String> semantic;
    private List<Frame> relations;

    public FrameRelation(String core, List<Frame> relations) {
        this.core = core;
        this.relations = relations;
    }

    public void setCore(String core) { this.core = core; }
    public String getCore() { return this.core; }

    public void setSemantic(List<String> semantic) { this.semantic = semantic; }
    public List<String> getSemantic() { return this.semantic; }

    public void setRelations(List<Frame> relations) { this.relations = relations; }

    public List<Frame> getRelations() {
        return this.relations;
    }
}
