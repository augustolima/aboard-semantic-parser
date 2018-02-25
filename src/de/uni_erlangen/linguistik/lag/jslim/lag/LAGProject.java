//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.uni_erlangen.linguistik.lag.jslim.lag;

import de.uni_erlangen.linguistik.lag.jslim.exception.JSLIMException;
import de.uni_erlangen.linguistik.lag.jslim.exception.ValException;
import de.uni_erlangen.linguistik.lag.jslim.motor.Motor;
import de.uni_erlangen.linguistik.lag.jslim.motor.MotorState;
import de.uni_erlangen.linguistik.lag.jslim.product.Product;
import de.uni_erlangen.linguistik.lag.jslim.project.Project;
import de.uni_erlangen.linguistik.lag.jslim.storage.IAttributeModel;
import de.uni_erlangen.linguistik.lag.jslim.storage.IOperationModel;
import de.uni_erlangen.linguistik.lag.jslim.storage.ITableModel;
import de.uni_erlangen.linguistik.lag.jslim.storage.IValFactory;
import de.uni_erlangen.linguistik.lag.jslim.storage.IWordBank;
import de.uni_erlangen.linguistik.lag.jslim.storage.IXmlProject;
import de.uni_erlangen.linguistik.lag.jslim.storage.impl.AttributeModel;
import de.uni_erlangen.linguistik.lag.jslim.storage.impl.OperationModel;
import de.uni_erlangen.linguistik.lag.jslim.storage.impl.PropletFormatter;
import de.uni_erlangen.linguistik.lag.jslim.storage.rule.PathFactory;
import de.uni_erlangen.linguistik.lag.jslim.storage.table.TableModel;
import de.uni_erlangen.linguistik.lag.jslim.storage.value.ValFactory;
import de.uni_erlangen.linguistik.lag.jslim.symbol.SymbolModel;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class LAGProject implements IXmlProject {
    public final Project project;
    public final SymbolModel symModel;
    public final IAttributeModel attributeModel;
    public final IOperationModel operationModel;
    public final ITableModel tableModel;
    protected final Map<LAGProject.Name, LAG> lags = new HashMap();
    protected LAGProject.Name selected;
    public final IValFactory valueFactory;
    public final PathFactory pathFactory = new PathFactory(this);
    private Motor motor;
    private IWordBank wordBank;
    private PropletFormatter fmt;

    public LAGProject(Project project) throws JSLIMException {
        this.project = project;
        this.operationModel = new OperationModel(this);
        this.attributeModel = new AttributeModel(this);
        this.tableModel = new TableModel(this);
        this.valueFactory = new ValFactory(this);
        this.symModel = SymbolModel.create();
    }

    public final void init(boolean cs) throws JSLIMException {
        int flag = 0;

        int i;
        for(i = 0; i < LAGProject.Name.values().length; ++i) {
            if (this.lags.containsKey(LAGProject.Name.values()[i])) {
                flag |= 1 << i;
            }
        }

        i = flag % 4;
        LAG lag;
        switch(i) {
            case 1:
                lag = (LAG)this.lags.get(LAGProject.Name.LAMORPH);
                lag.setLexicon(new MorLexicon(cs));
                break;
            case 2:
                lag = (LAG)this.lags.get(LAGProject.Name.LAHEAR);
                lag.setLexicon(new SynLexicon());
                break;
            case 3:
                lag = (LAG)this.lags.get(LAGProject.Name.LAMORPH);
                lag.setLexicon(new MorLexicon(cs));
                break;
            default:
                throw new JSLIMException("Neither morphology nor syntax");
        }

        this.motor = new Motor(this);

        try {
            Product product = this.project.getProduct();
            if (product.getComponent("bank") != null) {
                this.wordBank = null;
            } else {
                this.wordBank = null;
            }
        } catch (Exception var6) {
            System.err.println(var6.getMessage());
            this.wordBank = null;
        }

    }

    public Project getProject() {
        return this.project;
    }

    public LAG getLAG() throws JSLIMException {
        if (this.selected == null) {
            throw new JSLIMException("No LAG selected, the project certainly has not been correctly initialized");
        } else {
            LAG lag;
            if ((lag = (LAG)this.lags.get(this.selected)) == null) {
                throw new JSLIMException("No LAG selected, the project certainly has not been correctly initialized");
            } else {
                return lag;
            }
        }
    }

    public boolean isLAG(LAGProject.Name name) {
        return this.lags.get(name) != null;
    }

    public LAG getLAG(LAGProject.Name name) throws JSLIMException {
        LAG lag;
        if ((lag = (LAG)this.lags.get(name)) == null) {
            throw new JSLIMException("No LAG with name " + name);
        } else {
            return lag;
        }
    }

    public LAG setSelectedLAG(LAGProject.Name name) throws JSLIMException {
        if (!this.lags.containsKey(name)) {
            throw new JSLIMException("[" + this.getClass().getSimpleName() + "] No lag with name " + name + "!");
        } else if (this.selected == name) {
            return (LAG)this.lags.get(this.selected);
        } else {
            LAGProject.Name previous = this.selected;
            if (this.selected != null) {
                this.getLAG(previous).onSelectLost();
            }

            this.selected = name;
            LAG lag;
            (lag = this.getLAG(this.selected)).onSelect();
            if (this.motor != null) {
                this.motor.setLAG(lag);
            }

            return lag;
        }
    }

    public LAG setLAG(LAGProject.Name name, LAG lag) throws ValException {
        this.lags.put(name, lag);
        return lag;
    }

    public void addLAG(LAGProject.Name name) throws ValException {
        if (!this.lags.containsKey(name)) {
            this.setLAG(name, new LAG(this, name));
        }

    }

    public Motor getMotor() {
        return this.motor;
    }

    public IValFactory getValFactory() {
        return this.valueFactory;
    }

    public IWordBank getWordBank() {
        return this.wordBank;
    }

    public ITableModel getTableModel() {
        return this.tableModel;
    }

    public IAttributeModel getAttributeModel() {
        return this.attributeModel;
    }

    public IOperationModel getOperationModel() {
        return this.operationModel;
    }

    public MotorState parse(String text) throws JSLIMException {
        return this.motor.parse(text);
    }

    public final void xmlProject() throws JSLIMException {
        try {
            PrintWriter xmlProjectWriter = new PrintWriter(new BufferedWriter(new FileWriter(this.project.runtime.getProjectTmpFile(this.project, "lagproject.xml"))));
            this.xmlProject(xmlProjectWriter);
            xmlProjectWriter.close();
        } catch (IOException var2) {
            throw new JSLIMException("[" + this.getClass().getSimpleName() + "] Cannot create xml project!", var2);
        }
    }

    public void xmlProject(PrintWriter writer) {
        writer.append("<project>\n");
        LAG lag;
        if ((lag = (LAG)this.lags.get(LAGProject.Name.LAMORPH)) != null) {
            lag.xmlProject(writer);
        }

        if ((lag = (LAG)this.lags.get(LAGProject.Name.LAHEAR)) != null) {
            lag.xmlProject(writer);
        }

        if ((lag = (LAG)this.lags.get(LAGProject.Name.LATHINK)) != null) {
            lag.xmlProject(writer);
        }

        if ((lag = (LAG)this.lags.get(LAGProject.Name.LASPEAK)) != null) {
            lag.xmlProject(writer);
        }

        writer.append("</project>\n");
    }

    public void reset() {
        this.lags.clear();
        this.motor.reset();
        this.attributeModel.reset();
        this.tableModel.reset();
    }

    public String getEncoding() {
        try {
            return this.project.getProjectDescription().encoding;
        } catch (Exception var2) {
            return "UTF-8";
        }
    }

    public Map<LAGProject.Name, LAG> getLAGs() {
        return this.lags;
    }

    public PropletFormatter getDefaultPropletFormatter() {
        if (this.fmt == null) {
            this.fmt = new PropletFormatter(this);
        }

        return this.fmt;
    }

    public PropletFormatter createPropletFormatter() {
        return new PropletFormatter(this);
    }

    public static enum Name {
        LAMORPH,
        LAHEAR,
        LATHINK,
        LASPEAK,
        COMMON;

        private Name() {
        }
    }
}
