//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.uni_erlangen.linguistik.lag.jslim.lexicon;

import de.uni_erlangen.linguistik.lag.jslim.exception.JSLIMException;
import de.uni_erlangen.linguistik.lag.jslim.lag.LAGProject;
import de.uni_erlangen.linguistik.lag.jslim.motor.MWUInfo;
import de.uni_erlangen.linguistik.lag.jslim.storage.IMulticatContainer;
import de.uni_erlangen.linguistik.lag.jslim.storage.IProplet;
import de.uni_erlangen.linguistik.lag.jslim.storage.IVal;
import de.uni_erlangen.linguistik.lag.jslim.storage.IAttribute.Role;
import de.uni_erlangen.linguistik.lag.jslim.storage.impl.Proplet;
import de.uni_erlangen.linguistik.lag.jslim.storage.value.StringVal;
import java.io.Serializable;

public class LexTrieEntry implements Serializable {
    private static final long serialVersionUID = -2760202682454160223L;
    public IVal[] attrs;
    public final LexTrieEntryStencil stencil;
    public LexTrieEntry next;
    public static StringVal tuc = new StringVal("to_ucase");
    public static StringVal tlc = new StringVal("to_lcase");
    public static StringVal uc = new StringVal("ucase");
    public static StringVal lc = new StringVal("lcase");
    public static StringVal ic = new StringVal("ignore_case");
    public static StringVal no = new StringVal("no_change");
    public static StringVal delete = new StringVal("delete");
    public static StringVal insert = new StringVal("insert");
    public static StringVal replace = new StringVal("replace");
    public static StringVal transpose = new StringVal("transpose");

    public LexTrieEntry(IVal[] vals, LexTrieEntryStencil stencil) {
        this.attrs = vals;
        this.stencil = stencil;
    }

    public int getMWUCount() {
        return this.attrs.length - this.stencil.instanceIndices.length;
    }

    public MWUInfo getMWUInfo(int j) {
        int offset = this.stencil.instanceIndices.length;
        return (MWUInfo)this.attrs[offset + j];
    }

    public IProplet getProplet(LAGProject lagProject) throws JSLIMException {
        IProplet proplet = new Proplet(lagProject);

        int i;
        for(i = 0; i < this.stencil.templateIndices.length; ++i) {
            proplet.set(this.stencil.templateIndices[i], this.stencil.templateValues[i].deepCopy(lagProject));
        }

        for(i = 0; i < this.stencil.instanceIndices.length; ++i) {
            proplet.set(this.stencil.instanceIndices[i], this.attrs[i].deepCopy(lagProject));
        }

        return proplet;
    }

    public IProplet getProplet(LAGProject lagProject, int flag, IMulticatContainer container) throws JSLIMException {
        IProplet proplet = new Proplet(lagProject);
        ValTraverser traverser = new ValTraverser(lagProject, container);

        int cas;
        for(cas = 0; cas < this.stencil.templateIndices.length; ++cas) {
            proplet.set(this.stencil.templateIndices[cas], traverser.traverse(this.stencil.templateValues[cas]));
        }

        for(cas = 0; cas < this.stencil.instanceIndices.length; ++cas) {
            proplet.set(this.stencil.instanceIndices[cas], traverser.traverse(this.attrs[cas]));
        }

        cas = flag & 15;
        String surface;
        switch(cas) {
            case 0:
                String str = proplet.get(Role.sur).getIStringVal().getString();
                if (!str.isEmpty()) {
                    if (Character.isLowerCase(str.charAt(0))) {
                        proplet.set(Role.cas, lc);
                    } else if (Character.isUpperCase(str.charAt(0))) {
                        proplet.set(Role.cas, uc);
                    } else {
                        proplet.set(Role.cas, no);
                    }
                }
                break;
            case 1:
                proplet.set(Role.cas, tuc);
                surface = proplet.get(Role.sur).getIStringVal().getString();
                if (!surface.equals("")) {
                    proplet.set(Role.sur, new StringVal(surface.substring(0, 1).toLowerCase() + surface.substring(1)));
                }
                break;
            case 2:
                proplet.set(Role.cas, tlc);
                surface = proplet.get(Role.sur).getIStringVal().getString();
                if (!surface.equals("")) {
                    proplet.set(Role.sur, new StringVal(surface.substring(0, 1).toUpperCase() + surface.substring(1)));
                }
                break;
            case 3:
                proplet.set(Role.cas, ic);
                break;
            default:
                proplet.set(Role.cas, no);
        }

        int sec = flag & 240;
        switch(sec) {
            case 16:
                proplet.set(Role.sec, delete);
                break;
            case 32:
                proplet.set(Role.sec, insert);
                break;
            case 64:
                proplet.set(Role.sec, replace);
                break;
            case 128:
                proplet.set(Role.sec, transpose);
        }

//        if (proplet.contains(Role.ontology)) {
//            System.out.println("CAIU AQUI CARALHO");
//            System.out.println(proplet.get(Role.ontology));
//        }
        if (proplet.contains(Role.core) && !proplet.contains(Role.seg)) {
            proplet.set(Role.seg, proplet.get(Role.core).flatCopy(lagProject));
        } else if (proplet.contains(Role.noun) && !proplet.contains(Role.seg)) {
            proplet.set(Role.seg, proplet.get(Role.noun).flatCopy(lagProject));
        } else if (proplet.contains(Role.verb) && !proplet.contains(Role.seg)) {
            proplet.set(Role.seg, proplet.get(Role.verb).flatCopy(lagProject));
        } else if (proplet.contains(Role.adn) && !proplet.contains(Role.seg)) {
            proplet.set(Role.seg, proplet.get(Role.adn).flatCopy(lagProject));
        } else if (proplet.contains(Role.adv) && !proplet.contains(Role.seg)) {
            proplet.set(Role.seg, proplet.get(Role.adv).flatCopy(lagProject));
        } else {
            proplet.set(Role.seg, proplet.get(Role.sur).flatCopy(lagProject));
        }

        return proplet;
    }

    public String toString() {
        return this.stencil.toString();
    }
}
