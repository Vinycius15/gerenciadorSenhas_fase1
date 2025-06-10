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
import dao.UsuarioDAO; // Necessário para buscar o Salt do usuário

public class ItemCredencialDAO {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * CADASTRO (CREATE) - Insere um novo item de credencial no cofre.
     * Este métod exige a senha mestra e o salt para CRIPTOGRAFAR o dado antes de salvar.
     * @param item O objeto ItemCredencial (com o dado AINDA em texto P U R O).
     * @param senhaMestraTextoPuro A senha mestra do usuário logado.
     * @return O ID gerado para o novo item ou -1 em caso de falha.
     */
    public int cadastrarItem(ItemCredencial item, String senhaMestraTextoPuro) {
        // 1. Busca o SALT do usuário no banco, que é necessário para derivar a chave AES.
        // Em um sistema real, o Salt seria passado após a autenticação.
        String saltUsuario;
        try {
            saltUsuario = usuarioDAO.buscarSaltPorId(item.getIdUsuario());
        } catch (Exception e) {
            System.err.println("[ERRO] Não foi possível obter o Salt do usuário. Cadastro abortado.");
            return -1;
        }

        // 2. CRIPTOGRAFA o dado sensível (Senha/Cartão/Nota)
        String dadoCriptografado = CriptoUtil.criptografarItem(
                item.getSenhaCriptografada(), // ATENÇÃO: o model.senhaCriptografada contém o DADO EM TEXTO PURO aqui
                senhaMestraTextoPuro,
                saltUsuario
        );

        // 3. Define o SQL para inserção do dado CRIPTOGRAFADO
        String sql = "INSERT INTO ITEM_CREDENCIAL (id_usuario, tipo, titulo, login_servico, senha_criptografada, url) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        int idGerado = -1;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConexaoDB.getConexao();
            stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, item.getIdUsuario());
            stmt.setString(2, item.getTipo());
            stmt.setString(3, item.getTitulo());
            stmt.setString(4, item.getLoginServico());
            stmt.setString(5, dadoCriptografado); // SALVA O DADO CRIPTOGRAFADO!
            stmt.setString(6, item.getUrl());

            if (stmt.executeUpdate() > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    idGerado = rs.getInt(1);
                    System.out.println("[SUCESSO] Item '" + item.getTitulo() +
                            "' cadastrado com ID [" + idGerado + "].");
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERRO] Falha ao cadastrar item: " + e.getMessage());
        } finally {
            ConexaoDB.fechar(conn, stmt, rs);
        }
        return idGerado;
    }

    // add metod aux 'buscarSaltPorId(int idUsuario)' no UsuarioDAO

    //READ
    // UPDATE
    // DELETE
}