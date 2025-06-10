// Local: src/main/java/dao/CategoriaDAO.java

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Categoria;
import util.ConexaoDB;

public class CategoriaDAO {

    //create
    public boolean cadastrarCategoria(Categoria categoria) {
        String sql = "INSERT INTO CATEGORIA (nome_categoria) VALUES (?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean sucesso = false;

        try {
            conn = ConexaoDB.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, categoria.getNomeCategoria());

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                System.out.println("[SUCESSO] Categoria '" + categoria.getNomeCategoria() + "' cadastrada.");
                sucesso = true;
            }

        } catch (SQLException e) {
            // Se o nome já existir (UNIQUE constraint)
            System.err.println("[ERRO] Falha ao cadastrar a categoria: " + e.getMessage());
        } finally {
            ConexaoDB.fechar(conn, stmt);
        }
        return sucesso;
    }

    //read()
    public List<Categoria> listarTodas() {
        String sql = "SELECT id_categoria, nome_categoria FROM CATEGORIA ORDER BY nome_categoria";
        List<Categoria> categorias = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConexaoDB.getConexao();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_categoria");
                String nome = rs.getString("nome_categoria");
                Categoria cat = new Categoria(id, nome);
                categorias.add(cat);
            }

        } catch (SQLException e) {
            System.err.println("[ERRO] Falha ao listar categorias: " + e.getMessage());
        } finally {
            ConexaoDB.fechar(conn, stmt, rs);
        }
        return categorias;
    }

    //update
    public boolean atualizarCategoria(Categoria categoria) {
        String sql = "UPDATE CATEGORIA SET nome_categoria = ? WHERE id_categoria = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean sucesso = false;

        try {
            conn = ConexaoDB.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, categoria.getNomeCategoria());
            stmt.setInt(2, categoria.getIdCategoria());

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                System.out.println("[SUCESSO] Categoria ID [" + categoria.getIdCategoria() + "] atualizada para '" + categoria.getNomeCategoria() + "'.");
                sucesso = true;
            } else {
                System.out.println("[INFO] Categoria ID [" + categoria.getIdCategoria() + "] não encontrada para atualização.");
            }

        } catch (SQLException e) {
            System.err.println("[ERRO] Falha ao atualizar categoria: " + e.getMessage());
        } finally {
            ConexaoDB.fechar(conn, stmt);
        }
        return sucesso;
    }

    //delete
    public boolean removerCategoria(int idCategoria) {
        String sql = "DELETE FROM CATEGORIA WHERE id_categoria = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean sucesso = false;

        try {
            conn = ConexaoDB.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idCategoria);

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                System.out.println("[SUCESSO] Categoria ID [" + idCategoria + "] removida com sucesso.");
                sucesso = true;
            } else {
                System.out.println("[INFO] Categoria ID [" + idCategoria + "] não encontrada para remoção.");
            }

        } catch (SQLException e) {
            System.err.println("[ERRO] Falha ao remover categoria: " + e.getMessage());
        } finally {
            ConexaoDB.fechar(conn, stmt);
        }
        return sucesso;
    }
}