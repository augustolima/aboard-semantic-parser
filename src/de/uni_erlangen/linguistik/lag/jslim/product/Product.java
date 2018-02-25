//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.uni_erlangen.linguistik.lag.jslim.product;

import de.uni_erlangen.linguistik.lag.jslim.exception.JSLIMException;
import de.uni_erlangen.linguistik.lag.jslim.lag.ProductConfigFile;
import de.uni_erlangen.linguistik.lag.jslim.project.Project;
import de.uni_erlangen.linguistik.lag.jslim.project.ProjectManager;
import de.uni_erlangen.linguistik.lag.jslim.storage.IDocFunction.FuncSaxHandler;
import de.uni_erlangen.linguistik.lag.jslim.storage.impl.ProductFeature;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class Product implements Iterable<ProductComponent> {
    final String CLASSNAME = this.getClass().getSimpleName();
    String version = "";
    String name = "";
    String namestr = "";
    String date = "";
    String producer = "";
    String programmer = "";
    protected Map<String, ProductComponent> compos = new LinkedHashMap();
    private final Map<String, ProductFeature> features = new TreeMap();
    private final ProjectManager projects = new ProjectManager(this);

    public Product(ProductConfigFile productConfigFile, ProductConfigFile composConfigFile, ProductConfigFile featuresConfigFile) {
        try {
            this.name = productConfigFile.get("name");
            this.namestr = productConfigFile.get("namestr");
            this.version = productConfigFile.get("version");
            this.date = productConfigFile.get("date");
            this.producer = productConfigFile.get("producer");
            this.programmer = productConfigFile.get("programmer");
        } catch (Exception var15) {
            ;
        }

        List<String> failures = new LinkedList();
        Iterator it = composConfigFile.getKeys();

        String name;
        String classname;
        while(it.hasNext()) {
            name = (String)it.next();

            try {
                classname = composConfigFile.get(name);
                this.compos.put(name, (ProductComponent)Class.forName(classname).getConstructor(Product.class).newInstance(this));
            } catch (Exception var14) {
                failures.add(name);
            }
        }

        if (failures.size() > 0) {
            System.err.println("Warning! The following compenents cannot be loaded: " + failures);
            failures.clear();
        }

        it = featuresConfigFile.getKeys();

        while(it.hasNext()) {
            name = (String)it.next();

            try {
                classname = featuresConfigFile.get(name);
                ProductFeature pf = (ProductFeature)Class.forName(classname).newInstance();
                if (!pf.isSelfExplanatory()) {
                    try {
                        FuncSaxHandler sh = new FuncSaxHandler();
                        SAXParserFactory spf = SAXParserFactory.newInstance();
                        SAXParser sp = spf.newSAXParser();
                        spf.setValidating(false);
                        pf.parseXmlProject(sp, sh);
                    } catch (Exception var12) {
                        System.err.println("WARNING !!! Cannot parse xml file for option '" + pf.getName() + "': " + var12.getMessage());
                    }
                }

                this.features.put(name, pf);
            } catch (Exception var13) {
                System.err.println("WARNING !!! Cannot load option '" + name + "': " + var13.getMessage());
            }
        }

        if (failures.size() > 0) {
            System.err.println("Warning! The following features cannot be loaded: " + failures);
            failures.clear();
        }

    }

    public Iterator<ProductComponent> iterator() {
        return this.compos.values().iterator();
    }

    public boolean hasFeature(String name) {
        return this.features.containsKey(name);
    }

    public ProductFeature getFeature(String name) throws JSLIMException {
        if (!this.features.containsKey(name)) {
            throw new JSLIMException("[" + this.CLASSNAME + "] No feature with name: " + name);
        } else {
            return (ProductFeature)this.features.get(name);
        }
    }

    public ProductComponent getComponent(String name) throws JSLIMException {
        if (!this.compos.containsKey(name)) {
            throw new JSLIMException("[" + this.CLASSNAME + "] No component with name: " + name);
        } else {
            ProductComponent compo = (ProductComponent)this.compos.get(name);
            compo.load();
            return compo;
        }
    }

    public void loadProject(String projectName, String projectFile) throws JSLIMException {
        this.projects.load(projectName, projectFile);
    }

    public Project getProject() {
        return this.projects.get();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Components\n=========================================\n");
        Iterator var3 = this.compos.keySet().iterator();

        String name;
        while(var3.hasNext()) {
            name = (String)var3.next();
            sb.append("-" + name + "\n");
        }

        sb.append("Features\n=========================================\n");
        var3 = this.features.keySet().iterator();

        while(var3.hasNext()) {
            name = (String)var3.next();
            sb.append("-" + name + "\n");
        }

        return sb.toString();
    }

    public String getName() {
        return this.name;
    }

    public String[][] getInfo() {
        String[][] data = new String[this.features.size()][2];
        int i = 0;

        String name;
        for(Iterator var4 = this.features.keySet().iterator(); var4.hasNext(); data[i][1] = ((ProductFeature)this.features.get(name)).getNamestr()) {
            name = (String)var4.next();
            data[i][0] = name;
        }

        return data;
    }

    public String getVersion() {
        return this.version;
    }

    public String[][] getHelp() {
        int active = 0;
        Iterator var3 = this.features.values().iterator();

        while(var3.hasNext()) {
            ProductFeature pf = (ProductFeature)var3.next();
            if (pf.isActive()) {
                ++active;
            }
        }

        String[][] data = new String[active][2];
        int ctr = 0;
        Iterator var5 = this.features.keySet().iterator();

        while(var5.hasNext()) {
            String name = (String)var5.next();
            ProductFeature feature = (ProductFeature)this.features.get(name);
            if (feature.isActive()) {
                data[ctr][0] = '-' + name;
                data[ctr][1] = feature.getNamestr();
                ++ctr;
            }
        }

        return data;
    }

    public String getNamestr() {
        return this.namestr;
    }

    public String getSqlFile() { return "wordBank.sql"; }
}
