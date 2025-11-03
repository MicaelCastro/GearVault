package com.mycompany.videogamesstock;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// Classe responsável por listar, adicionar, editar e eliminar produtos.
public class ProdutoFormDialog extends JPanel {

    private JTable tabelaProdutos;
    private JTextField tfNome;
    private JComboBox<String> cbPlataforma;
    private JTextField tfPreco;
    private JTextField tfStock;
    private JLabel lblAviso;

    private ProdutoDAO produtoDAO = new ProdutoDAO();         // DAO para operações com produtos
    private Produto produtoAtual;                             // Produto atualmente selecionado/editar

    public ProdutoFormDialog() {
        setLayout(new BorderLayout(10, 10));
        initComponents();
        carregarTabela();
    }

    private void initComponents() {
        // --- Tabela de produtos ---
        String[] colunas = {"ID", "Jogo", "Plataforma", "Preço", "Stock"};
        tabelaProdutos = new JTable(new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;                                         // Tabela só leitura
            }
        });
        add(new JScrollPane(tabelaProdutos), BorderLayout.CENTER);

        // --- Painel inferior com campos e botões ---
        JPanel painelInferior = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nome
        gbc.gridx = 0; gbc.gridy = 0;
        painelInferior.add(new JLabel("Nome:"), gbc);
        tfNome = new JTextField(20);
        gbc.gridx = 1;
        painelInferior.add(tfNome, gbc);

        // Plataforma
        gbc.gridx = 0; gbc.gridy = 1;
        painelInferior.add(new JLabel("Plataforma:"), gbc);
        cbPlataforma = new JComboBox<>();
        preencherPlataformas();
        gbc.gridx = 1;
        painelInferior.add(cbPlataforma, gbc);

        // Preço
        gbc.gridx = 0; gbc.gridy = 2;
        painelInferior.add(new JLabel("Preço:"), gbc);
        tfPreco = new JTextField(10);
        gbc.gridx = 1;
        painelInferior.add(tfPreco, gbc);

        // Stock
        gbc.gridx = 0; gbc.gridy = 3;
        painelInferior.add(new JLabel("Stock:"), gbc);
        tfStock = new JTextField(10);
        gbc.gridx = 1;
        painelInferior.add(tfStock, gbc);

        // Aviso inline
        lblAviso = new JLabel(" ", JLabel.CENTER);
        lblAviso.setForeground(Color.RED);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        painelInferior.add(lblAviso, gbc);

        // Botões
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdicionar = new JButton("Adicionar");
        JButton btnSalvar = new JButton("Salvar edição");
        JButton btnApagar = new JButton("Eliminar");
        btnPanel.add(btnAdicionar);
        btnPanel.add(btnSalvar);
        btnPanel.add(btnApagar);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        painelInferior.add(btnPanel, gbc);

        add(painelInferior, BorderLayout.SOUTH);

        // --- Ações dos botões ---
        btnAdicionar.addActionListener(e -> adicionarProduto());
        btnSalvar.addActionListener(e -> salvarEdicao());
        btnApagar.addActionListener(e -> apagarProduto());
        tabelaProdutos.getSelectionModel().addListSelectionListener(e -> carregarProdutoSelecionado());
    }

    // Preenche ComboBox com plataformas únicas existentes
    private void preencherPlataformas() {
        cbPlataforma.removeAllItems();
        produtoDAO.getTodasPlataformas().forEach(cbPlataforma::addItem);
    }

    // Carrega dados da tabela de produtos
    private void carregarTabela() {
        DefaultTableModel model = (DefaultTableModel) tabelaProdutos.getModel();
        model.setRowCount(0);
        List<Produto> lista = produtoDAO.listarProdutos();
        for (Produto p : lista) {
            model.addRow(new Object[]{
                    p.getId(),
                    p.getNome(),
                    p.getPlataforma(),
                    p.getPreco(),
                    p.getStock()
            });
        }
    }

    // Limpa campos de edição/adição
    private void limparCampos() {
        tfNome.setText("");
        cbPlataforma.setSelectedIndex(0);
        tfPreco.setText("");
        tfStock.setText("");
        lblAviso.setText(" ");
        produtoAtual = null;
        tabelaProdutos.clearSelection();
    }

    // --- ADICIONAR PRODUTO ---
    private void adicionarProduto() {
        lblAviso.setText(" ");
        String nome = tfNome.getText().trim();
        String plataforma = (String) cbPlataforma.getSelectedItem();
        double preco;
        int stock;

        if (nome.isEmpty()) {
            lblAviso.setText("Insira o nome do produto.");
            return;
        }

        try {
            preco = Double.parseDouble(tfPreco.getText().trim());
            stock = Integer.parseInt(tfStock.getText().trim());
        } catch (NumberFormatException e) {
            lblAviso.setText("Preço ou Stock inválidos!");
            return;
        }
       
        Produto novo = new Produto();
        novo.setNome(nome);
        novo.setPlataforma(plataforma);
        novo.setPreco(preco);
        novo.setStock(stock);

        produtoDAO.criarProduto(novo);             // Insere no banco
        carregarTabela();                          // Actualiza tabela
        limparCampos();
    }

    // --- CARREGA PRODUTO SELECIONADO PARA EDIÇÃO ---
    private void carregarProdutoSelecionado() {
        int linha = tabelaProdutos.getSelectedRow();
        if (linha == -1) return;
        int id = (int) tabelaProdutos.getValueAt(linha, 0);
        produtoAtual = produtoDAO.getProdutoById(id);
        tfNome.setText(produtoAtual.getNome());
        cbPlataforma.setSelectedItem(produtoAtual.getPlataforma());
        tfPreco.setText(String.valueOf(produtoAtual.getPreco()));
        tfStock.setText(String.valueOf(produtoAtual.getStock()));
    }

    // --- EDITAR PRODUTO ---
    private void salvarEdicao() {
        if (produtoAtual == null) {
            lblAviso.setText("Selecione um produto para editar.");
            return;
        }

        lblAviso.setText(" ");
        String nome = tfNome.getText().trim();
        String plataforma = (String) cbPlataforma.getSelectedItem();
        double preco;
        int stock;

        try {
            preco = Double.parseDouble(tfPreco.getText().trim());
            stock = Integer.parseInt(tfStock.getText().trim());
        } catch (NumberFormatException e) {
            lblAviso.setText("Preço ou Stock inválidos!");
            return;
        }        

        produtoAtual.setNome(nome);
        produtoAtual.setPlataforma(plataforma);
        produtoAtual.setPreco(preco);
        produtoAtual.setStock(stock);

        produtoDAO.atualizarProduto(produtoAtual);         // Atualiza na base de dados
        carregarTabela();
        limparCampos();
    }

    // --- APAGAR PRODUTO ---
    private void apagarProduto() {
        int linha = tabelaProdutos.getSelectedRow();
        if (linha == -1) {
            lblAviso.setText("Selecione um produto para eliminar.");
            return;
        }

        int id = (int) tabelaProdutos.getValueAt(linha, 0);
        Produto p = produtoDAO.getProdutoById(id);
        if (p != null) {
            produtoDAO.apagarProduto(p);                   // Remove da base de dados
            carregarTabela();
            limparCampos();
        }
    }
}