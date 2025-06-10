// Local: src/main/java/dao/ItemCredencialDAO.java

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.ItemCredencial;
import util.ConexaoDB;
import util.CriptoUtil;

public class ItemCredencialDAO {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // --- CREATE ---

    /**
     * CADASTRO (CREATE) - Insere um novo item de credencial no cofre.
     * Criptografa o dado sensível antes de salvar.
     */
    public int cadastrarItem(ItemCredencial item, String senhaMestraTextoPuro) {
        String sql = "INSERT INTO ITEM_CREDENCIAL (id_usuario, tipo, titulo, login_servico, senha_criptografada, url) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        int idGerado = -1;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // 1. Obtém o Salt do usuário (necessário para a chave de criptografia)
            String saltUsuario = usuarioDAO.buscarSaltPorId(item.getIdUsuario());

            // 2. CRIPTOGRAFA o dado sensível
            String dadoCriptografado = CriptoUtil.criptografarItem(
                    item.getSenhaCriptografada(), // Dado em texto P U R O neste ponto
                    senhaMestraTextoPuro,
                    saltUsuario
            );

            conn = ConexaoDB.getConexao();
            stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, item.getIdUsuario());
            stmt.setString(2, item.getTipo());
            stmt.setString(3, item.getTitulo());
            stmt.setString(4, item.getLoginServico());
            stmt.setString(5, dadoCriptografado); // Salva o dado CRIPTOGRAFADO
            stmt.setString(6, item.getUrl());

            if (stmt.executeUpdate() > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    idGerado = rs.getInt(1);
                    System.out.println("[SUCESSO] Item '" + item.getTitulo() + "' cadastrado com ID [" + idGerado + "].");
                }
            }
        } catch (Exception e) { // Captura SQLException e RuntimeException de CriptoUtil
            System.err.println("[ERRO] Falha ao cadastrar item: " + e.getMessage());
        } finally {
            ConexaoDB.fechar(conn, stmt, rs);
        }
        return idGerado;
    }

    // --- READ ---

    /**
     * CONSULTA (READ) - Lista todos os itens de um usuário e descriptografa o dado sensível.
     */
    public List<ItemCredencial> listarItensPorUsuario(int idUsuario, String senhaMestraTextoPuro) {
        String sql = "SELECT * FROM ITEM_CREDENCIAL WHERE id_usuario = ?";
        List<ItemCredencial> itens = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // 1. Busca o Salt do usuário para descriptografia
            String saltUsuario = usuarioDAO.buscarSaltPorId(idUsuario);

            conn = ConexaoDB.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idUsuario);
            rs = stmt.executeQuery();

            while (rs.next()) {
                // 2. DESCRIPTOGRAFA o dado lido do banco
                String dadoCriptografado = rs.getString("senha_criptografada");
                String dadoDescriptografado = CriptoUtil.descriptografarItem(
                        dadoCriptografado,
                        senhaMestraTextoPuro,
                        saltUsuario
                );

                // 3. Mapeia e adiciona à lista
                int id = rs.getInt("id_item");
                String tipo = rs.getString("tipo");
                String titulo = rs.getString("titulo");
                String loginServico = rs.getString("login_servico");
                String url = rs.getString("url");
                LocalDateTime dataMod = rs.getTimestamp("data_ultima_modificacao").toLocalDateTime();

                ItemCredencial item = new ItemCredencial(id, idUsuario, tipo, titulo,
                        loginServico, dadoDescriptografado, // SALVA O DADO DESCRIPTOGRAFADO NO MODEL
                        url, dataMod);
                itens.add(item);
            }

        } catch (Exception e) {
            System.err.println("[ERRO] Falha ao listar ou descriptografar itens: " + e.getMessage());
        } finally {
            ConexaoDB.fechar(conn, stmt, rs);
        }
        return itens;
    }

    // --- UPDATE ---

    /**
     * ATUALIZAÇÃO (UPDATE) - Atualiza os dados de um item de credencial.
     * Requer a criptografia do novo dado sensível.
     */
    public boolean atualizarItem(ItemCredencial item, String senhaMestraTextoPuro) {
        String sql = "UPDATE ITEM_CREDENCIAL SET tipo=?, titulo=?, login_servico=?, senha_criptografada=?, url=?, data_ultima_modificacao=CURRENT_TIMESTAMP WHERE id_item=? AND id_usuario=?";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean sucesso = false;

        try {
            String saltUsuario = usuarioDAO.buscarSaltPorId(item.getIdUsuario());
            String dadoCriptografado = CriptoUtil.criptografarItem(
                    item.getSenhaCriptografada(),
                    senhaMestraTextoPuro,
                    saltUsuario
            );

            conn = ConexaoDB.getConexao();
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, item.getTipo());
            stmt.setString(2, item.getTitulo());
            stmt.setString(3, item.getLoginServico());
            stmt.setString(4, dadoCriptografado); // Salva o novo dado CRIPTOGRAFADO
            stmt.setString(5, item.getUrl());
            stmt.setInt(6, item.getIdItem());
            stmt.setInt(7, item.getIdUsuario());

            if (stmt.executeUpdate() > 0) {
                System.out.println("[SUCESSO] Item ID [" + item.getIdItem() + "] atualizado.");
                sucesso = true;
            } else {
                System.out.println("[INFO] Item ID [" + item.getIdItem() + "] não encontrado ou você não tem permissão.");
            }

        } catch (Exception e) {
            System.err.println("[ERRO] Falha ao atualizar item: " + e.getMessage());
        } finally {
            ConexaoDB.fechar(conn, stmt);
        }
        return sucesso;
    }

    // --- DELETE ---

    /**
     * REMOÇÃO (DELETE) - Remove um item de credencial.
     * A remoção do item também removerá suas associações em CRED_CATEGORIA (CASCADE).
     */
    public boolean removerItem(int idItem, int idUsuario) {
        // Garantir que apenas o dono possa deletar o item
        String sql = "DELETE FROM ITEM_CREDENCIAL WHERE id_item = ? AND id_usuario = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean sucesso = false;

        try {
            conn = ConexaoDB.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idItem);
            stmt.setInt(2, idUsuario);

            if (stmt.executeUpdate() > 0) {
                System.out.println("[SUCESSO] Item ID [" + idItem + "] removido com sucesso.");
                sucesso = true;
            } else {
                System.out.println("[INFO] Item ID [" + idItem + "] não encontrado ou não pertence a este usuário.");
            }

        } catch (SQLException e) {
            System.err.println("[ERRO] Falha ao remover item: " + e.getMessage());
        } finally {
            ConexaoDB.fechar(conn, stmt);
        }
        return sucesso;
    }
}