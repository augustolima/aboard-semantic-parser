//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.uni_erlangen.linguistik.lag.jslim.storage;

import de.uni_erlangen.linguistik.lag.jslim.exception.JSLIMException;
import de.uni_erlangen.linguistik.lag.jslim.exception.ValException;

public interface IAttribute extends IXmlProject {
    String getName();

    int getIndex();

    int getType();

    boolean isReadonly();

    void setReadonly(boolean var1);

    boolean isVisible();

    void setVisible(boolean var1);

    boolean isIgnored();

    void setIgnore(boolean var1);

    IVal getMetadatum(String var1);

    void setMetadatum(String var1, IVal var2) throws JSLIMException;

    void addRole(IAttribute.Role var1) throws ValException;

    void rmvRole(IAttribute.Role var1);

    public static enum Role {
        sur,
        core,
        syn,
        allo,
        flx,
        seg,
        cas,
        sec,
        mwc,
        mwu,
        mwh,
        verb,
        noun,
        adj,
        adn,
        adv,
        arg,
        fnc,
        mdd,
        mdr,
        nc,
        pc,
        cat,
        sem,
        dbg,
        wrd,
        prn,
        idy,
        ontology;

        private Role() {
        }
    }
}
