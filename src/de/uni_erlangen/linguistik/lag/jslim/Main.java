//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.uni_erlangen.linguistik.lag.jslim;

import br.ufpe.cin.assistive.*;
import de.uni_erlangen.linguistik.lag.jslim.exception.JSLIMException;
import de.uni_erlangen.linguistik.lag.jslim.lag.LAGProject;
import de.uni_erlangen.linguistik.lag.jslim.lag.ProductConfigFile;
import de.uni_erlangen.linguistik.lag.jslim.lag.LAGProject.Name;
import de.uni_erlangen.linguistik.lag.jslim.motor.Motor;
import de.uni_erlangen.linguistik.lag.jslim.motor.MotorState;
import de.uni_erlangen.linguistik.lag.jslim.product.Product;
import de.uni_erlangen.linguistik.lag.jslim.project.Project;
import de.uni_erlangen.linguistik.lag.jslim.storage.IAttributeModel;
import org.apache.jena.ontology.Individual;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.*;

public class Main {
    static final String PRODUCT = "product";
    static final String COMPONENTS = "components";
    static final String FEATURES = "features";
    static final int MAJOR = 2;
    static final int MINOR = 1;
    static Product product;
    private static ArrayList<Option> options = new ArrayList<>();
    private static boolean isFirstAttempt = true;
    private static boolean continueSystem = false;
    private static LexiconGenerator lexiconGenerator = new LexiconGenerator();

    /* FONT COLORS */
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    /* BACKGROUND COLORS */
    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    public Main() {
    }

    public static void main(String[] args) throws IOException {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Deseja gerar o léxico? (y/n)");
            String lexicon = br.readLine().toLowerCase();

            if (lexicon.equalsIgnoreCase("y")) {
                if (generateLexicon()) { continueSystem = true; }
                else { System.exit(0); }
            } else {
                continueSystem = true;
            }

//            System.exit(0);

            if (continueSystem) {
                init("src\\english1-syn\\common\\english1.pro");

                int option = -1;

                Motor motor = product.getProject().lagProject.getMotor();
                SemanticEngine semanticEngine = motor.getSemanticEngine();
                while ( option != 0 ) {
                    if (isFirstAttempt) {
                        // generate the first candidates
                        // based on the color selected
                        isFirstAttempt = false;
                        option = 2;
                        System.out.println("Primeira interação...");
                        System.out.println("Seleção automática da opção " + ANSI_YELLOW_BACKGROUND + "\"Quem?\"" + ANSI_RESET);



                        int count = 0;
                        List<String> whoCandidates = semanticEngine.getFirstCandidates("noun");
                        for (String candidate : whoCandidates) {
                            System.out.printf("%d) %s \n", (++count), candidate);
                        }

//                        System.exit(0);

//                        ArrayList<Individual> whoCandidates = semanticEngine.getWhoCSPart();

//                        System.out.println("Lista de Candidatos:");
//                        for (int i = 0; i < whoCandidates.size(); i++) {
//                            System.out.printf("%d) %s \n", (i + 1), whoCandidates.get(i).getLocalName());
//                        }

                        try {
                            int pictogramOption = Integer.parseInt(getPictogram(br));

                            if (pictogramOption > 0 && pictogramOption <= whoCandidates.size()) {
                                options.get(2).setSelected(true);
                                options.get(2).setValue(whoCandidates.get(pictogramOption - 1));
                                MotorState ms = motor.parse(whoCandidates.get(pictogramOption - 1));
                                System.out.println(ms.getOutput());

                                ArrayList<String> semantics = semanticEngine.getRelationsFromSur(options.get(2).getValue());

                                List<String> frames = semanticEngine.getFramesRelations(options.get(2).getValue(), semantics);

                                options.get(2).setOntology(semantics);
                                options.get(2).setRelations(frames);

//                                System.exit(0);
                            } else {
                                System.out.println("Pictograma inexistente...");
                                isFirstAttempt = true;
                            }

//                            System.exit(0);
                        } catch (NumberFormatException nfe) {
                            System.out.println("Valor informado não existe, tente novamente.");
                            isFirstAttempt = true;
                        }
                    } else {
                        StringBuilder sbo = new StringBuilder();
                        System.out.println("\nEscolha a opção desejada (informe o número):");
                        for (int i = 0; i < options.size(); i++) {
                            sbo.append(i + ") ");

                            sbo.append(options.get(i).getName());

                            if (options.get(i).isSelected()) {
                                sbo.append(" (" + options.get(i).getValue().toUpperCase() + ")");
                            }

                            sbo.append("\n");
                        }

                        System.out.println(sbo.toString());

                        try {
//                        System.out.println("last option: " + option);
                            int lastOption = option;
                            System.out.print("Opção: ");
                            option = Integer.parseInt(br.readLine());

                            if (option < 0 || option > 8 ) {
                                System.out.println("Número informado não existe, tente novamente.");
                            } else if (option == 0){
                                System.out.println("Saindo...");
                                return;
                            } else if (option == 8) {
                                isFirstAttempt = true;
                                // parse all the sentences
                                // and then clear the options
                                StringBuilder sb = new StringBuilder();
                                options.forEach( opt -> {
                                    if (opt.isSelected()) {
                                        sb.append(opt.getValue() + " ");
                                    }
                                });

                                sb.append(".");

//                            System.out.println("sb.toString()");
                                System.out.println(sb.toString());
                                motor.reset();
                                MotorState ms = motor.parse(sb.toString());

//                            System.out.println("ms.getOutput()");
                                System.out.println(ms.getOutput());

                                clearDataOptions();

                                System.out.println("Deseja começar novamente? (y/n)");
                                if (br.readLine().equalsIgnoreCase("n")) {
                                    option = 0;
                                    return;
                                } else {
                                    isFirstAttempt = true;
                                }
                            } else {
                                // get the candidates
                                // based on the color selected
                                // and the previous option
                                Option lastOpt = options.get(lastOption);
                                Option currentOption = options.get(option);

                                // deve colocar false e true nos frames devido ao agente ou verbo
//                                changeOptionFramesByPatient(lastOpt, currentOption);

                                List<String> candidates;

                                if (options.get(3).isSelected()) {
                                    candidates = semanticEngine.getNextCandidates(options.get(3), currentOption);
                                } else {
                                    candidates = semanticEngine.getNextCandidates(options.get(2), currentOption);
                                }

//                                ArrayList<Individual> candidates = getCandidates(lastOption, semanticEngine);

                                if (candidates.size() > 0) {
                                    System.out.println("Opções para seleção: ");
                                    for (int i = 0; i < candidates.size(); i++) {
                                        System.out.printf("%d) %s \n", (i + 1), candidates.get(i));
                                    }

                                    try {
                                        int pictogramOption = Integer.parseInt(getPictogram(br));

                                        setOptionValue(option, candidates.get(pictogramOption - 1));

                                        System.out.println("Opção escolhida: " + pictogramOption +
                                                " = " + candidates.get(pictogramOption - 1));

                                        motor.reset();
                                        MotorState ms = motor.parse(candidates.get(pictogramOption - 1));

                                        ArrayList<String> semantics = semanticEngine.getRelationsFromSur(options.get(option).getValue());

                                        List<String> frames = semanticEngine.getFramesRelations(options.get(option).getValue(), semantics);

                                        options.get(option).setOntology(semantics);
                                        options.get(option).setRelations(frames);

                                        System.out.println(ms.getOutput());
                                    } catch (NumberFormatException nfe) {
                                        System.out.println("Valor informado não existe, tente novamente.");
                                    }
                                } else {
                                    System.out.println("Não há mais sugestões para dar :(");
                                }
                            }

                        } catch (NumberFormatException nfe) {
                            System.out.println("Valor informado não existe, tente novamente.");
                        }
                    }
                }
            }

            System.exit(0);
        } catch (JSLIMException var2) {
            System.err.println(var2.getFullMessage());
            var2.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static ArrayList<Individual> getCandidates(Integer lastOption, SemanticEngine semanticEngine) {
//        if (lastOption == 1 || lastOption == 4 || lastOption == 6) return semanticEngine.getAgentCandidatesFromSur(options.get(lastOption).getValue());
        if (lastOption == 2) return semanticEngine.getAgentCandidatesFromSur(options.get(lastOption).getValue());
        if (lastOption == 3) return semanticEngine.getActionCandidatesFromSur(options.get(lastOption).getValue());
//        if (lastOption == 4) return semanticEngine.getPatientCandidatesFromSur(options.get(lastOption).getValue());
        if (lastOption == 5) return semanticEngine.getPatientCandidatesFromSur(options.get(lastOption).getValue());
//        if (lastOption == 6) return semanticEngine.getAgentCandidatesFromSur(options.get(lastOption).getValue());
        if (lastOption == 7) return semanticEngine.getPlaceCandidatesFromSur(options.get(lastOption).getValue());

        return null;
    }

    private static void setOptionValue(Integer option, String pictogram) {
        options.get(option).setValue(pictogram);
        options.get(option).setSelected(true);
    }

    private static String getPictogram(BufferedReader br) throws IOException {
        System.out.println("Informe o pictograma: ");
        return br.readLine().toLowerCase();
    }

    private static void changeOptionFramesByPatient(Option last, Option current) {
        List<Frame> inactiveFrames = new ArrayList<>();

        if (last.getPos().equalsIgnoreCase("verb") && last.isSelected()) {
            for (Frame frame : last.getRelations()) {
                for (Frame f : options.get(2).getRelations()) {
//                    if (f.getName() == )
                }
            }
        }
    }

    private static void fillOptions() {
        options.add(0, new Option("Sair"));
        options.add(1, new Option("Descreva?", "Roxo", "adj", "hasManner"));
        options.add(2, new Option("Quem?", "Amarelo", "noun", "hasAgent"));
        options.add(3, new Option("Fazendo?",  "Laranja", "verb", "hasMainAction"));
        options.add(4, new Option("Descreva?", "Roxo", "adj", "hasManner"));
        options.add(5, new Option("O que?", "Verde", "noun", "hasPatient"));
        options.add(6, new Option("Descreva?", "Roxo", "adj", "hasManner"));
        options.add(7, new Option("Onde?", "Azul", "noun", "hasPlace"));
        options.add(8, new Option("Falar"));
    }

    private static void clearDataOptions() {
        for (Option option : options) {
            option.setSelected(false);
            option.setValue("");
        }
    }

    private static Boolean isFirstAttempt() {
        for (Option option : options) {
            if (option.isSelected()) return false;
        }
        return true;
    }

    public static Product init(String filename) throws JSLIMException {
        fillOptions();
        product = getProduct();
        product.loadProject("Default", filename);
        return product;
    }

    private static void options(String[] args) {
        if (args.length == 0) {
            System.err.println("jslim <project file>");
            System.exit(1);
        }

        if (args[0].equals("-v")) {
            Package p = Main.class.getPackage();
            System.out.println(p.getSpecificationVersion() + "." + p.getImplementationVersion());
            System.exit(1);
        }

    }

    public static Product getProduct() throws JSLIMException {
        return new Product(new ProductConfigFile("product"), new ProductConfigFile("components"), new ProductConfigFile("features"));
    }

    public static LAGProject getDummyLAGProject() throws JSLIMException {
        Product product = getProduct();
        Project project = new Project(product, (String)null, "test");
        LAGProject lagProject = project.lagProject;
        IAttributeModel model = lagProject.getAttributeModel();
        String[] var7;
        int var6 = (var7 = new String[]{"sur", "core"}).length;

        String name;
        int var5;
        for(var5 = 0; var5 < var6; ++var5) {
            name = var7[var5];
            model.add(name, 1);
        }

        var6 = (var7 = new String[]{"cat", "sem", "nc", "pc", "pfs", "sfs", "combi", "val", "noun", "verb", "adj"}).length;

        for(var5 = 0; var5 < var6; ++var5) {
            name = var7[var5];
            model.add(name, 4);
        }

        model.addDefault();
        lagProject.addLAG(Name.LAMORPH);
        lagProject.init(false);
        return lagProject;
    }

    public static Project getDummyProject() throws JSLIMException {
        return new Project(getProduct(), (String)null, "dummy");
    }

    public static void run() throws JSLIMException {
        product.getComponent("shell").getService().start();
    }

    private static boolean generateLexicon() throws IOException {
        if (lexiconGenerator.generate()) {
            System.out.println("LÉXICO CRIADO COM SUCESSO");
            return true;
        }
        System.out.println("FALHA AO TENTAR GERAR O LÉXICO");
        return false;
    }
}
