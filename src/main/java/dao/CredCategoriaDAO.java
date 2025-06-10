// Local: src/main/java/dao/CredCategoriaDAO.java

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import util.ConexaoDB;

public class CredCategoriaDAO {

    /**
     * PROCESSO DE NEGÓCIO: Associa um ItemCredencial a uma Categoria. (CREATE na tabela associativa)
     * @param idItem ID da credencial.
     * @param idCategoria ID da categoria.
     * @return true se a associação foi bem-sucedida.
     */
    public boolean associar(int idItem, int idCategoria) {
        // Tenta inserir a associação. Se já existir (PK Duplicada), o PostgreSQL retornará um erro.
        String sql = "INSERT INTO CRED_CATEGORIA (id_item, id_categoria) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean sucesso = false;

        try {
            conn = ConexaoDB.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idItem);
            stmt.setInt(2, idCategoria);

            if (stmt.executeUpdate() > 0) {
                System.out.println("[SUCESSO] Item [" + idItem + "] associado à Categoria [" + idCategoria + "].");
                sucesso = true;
            }

        } catch (SQLException e) {
            // Se a associação já existir, ou se FK for inválida
            System.err.println("[ERRO] Falha ao associar. (Pode já estar associado ou IDs inválidos): " + e.getMessage());
        } finally {
            ConexaoDB.fechar(conn, stmt);
        }
        return sucesso;
    }

    /**
     * PROCESSO DE NEGÓCIO: Desassocia um ItemCredencial de uma Categoria. (DELETE na tabela associativa)
     * @param idItem ID da credencial.
     * @param idCategoria ID da categoria.
     * @return true se a desassociação foi bem-sucedida.
     */
    public boolean desassociar(int idItem, int idCategoria) {
        // Deleta a linha específica na tabela M:N
        String sql = "DELETE FROM CRED_CATEGORIA WHERE id_item = ? AND id_categoria = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean sucesso = false;

        try {
            conn = ConexaoDB.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idItem);
            stmt.setInt(2, idCategoria);

            if (stmt.executeUpdate() > 0) {
                System.out.println("[SUCESSO] Associação removida entre Item [" + idItem + "] e Categoria [" + idCategoria + "].");
                sucesso = true;
            } else {
                System.out.println("[INFO] Associação não encontrada para remover.");
            }

        } catch (SQLException e) {
            System.err.println("[ERRO] Falha ao desassociar: " + e.getMessage());
        } finally {
            ConexaoDB.fechar(conn, stmt);
        }
        return sucesso;
    }

    /**
     * PROCESSO DE NEGÓCIO: Lista as Categorias associadas a um Item (READ na tabela associativa, com JOIN).
     * @param idItem ID da credencial.
     * @return Um Map de [ID da Categoria, Nome da Categoria].
     */
    public Map<Integer, String> listarCategoriasPorItem(int idItem) {
        // JOIN entre CRED_CATEGORIA e CATEGORIA
        String sql = "SELECT C.id_categoria, C.nome_categoria FROM CATEGORIA C " +
                "JOIN CRED_CATEGORIA AC ON C.id_categoria = AC.id_categoria " +
                "WHERE AC.id_item = ?";

        Map<Integer, String> categorias = new HashMap<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConexaoDB.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idItem);
            rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_categoria");
                String nome = rs.getString("nome_categoria");
                categorias.put(id, nome);
            }

            System.out.println("[INFO] " + categorias.size() + " categorias encontradas para o Item [" + idItem + "].");

        } catch (SQLException e) {
            System.err.println("[ERRO] Falha ao listar categorias do item: " + e.getMessage());
        } finally {
            ConexaoDB.fechar(conn, stmt, rs);
        }
        return categorias;
    }
}