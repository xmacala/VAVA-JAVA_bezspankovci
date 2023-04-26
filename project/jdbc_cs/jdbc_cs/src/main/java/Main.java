import java.sql.*;
import io.github.cdimascio.dotenv.Dotenv;

public class Main {

    public static void main(String[] args) {
            Dotenv env = null;
            Connection conn = null;
            Statement statement = null;

            // load .env file with database connection details
            env = Dotenv.configure()
                    .directory("/Users/ondrejlesak/Documents/school/6th semester/VAVA/vava/project/jdbc_cs/src/main/java/")
                    .filename(".env")
                    .load();

            String connUrl =  String.format("jdbc:postgresql://%s/%s", env.get("PGDB_HOST"), env.get("PGDB_NAME")); // JDBC connection URL
            try {
                // intialize connection to database
                Class.forName("org.postgresql.Driver");
                conn = DriverManager.getConnection(connUrl, env.get("PGDB_USER"), env.get("PGDB_PASS"));

                // SQL query statement
                String sql = "INSERT INTO lcs(value, num_val) VALUES('example', 1234)";

                statement = conn.createStatement();
                statement.executeUpdate(sql);
                statement.close();

                // SELECT query
                sql = "SELECT * FROM lcs";
                statement = conn.createStatement();
                ResultSet res = statement.executeQuery(sql);

                // display query results
                while (res.next()) {
                    System.out.println(res.getArray("id") + "\t|\t" + res.getArray("value") + "\t|\t" + res.getArray("num_val"));
                }

                res.close();
                statement.close();

                // close the connection after finishing database manipulation
                conn.close();
            }
            catch(SQLException sqle) {
                System.err.println(sqle);
                sqle.printStackTrace();
            }
            catch (ClassNotFoundException cllse) {
                cllse.printStackTrace();
                System.err.println(cllse);
            }
    }
}
