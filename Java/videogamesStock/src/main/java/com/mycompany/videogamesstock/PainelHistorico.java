package com.mycompany.videogamesstock;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;

public class PainelHistorico extends JPanel {

    private JTable tabelaHistorico;                            // Tabela para exibir o histórico de pedidos
    private DefaultTableModel modelHistorico;                  // Modelo da tabela (estrutura e dados)
    private PedidoDAO pedidoDAO = new PedidoDAO();             // Acesso aos dados dos pedidos

    // Construtor: inicializa o painel e configura a tabela
    public PainelHistorico() {
        setLayout(new BorderLayout());                         // Define o layout principal
        
        // Cabeçalhos das colunas (suportam internacionalização)
        String[] colunas = {
            I18n.t("id"),
            I18n.t("cliente"),
            I18n.t("data"),
            I18n.t("itens"),
            I18n.t("total")
        };
        
        // Modelo da tabela (não editável)
        modelHistorico = new DefaultTableModel(colunas,0) {
            @Override
            public boolean isCellEditable(int row,int col){ 
                return false; 
            }
        };
        
        // Cria a tabela e adiciona-a ao painel com scroll
        tabelaHistorico = new JTable(modelHistorico);
        add(new JScrollPane(tabelaHistorico), BorderLayout.CENTER);
    }

    // --- Carrega os dados do histórico de pedidos na tabela ---
    public void carregarHistorico() {
        modelHistorico.setRowCount(0);                        // Limpa a tabela
        
        // Percorre todos os pedidos existentes
        for(Pedido p : pedidoDAO.listarPedidos()) {
            StringBuilder itens = new StringBuilder();
            double total = 0;
            
            // Associa os itens e calcula o total
            for(PedidoItem i : p.getItens()) {
                itens.append(i.getProduto().getNome()).append(" x").append(i.getQuantidade()).append(", ");
                total += i.getQuantidade()*i.getPrecoUnit();
            }
            if(itens.length()>2) itens.setLength(itens.length()-2);
            
            // Adiciona uma nova linha à tabela
            modelHistorico.addRow(new Object[]{
                p.getId(),
                p.getCliente().getNome(),
                p.getData(),
                itens.toString(),
                total
            });
        }
    }
    
    // Atualiza o texto das colunas quando muda o idioma
    public void reloadTexts() {
    modelHistorico.setColumnIdentifiers(new String[]{
        I18n.t("id"),
        I18n.t("cliente"),
        I18n.t("data"),
        I18n.t("itens"),
        I18n.t("total")
    });
    tabelaHistorico.getTableHeader().repaint(); // força atualização do cabeçalho
    revalidate();
    repaint();
    }
}