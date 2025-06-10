// Local: src/main/java/dao/UsuarioDAO.java

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.Usuario;
import util.ConexaoDB;

public class UsuarioDAO {

    /**
     * CADASTRO (CREATE) - Insere um novo usuário no banco de dados.
     * @param usuario O objeto Usuario com os dados a serem salvos.
     * @return O ID gerado para o novo usuário ou -1 em caso de falha.
     */
    public int cadastrarUsuario(Usuario usuario) {
        // O campo data_cadastro é preenchido automaticamente pelo DEFAULT CURRENT_DATE
        String sql = "INSERT INTO USUARIO (nome, login, senha_mestra_hash, salt) " +
                "VALUES (?, ?, ?, ?)";
        int idGerado = -1;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConexaoDB.getConexao();
            // Pede ao banco para retornar o ID gerado (SERIAL)
            stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

            // 1. Configura os parâmetros do SQL
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getLogin());
            stmt.setString(3, usuario.getSenhaMestraHash());
            stmt.setString(4, usuario.getSalt());

            // 2. Executa a inserção
            int linhasAfetadas = stmt.executeUpdate();

            // 3. Obtém o ID gerado se a inserção foi bem-sucedida
            if (linhasAfetadas > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    idGerado = rs.getInt(1);
                    // SAÍDA ESPERADA no console
                    System.out.println("[SUCESSO] Usuário '" + usuario.getLogin() +
                            "' cadastrado com ID [" + idGerado + "].");
                }
            }

        } catch (SQLException e) {
            // SAÍDA ESPERADA no console em caso de falha (ex: login duplicado)
            System.err.println("[ERRO] Falha ao cadastrar o usuário: " + e.getMessage());
        } finally {
            ConexaoDB.fechar(conn, stmt, rs); // Fecha os recursos de forma segura
        }
        return idGerado;
    }

    // --- Outros métodos CRUD virão aqui (READ, UPDATE, DELETE) ---

}