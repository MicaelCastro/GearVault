package com.mycompany.videogamesstock;

public class PedidoItem {
    private int id;
    private Pedido pedido;
    private Produto produto;
    private int quantidade;
    private double precoUnit;

    public PedidoItem() {}                     // Construtor vazio

    // ---------------- GETTERS E SETTERS ----------------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }

    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public double getPrecoUnit() { return precoUnit; }
    public void setPrecoUnit(double precoUnit) { this.precoUnit = precoUnit; }
}