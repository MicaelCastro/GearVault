package com.mycompany.videogamesstock;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class PainelVenda extends JPanel {

    // ---------------- CAMPOS ----------------
    private JComboBox<Cliente> cbClientes;
    private JTextField tfPesquisa;
    private JTable tabelaProdutos;
    private DefaultTableModel modelProdutos;
    private TableRowSorter<DefaultTableModel> sorterProdutos;

    private JTable tabelaPedido;
    private DefaultTableModel modelPedido;

    private JTextField tfQuantidade;
    private JButton btnAdicionarItem, btnFinalizar;
    private JLabel lblAviso, lblCliente, lblPesquisa, lblQtd;

    private ProdutoDAO produtoDAO = new ProdutoDAO();
    private ClienteDAO clienteDAO = new ClienteDAO();
    private PedidoDAO pedidoDAO = new PedidoDAO();

    private Pedido pedidoAtual = new Pedido();

    private PainelPrincipal painelPrincipal;
    private PainelHistorico painelHistorico;

    // Para mensagens temporárias
    private String chaveAvisoAtual = "mensagemVazia";

    // ---------------- CONSTRUTOR ----------------
    public PainelVenda(PainelPrincipal pp, PainelHistorico ph) {
        this.painelPrincipal = pp;
        this.painelHistorico = ph;
        initComponents();                               // Inicializa a interface
    }

    // ---------------- INICIALIZAÇÃO ----------------
    private void initComponents() {
        setLayout(new BorderLayout(5,5));

        // ---------------- TOPO ----------------
        JPanel topo = new JPanel(new BorderLayout(5,5));
        
        // Painel para cliente
        JPanel painelCliente = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblCliente = new JLabel();
        painelCliente.add(lblCliente);

        cbClientes = new JComboBox<>();
        atualizarComboClientes();
        painelCliente.add(cbClientes);

        // Painel para pesquisa de produtos
        JPanel painelPesquisa = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblPesquisa = new JLabel();
        painelPesquisa.add(lblPesquisa);

        tfPesquisa = new JTextField(15);
        painelPesquisa.add(tfPesquisa);

        topo.add(painelCliente, BorderLayout.WEST);
        topo.add(painelPesquisa, BorderLayout.EAST);
        add(topo, BorderLayout.NORTH);

        // ---------------- CENTRO ----------------
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.6);                      // 60% para produtos, 40% para pedido

        // Tabela de produtos
        modelProdutos = new DefaultTableModel(0,5) {
            public boolean isCellEditable(int row,int col){ return false; }
        };
        tabelaProdutos = new JTable(modelProdutos);
        sorterProdutos = new TableRowSorter<>(modelProdutos);
        tabelaProdutos.setRowSorter(sorterProdutos);
        carregarTabelaProdutos();
        split.setTopComponent(new JScrollPane(tabelaProdutos));

        // Tabela do pedido atual
        modelPedido = new DefaultTableModel(0,4) {
            public boolean isCellEditable(int row,int col){ return false; }
        };
        tabelaPedido = new JTable(modelPedido);
        split.setBottomComponent(new JScrollPane(tabelaPedido));

        add(split, BorderLayout.CENTER);

        // ---------------- SUL ----------------
        JPanel sul = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblQtd = new JLabel();
        sul.add(lblQtd);

        tfQuantidade = new JTextField(3);
        sul.add(tfQuantidade);

        btnAdicionarItem = new JButton();
        sul.add(btnAdicionarItem);

        btnFinalizar = new JButton();
        sul.add(btnFinalizar);

        lblAviso = new JLabel("", JLabel.CENTER);
        lblAviso.setForeground(Color.RED);

        JPanel painelSul = new JPanel(new BorderLayout());
        painelSul.add(sul, BorderLayout.NORTH);
        painelSul.add(lblAviso, BorderLayout.SOUTH);
        add(painelSul, BorderLayout.SOUTH);

        // ---------------- AÇÕES ----------------
        btnAdicionarItem.addActionListener(e -> adicionarItem());
        btnFinalizar.addActionListener(e -> finalizarPedido());

        // Listener para pesquisa dinâmica
        tfPesquisa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filtrar() {
                String texto = tfPesquisa.getText().trim();
                if(texto.isEmpty()) sorterProdutos.setRowFilter(null);
                else sorterProdutos.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 1));
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e){ filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e){ filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e){ filtrar(); }
        });

        atualizarTextos(); // Inicializa os textos com i18n
    }

    // ---------------- MÉTODOS i18n ----------------
    public void atualizarTextos() {
        // Labels
        lblCliente.setText(I18n.t("cliente") + ":");
        lblPesquisa.setText(I18n.t("pesquisarProduto") + ":");
        lblQtd.setText(I18n.t("quantidade") + ":");

        // Botões
        btnAdicionarItem.setText(I18n.t("adicionarAoPedido"));
        btnFinalizar.setText(I18n.t("finalizarPedido"));

        // Cabeçalhos tabela produto
        String[] colunasProdutos = {
            I18n.t("id"), I18n.t("nome"), I18n.t("plataforma"), I18n.t("preco"), I18n.t("stock")
        };
        for(int i=0;i<colunasProdutos.length;i++)
            tabelaProdutos.getColumnModel().getColumn(i).setHeaderValue(colunasProdutos[i]);
        tabelaProdutos.getTableHeader().repaint();

        // Cabeçalhos tabela pedido
        String[] colunasPedido = {
            I18n.t("produto"), I18n.t("quantidade"), I18n.t("precoUnit"), I18n.t("subtotal")
        };
        for(int i=0;i<colunasPedido.length;i++)
            tabelaPedido.getColumnModel().getColumn(i).setHeaderValue(colunasPedido[i]);
        tabelaPedido.getTableHeader().repaint();

        // Atualiza ComboBox de clientes (opcional, se houver placeholders)
        atualizarComboClientes();

        // Reaplicar a última mensagem de aviso
        lblAviso.setText(I18n.t(chaveAvisoAtual));
    }

    private void atualizarComboClientes() {
        cbClientes.removeAllItems();
        for (Cliente c : clienteDAO.listarClientes()) cbClientes.addItem(c);
    }

    // ---------------- MÉTODOS DE DADOS ----------------
    private void carregarTabelaProdutos() {
        modelProdutos.setRowCount(0);
        for(Produto p : produtoDAO.listarProdutos()) {
            modelProdutos.addRow(new Object[]{
                p.getId(), p.getNome(), p.getPlataforma(), p.getPreco(), p.getStock()
            });
        }
    }

    public void atualizarProdutos() {
        carregarTabelaProdutos();
        sorterProdutos.setRowFilter(null);
    }

    private void atualizarTabelaPedido() {
        modelPedido.setRowCount(0);
        for(PedidoItem item : pedidoAtual.getItens()) {
            modelPedido.addRow(new Object[]{
                item.getProduto().getNome(),
                item.getQuantidade(),
                item.getPrecoUnit(),
                item.getQuantidade() * item.getPrecoUnit()
            });
        }
    }

    // ---------------- MÉTODOS DE AÇÃO ----------------
    private void adicionarItem() {
        int linha = tabelaProdutos.getSelectedRow();
        if(linha == -1) {
            mostrarAvisoTemporario("selecionarProduto", Color.RED, 7000);
            return;
        }

        int quantidade;
        try {
            quantidade = Integer.parseInt(tfQuantidade.getText().trim());
        } catch(Exception ex){
            mostrarAvisoTemporario("quantidadeInvalida", Color.RED, 7000);
            return;
        }

        if(quantidade <= 0){
            mostrarAvisoTemporario("quantidadeMaiorZero", Color.RED, 7000);
            return;
        }

        int modeloLinha = tabelaProdutos.convertRowIndexToModel(linha);
        int produtoId = (Integer) modelProdutos.getValueAt(modeloLinha, 0);
        Produto p = produtoDAO.getProdutoById(produtoId);

        if(p.getStock() < quantidade){
            mostrarAvisoTemporario("stockInsuficiente", Color.RED, 7000);
            return;
        }

        if (p.getPreco() <= 0) {
            mostrarAvisoTemporario("produtoSemPreco", Color.RED, 7000);
            return;
        }

        // Cria item do pedido e adiciona
        PedidoItem item = new PedidoItem();
        item.setProduto(p);
        item.setQuantidade(quantidade);
        item.setPrecoUnit(p.getPreco());
        pedidoAtual.adicionarItem(item);

        mostrarAvisoTemporario("itemAdicionadoSucesso", new Color(0,128,0), 7000);
        atualizarTabelaPedido();
    }

    private void finalizarPedido() {
        Cliente c = (Cliente) cbClientes.getSelectedItem();
        if(c == null){
            mostrarAvisoTemporario("nenhumClienteSelecionado", Color.RED, 7000);
            return;
        }
        if(pedidoAtual.getItens().isEmpty()){
            mostrarAvisoTemporario("nenhumItemNoPedido", Color.RED, 7000);
            return;
        }

        pedidoAtual.setCliente(c);

        try {
            pedidoDAO.criarPedido(pedidoAtual);                  // Persiste pedido

            pedidoAtual = new Pedido();                          // Novo pedido
            modelPedido.setRowCount(0);

            atualizarProdutos();
            tfPesquisa.setText("");

            if(painelPrincipal != null) painelPrincipal.atualizarStockEDashboard();
            if(painelHistorico != null) painelHistorico.carregarHistorico();

            mostrarAvisoTemporario("pedidoFinalizadoSucesso", new Color(0,128,0), 7000);

        } catch(Exception e) {
            lblAviso.setForeground(Color.RED);
            lblAviso.setText(I18n.t("erroFinalizarPedido") + e.getMessage());
            new javax.swing.Timer(7000, ev -> lblAviso.setText(I18n.t("mensagemVazia"))).start();
            e.printStackTrace();
        }
    }

    // ---------------- MÉTODO AUXILIAR PARA AVISOS TEMPORÁRIOS ----------------
    private void mostrarAvisoTemporario(String chave, Color cor, int tempoMs){
        chaveAvisoAtual = chave;
        lblAviso.setForeground(cor);
        lblAviso.setText(I18n.t(chaveAvisoAtual));
        javax.swing.Timer timer = new javax.swing.Timer(tempoMs, ev -> lblAviso.setText(I18n.t("mensagemVazia")));
        timer.setRepeats(false);
        timer.start();
    }
}