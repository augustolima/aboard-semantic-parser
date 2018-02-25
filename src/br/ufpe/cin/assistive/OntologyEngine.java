package br.ufpe.cin.assistive;

import org.apache.jena.ontology.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class OntologyEngine {

    /***********************************/
    /* Constants                       */
    /***********************************/

    // Directory where we've stored the local data files, such as assistive.owl
    public static final String SOURCE = "./src/english1-syn/";

    // Pizza ontology namespace
    public static final String ASSISTIVE_NS = "http://www.semanticweb.org/nmf/ontologies/2017/11/untitled-ontology-26#";

    /***********************************/
    /* Static variables                */
    /***********************************/
    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( OntologyEngine.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/
    private String prefix;
    private OntModel ontModel;

    /***********************************/
    /* Constructors                    */
    /***********************************/
    public OntologyEngine() {
//        System.out.println("Starting ontology engine");
        this.ontModel = getModel();
        loadData( this.ontModel );
        this.prefix = "prefix assistive: <" + ASSISTIVE_NS + ">\n" +
                "prefix rdfs: <" + RDFS.getURI() + ">\n" +
                "prefix owl: <" + OWL.getURI() + ">\n" +
                "prefix rdf: <" + RDF.getURI() + ">\n";
    }

    protected OntModel getModel() {
        return ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
    }

    protected void loadData( Model model ) {
        FileManager.get().readModel( model, SOURCE + "assistive2.owl", "RDF/XML" );
    }

    public ArrayList<Individual> getCandidates(Map<String, ArrayList<String>> relations) {
        ArrayList<ArrayList> instances = new ArrayList<>();

        relations.forEach( (key, value) -> {
            for (String relation : relations.get(key)) {
                instances.add(getInstancesOfClass(relation));
            }
        } );

        //flatten instances array
        return (ArrayList<Individual>) instances.stream().flatMap(individual -> individual.stream()).collect(Collectors.toList());
    }

    public ArrayList<Individual> getCandidates(ArrayList<String> relations) {
        ArrayList<ArrayList> instances = new ArrayList<>();

        for (String relation : relations) {
            instances.add(getInstancesOfClass(relation));
        }

        //flatten instances array
        return (ArrayList<Individual>) instances.stream().flatMap(individual -> individual.stream()).collect(Collectors.toList());
    }

    public ArrayList<Individual> getCandidates(List<String> classNames) {
        ArrayList<ArrayList> instances = new ArrayList<>();

        for (String className : classNames) {
            instances.add(getInstancesOfClass(className));
        }

        //flatten instances array
        return (ArrayList<Individual>) instances.stream().flatMap(individual -> individual.stream()).collect(Collectors.toList());
    }

    public ArrayList<Individual> getCandidatesFromAgent(String agent) {
        List<String> frames = getFramesWithClassAsAgent(agent);
        List<String> actions = new ArrayList<>();
        for (String frame : frames) {
            for (String action : getMainActionsFromClass(frame)) {
                if (!actions.contains(action)) {
                    actions.add(action);
                }
            }
        }
        return getCandidates(actions);
    }

    public ArrayList<Individual> getCandidatesFromAction(String action) {
        List<String> frames = getFramesWithClassAsMainAction(action);
        List<String> patients = new ArrayList<>();
        for (String frame : frames) {
            for (String patient : getPatientsFromClass((frame))) {
                if (!patients.contains(patient)) {
                    patients.add(patient);
                }
            }
        }
        return getCandidates(patients);
    }

    public ArrayList<Individual> getCandidatesFromPatient(String patient) {
        List<String> frames = getFramesWithClassAsPatient(patient);
        List<String> places = new ArrayList<>();
        for (String frame : frames) {
            for (String place : getPlacesFromClass(frame)) {
                if (!places.contains(place)) {
                    places.add(place);
                }
            }
        }
        return getCandidates(places);
    }

    public ArrayList<Individual> getCandidatesFromPlace(String place) {
        List<String> frames = getFramesWithClassAsAgent(place);
        List<String> places = new ArrayList<>();
        for (String frame : frames) {
            for (String pla : getMainActionsFromClass(frame)) {
                if (!places.contains(pla)) {
                    places.add(pla);
                }
            }
        }
        return getCandidates(places);
    }

    public ArrayList<Individual> getWhoCSPartCandidates() {
        ArrayList<ArrayList> instances = new ArrayList<ArrayList>();

        for (String className : getPOSFromClass("WhoCSPart")) {
            instances.add(getInstancesOfClass(className));
        }

        return (ArrayList<Individual>) instances.stream().flatMap(individual -> individual.stream()).collect(Collectors.toList());
    }

    public ArrayList<Individual> getWhatDoingCSPartCandidates() {
        ArrayList<ArrayList> instances = new ArrayList<ArrayList>();

        for (String className : getPOSFromClass("WhatDoingCSPart")) {
            instances.add(getInstancesOfClass(className));
        }

        return (ArrayList<Individual>) instances.stream().flatMap(individual -> individual.stream()).collect(Collectors.toList());
    }

    public ArrayList<Individual> getWhatCSPartCandidates() {
        ArrayList<ArrayList> instances = new ArrayList<ArrayList>();

        for (String className : getPOSFromClass("WhatCSPart")) {
            instances.add(getInstancesOfClass(className));
        }

        return (ArrayList<Individual>) instances.stream().flatMap(individual -> individual.stream()).collect(Collectors.toList());
    }

    public ArrayList<Individual> getWhereCSPartCandidates() {
        ArrayList<ArrayList> instances = new ArrayList<ArrayList>();

        for (String className : getPOSFromClass("WhereCSPart")) {
            instances.add(getInstancesOfClass(className));
        }

        return (ArrayList<Individual>) instances.stream().flatMap(individual -> individual.stream()).collect(Collectors.toList());
    }

    public ArrayList<Individual> getDescribeCSPartCandidates() {
        ArrayList<ArrayList> instances = new ArrayList<ArrayList>();

        for (String className : getPOSFromClass("DescribeCSPart")) {
            instances.add(getInstancesOfClass(className));
        }

        return (ArrayList<Individual>) instances.stream().flatMap(individual -> individual.stream()).collect(Collectors.toList());
    }

    private ArrayList<Individual> getInstancesOfClass(String className) {

        ArrayList<Individual> instances = new ArrayList<>();

        try {
            OntClass ontClass = this.ontModel.getOntClass(ASSISTIVE_NS + className);

            if (ontClass.isClass()) {
                ExtendedIterator<Individual> individuals = (ExtendedIterator<Individual>) ontClass.listInstances();

                while(individuals.hasNext()) {
                    Individual individual = individuals.next();
                    instances.add(individual);
                }
            }
        } catch(RuntimeException re) {
            System.out.println("Não foi encontrado a classe destino");
        }

        return instances;
    }

    public List<String> getAllClassWithHasAgent() {
        return executeQuery(queryBuilderHasProperty("hasAgent"), "subject");
    }

    public List<String> getAllClassWithHasPatient() {
        return executeQuery(queryBuilderHasProperty("hasPatient"),"subject");
    }

    public List<String> getAllClassWithHasMainAction() {
        return executeQuery(queryBuilderHasProperty("hasMainAction"), "subject");
    }

    public List<String> getAllClassWithHasPlace() {
        return executeQuery(queryBuilderHasProperty("hasPlace"), "subject");
    }

    public List<String> getAllClassWithHasOptionalPatient() {
        return executeQuery(queryBuilderHasProperty("hasOptionalPatient"), "subject");
    }

    public List<String> getAllClassWithHasPOS() {
        return executeQuery(queryBuilderHasProperty("hasPOS"), "subject");
    }

    public List<String> getFramesWithClassAsAgent(String className) {
        List<String> frames = new ArrayList<>();

        for (String classWithAgent : getAllClassWithHasAgent()) {
            List<String> agents = getAgentsFromClass(classWithAgent);
            if (agents.contains(className)) {
                frames.add(classWithAgent);
            }
        }

        return frames;
    }

    public List<String> getFramesWithClassAsMainAction(String className) {
        List<String> frames = new ArrayList<>();

        for (String classWithMainAction : getAllClassWithHasMainAction()) {
            List<String> actions = getMainActionsFromClass(classWithMainAction);
            if (actions.contains(className)) {
                frames.add(classWithMainAction);
            }
        }

        return frames;
    }

    public List<String> getFramesWithClassAsPatient(String className) {
        List<String> frames = new ArrayList<>();

        for (String classWithPatient : getAllClassWithHasPatient()) {
            List<String> patients = getPatientsFromClass(classWithPatient);
            if (patients.contains(className)) {
                frames.add(classWithPatient);
            }
        }

        return frames;
    }

    public List<String> getFramesWithClassAsPlace(String className) {
        List<String> frames = new ArrayList<>();

        for (String classWithHasPlace : getAllClassWithHasPlace()) {
            List<String> places = getPlacesFromClass(classWithHasPlace);
            if (places.contains(className)) {
                frames.add(classWithHasPlace);
            }
        }

        return frames;
    }

    public List<String> getFramesWithClassAsOptionalPatient(String className) {
        List<String> frames = new ArrayList<>();

        for (String classWithHasOptionalPatient : getAllClassWithHasOptionalPatient()) {
            List<String> patients = getOptionalPatientsFromClass(classWithHasOptionalPatient);
            if (patients.contains(className)) {
                frames.add(classWithHasOptionalPatient);
            }
        }

        return frames;
    }

    private Query queryBuilderHasProperty(String property) {
        return QueryFactory.create(
                this.prefix +
                "SELECT ?subject " +
                "WHERE { ?subject " +
                "owl:equivalentClass/owl:onProperty " +
                "assistive:" + property + " }");
    }

    private List<String> executeQuery(Query query, String field) {
        List<String> resultQuery = new ArrayList<>();

        QueryExecution qexec = QueryExecutionFactory.create( query, this.ontModel );

        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                RDFNode node = qs.get(field);
                for (Resource res : explodeAnonNode(node.asResource())) {
                    resultQuery.add(res.getLocalName());
                }
            }
        } catch(Exception e) {
            System.out.println("ERRO?: " + e.toString());
        }
        finally {
            qexec.close();
        }

        return resultQuery;
    }

    private List<String> getAgentsFromClass(String className) {
        return executeQuery(queryPropertyValuesBuilder("hasAgent", className), "value");
    }

    private List<String> getPatientsFromClass(String className) {
        return executeQuery(queryPropertyValuesBuilder("hasPatient", className), "value");
    }

    private List<String> getMainActionsFromClass(String className) {
        return executeQuery(queryPropertyValuesBuilder("hasMainAction", className), "value");
    }

    private List<String> getAttributesFromClass(String className) {
        return executeQuery(queryPropertyValuesBuilder("hasAttribute", className), "value");
    }

    private List<String> getPlacesFromClass(String className) {
        return executeQuery(queryPropertyValuesBuilder("hasPlace", className), "value");
    }

    private List<String> getMannersFromClass(String className) {
        return executeQuery(queryPropertyValuesBuilder("hasManner", className), "value");
    }

    private List<String> getOptionalPatientsFromClass(String className) {
        return executeQuery(queryPropertyValuesBuilder("hasOptionalPatient", className), "value");
    }

    public List<String> getPOSFromClass(String className) {
        return executeQuery(queryPropertyValuesBuilder("hasPOS", className), "value");
    }

    private Query queryPropertyValuesBuilder(String property, String className) {
        return QueryFactory.create(
                this.prefix +
                "SELECT ?value " +
                "WHERE { assistive:" + className + " owl:equivalentClass " +
                "[ owl:onProperty assistive:" + property + " ; owl:someValuesFrom ?value ] }");
    }

    private List<Resource> explodeAnonNode(Resource resource) {
        List<Property> collectionProperties = new LinkedList<>(Arrays.asList(OWL.unionOf, OWL.intersectionOf, RDF.first, RDF.rest));

        List<Resource> resources = new LinkedList<>();
        Boolean needToTraverseNext = false;

        if (resource.isAnon()) {
            for (Property cp : collectionProperties) {
                if (resource.hasProperty(cp) && !resource.getPropertyResourceValue(cp).equals(RDF.nil)) {
                    Resource nextResource = resource.getPropertyResourceValue(cp);
                    resources.addAll(explodeAnonNode(nextResource));
                    needToTraverseNext = true;
                }
            }

            if (!needToTraverseNext) {
                resources.add(resource);
            }
        } else {
            resources.add(resource);
        }

        return resources;
    }

    public List<String> getAllInstancesFromLinguisticOntology() {
        return executeQuery(queryAllInstancesFromOntology("LinguisticOntology"), "instances");
    }

    public List<String> getAllInstancesFromDomainOntology() {
//    public void getAllInstancesFromDomainOntology() {
//        Resource individual = this.ontModel.getResource("want");
//        System.out.println("this.ontModel.getIndividual(\"want\")");
//        System.out.println(individual.getURI());
//        System.out.println("individual.getClass()");
//        System.out.println(individual.getClass().getClasses().length);
        return executeQuery(queryAllInstancesFromOntology("DomainOntology"), "instances");
    }

    private Query queryAllInstancesFromOntology(String ontology) {
        return QueryFactory.create(
                this.prefix +
                "SELECT ?instances\n" +
                "WHERE {\n" +
                "  ?instances rdf:type/rdfs:subClassOf* assistive:" + ontology +
                "\n}"
        );
    }

    public List<String> getAllInstancesFromPOS(String pos) {
        if (pos.equalsIgnoreCase("noun")) {
            // colocar o array que deseja consultar no filtro
           return executeQuery(queryInstancesByPOSBuilder(new String[]{"Noun", "Pronoun"}), "entity");
        } else if (pos.equalsIgnoreCase("verb")) {
            return executeQuery(queryInstancesByPOSBuilder(new String[]{"Verb"}), "entity");
        } else {
            return executeQuery(queryInstancesByPOSBuilder(new String[]{"Adjective", "Adverb"}), "entity");
        }
    }

    private Query queryInstancesByPOSBuilder(String[] filter) {
        StringBuilder sb = new StringBuilder("SELECT ?entity\n" +
                "WHERE {\n" +
                " ?entity rdf:type/rdfs:subClassOf assistive:PartOfSpeech .\n" +
                " ?entity rdf:type/rdfs:subClassOf* ?class .\n" +
                " FILTER (");

        if (filter.length == 1) {
            sb.append("?class = assistive:" + filter[0] + ")\n}");
        } else {
            for (int i = 0; i < filter.length; i++) {
                if ( (i + 1) == filter.length ) {
                    sb.append("?class = assistive:" + filter[i]);
                } else {
                    sb.append("?class = assistive:" + filter[i] + " || ");
                }

            }
            sb.append(")\n}");
        }

        return QueryFactory.create(
                this.prefix +
                sb.toString()
        );
    }

    public List<String> getClassFromLexicon(String lexicon, String ontology) {
        return executeQuery(queryClassFromIndividual(lexicon, ontology, 0), "class");
    }

    private Query queryClassFromIndividual(String lexicon, String ontology, int limitNumber) {
        String limit = "";

        if (limitNumber > 0) {
            limit = " " + Integer.toString(limitNumber);
        }
        return QueryFactory.create(
                this.prefix +
                "SELECT ?class WHERE { \n" +
                "assistive:" + lexicon + " rdf:type/rdfs:subClassOf* ?class .\n" +
                "?class rdfs:subClassOf+ assistive:" + ontology +
                "\n}" + limit
        );
    }

    public List<Frame> getAllFrames() {
        List<Frame> frames = new ArrayList<>();
        Frame fr = null;
        int frame_id = 1;
        for (String frame : executeQuery(queryAllFrames(), "value")) {
            fr = new Frame(frame_id, frame);
            fr.addRelation("hasMainAction", getMainActionsFromClass(frame));
            fr.addRelation("hasAgent", getAgentsFromClass(frame));
            fr.addRelation("hasPatient", getPatientsFromClass(frame));
            fr.addRelation("hasPlace", getPlacesFromClass(frame));
            fr.addRelation("hasManner", getMannersFromClass(frame));

            frames.add(fr);
            frame_id++;
        }

        return frames;
    }

    private Query queryAllFrames() {
        return QueryFactory.create(
                this.prefix +
                "SELECT ?value WHERE {\n" +
                "?value rdfs:subClassOf assistive:Frame" +
                "\n}"
        );
    }

    private Query queryIndividualsByClass(String className) {
        return QueryFactory.create(
                this.prefix +
                "SELECT ?value WHERE {\n" +
                "?value rdf:type/rdfs:subClassOf* assistive:" + className +
                "\n}"
        );
    }

    public List<String> getSemanticClasses(String frame, String property) {
        return executeQuery(queryPropertyValuesBuilder(property, frame), "value");
    }
}