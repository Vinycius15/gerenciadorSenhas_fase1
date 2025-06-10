package model;

import java.time.LocalDateTime;

public class ItemCredencial {

    // Atributos mapeados da tabela ITEM_CREDENCIAL
    private int idItem;
    private int idUsuario;         // FK para o proprietário
    private String tipo;           // Ex: 'SENHA', 'CARTAO', 'NOTA'
    private String titulo;
    private String loginServico;
    private String senhaCriptografada; // O dado sensível
    private String url;
    private LocalDateTime dataUltimaMod;

    // Construtor 1: Para ler (READ) dados do banco (todos os campos)
    public ItemCredencial(int idItem, int idUsuario, String tipo, String titulo,
                          String loginServico, String senhaCriptografada, String url,
                          LocalDateTime dataUltimaMod) {
        this.idItem = idItem;
        this.idUsuario = idUsuario;
        this.tipo = tipo;
        this.titulo = titulo;
        this.loginServico = loginServico;
        this.senhaCriptografada = senhaCriptografada;
        this.url = url;
        this.dataUltimaMod = dataUltimaMod;
    }

    // Construtor 2: Para cadastrar (CREATE) novo item (ID e Data são gerados ou omitidos)
    public ItemCredencial(int idUsuario, String tipo, String titulo, String loginServico,
                          String senhaCriptografada, String url) {
        this.idUsuario = idUsuario;
        this.tipo = tipo;
        this.titulo = titulo;
        this.loginServico = loginServico;
        this.senhaCriptografada = senhaCriptografada;
        this.url = url;
    }

    // 🚨 Importante: Lembre-se de adicionar a importação: import java.time.LocalDateTime;

    // -----------------------------------------------------
    // GETTERS e SETTERS (Essenciais para o CRUD)
    // -----------------------------------------------------

    public int getIdItem() {
        return idItem;
    }

    public void setIdItem(int idItem) {
        this.idItem = idItem;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getLoginServico() {
        return loginServico;
    }

    public void setLoginServico(String loginServico) {
        this.loginServico = loginServico;
    }

    public String getSenhaCriptografada() {
        return senhaCriptografada;
    }

    public void setSenhaCriptografada(String senhaCriptografada) {
        this.senhaCriptografada = senhaCriptografada;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getDataUltimaMod() {
        return dataUltimaMod;
    }

    public void setDataUltimaMod(LocalDateTime dataUltimaMod) {
        this.dataUltimaMod = dataUltimaMod;
    }
}