//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.uni_erlangen.linguistik.lag.jslim.motor;

import br.ufpe.cin.assistive.SemanticEngine;
import de.uni_erlangen.linguistik.lag.jslim.exception.JSLIMException;
import de.uni_erlangen.linguistik.lag.jslim.exception.ValException;
import de.uni_erlangen.linguistik.lag.jslim.lag.LAG;
import de.uni_erlangen.linguistik.lag.jslim.lag.LAGProject;
import de.uni_erlangen.linguistik.lag.jslim.lexicon.LexTrieEntry;
import de.uni_erlangen.linguistik.lag.jslim.lexicon.LexTrieSegmenter;
import de.uni_erlangen.linguistik.lag.jslim.storage.*;
import de.uni_erlangen.linguistik.lag.jslim.storage.IHasRulePackage.RulePackage;
import de.uni_erlangen.linguistik.lag.jslim.storage.impl.Allocation;
import java.util.Iterator;
import java.util.ListIterator;

public class Motor implements IMotor {
    public static boolean seg = false;
    public static final int SEGM_DEF = 0;
    public static final int SEGM_IC = 1;
    public static final int SEGM_ROBUST = 2;
    final int LEXICAL_AMBIGUITY_MAX = 1024;
    protected final LAGProject mLagProject;
    protected final IValFactory mFactory;
    protected char[] buffer;
    protected MotorState currentMotorState;
    protected final LexTrieEntry[] entries = new LexTrieEntry[1024];
    final int[] indices = new int[1024];
    final int[] flags = new int[1024];
    protected SemanticEngine semanticEngine = new SemanticEngine();

    public Motor(LAGProject lagProject) throws JSLIMException {
        this.mLagProject = lagProject;
        this.mFactory = lagProject.valueFactory;
        MotorInitializer.init(lagProject, this);
        this.currentMotorState.addSemanticEngine(this.semanticEngine);
    }

    public final void reset() {
        for(MotorState ms = this.currentMotorState; ms != null; ms = ms.before) {
            ms.reset();
        }

    }

    public final MotorState parse(String input) throws JSLIMException {
        this.buffer = input.toCharArray();
        this.parse(0);
        return this.currentMotorState;
    }

    private final void parse(int index) throws JSLIMException {
        this.reset();
        index = this.currentMotorState.shiftWhitespace(this.buffer, index);
        IRuleModel rmodel = this.currentMotorState.lag.getRuleModel();

        for(int mode = 0; mode < this.currentMotorState.modes; ++mode) {
            int flag = this.recognize(rmodel.getStartStates(), index, 2, mode);
            if (flag != 0) {
                this.applyStartState();
                if (this.currentMotorState.agendaSize() != 0) {
                    this.processAgenda(mode);
                    this.afterHooks();
                    if (LexTrieSegmenter.SEC_MODE < 2 || this.currentMotorState.wellFormedComplete.size() > 0 || this.currentMotorState.wellFormedConfirmed.size() > 0 || this.currentMotorState.wellFormedUnconfirmed.size() > 0) {
                        return;
                    }
                }
            }
        }

    }

    private final void processAgenda(int mode) throws JSLIMException {
        while(this.currentMotorState.agendaSize() != 0) {
            this.currentMotorState.searchState = this.currentMotorState.dequeue();
            if (this.currentMotorState.searchState.index >= this.buffer.length) {
                this.currentMotorState.eob(this.currentMotorState.searchState);
            } else {
                int rmode = this.currentMotorState.searchState.mError != 0 ? 0 : mode;
                int num = this.recognize(this.currentMotorState.searchState.grammarState.getRP().rules, this.currentMotorState.searchState.index, 2, rmode);
                switch(num) {
                    case 0:
                        this.currentMotorState.unknown(this.currentMotorState.searchState, this.buffer);
                        break;
                    default:
                        int count = this.currentMotorState.agendaSize();
                        IState currentState = this.currentMotorState.searchState.grammarState;

                        for(int lex = 0; !this.currentMotorState.recognized.isEmpty(); ++lex) {
                            SearchState newState = (SearchState)this.currentMotorState.recognized.poll();
                            IProplet proplet = newState.grammarState.getSS().get(0);
                            newState.grammarState.setSS(currentState.getSS().flatCopy());
                            newState.grammarState.getSS().setNW(proplet);
                            newState.grammarState.setGlobals(currentState.getGlobals().flatCopy(this.mLagProject));
                            newState.grammarState.getContainer().add(currentState.getContainer());
                            newState.grammarState.setRP(currentState.getRP());
                            newState.mMWUInfo = this.currentMotorState.searchState.mMWUInfo;
                            newState.mError += this.currentMotorState.searchState.mError;
                            this.beforeHooks(newState);
                            this.addNewStates(newState, lex);
                        }
                }
            }
        }

    }

    final boolean checkFinalState(SearchState searchState) throws JSLIMException {
        IState state = searchState.grammarState;
        LAG lag = this.currentMotorState.lag;
        RulePackage rp = state.getRP();
        IFinalState[] var8;
        int var7 = (var8 = lag.getRuleModel().getFinalStates()).length;

        for(int var6 = 0; var6 < var7; ++var6) {
            IFinalState finalState = var8[var6];
            if (finalState.isRPRef(rp)) {
                if (-1 != state.matches(finalState)) {
                    state.getAllocation().reset();
                    return true;
                }

                state.getAllocation().reset();
            }
        }

        return false;
    }

    private final int recognize(IRule[] rules, int index, int smode, int rmode) throws JSLIMException {
        return this.currentMotorState.before == null ? this.recognizeLex(rules, (MWUInfo)null, (IMulticatContainer)null, index, smode, rmode) : this.recognizeLAG(rules, index);
    }

    protected int recognizeLex(IRule[] rules, MWUInfo info, IMulticatContainer container, int index, int smode, int rmode) throws JSLIMException {
        int num = this.currentMotorState.lag.lexicon.getLex(this.mLagProject, this.buffer, index, this.entries, this.indices, this.flags, smode, rmode);
        switch(num) {
            case 0:
                return 0;
            default:
                int i = 0;

                label72:
                for(; i < num; ++i) {

                    if (this.entries[i].getProplet(this.mLagProject).contains(IAttribute.Role.ontology)) {
//                        System.out.println("this.entries[i] get relations");
                        semanticEngine.addRelations(this.entries[i].getProplet(this.mLagProject));
//                        System.out.println(semanticEngine.getRelations());
//                        System.out.println("\n\n");
                    }

//                    this.currentMotorState.setProplet(this.entries[i].getProplet(this.mLagProject));
                    long nwsig = 0L;
                    int[] var15;
                    int var14 = (var15 = this.entries[i].stencil.templateIndices).length;

                    int attr;
                    int var13;
                    for(var13 = 0; var13 < var14; ++var13) {
                        attr = var15[var13];
                        nwsig |= (long)(1 << attr);
                    }

                    var14 = (var15 = this.entries[i].stencil.instanceIndices).length;

                    for(var13 = 0; var13 < var14; ++var13) {
                        attr = var15[var13];
                        nwsig |= (long)(1 << attr);
                    }

                    IRule[] var21 = rules;
                    var14 = rules.length;

                    for(var13 = 0; var13 < var14; ++var13) {
                        IRule rule = var21[var13];
                        long sig = rule.getNWSignature();
                        if ((sig & nwsig) == sig) {
                            if (seg) {
                                System.out.println("segm: " + this.entries[i].attrs[0]);
                            }

                            SearchState searchState = SearchState.create(this.entries[i], info == null ? info : info.flatCopy(), container, this.indices[i], this.flags[i], this.mLagProject, this.currentMotorState.lag.createAllocation());
                            Iterator var19 = this.currentMotorState.beforeHooks.iterator();

                            while(var19.hasNext()) {
                                IMotorHook hook = (IMotorHook)var19.next();
                                hook.execute(searchState);
                            }

                            this.currentMotorState.recognized.add(searchState);
                            if (info != null) {
                                break;
                            }

                            int j = 0;

                            while(true) {
                                if (j >= this.entries[i].getMWUCount()) {
                                    continue label72;
                                }


                                searchState = SearchState.create(this.entries[i], this.entries[i].getMWUInfo(j).flatCopy(), container, this.indices[i], this.flags[i], this.mLagProject, this.currentMotorState.lag.createAllocation());
                                this.currentMotorState.recognized.add(searchState);
                                ++j;
                            }
                        }
                    }
                }

                return this.currentMotorState.recognized.size();
        }
    }

    private int recognizeLAG(IRule[] rules, int index) throws JSLIMException {
        this.currentMotorState = this.currentMotorState.before;

        try {
            this.parse(index);
        } finally {
            this.currentMotorState = this.currentMotorState.after;
        }

        Iterator var4 = this.currentMotorState.before.wellFormedComplete.iterator();

        SearchState searchState;
        while(var4.hasNext()) {
            searchState = (SearchState)var4.next();
            searchState.grammarState.setAllocation(new Allocation(this.currentMotorState.lag.allocModel));
            this.currentMotorState.recognized.add(searchState);
        }

        var4 = this.currentMotorState.before.wellFormedConfirmed.iterator();

        while(var4.hasNext()) {
            searchState = (SearchState)var4.next();
            searchState.grammarState.setAllocation(new Allocation(this.currentMotorState.lag.allocModel));
            this.currentMotorState.recognized.add(searchState);
        }

        var4 = this.currentMotorState.before.wellFormedUnconfirmed.iterator();

        while(var4.hasNext()) {
            searchState = (SearchState)var4.next();
            searchState.grammarState.setAllocation(new Allocation(this.currentMotorState.lag.allocModel));
            this.currentMotorState.recognized.add(searchState);
        }

        return this.currentMotorState.recognized.size();
    }

    private void afterHooks() throws JSLIMException {
        for(ListIterator it = this.currentMotorState.wellFormedComplete.listIterator(); it.hasNext(); it.remove()) {
            SearchState searchState = (SearchState)it.next();
            boolean succeed = true;

            IMotorHook hook;
            for(Iterator var5 = this.currentMotorState.afterHooks.iterator(); var5.hasNext(); succeed &= hook.execute(searchState)) {
                hook = (IMotorHook)var5.next();
            }

            if (!succeed) {
                this.currentMotorState.wellFormedUnconfirmed.add(searchState);
            } else {
                this.currentMotorState.wellFormedConfirmed.add(searchState);
            }
        }

    }

    private final boolean beforeHooks(SearchState searchState) throws JSLIMException {
        Iterator var3 = this.currentMotorState.beforeHooks.iterator();

        while(var3.hasNext()) {
            IMotorHook hook = (IMotorHook)var3.next();
            if (!hook.execute(searchState)) {
                return false;
            }
        }

        return true;
    }

    final void applyStartState() throws JSLIMException {
        LAG lag = this.currentMotorState.lag;
        int lex = 0;

        for(Iterator it = this.currentMotorState.recognized.iterator(); it.hasNext(); ++lex) {
            SearchState searchState = (SearchState)it.next();
            this.beforeHooks(searchState);
            it.remove();
            IState grammarState = searchState.grammarState;
            int rul = 0;
            IMulticatContainer container = grammarState.getContainer();
            IStartState[] var11;
            int var10 = (var11 = lag.getRuleModel().getStartStates()).length;

            for(int var9 = 0; var9 < var10; ++var9) {
                IRule statePattern = var11[var9];
                ++rul;
                IState state = grammarState.flatCopy();
                state.setContainer(container.deepCopy(lag.lagProject));
                if (state.matches(statePattern) >= 0) {
                    state.getAllocation().reset();
                    state.setRP(statePattern.getRP());
                    searchState.grammarState = state;
                    this.currentMotorState.enqueue(searchState);
                }
            }
        }

    }

    protected void addNewStates(SearchState searchState, int lex) throws JSLIMException {
        this.currentMotorState.searchState = searchState;
        IState grammarState = searchState.grammarState;

        try {
            IRule[] rp = grammarState.getRP().rules;
            int rul = 0;
            IMulticatContainer container = grammarState.getContainer();
            IRule[] var10 = rp;
            int var9 = rp.length;

            for(int var8 = 0; var8 < var9; ++var8) {
                IRule rule = var10[var8];
                IState state = grammarState.flatCopy();
                state.setContainer(container.deepCopy(rule.getClauses()[0].getLAG().lagProject));
                int clause;
                if (-1 != (clause = state.matches(rule))) {
                    MWUInfo info = searchState.mMWUInfo;
                    if (info != null) {
                        info = info.flatCopy();
                    }

                    if (rule.execute(state, info, clause)) {
                        SearchState newState = new SearchState(state, searchState);
                        this.currentMotorState.enqueue(newState);
                    }
                }

                ++rul;
            }

        } catch (Exception var15) {
            var15.printStackTrace();
            throw new ValException("[Motor.addNewStates] Cannot add new state to agenda:\n\n" + searchState, var15);
        }
    }

    public void setLAG(LAG lag) {
        MotorState ms;
        for(ms = this.currentMotorState; ms != null; ms = ms.after) {
            if (ms.lag == lag) {
                this.currentMotorState = ms;
                return;
            }
        }

        for(ms = this.currentMotorState.before; ms != null; ms = ms.before) {
            if (ms.lag == lag) {
                this.currentMotorState = ms;
                return;
            }
        }

    }

    public SemanticEngine getSemanticEngine() {
        return this.semanticEngine;
    }
}
