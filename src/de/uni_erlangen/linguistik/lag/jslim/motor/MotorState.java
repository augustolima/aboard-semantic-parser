//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.uni_erlangen.linguistik.lag.jslim.motor;

import br.ufpe.cin.assistive.SemanticEngine;
import de.uni_erlangen.linguistik.lag.jslim.exception.JSLIMException;
import de.uni_erlangen.linguistik.lag.jslim.lag.LAG;
import de.uni_erlangen.linguistik.lag.jslim.lag.LAGProject.Name;
import de.uni_erlangen.linguistik.lag.jslim.lexicon.LexTrie;
import de.uni_erlangen.linguistik.lag.jslim.storage.IProplet;
import de.uni_erlangen.linguistik.lag.jslim.storage.IState;
import de.uni_erlangen.linguistik.lag.jslim.storage.IAttribute.Role;
import de.uni_erlangen.linguistik.lag.jslim.storage.impl.PropletFormatter;
import org.apache.jena.ontology.Individual;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

public abstract class MotorState {
    protected final Motor motor;
    protected final LAG lag;
    protected MotorState before;
    protected MotorState after;
    protected List<IMotorHook> beforeHooks = new ArrayList();
    protected List<IMotorHook> afterHooks = new ArrayList();
    final LinkedList<SearchState> wellFormedConfirmed = new LinkedList();
    final LinkedList<SearchState> wellFormedUnconfirmed = new LinkedList();
    final LinkedList<SearchState> wellFormedComplete = new LinkedList();
    final LinkedList<SearchState> wellFormedIncomplete = new LinkedList();
    final LinkedList<SearchState> correctedConfirmed = new LinkedList();
    final LinkedList<SearchState> correctedUnconfirmed = new LinkedList();
    final LinkedList<SearchState> correctedComplete = new LinkedList();
    final LinkedList<SearchState> ungrammaticalStart = new LinkedList();
    final LinkedList<SearchState> ungrammaticalContinuation = new LinkedList();
    protected Set<Integer> unrecognized = new HashSet();
    public SearchState searchState = null;
    protected LinkedList<SearchState> recognized = new LinkedList();
    protected final Set<IState> resultStates = new LinkedHashSet();
    protected final int modes;
    protected SemanticEngine semanticEngine;

    public abstract SearchState dequeue();

    public abstract void enqueue(SearchState var1) throws JSLIMException;

    public abstract int agendaSize();

    protected MotorState(Motor motor, LAG lag, int modes) {
        this.lag = lag;
        this.motor = motor;
        this.modes = modes;
    }

    public static MotorState create(Motor motor, LAG lag) throws JSLIMException {
        if (lag.name.equals(Name.LAHEAR)) {
            return new SyntaxMotorState(motor, lag);
        } else if (lag.name.equals(Name.LAMORPH)) {
            return new MorphologyMotorState(motor, lag);
        } else {
            throw new JSLIMException("Unknown LAG: " + lag.name);
        }
    }

    public void setBefore(MotorState motorState) {
        this.before = motorState;
        motorState.after = this;
    }

    public void reset() {
        this.recognized.clear();
        this.unrecognized.clear();
        this.wellFormedComplete.clear();
        this.wellFormedConfirmed.clear();
        this.wellFormedUnconfirmed.clear();
        this.wellFormedIncomplete.clear();
        this.correctedComplete.clear();
        this.correctedConfirmed.clear();
        this.correctedUnconfirmed.clear();
        this.resultStates.clear();
        this.searchState = null;
        this.resultStates.clear();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("before:" + (this.before != null ? this.before.lag.name : "none"));
        sb.append("after:" + (this.after != null ? this.after.lag.name : "none"));
        return sb.toString();
    }

    protected abstract void unknown(SearchState var1, char[] var2) throws JSLIMException;

    protected void eob(SearchState searchState) throws JSLIMException {
        if (this.motor.checkFinalState(searchState)) {
            if ((searchState.mError & 4080) != 0) {
                this.correctedComplete.add(searchState);
            } else {
                this.wellFormedComplete.add(searchState);
            }
        }

    }

    protected int shiftWhitespace(char[] buffer, int index) {
        while(index < buffer.length && Character.isWhitespace(buffer[index])) {
            ++index;
        }

        return index;
    }

    public String getCSVOutput(long id, String input, int[] indices) {
        StringWriter sw = new StringWriter(1024);

        try {
            this.getCSVOutput(id, input, indices, sw);
        } catch (IOException var7) {
            var7.printStackTrace();
        }

        return sw.toString();
    }

    public void getCSVOutput(long id, String input, int[] indices, Writer sw) throws IOException {
        SearchState st;
        Iterator var7;
        if (this.wellFormedConfirmed.size() > 0) {
            var7 = this.wellFormedConfirmed.iterator();

            while(var7.hasNext()) {
                st = (SearchState)var7.next();
                sw.append(st.grammarState.toCSVString(id + "\t" + input + "\tcomplete", indices) + "\n");
            }
        } else if (this.wellFormedUnconfirmed.size() > 0) {
            var7 = this.wellFormedUnconfirmed.iterator();

            while(var7.hasNext()) {
                st = (SearchState)var7.next();
                sw.append(st.grammarState.toCSVString(id + "\t" + input + "\tpossible", indices) + "\n");
            }
        } else if (this.wellFormedComplete.size() > 0) {
            var7 = this.wellFormedComplete.iterator();

            while(var7.hasNext()) {
                st = (SearchState)var7.next();
                sw.append(st.grammarState.toCSVString(id + "\t" + input + "\tcomplete", indices) + "\n");
            }
        } else {
            int i;
            if (this.wellFormedIncomplete.size() > 0) {
                sw.append(id + "\t" + input + "\tincomplete");

                for(i = 0; i < indices.length; ++i) {
                    sw.append("\t");
                }

                sw.append("\tU\n");
            } else {
                sw.append(id + "\t" + input + "\tintermediate");

                for(i = 0; i < indices.length; ++i) {
                    sw.append("\t");
                }

                sw.append("\tU\n");
            }
        }

    }

    public String getOutputSummary() {
        String msg = "";
        if (this.wellFormedConfirmed.size() > 0) {
            msg = "Confirmed (" + this.wellFormedConfirmed.size() + "/" + this.wellFormedUnconfirmed.size() + ")";
        } else if (this.wellFormedUnconfirmed.size() > 0) {
            msg = "Unconfirmed (" + this.wellFormedUnconfirmed.size() + ")";
        } else if (this.wellFormedComplete.size() > 0) {
            msg = "Complete (" + this.wellFormedComplete.size() + ")";
        } else {
            int r;
            if ((r = this.correctedComplete.size()) > 0) {
                msg = "Corrected (" + r + ")";
            } else if ((r = this.correctedConfirmed.size()) > 0) {
                msg = "Corrected confirmed (" + r + ")";
            } else if ((r = this.correctedUnconfirmed.size()) > 0) {
                msg = "Corrected unconfirmed (" + r + ")";
            } else if ((r = this.wellFormedIncomplete.size()) > 0) {
                msg = "No (" + r + ")";
            } else {
                msg = "No";
            }
        }

        return msg;
    }

    public String getOutput(MotorState.Result level) {
        return this.getOutput(this.lag.lagProject.getDefaultPropletFormatter(), level);
    }

    public String getOutput() {
        return this.getOutput(this.lag.lagProject.getDefaultPropletFormatter());
    }

    public String getOutput(PropletFormatter fmt) {
        StringWriter sw = new StringWriter(1024);

        try {
            this.getOutput(fmt, fmt.getResultLevel(), sw);
        } catch (IOException var4) {
            System.err.println(var4.getMessage());
            var4.printStackTrace();
        }

        return sw.toString();
    }

    public String getOutput(PropletFormatter fmt, MotorState.Result level) {
        StringWriter sw = new StringWriter(1024);

        try {
            this.getOutput(fmt, level, sw);
        } catch (IOException var5) {
            System.err.println(var5.getMessage());
            var5.printStackTrace();
        }

        return sw.toString();
    }

    public void getOutput(PropletFormatter fmt, MotorState.Result level, Writer wr) throws IOException {
        boolean nothing = true;
        SearchState ws;
        Iterator var6;
        if (this.wellFormedConfirmed.size() > 0) {
            nothing = false;
            wr.append("\nWell-formed confirmed (" + this.wellFormedConfirmed.size() + ")\n\n");
            var6 = this.wellFormedConfirmed.iterator();

            while(var6.hasNext()) {
                ws = (SearchState)var6.next();
                wr.append(ws.toString(fmt) + "\n");
            }
        }

        if ((nothing || level.ordinal() >= MotorState.Result.Unconfirmed.ordinal()) && this.wellFormedUnconfirmed.size() > 0) {
            nothing = false;
            wr.append("\nWell-formed unconfirmed (" + this.wellFormedUnconfirmed.size() + ")\n\n");
            var6 = this.wellFormedUnconfirmed.iterator();

            while(var6.hasNext()) {
                ws = (SearchState)var6.next();
                wr.append(ws.toString(fmt) + "\n");
            }
        }

        if ((nothing || level.ordinal() >= MotorState.Result.Complete.ordinal()) && this.wellFormedComplete.size() > 0) {
            nothing = false;
            wr.append("\nWell-formed complete (" + this.wellFormedComplete.size() + ")\n\n");
            var6 = this.wellFormedComplete.iterator();

            while(var6.hasNext()) {
                ws = (SearchState)var6.next();
                wr.append(ws.toString(fmt) + "\n");
            }
        }

        if (nothing && this.correctedComplete.size() > 0) {
            nothing = false;
            wr.append("\n");
            int ctr = 0;
            List<SearchState> ls = new ArrayList(this.correctedConfirmed.size() + this.correctedUnconfirmed.size() + this.correctedComplete.size());
            ls.addAll(this.correctedConfirmed);
            ls.addAll(this.correctedUnconfirmed);
            ls.addAll(this.correctedComplete);

            SearchState st;
            String sur;
            String cor;
            String cat;
            String sem;
            for(Iterator var8 = ls.iterator(); var8.hasNext(); wr.append(String.format("\t%2s: %-16s %-16s %-16s %-28s (%-1s)\n", ctr++, sur, cor, cat, sem, LexTrie.flag2str(st.mError)))) {
                st = (SearchState)var8.next();
                IProplet p = st.grammarState.getSS().getProplets()[0];
                sem = "";
                cat = "";
                cor = "";
                sur = "";

                try {
                    sur = p.get(Role.sur).getIStringVal().getString();
                    cor = p.contains(Role.core) ? p.get(Role.core).toString() : "--";
                    cat = p.contains(Role.cat) ? p.get(Role.cat).toString(st.grammarState.getContainer()) : "--";
                    sem = p.contains(Role.sem) ? p.get(Role.sem).toString(st.grammarState.getContainer()) : "--";
                } catch (Exception var15) {
                    var15.printStackTrace();
                }
            }
        }

        if ((nothing || level.ordinal() >= MotorState.Result.Complete.ordinal()) && this.wellFormedIncomplete.size() > 0) {
            nothing = false;
            wr.append("\nWell-formed incomplete\n\n");
            var6 = this.wellFormedIncomplete.iterator();

            while(var6.hasNext()) {
                ws = (SearchState)var6.next();
                wr.append(ws.toString(fmt) + "\n");
            }
        } else if (nothing) {
            if (this.searchState != null) {
                wr.append("\nUngrammatical continuation\n\n");
                wr.append(this.searchState.toString(fmt) + "\n");
            } else {
//                this.ungrammaticalStart.
                wr.append("\nUngrammatical start\n\n");
            }
        }

    }

    public boolean complete() {
        return this.wellFormedComplete.size() > 0;
    }

    public boolean confirmed() {
        return this.wellFormedConfirmed.size() > 0;
    }

    public boolean unconfirmed() {
        return this.wellFormedUnconfirmed.size() > 0;
    }

    public boolean correctedConfirmed() {
        return this.correctedConfirmed.size() > 0;
    }

    public boolean correctedUnconfirmed() {
        return this.correctedUnconfirmed.size() > 0;
    }

    public boolean correctedComplete() {
        return this.correctedComplete.size() > 0;
    }

    public boolean incomplete() {
        return this.wellFormedIncomplete.size() > 0;
    }

//    public boolean ungrammaticalStart() { return this.ungrammaticalStart.size() > 0; }

//    public boolean ungrammaticalContinuation() { return this.ungrammaticalContinuation.size() > 0; }

    public int getOutputSize() {
        if (this.wellFormedConfirmed.size() > 0) {
            return this.wellFormedConfirmed.size();
        } else if (this.wellFormedUnconfirmed.size() > 0) {
            return this.wellFormedUnconfirmed.size();
        } else if (this.wellFormedComplete.size() > 0) {
            return this.wellFormedComplete.size();
        } else if (this.correctedConfirmed.size() > 0) {
            return this.correctedConfirmed.size();
        } else if (this.correctedUnconfirmed.size() > 0) {
            return this.correctedUnconfirmed.size();
        } else {
            return this.correctedComplete.size() > 0 ? this.correctedComplete.size() : 0;
        }
    }

    public void addSemanticEngine(SemanticEngine semanticEngine) {
        this.semanticEngine = semanticEngine;
    }

    public Map<String, ArrayList<String>> getRelations() {
        return this.semanticEngine.getRelations();
    }

//    public ArrayList<Individual> getRelationFromSur(String sur) {
//        return this.semanticEngine.getCandidatesFromSur(sur);
//    }

    public ArrayList<Individual> getCandidates(Map<String, ArrayList<String>> className) {
        return this.semanticEngine.getCandidates(className);
    }

    public static enum Result {
        Confirmed,
        Unconfirmed,
        Complete,
        Incomplete,
        CorrectedConfirmed,
        CorrectedUnconfirmed,
        CorrectedComplete,
        UngrammaticalContinuation,
        UngrammaticalStart;

        private Result() {
        }
    }
}
