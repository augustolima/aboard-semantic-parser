//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.uni_erlangen.linguistik.lag.jslim.compo.wb;

import de.uni_erlangen.linguistik.lag.jslim.exception.JSLIMException;
import de.uni_erlangen.linguistik.lag.jslim.lag.LAGProject;
import de.uni_erlangen.linguistik.lag.jslim.project.Project;
import de.uni_erlangen.linguistik.lag.jslim.storage.IAttribute;
import de.uni_erlangen.linguistik.lag.jslim.storage.IProplet;
import de.uni_erlangen.linguistik.lag.jslim.storage.ISentenceStart;
import de.uni_erlangen.linguistik.lag.jslim.storage.IVal;
import de.uni_erlangen.linguistik.lag.jslim.storage.IAttribute.Role;
import de.uni_erlangen.linguistik.lag.jslim.storage.impl.Proplet;
import de.uni_erlangen.linguistik.lag.jslim.storage.value.IntegerVal;

import java.io.*;
import java.sql.*;
import java.util.*;

public class WordBankDAO {
    boolean created;
    private final Connection connection;
    private static String driver = "org.sqlite.JDBC";
    private static String protocol = "jdbc:sqlite:";
    static String userHomeDir = System.getProperty("user.home", ".");
    static String systemDir = userHomeDir + "/aboard";
//        System.setProperty("derby.system.home", systemDir);

    public WordBankDAO() throws JSLIMException {
        this.connection = connect();
//        this.dropTables();
//        this.createTables();
//        this.store();
    }

    /**
     * Create a new table in the test database
     *
     */
    private void createTables() throws JSLIMException {

        try (
//                Connection conn = DriverManager.getConnection(systemDir);
             Statement stmt = this.connection.createStatement()) {
            // create a new table
            System.out.print("[Wordbank] Create table type ... ");
            stmt.execute("CREATE TABLE type (baseform VARCHAR(255), pos INTEGER, id INTEGER, PRIMARY KEY(id))");
            System.out.println("done");
            System.out.print("[Wordbank] Create table proplet ... ");
            stmt.execute("CREATE TABLE proplet (sur VARCHAR(255), core VARCHAR(255), cat VARCHAR(255), cas VARCHAR(255), ontology VARCHAR(255), flx VARCHAR(255), mdd VARCHAR(255), arg VARCHAR(255), pc VARCHAR(255), idy VARCHAR(255), mwu VARCHAR(255), mwh VARCHAR(255), id INTEGER, PRIMARY KEY(id))");
            System.out.println("done");
            System.out.print("[Wordbank] Create table token ... ");
            stmt.execute("CREATE TABLE token (id INTEGER, type_id INTEGER, proplet_id INTEGER, PRIMARY KEY(id), FOREIGN KEY (type_id) references type(id), FOREIGN KEY (proplet_id) references proplet(id))");
            System.out.println("done");
            System.out.print("[Wordbank] Create table semantic ... ");
            stmt.execute("CREATE TABLE semantic (id INTEGER, name VARCHAR(255), PRIMARY KEY (id))");
            System.out.println("done");
            System.out.print("[Wordbank] Create table semantic relation ... ");
            stmt.execute("CREATE TABLE semantic_relation (semantic_id INTEGER,proplet_id INTEGER, PRIMARY KEY (semantic_id, proplet_id), FOREIGN KEY (semantic_id) references semantic(id),FOREIGN KEY (proplet_id) references proplet(id))");
            System.out.println("done");
            System.out.print("[Wordbank] Create table frame ... ");
            stmt.execute("CREATE TABLE frame (id INTEGER, name VARCHAR(255), PRIMARY KEY (id))");
            System.out.println("done");
            System.out.print("[Wordbank] Create table frame relation ... ");
            stmt.execute("CREATE TABLE frame_relation (token_id INTEGER, frame_id INTEGER, PRIMARY KEY (token_id, frame_id), FOREIGN KEY (frame_id) references frame(id),FOREIGN KEY (token_id) references token(id))");
            System.out.println("done");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public Connection connect() {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:" + systemDir + "/aboard.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

//            return conn;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
//            try {
//                if (conn != null) {
//                    conn.close();
//                }
//            } catch (SQLException ex) {
//                System.out.println(ex.getMessage());
//            }
        }

        return conn;
    }

    public void finalize() {
        if (this.created) {
            try {
                this.dropTables();
                this.connection.commit();
                this.connection.close();
            } catch (Exception var2) {
                var2.printStackTrace();
            }
        }

    }

    private void dropTables() throws JSLIMException {
        try {
            Statement statement = this.connection.createStatement();
            statement.execute("DROP TABLE type");
            statement.execute("DROP TABLE semantic");
            statement.execute("DROP TABLE proplet");
            statement.execute("DROP TABLE semantic_relation");
            statement.execute("DROP TABLE token");
            statement.execute("DROP TABLE frame");
            statement.execute("DROP TABLE frame_relation");
        } catch (SQLException var2) {
            throw new JSLIMException("[WordBank] Cannot drop tables!", var2);
        }
    }

    /**
     * Store wordbank data
     */
    public void store() {

        String filename = "./src/english1-syn/wordbank.sql";
        File file = new File(filename);

        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));
            System.out.print("Saving data... ");
            String aLine;
            while ((aLine = in.readLine()) != null) {
                Statement statement = this.connection.createStatement();
                if (!aLine.equals("") && !aLine.startsWith("--") && !aLine.equals("\n") && !aLine.equals(" ") && !aLine.isEmpty()) {
                    statement.execute(aLine);
                }
            }
            System.out.println("done");
//            PreparedStatement pstmt = this.connection.prepareStatement("");
//            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     *
     * @param pos
     * @return list of first candidates based on CS option (POS)
     */
    public List<String> getFirstCandidates(String pos) throws SQLException {
        List<String> candidates = new ArrayList<>();
        String sql = "select proplet.core from proplet" +
                " inner join token on token.proplet_id = proplet.id" +
                " inner join type on token.type_id = type.id and type.pos = '" + pos + "'";
        try {
            ResultSet rs = select(sql);

            while (rs.next()) {
                candidates.add(rs.getString("core"));
            }
        }catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return candidates;
    }


    public List<String> getFramesRelations(String core, List<String> semantics) {
        List<String> frames = new ArrayList<>();

//        int proplet_id = ;

        return this.getFrames(this.getPropletBySemantic(core, semantics));
    }

    public List<String> getFrames(int proplet_id) {
        // pegar o token_id a partir deste proplet
//        int token_id = this.getTokenByPropletId(proplet_id);
        // pegar a lista de frames a partir do token_id
        // retornar a lista de frames
        return this.getFrameList(this.getTokenByPropletId(proplet_id));
    }

    public List<String> getFrameList(int token_id) {
        String sql = "select frame.name from frame" +
                " inner join frame_relation on frame_relation.frame_id = frame.id and frame_relation.token_id = token.id" +
                " inner join token on token.id = " + token_id;

        List<String> frames = new ArrayList<>();

        try {
            ResultSet rs = select(sql);
            while (rs.next()) {
                frames.add(rs.getString("name"));
            }

        }catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return frames;
    }

    public int getTokenByPropletId(int proplet_id) {
        String sql = "select token.id from token" +
                " inner join proplet on proplet.id = token.proplet_id and proplet.id = " + proplet_id;
        try {
            ResultSet rs = select(sql);

           return rs.getInt("id");

        }catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return 0;
    }

    public int getPropletBySemantic(String core, List<String> semantics) {
        StringBuilder sb = new StringBuilder();
        int proplet_id = 0;
        for (int i = 0; i < semantics.size(); i++) {
            if ((i + 1) < semantics.size()) {
                sb.append("'" + semantics.get(i) + "', ");
            } else {
                sb.append("'" + semantics.get(i) + "'");
            }
        }
        String sql = "select distinct proplet.id from proplet" +
                " inner join semantic on semantic.name IN (" + sb.toString() + ")" +
                " inner join semantic_relation on semantic_relation.semantic_id = semantic.id and semantic_relation.proplet_id = proplet.id" +
                " where proplet.core = '" + core + "'";

        try {
            ResultSet rs = select(sql);

//            while (rs.next()) {
                proplet_id = rs.getInt("id");
//            }
        }catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return proplet_id;
    }

    public List<String> getCandidates(String classes) {
        List<String> candidates = new ArrayList<>();

        String sql = "select distinct proplet.core from proplet" +
                " inner join semantic on semantic.name IN (" + classes + ")" +
                " inner join semantic_relation on semantic_relation.proplet_id = proplet.id and semantic_relation.semantic_id = semantic.id";

        try {
            ResultSet rs = select(sql);

            while (rs.next()) {
                candidates.add(rs.getString("core"));
            }
        }catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return candidates;
    }

    /**
     * select tables
     */
    public ResultSet select(String sql){

        try {
            Statement stmt  = this.connection.createStatement();
//            ResultSet rs    = stmt.executeQuery(sql);
            // loop through the result set
//            while (rs.next()) {
//                System.out.println(rs.getString("core"));
//            }
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
