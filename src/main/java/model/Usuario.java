// Local: src/main/java/model/Usuario.java

package model;

import java.time.LocalDate;

public class Usuario {

    // Atributos (Mapeamento das colunas do DB)
    private int idUsuario;
    private String nome;
    private String login;
    private String senhaMestraHash; // CHAR(64)
    private String salt;            // CHAR(32)
    private LocalDate dataCadastro;

    // Construtor 1: Para ler (READ) dados do banco (todos os campos)
    public Usuario(int idUsuario, String nome, String login,
                   String senhaMestraHash, String salt, LocalDate dataCadastro) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.login = login;
        this.senhaMestraHash = senhaMestraHash;
        this.salt = salt;
        this.dataCadastro = dataCadastro;
    }

    // Construtor 2: Para cadastrar (CREATE) novo usuário (ID e Data são gerados pelo DB)
    public Usuario(String nome, String login, String senhaMestraHash, String salt) {
        this.nome = nome;
        this.login = login;
        this.senhaMestraHash = senhaMestraHash;
        this.salt = salt;
    }

    // -----------------------------------------------------
    // GETTERS e SETTERS (Gerados pelo IntelliJ)
    // -----------------------------------------------------

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenhaMestraHash() {
        return senhaMestraHash;
    }

    public void setSenhaMestraHash(String senhaMestraHash) {
        this.senhaMestraHash = senhaMestraHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}