// Local: src/main/java/model/Categoria.java

package model;

public class Categoria {

    // Atributos mapeados das colunas do DB
    private int idCategoria;
    private String nomeCategoria; // UNIQUE NOT NULL no DB

    // Construtor 1: Para ler (READ) dados do banco
    public Categoria(int idCategoria, String nomeCategoria) {
        this.idCategoria = idCategoria;
        this.nomeCategoria = nomeCategoria;
    }

    // Construtor 2: Para cadastrar (CREATE) nova categoria (ID é gerado pelo DB)
    public Categoria(String nomeCategoria) {
        this.nomeCategoria = nomeCategoria;
    }

    // -----------------------------------------------------
    // GETTERS e SETTERS
    // -----------------------------------------------------

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNomeCategoria() {
        return nomeCategoria;
    }

    public void setNomeCategoria(String nomeCategoria) {
        this.nomeCategoria = nomeCategoria;
    }

    // exibir no console
    @Override
    public String toString() {
        return "ID [" + idCategoria + "] - " + nomeCategoria;
    }
}