package com.mycompany.videogamesstock;

import java.sql.Timestamp;

public class Produto {
    private int id;
    private String nome;
    private String plataforma;
    private double preco;
    private int stock;    

    // ---------------- GETTERS E SETTERS ----------------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getPlataforma() { return plataforma; }
    public void setPlataforma(String plataforma) { this.plataforma = plataforma; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    
}