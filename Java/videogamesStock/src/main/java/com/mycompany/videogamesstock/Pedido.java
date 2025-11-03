package com.mycompany.videogamesstock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private int id;
    private Cliente cliente;
    private LocalDateTime data;
    private List<PedidoItem> itens;

    // ---------------- CONSTRUTOR ----------------
    public Pedido() {
        this.itens = new ArrayList<>(); // Inicializa a lista de itens vazia
        this.data = LocalDateTime.now(); // Define data atual como padrão
    }

    // ---------------- GETTERS E SETTERS ----------------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }

    public List<PedidoItem> getItens() { return itens; }
    public void setItens(List<PedidoItem> itens) { this.itens = itens; }
    
    
    // ---------------- MÉTODO AUXILIAR ----------------
    /**
     * Adiciona um item à lista do pedido.
     * Não faz nada se o item for null.
     */
    public void adicionarItem(PedidoItem item) {
        if(item != null) {
            itens.add(item);
        }
    }
}