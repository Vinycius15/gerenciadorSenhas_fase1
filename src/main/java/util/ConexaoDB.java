// Local: src/main/java/util/ConexaoDB.java

package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoDB {

    // 🚨 ATUALIZE ESTES VALORES COM SUAS CREDENCIAIS DO POSTGRESQL 🚨
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/gerenciador_senhas_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "SUA_SENHA_AQUI";

    /**
     * Tenta estabelecer a conexão com o banco de dados.
     * @return Um objeto Connection, ou lança uma exceção se a conexão falhar.
     * @throws SQLException Se houver um erro de acesso ao banco (driver, credenciais, servidor).
     */
    public static Connection getConexao() throws SQLException {
        // O DriverManager usa o driver do PostgreSQL que você adicionou via Maven
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Fecha a conexão e outros recursos JDBC de forma segura.
     * Evita vazamento de recursos no banco de dados.
     */
    public static void fechar(Connection conn, java.sql.Statement stmt, java.sql.ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Erro ao fechar recursos JDBC: " + e.getMessage());
        }
    }

    // Sobrecarga para fechar apenas Connection e Statement
    public static void fechar(Connection conn, java.sql.Statement stmt) {
        fechar(conn, stmt, null);
    }
}