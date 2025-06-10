// Local: src/main/java/dao/UsuarioDAO.java

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.Usuario;
import util.ConexaoDB;

public class UsuarioDAO {
   //CREATE()
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
       //READ()
        public Usuario buscarUsuarioPorLogin(String login) {
            // Seleciona todos os campos, incluindo o hash e salt para autenticação
            String sql = "SELECT id_usuario, nome, login, senha_mestra_hash, salt, data_cadastro " +
                    "FROM USUARIO WHERE login = ?";
            Usuario usuario = null;
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = ConexaoDB.getConexao();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, login); // Define o parâmetro SQL

                rs = stmt.executeQuery(); // Executa a consulta

                // Se um resultado for encontrado:
                if (rs.next()) {
                    // Mapeia o resultado do banco para o objeto Usuario
                    int id = rs.getInt("id_usuario");
                    String nome = rs.getString("nome");
                    String hash = rs.getString("senha_mestra_hash");
                    String salt = rs.getString("salt");

                    // Conversão de java.sql.Date para java.time.LocalDate
                    LocalDate dataCadastro = rs.getDate("data_cadastro").toLocalDate();

                    // Cria o objeto Usuario usando o construtor completo
                    usuario = new Usuario(id, nome, login, hash, salt, dataCadastro);

                    // SAÍDA ESPERADA no console (para fins de debug/teste em modo texto)
                    System.out.println("[INFO] Usuário '" + login + "' encontrado com ID: " + id + ".");
                } else {
                    // SAÍDA ESPERADA se não for encontrado
                    System.out.println("[INFO] Usuário com login '" + login + "' não encontrado.");
                }

            } catch (SQLException e) {
                System.err.println("[ERRO] Falha ao buscar usuário: " + e.getMessage());
            } finally {
                ConexaoDB.fechar(conn, stmt, rs);
            }
            return usuario;

        }
    //UODATE()
    public boolean atualizarUsuario(Usuario usuario) {
        // Atualiza apenas Nome e Login. A senha é atualizada em um método separado (mais seguro).
        String sql = "UPDATE USUARIO SET nome = ?, login = ? WHERE id_usuario = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean sucesso = false;

        try {
            conn = ConexaoDB.getConexao();
            stmt = conn.prepareStatement(sql);

            // 1. Configura os novos valores
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getLogin());

            // 2. Define a condição (WHERE)
            stmt.setInt(3, usuario.getIdUsuario());

            int linhasAfetadas = stmt.executeUpdate();

            // SAÍDA ESPERADA no console
            if (linhasAfetadas > 0) {
                System.out.println("[SUCESSO] Dados do usuário ID [" + usuario.getIdUsuario() + "] atualizados.");
                sucesso = true;
            } else {
                System.out.println("[INFO] Nenhum usuário encontrado com ID [" + usuario.getIdUsuario() + "] para atualizar.");
            }

        } catch (SQLException e) {
            // Este erro pode ocorrer se o novo 'login' já existir (UNIQUE constraint)
            System.err.println("[ERRO] Falha ao atualizar usuário: " + e.getMessage());
        } finally {
            ConexaoDB.fechar(conn, stmt); // Fecha os recursos sem ResultSet
        }
        return sucesso;
    }
}
