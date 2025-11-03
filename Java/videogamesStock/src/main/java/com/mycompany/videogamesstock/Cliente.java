package com.mycompany.videogamesstock;

public class Cliente {
    // Atributos privados da classe (encapsulamento)
    private int id;
    private String nome;
    private String contacto;
    private String email;

    // Construtor vazio (necessário para frameworks e instâncias simples)
    public Cliente() {}

    // Construtor completo (inicializa todos os campos)
    public Cliente(int id, String nome, String contacto, String email) {
        this.id = id;
        this.nome = nome;
        this.contacto = contacto;
        this.email = email;
    }

    // Métodos getters e setters para aceder e modificar os atributos
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // Representação textual do objeto (usada para listar)
    @Override
    public String toString() {
        return nome;
    }
}