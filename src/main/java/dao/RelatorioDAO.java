// Local: src/main/java/dao/RelatorioDAO.java

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import util.ConexaoDB;

public class RelatorioDAO {

    /**
     * RELATÓRIO 1: Contagem de Itens Salvos por Usuário.
     * Envolve: USUARIO e ITEM_CREDENCIAL.
     * @return Map<String, Integer> onde a chave é o nome do usuário e o valor é a contagem de itens.
     */
    public Map<String, Integer> contarItensPorUsuario() {
        // Usa LEFT JOIN para incluir usuários que não têm itens (contagem = 0)
        String sql = "SELECT U.nome, COUNT(I.id_item) AS total_itens " +
                "FROM USUARIO U " +
                "LEFT JOIN ITEM_CREDENCIAL I ON U.id_usuario = I.id_usuario " +
                "GROUP BY U.nome " +
                "ORDER BY total_itens DESC";

        // LinkedHashMap para manter a ordem da consulta
        Map<String, Integer> resultados = new LinkedHashMap<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConexaoDB.getConexao();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String nome = rs.getString("nome");
                int contagem = rs.getInt("total_itens");
                resultados.put(nome, contagem);
            }

        } catch (SQLException e) {
            System.err.println("[ERRO] Falha ao gerar Relatório 1: " + e.getMessage());
        } finally {
            ConexaoDB.fechar(conn, stmt, rs);
        }
        return resultados;
    }

    /**
     * RELATÓRIO 2: Contagem de Credenciais Associadas a Cada Categoria.
     * Envolve: CATEGORIA e CRED_CATEGORIA (Tabela Associativa).
     * @return Map<String, Integer> onde a chave é o nome da categoria e o valor é o número de itens.
     */
    public Map<String, Integer> contarItensPorCategoria() {
        // Usa LEFT JOIN para incluir categorias que não têm itens (contagem = 0)
        String sql = "SELECT C.nome_categoria, COUNT(AC.id_item) AS total_associados " +
                "FROM CATEGORIA C " +
                "LEFT JOIN CRED_CATEGORIA AC ON C.id_categoria = AC.id_categoria " +
                "GROUP BY C.nome_categoria " +
                "ORDER BY total_associados DESC";

        Map<String, Integer> resultados = new LinkedHashMap<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConexaoDB.getConexao();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String nome = rs.getString("nome_categoria");
                int contagem = rs.getInt("total_associados");
                resultados.put(nome, contagem);
            }

        } catch (SQLException e) {
            System.err.println("[ERRO] Falha ao gerar Relatório 2: " + e.getMessage());
        } finally {
            ConexaoDB.fechar(conn, stmt, rs);
        }
        return resultados;
    }

    /**
     * RELATÓRIO 3: Itens que Pertencem a Múltiplas Categorias (Complexidade M:N).
     * Envolve: ITEM_CREDENCIAL, CRED_CATEGORIA, CATEGORIA.
     * Este relatório mostra a complexidade do modelo M:N.
     * @return Map<String, Integer> onde a chave é o título do item e o valor é o número de categorias associadas.
     */
    public Map<String, Integer> itensComMultiplasCategorias(int minimoCategorias) {
        String sql = "SELECT I.titulo, COUNT(AC.id_categoria) AS num_categorias " +
                "FROM ITEM_CREDENCIAL I " +
                "JOIN CRED_CATEGORIA AC ON I.id_item = AC.id_item " +
                "GROUP BY I.titulo " +
                "HAVING COUNT(AC.id_categoria) >= ? " +
                "ORDER BY num_categorias DESC";

        Map<String, Integer> resultados = new LinkedHashMap<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConexaoDB.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, minimoCategorias); // Define o mínimo de categorias (ex: 2)
            rs = stmt.executeQuery();

            while (rs.next()) {
                String titulo = rs.getString("titulo");
                int contagem = rs.getInt("num_categorias");
                resultados.put(titulo, contagem);
            }

        } catch (SQLException e) {
            System.err.println("[ERRO] Falha ao gerar Relatório 3: " + e.getMessage());
        } finally {
            ConexaoDB.fechar(conn, stmt, rs);
        }
        return resultados;
    }
}