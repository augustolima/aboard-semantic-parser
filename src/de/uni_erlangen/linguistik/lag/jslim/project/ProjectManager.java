//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.uni_erlangen.linguistik.lag.jslim.project;

import de.uni_erlangen.linguistik.lag.jslim.exception.JSLIMException;
import de.uni_erlangen.linguistik.lag.jslim.product.Product;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class ProjectManager {
    private final Product product;
    private HashMap<String, Project> name2project = new LinkedHashMap();
    private Set<String> projectFiles = new LinkedHashSet();
    private String active;

    public ProjectManager(Product product) {
        this.product = product;
    }

    public void load(String projectName, String projectFile) throws JSLIMException {
        projectFile = (new File(projectFile)).getAbsolutePath();
        if (this.projectFiles.contains(projectFile)) {
            throw new JSLIMException("Cannot load project: project is already loaded!");
        } else if (this.name2project.containsKey(projectName)) {
            throw new JSLIMException("Cannot load project: a project with the provided name is alread loaded!");
        } else {
            this.load(new Project(this.product, projectFile, projectName));
        }
    }

    public void load(Project project) throws JSLIMException {
        if (this.active == null) {
            this.active = project.getName();
        }

        this.name2project.put(project.getName(), project);
        this.projectFiles.add(project.getProjectDescription().projectFile.getAbsolutePath());
        project.parse();
    }

    public Project unload(String projectName) throws JSLIMException {
        if (this.name2project.containsKey(projectName)) {
            this.projectFiles.remove(((Project)this.name2project.get(projectName)).projectDescription.projectFile.getAbsolutePath());
            return (Project)this.name2project.remove(projectName);
        } else {
            throw new JSLIMException("[ProjectManager] Project not loaded!");
        }
    }

    public boolean isLoaded(String projectFile) {
        return this.projectFiles.contains(projectFile) || this.name2project.containsKey(projectFile);
    }

    public Project get(String projectName) throws JSLIMException {
        Project p = (Project)this.name2project.get(projectName);
        if (p == null) {
            throw new JSLIMException("[ProjectManager] No project with name " + projectName + "," + this.name2project.keySet());
        } else {
            return p;
        }
    }

    public Project get() {
        return (Project)this.name2project.get(this.active);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        Iterator var3 = this.name2project.keySet().iterator();

        while(var3.hasNext()) {
            String key = (String)var3.next();
            sb.append(key + " ");
        }

        sb.append("]");
        return sb.toString();
    }
}
