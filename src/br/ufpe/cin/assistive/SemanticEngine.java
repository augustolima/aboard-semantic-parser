package br.ufpe.cin.assistive;

import de.uni_erlangen.linguistik.lag.jslim.compo.wb.WordBank;
import de.uni_erlangen.linguistik.lag.jslim.compo.wb.WordBankDAO;
import de.uni_erlangen.linguistik.lag.jslim.exception.JSLIMException;
import de.uni_erlangen.linguistik.lag.jslim.storage.IAttribute;
import de.uni_erlangen.linguistik.lag.jslim.storage.IListVal;
import de.uni_erlangen.linguistik.lag.jslim.storage.IProplet;
import de.uni_erlangen.linguistik.lag.jslim.storage.IVal;
import org.apache.jena.ontology.Individual;

import java.sql.SQLException;
import java.util.*;

public class SemanticEngine {

    protected Map<String, ArrayList<String>> relations = new HashMap();
    protected OntologyEngine ontologyEngine;
    protected WordBankDAO wb;

    public SemanticEngine() throws JSLIMException {
        this.ontologyEngine = new OntologyEngine();
        this.wb = new WordBankDAO();
    }

    public WordBankDAO getWordBank() { return this.wb; }

    public void addRelations(IProplet iProplet) throws JSLIMException {

        IListVal listVal = iProplet.get(IAttribute.Role.ontology).getIListVal();
        IVal sur = iProplet.get(IAttribute.Role.sur);
//        IVal core = iProplet.get(IAttribute.Role.core);
//        System.out.println("sur: " + sur.toString());

        if (!relations.containsKey(sur.toString())) {
            relations.put(sur.toString(), new ArrayList());
        }

        for (IVal val : listVal._getValues()) {
            if (!relations.get(sur.toString()).contains(val.toString())) {
                relations.get(sur.toString()).add(val.toString());
            }
        }
    }

    public Map<String, ArrayList<String>> getRelations() {
        return this.relations;
    }

    public ArrayList<Individual> getCandidates(Map<String, ArrayList<String>> classNames) {
        return this.ontologyEngine.getCandidates(classNames);
    }

    public ArrayList<Individual> getAgentCandidatesFromSur(String sur) {
        ArrayList<Individual> candidates = new ArrayList<>();
        for (String relation : getRelationsFromSur(sur)) {
            candidates.addAll(this.ontologyEngine.getCandidatesFromAgent(relation));
        }
        return candidates;
    }

    public ArrayList<Individual> getActionCandidatesFromSur(String sur) {
        ArrayList<Individual> candidates = new ArrayList<>();
        for (String relation : getRelationsFromSur(sur)) {
            candidates.addAll(this.ontologyEngine.getCandidatesFromAction(relation));
        }
        return candidates;
    }

    public ArrayList<Individual> getPatientCandidatesFromSur(String sur) {
        System.out.println("getPatientCandidatesFromSur: " + sur);
        ArrayList<Individual> candidates = new ArrayList<>();
        for (String relation : getRelationsFromSur(sur)) {
            candidates.addAll(this.ontologyEngine.getCandidatesFromPatient(relation));
        }
        return candidates;
    }

    public ArrayList<Individual> getPlaceCandidatesFromSur(String sur) {
        ArrayList<Individual> candidates = new ArrayList<>();
        for (String relation : getRelationsFromSur(sur)) {
            candidates.addAll(this.ontologyEngine.getCandidatesFromPlace(relation));
        }
        return candidates;
    }

    public ArrayList<String> getRelationsFromSur(String sur) {
        return this.relations.get(sur);
    }

    public List<String> getFramesRelations(String core, List<String> semantics) { return this.wb.getFramesRelations(core, semantics); }

    public List<String> getFirstCandidates(String pos) throws SQLException {
        return this.wb.getFirstCandidates(pos);
    }

    public ArrayList<Individual> getWhoCSPart() {
        return this.ontologyEngine.getWhoCSPartCandidates();
    }

    public List<String> getAllInstancesFromLinguisticOntology() {
        return this.ontologyEngine.getAllInstancesFromLinguisticOntology();
    }

    public List<String> getNextCandidates(Option last, Option current) {
        List<String> classes = new ArrayList<>();
//        List<String> candidates = new ArrayList<>();
        for (Frame frame : last.getRelations()) {
            if (frame.isActive()) {
                for (String classe : this.ontologyEngine.getSemanticClasses(frame.getName(), current.getCsPart())) {
                    if (!classes.contains(classe)) {
                        classes.add(classe);
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < classes.size(); i++) {
            if ((i + 1) < classes.size()) {
                sb.append("'" + classes.get(i) + "', ");
            } else {
                sb.append("'" + classes.get(i) + "'");
            }
        }

        return this.wb.getCandidates(sb.toString());
    }
}
