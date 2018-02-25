package br.ufpe.cin.assistive;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LexiconGenerator {
    private OntologyEngine ontologyEngine;
    private static final String SOURCE = "./src/english1-syn/allo/";
    private static final String SOURCE_SEM_LEX = "./src/english1-syn/morph/";
    private static final String SEM_LEX_FILENAME = "sem.lex";
    private static final String NOUN = "noun.lex";
    private static final String VERB = "verb.lex";
    private static final String ADJ = "adj.lex";
    private static final String FILENAME = "allo.all";
    private static final String WB_SOURCE = "./src/english1-syn/";
    private static final String WB_FILENAME = "wordbank.sql";
    private Map<String, List<String>> verbs = new HashMap<>();
    private Map<String, List<String>> nouns = new HashMap<>();
    private Map<String, List<String>> adjs = new HashMap<>();
    private List<Frame> frames;
    private List<String> relatedClasses = new ArrayList<>();
    private List<String> properties = new ArrayList<>();
    private List<Token> tokensList = new ArrayList<>();
    private int id = 1;
    private int class_id = 0;
    private int syntactic_id = 1;


    public LexiconGenerator() {
        this.ontologyEngine = new OntologyEngine();
        frames = getAllFrames();
        for (Frame frame : frames) {
            System.out.println("\nFrame: " + frame.getName());

            frame.getRelations().forEach( (key, value) -> {
                System.out.print("Relation: ");
                System.out.println(key.toString());

                if (!properties.contains(key.toString())) {
                    properties.add(key.toString());
                }

                for (String property : value) {
                    System.out.println("Property: " + property);
                    if (!relatedClasses.contains(property)) {
                        relatedClasses.add(class_id, property);
                        class_id++;
                    }
                }
            });
        }
//
        System.out.println("syntacticNames.size()");
        System.out.println(properties.size());
        System.out.println("relatedClasses.size()");
        System.out.println(relatedClasses.size());
//
//        System.exit(0);
    }

    public boolean generate() throws IOException {

        if (generateVerbs() && generateNouns() && generateAttributes()) {
            if (mergeFiles()) {
                return generateWordBankData();
//                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    private boolean generateWordBankData() {

        if (insertionSQL()) return true;

        return false;
    }

    private boolean insertionSQL() {
        Writer writer = null;

        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(WB_SOURCE + WB_FILENAME), "utf-8")
            );

            StringBuilder sb = new StringBuilder();

            StringBuilder classes = new StringBuilder();

            StringBuilder framesNames = new StringBuilder();

            StringBuilder types = new StringBuilder();

            StringBuilder tokens = new StringBuilder();

            StringBuilder proplets = new StringBuilder();

            StringBuilder relations = new StringBuilder();

            StringBuilder framesRelations = new StringBuilder();


            for (Frame frame : frames) {
                framesNames.append("INSERT INTO FRAME (ID, NAME) VALUES (" + frame.getId() + ", '" + frame.getName() + "');\n");
//                frame.getRelations().forEach((key, value) -> {
//                    System.out.println(key.toString());

//                    if (!properties.contains(key.toString())) {
//                        properties.add(key.toString());
//                        framesNames.append("INSERT INTO SEMANTIC (ID, NAME) VALUES (" + (syntactic_id++) + ", '" + key + "');\n");
//                    }

//                    for (String property : value) {
//                        System.out.println("Property: " + property);
//                        if (!relatedClasses.contains(property)) {
//                            relatedClasses.add(property);
//                            classes.append("INSERT INTO SEMANTIC (ID, NAME) VALUES (" + (class_id++) + ", '" + property + "');\n");
//                        }
//                    }
//                });
            }

            for (String className : relatedClasses) {
//                System.out.println(className);
                int index = relatedClasses.lastIndexOf(className);
//                System.out.println("index:");
//                System.out.println(index);
                classes.append("INSERT INTO SEMANTIC (ID, NAME) VALUES (" + (index + 1) + ", '" + className + "');\n");
            }

//            System.exit(0);


            relations.append("-- VERBS\n");
            proplets.append("-- VERBS\n");
            types.append("-- VERBS\n");
            tokens.append("-- VERBS\n");
            framesRelations.append("-- VERBS\n");
            verbs.forEach((key, value) -> {
//                System.out.println("key: " + key.toString());
                List<Verb> verbClasses = new ArrayList<>();
                types.append("INSERT INTO TYPE (ID, BASEFORM, POS) VALUES (" + id + ", '" + key.toString() + "', 'verb');\n");
                proplets.append("INSERT INTO PROPLET (ID, SUR, CORE) VALUES (" + id + ", '" + key.toString() + "', '" + key.toString() + "');\n");
                tokens.append("INSERT INTO TOKEN (ID, TYPE_ID, PROPLET_ID) VALUES (" + id + ", " + id + ", " + id + ");\n");
//                tokensList.add(new Token(id, id, key.toString()));

                // proplet (n:n) semantic_relation
                List<String> list = new ArrayList<>();
                for (String name : value) {
                    if (relatedClasses.contains(name) && !list.contains(name)) {
                        list.add(name);
//                        System.out.println("name: " + name);
                        relations.append("INSERT INTO SEMANTIC_RELATION (SEMANTIC_ID, PROPLET_ID) VALUES ("
                                + (relatedClasses.lastIndexOf(name) + 1) + ", " + id + ");\n");

//                        tokensList.add(new Token(id, id, name));
                        Verb verb = new Verb(id);
                        verb.setClasses((relatedClasses.lastIndexOf(name) + 1), name);
                        verbClasses.add(verb);
                    }
                }


                for (Frame frame : frames) {
                    frame.getRelations().forEach((k, v) -> {
                        if (k.equalsIgnoreCase("hasMainAction")) {
//                            System.out.println(frame.getName());
//                            System.out.println(k);
//                            System.out.println(v);
                            // verifica se algum dessas classes tá na classe related do verbo
                            for (Verb verb : verbClasses ) {
                                for (Classe classe : verb.getClasses()) {
                                    if (v.contains(classe.getName())) {
                                        framesRelations.append("INSERT INTO FRAME_RELATION (TOKEN_ID, FRAME_ID) VALUES ("
                                                + verb.getId() + ", " + frame.getId() + ");\n");
                                    }
                                }
                            }
                        }
                    });

                }

//                System.out.println("ACABOU\n\n");
                id++;

            });

            relations.append("-- NOUNS\n");
            proplets.append("-- NOUNS\n");
            types.append("-- NOUNS\n");
            tokens.append("-- NOUNS\n");
            framesRelations.append("-- NOUNS\n");
            nouns.forEach( (key, value) -> {
                //                System.out.println("key: " + key.toString());
                List<Verb> nounClasses = new ArrayList<>();
                types.append("INSERT INTO TYPE (ID, BASEFORM, POS) VALUES (" + id + ", '" + key.toString() + "', 'noun');\n");
                proplets.append("INSERT INTO PROPLET (ID, SUR, CORE) VALUES (" + id + ", '" + key.toString() + "', '" + key.toString() + "');\n");
                tokens.append("INSERT INTO TOKEN (ID, TYPE_ID, PROPLET_ID) VALUES (" + id + ", " + id + ", " + id + ");\n");
//                tokensList.add(new Token(id, id, key.toString()));

                // proplet (n:n) semantic_relation
                List<String> list = new ArrayList<>();
                for (String name : value) {
                    if (relatedClasses.contains(name) && !list.contains(name)) {
                        list.add(name);
//                        System.out.println("caiu aqui");
//                        System.out.println(name);
                        relations.append("INSERT INTO SEMANTIC_RELATION (SEMANTIC_ID, PROPLET_ID) VALUES ("
                                + (relatedClasses.lastIndexOf(name) + 1) + ", " + id + ");\n");

//                        tokensList.add(new Token(id, id, name));
                        Verb verb = new Verb(id);
                        verb.setClasses((relatedClasses.lastIndexOf(name) + 1), name);
                        nounClasses.add(verb);
                    }
                }


                for (Frame frame : frames) {
                    frame.getRelations().forEach((k, v) -> {
                        if (!k.equalsIgnoreCase("hasMainAction")) {
//                            System.out.println(frame.getName());
//                            System.out.println(k);
//                            System.out.println(v);
                            // verifica se algum dessas classes tá na classe related do verbo
                            for (Verb verb : nounClasses ) {
                                for (Classe classe : verb.getClasses()) {
                                    if (v.contains(classe.getName())) {
//                                        System.out.println("classe.getName()");
//                                        System.out.println(classe.getName());
                                        framesRelations.append("INSERT INTO FRAME_RELATION (TOKEN_ID, FRAME_ID) VALUES ("
                                                + verb.getId() + ", " + frame.getId() + ");\n");
                                    }
                                }
                            }
                        }
                    });

                }

//                System.out.println("ACABOU\n\n");
                id++;
            });
//
            relations.append("-- ADJ\n");
            proplets.append("-- ADJ\n");
            types.append("-- ADJ\n");
            tokens.append("-- ADJ\n");
            framesRelations.append("-- ADJ\n");
            adjs.forEach( (key, value) -> {
                //                System.out.println("key: " + key.toString());
                List<Verb> nounClasses = new ArrayList<>();
                types.append("INSERT INTO TYPE (ID, BASEFORM, POS) VALUES (" + id + ", '" + key.toString() + "', 'adj');\n");
                proplets.append("INSERT INTO PROPLET (ID, SUR, CORE) VALUES (" + id + ", '" + key.toString() + "', '" + key.toString() + "');\n");
                tokens.append("INSERT INTO TOKEN (ID, TYPE_ID, PROPLET_ID) VALUES (" + id + ", " + id + ", " + id + ");\n");
//                tokensList.add(new Token(id, id, key.toString()));

                // proplet (n:n) semantic_relation
                List<String> list = new ArrayList<>();
                for (String name : value) {
                    if (relatedClasses.contains(name) && !list.contains(name)) {
                        list.add(name);
//                        System.out.println("caiu aqui");
//                        System.out.println(name);
                        relations.append("INSERT INTO SEMANTIC_RELATION (SEMANTIC_ID, PROPLET_ID) VALUES ("
                                + (relatedClasses.lastIndexOf(name) + 1) + ", " + id + ");\n");

//                        tokensList.add(new Token(id, id, name));
                        Verb verb = new Verb(id);
                        verb.setClasses((relatedClasses.lastIndexOf(name) + 1), name);
                        nounClasses.add(verb);
                    }
                }


                for (Frame frame : frames) {
                    frame.getRelations().forEach((k, v) -> {
                        if (!k.equalsIgnoreCase("hasMainAction")) {
//                            System.out.println(frame.getName());
//                            System.out.println(k);
//                            System.out.println(v);
                            // verifica se algum dessas classes tá na classe related do verbo
                            for (Verb verb : nounClasses ) {
                                for (Classe classe : verb.getClasses()) {
                                    if (v.contains(classe.getName())) {
//                                        System.out.println("classe.getName()");
//                                        System.out.println(classe.getName());
                                        framesRelations.append("INSERT INTO FRAME_RELATION (TOKEN_ID, FRAME_ID) VALUES ("
                                                + verb.getId() + ", " + frame.getId() + ");\n");
                                    }
                                }
                            }
                        }
                    });

                }

//                System.out.println("ACABOU\n\n");
                id++;
            });

//            sb.append("\n");
            sb.append(classes);
            sb.append("\n");
            sb.append(types);
            sb.append("\n");
            sb.append(proplets);
            sb.append("\n");
            sb.append(relations);
            sb.append("\n");
            sb.append(tokens);
            sb.append("\n");
            sb.append(framesNames);
            sb.append("\n");
            sb.append(framesRelations);

            writer.write(sb.toString());

            return true;
        } catch (IOException ex) {
            // report
            return false;
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {/*ignore*/}
        }
    }


    private boolean mergeFiles() throws IOException {
        File[] files = new File[]{
                new File(SOURCE + VERB),
                new File(SOURCE + NOUN),
                new File(SOURCE + ADJ)
        };

        Files.deleteIfExists(Paths.get(SOURCE + FILENAME));

        File file = new File(SOURCE + FILENAME);

        FileWriter fstream = null;
        BufferedWriter out = null;
        try {
            fstream = new FileWriter(file, true);
            out = new BufferedWriter(fstream);
        } catch (IOException e1) {
            e1.printStackTrace();
            return false;
        }

        out.write("# -*- coding: utf-8 -*-\n" +
                "# auto generated file (AssistiveAlloProcessor)\n" +
                "# source: /semantic-parser/src/english1-syn/allo/allo.all\n" +
                "# lcdate: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        out.newLine();
        out.newLine();

        out.write("[sur: \".\", cat: (v' decl)]");

        out.newLine();
        out.newLine();

        for (File f : files) {
            System.out.println("merging: " + f.getName());
            FileInputStream fis;
            try {
                fis = new FileInputStream(f);
                BufferedReader in = new BufferedReader(new InputStreamReader(fis));

                String aLine;
                if (f.getName().contains("verb")) {
                    out.write("# verbs");
                }
                if (f.getName().contains("noun")) {
                    out.write("# nouns");
                }
                if (f.getName().contains("adj")) {
                    out.write("# adjectives");
                }

                out.newLine();
                while ((aLine = in.readLine()) != null) {
                    out.write(aLine);
                    out.newLine();
                }

                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            out.newLine();
        }

        out.write("# eog: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        try {
            out.close();
            Files.deleteIfExists(Paths.get(SOURCE + VERB));
            Files.deleteIfExists(Paths.get(SOURCE + NOUN));
            Files.deleteIfExists(Paths.get(SOURCE + ADJ));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean generateVerbs() {
        //(n' v)
        // T_1
        List<String> verbs = getVerbs();
        if (generateSemLex(verbs)) {
            return write(VERB, verbs, "n' v", "T_1");
        }

        return false;
    }

    private boolean generateSemLex(List<String> verbs) {
        return writeSemLex(SEM_LEX_FILENAME, verbs);
    }

    private boolean generateNouns() {
        // (sn)
        // T_2
        return write(NOUN, getNouns(), "sn", "T_2");
    }

    private boolean generateAttributes() {
        // (adj)
        // T_3
        return write(ADJ, getAttributes(), "adj", "T_3");
    }

    private List<String> getNouns() {
        return this.ontologyEngine.getAllInstancesFromPOS("noun");
    }

    private List<String> getVerbs() {
        return this.ontologyEngine.getAllInstancesFromPOS("verb");
    }

    private List<String> getAttributes() {
        return this.ontologyEngine.getAllInstancesFromPOS("adj");
    }

    private boolean write(String filename, List<String> lexica, String cat, String syn) {
        Writer writer = null;

        StringBuilder sb = new StringBuilder();

        sb.append("!template[cat: (" + cat + "), syn: " + syn + "]\n")
          .append("![sur, core, ontology]\n");

        try {
            writer = new BufferedWriter(
                        new OutputStreamWriter(
                            new FileOutputStream(SOURCE + filename), "utf-8")
                    );

            for (String lexico : lexica) {
                List<String> classNames = getLexiconClass(lexico);

                if (classNames.size() == 0) continue;

//                System.out.println("classNames.size()");
//                System.out.println(classNames.size());

                // sur and core fields
                sb.append(lexico + " " + lexico + " (");

                List<String> vlist = new ArrayList<>();
                List<String> nlist = new ArrayList<>();
                List<String> alist = new ArrayList<>();

                // ontology field
                for (int i = 0; i < classNames.size(); i++) {
                    if (syn.equalsIgnoreCase("T_1")) {
                        if (frameActionContainClass(classNames.get(i))) {
                            vlist.add(classNames.get(i));
                            sb.append(classNames.get(i));
                            if ((i + 1) < classNames.size()) {
                                sb.append(" ");
                            }
                        }
                    } else if (syn.equalsIgnoreCase("T_2")) {
                        if (nounFramesContainClass(classNames.get(i))) {
                            nlist.add(classNames.get(i));
                            sb.append(classNames.get(i));
                            if ((i + 1) < classNames.size()) {
                                sb.append(" ");
                            }
                        }
                    } else {
                        if (adjFramesContainClass(classNames.get(i))){
                            alist.add(classNames.get(i));
                            sb.append(classNames.get(i));
                            if ((i + 1) < classNames.size()) {
                                sb.append(" ");
                            }
                        }
                    }
                }
                sb.append(")\n");

                if (vlist.size() > 0) {
                    System.out.println("Adding VERBS list");
                    verbs.put(lexico, vlist);
                }
                if (nlist.size() > 0) {
                    System.out.println("Adding NOUNS list");
                    nouns.put(lexico, nlist);
                }
                if (alist.size() > 0) {
                    System.out.println("Adding ADJS list");
                    adjs.put(lexico, alist);
                }

            }
            writer.write(sb.toString());
        } catch (IOException ex) {
            // report
            return false;
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {/*ignore*/}
        }

        sb.setLength(0);

        return true;
    }

    private boolean writeSemLex(String filename, List<String> vbs) {
        Writer writer = null;

        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(SOURCE_SEM_LEX + filename), "utf-8")
            );
            for (int i = 0; i < vbs.size(); i++) {
                writer.write("[sur: " + vbs.get(i) + ", cat: <(), (a'), (d' a')>]");
                if ( (i + 1) < vbs.size()) {
                    writer.write("\n");
                }
            }
        } catch (IOException ex) {
            // report
            return false;
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {/*ignore*/}
        }

        return true;
    }

    private List<Frame> getFrameFromClass(String className) {
        List<Frame> relatedFrames = new ArrayList<>();

        for (Frame frame: frames) {
            if (nounContain(frame, className)) {
                relatedFrames.add(frame);
            }
        }

        return relatedFrames;
    }

    private List<String> getLexiconClass(String lexicon) {
        return this.ontologyEngine.getClassFromLexicon(lexicon, "DomainOntology");
    }

    private List<Frame> getAllFrames() {
        return this.ontologyEngine.getAllFrames();
    }

    private boolean frameContainClass(String className) {
        return frameActionContainClass(className) ||
                frameAgentContainClass(className) ||
                framePatientContainClass(className) ||
                framePlaceContainClass(className);
    }

    private boolean nounFramesContainClass(String className) {
        return  frameAgentContainClass(className) ||
                framePatientContainClass(className) ||
                framePlaceContainClass(className);
    }

    private boolean adjFramesContainClass(String className) {
        return  frameMannerContainClass(className);
    }

    private boolean nounContain(Frame frame, String className) {
        if (frame.getRelations().get("hasMainAction").contains(className)) return true;
        if (frame.getRelations().get("hasPlace").contains(className)) return true;
        if (frame.getRelations().get("hasPatient").contains(className)) return true;

        return false;
    }

//    private boolean nounFramesContainClass(String className) {
//        return  frameAttributeContainClass(className);
//    }

    private boolean frameActionContainClass(String className) {

        for (Frame frame : frames) {
            if (frame.getRelations().get("hasMainAction").contains(className)) return true;
        }

        return false;
    }

    private boolean frameAgentContainClass(String className) {

        for (Frame frame : frames) {
            if (frame.getRelations().get("hasAgent").contains(className)) return true;
        }

        return false;
    }

    private boolean frameMannerContainClass(String className) {
        for (Frame frame : frames) {
//            frame.getRelations().forEach((k, v) -> {
//                System.out.println("k");
//                System.out.println(k);
//                if (k.equalsIgnoreCase("hasManner")) {
                    if (frame.getRelations().get("hasManner").contains(className)) return true;
//                }

//                System.out.println("v");
//                System.out.println(v);
//            });
//            return true;
//
        }

        return false;
    }

    private boolean framePatientContainClass(String className) {

        for (Frame frame : frames) {
            if (frame.getRelations().get("hasPatient").contains(className)) return true;
        }

        return false;
    }

    private boolean framePlaceContainClass(String className) {

        for (Frame frame : frames) {
            if (frame.getRelations().get("hasPlace").contains(className)) return true;
        }

        return false;
    }
}
