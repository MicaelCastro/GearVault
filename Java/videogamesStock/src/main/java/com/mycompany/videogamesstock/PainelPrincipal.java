package com.mycompany.videogamesstock;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.*;
import java.util.List;

public class PainelPrincipal extends JPanel {

    private CardLayout cardLayout;
    private JPanel contentPanel;

    // ---------------- DASHBOARD ----------------
    private JLabel lblTotalProdutos;
    private JTable tabelaUltimos5;

    // ---------------- STOCK ----------------
    private JTable tabelaStock;
    private JTextField tfPesquisa;
    private JLabel lblContagem;
    private JLabel lblAviso;
    private JLabel lblPesquisar;
    private TableRowSorter<DefaultTableModel> sorterStock;

    // ---------------- CRUD INLINE ----------------
    private JTextField tfNome;
    private JComboBox<String> cbPlataformaCRUD;
    private JTextField tfPrecoCRUD;
    private JTextField tfStockCRUD;
    private JLabel lblAvisoCRUD;
    private JLabel lblNomeCRUD;
    private JLabel lblPlataformaCRUD;
    private JLabel lblPrecoCRUD;
    private JLabel lblStockCRUD;
    private JPanel painelCRUD;
    private JButton btnSalvarInline;
    private JButton btnCancelarInline;

    // ---------------- BOTÕES CRUD ----------------
    private JButton btnAdicionar, btnEditar, btnApagar;

    private ProdutoDAO produtoDAO = new ProdutoDAO();

    // ---------------- PAINEL VENDAS ----------------
    private PainelVenda painelVenda;

    // ---------------- PAINEL HISTÓRICO ----------------
    private PainelHistorico painelHistorico;

    // ---------------- PAINEL RELATÓRIOS ----------------
    private PainelRelatorio painelRelatorio;

    // ---------------- BARRA LATERAL ----------------
    private JButton btnDashboard, btnStock, btnVendas, btnHistorico, btnRelatorios, btnLogout;
    private Color corAtiva = new Color(0, 128, 0); // verde
    private Color corNormal = UIManager.getColor("Button.background");

    // ---------------- MENSAGENS DE AVISO ----------------
    private String ultimaMensagemAviso = " ";
    private Color corUltimaMensagem = Color.RED;

    private String ultimaMensagemCRUD = " ";
    private Color corUltimaMensagemCRUD = Color.RED;

    private JFrame framePrincipal;
    
    // ---------------- PAGINAÇÃO ----------------
    private List<Produto> todosProdutos = new ArrayList<>();
    private List<Produto> produtosFiltrados = new ArrayList<>();
    private int paginaAtual = 0;
    private final int POR_PAGINA = 50;
    private JButton btnAnteriorPagina, btnProximaPagina;

    public PainelPrincipal(JFrame framePrincipal) {
        this.framePrincipal = framePrincipal;
        initComponents();
        carregarDashboard();
        carregarTabelaStock();
        carregarHistorico();
        setBotaoAtivo(btnDashboard);
    }

    private void initComponents() {
        this.setLayout(new BorderLayout());

        // ---------------- BARRA LATERAL ----------------
        JPanel sidePanel = new JPanel(new GridLayout(0,1,5,5));
        sidePanel.setPreferredSize(new Dimension(150,0));

        // cria botões laterais com textos traduzidos
        btnDashboard = new JButton(I18n.t("dashboard"));
        btnStock = new JButton(I18n.t("stock"));
        btnVendas = new JButton(I18n.t("vendas"));
        btnHistorico = new JButton(I18n.t("historico"));
        btnRelatorios = new JButton(I18n.t("relatorios"));
        btnLogout = new JButton(I18n.t("logout"));

        // adiciona os botões à barra lateral
        sidePanel.add(btnDashboard);
        sidePanel.add(btnStock);
        sidePanel.add(btnVendas);
        sidePanel.add(btnHistorico);
        sidePanel.add(btnRelatorios);
        sidePanel.add(btnLogout);
        this.add(sidePanel, BorderLayout.WEST);

        // ---------------- PAINEL CENTRAL ----------------
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // ---------------- DASHBOARD ----------------
        JPanel painelDashboard = new JPanel(new BorderLayout(10,10));
        lblTotalProdutos = new JLabel(I18n.t("totalProdutos")+": 0", JLabel.CENTER);
        lblTotalProdutos.setFont(new Font("Arial", Font.BOLD, 20));
        painelDashboard.add(lblTotalProdutos, BorderLayout.NORTH);

        // tabela dos últimos 5 produtos adicionados
        String[] colunasUltimos = {I18n.t("id"), I18n.t("nome"), I18n.t("plataforma"), I18n.t("preco")};
        tabelaUltimos5 = new JTable(new DefaultTableModel(colunasUltimos,0));
        painelDashboard.add(new JScrollPane(tabelaUltimos5), BorderLayout.CENTER);

        // ---------------- STOCK ----------------
        JPanel painelStock = criarPainelStock();

        // ---------------- HISTÓRICO ----------------
        painelHistorico = new PainelHistorico();
        painelHistorico.carregarHistorico();

        // ---------------- VENDAS ----------------
        painelVenda = new PainelVenda(this, painelHistorico);

        // ---------------- RELATÓRIOS ----------------
        painelRelatorio = new PainelRelatorio();

        // ---------------- ADICIONAR PAINÉIS AO CARDLAYOUT ----------------
        contentPanel.add(painelDashboard,"dashboard");
        contentPanel.add(painelStock,"stock");
        contentPanel.add(painelVenda,"vendas");
        contentPanel.add(painelHistorico,"historico");
        contentPanel.add(painelRelatorio,"relatorios");

        this.add(contentPanel, BorderLayout.CENTER);

        // ---------------- CONFIGURA AÇÕES DOS BOTÕES LATERAIS ----------------
        configurarBotoesLaterais();

        // ---------------- CONFIGURA PESQUISA STOCK ----------------
        configurarPesquisaStock();

        // ---------------- CONFIGURA LÓGICA CRUD STOCK ----------------
        configurarCRUD(btnAdicionar, btnEditar, btnApagar);

        // mostra o dashboard no arranque
        cardLayout.show(contentPanel,"dashboard");
    }

    private JPanel criarPainelStock() {
        JPanel painelStock = new JPanel(new BorderLayout(5,5));

        // ---------------- TOPO: CRUD + Pesquisa ----------------
        JPanel topoPainel = new JPanel(new BorderLayout());

        // Botões CRUD
        JPanel crudPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAdicionar = new JButton(I18n.t("adicionar"));
        btnAdicionar.setBackground(Color.GREEN);
        btnAdicionar.setForeground(Color.BLACK);

        btnEditar = new JButton(I18n.t("editar"));
        btnEditar.setBackground(Color.YELLOW);
        btnEditar.setForeground(Color.BLACK);

        btnApagar = new JButton(I18n.t("apagar"));
        btnApagar.setBackground(Color.RED);
        btnApagar.setForeground(Color.WHITE);

        crudPanel.add(btnAdicionar);
        crudPanel.add(btnEditar);
        crudPanel.add(btnApagar);
        topoPainel.add(crudPanel, BorderLayout.NORTH);

        // campo de pesquisa + contagem de registos
        JPanel pesquisaPanel = new JPanel(new BorderLayout(5,5));
        lblPesquisar = new JLabel(I18n.t("pesquisar")+": ");
        tfPesquisa = new JTextField();
        lblContagem = new JLabel("0 " + I18n.t("registos"));
        pesquisaPanel.add(lblPesquisar, BorderLayout.WEST);
        pesquisaPanel.add(tfPesquisa, BorderLayout.CENTER);
        pesquisaPanel.add(lblContagem, BorderLayout.EAST);
        topoPainel.add(pesquisaPanel, BorderLayout.SOUTH);

        painelStock.add(topoPainel, BorderLayout.NORTH);

        // ---------------- CENTRO: CRUD inline + Tabela ----------------
        painelCRUD = new JPanel(new FlowLayout());
        tfNome = new JTextField(15);
        cbPlataformaCRUD = new JComboBox<>();
        tfPrecoCRUD = new JTextField(8);
        tfStockCRUD = new JTextField(5);
        lblAvisoCRUD = new JLabel(" ", JLabel.CENTER);
        lblAvisoCRUD.setForeground(Color.RED);
        btnSalvarInline = new JButton(I18n.t("salvar"));
        btnCancelarInline = new JButton(I18n.t("cancelar"));

        // labels do CRUD inline
        lblNomeCRUD = new JLabel(I18n.t("nome")+":");
        lblPlataformaCRUD = new JLabel(I18n.t("plataforma")+":");
        lblPrecoCRUD = new JLabel(I18n.t("preco")+":");
        lblStockCRUD = new JLabel(I18n.t("stock")+":");

        // adiciona campos e botões ao painel CRUD
        painelCRUD.add(lblNomeCRUD); painelCRUD.add(tfNome);
        painelCRUD.add(lblPlataformaCRUD); painelCRUD.add(cbPlataformaCRUD);
        painelCRUD.add(lblPrecoCRUD); painelCRUD.add(tfPrecoCRUD);
        painelCRUD.add(lblStockCRUD); painelCRUD.add(tfStockCRUD);
        painelCRUD.add(btnSalvarInline); painelCRUD.add(btnCancelarInline);
        painelCRUD.add(lblAvisoCRUD);
        painelCRUD.setVisible(false);
        
        // tabela principal de stock
        String[] colunasStock = {I18n.t("id"), I18n.t("nome"), I18n.t("plataforma"), I18n.t("preco"), I18n.t("stock")};
        tabelaStock = new JTable(new DefaultTableModel(colunasStock,0));
        sorterStock = new TableRowSorter<>((DefaultTableModel)tabelaStock.getModel());
        tabelaStock.setRowSorter(sorterStock);
        JScrollPane scrollStock = new JScrollPane(tabelaStock);

        // junta CRUD + tabela no centro
        JPanel centroPainel = new JPanel();
        centroPainel.setLayout(new BoxLayout(centroPainel, BoxLayout.Y_AXIS));
        centroPainel.add(painelCRUD);
        centroPainel.add(scrollStock);

        painelStock.add(centroPainel, BorderLayout.CENTER);

        // ---------------- SUL: Avisos + Paginação ----------------
        lblAviso = new JLabel(" ", JLabel.CENTER);
        lblAviso.setForeground(Color.RED);

        // botões de navegação de páginas
        JPanel pnlPaginas = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnAnteriorPagina = new JButton(I18n.t("anterior"));
        btnProximaPagina = new JButton(I18n.t("proxima"));
        pnlPaginas.add(btnAnteriorPagina);
        pnlPaginas.add(btnProximaPagina);

        // painel que junta aviso + paginação
        JPanel sulPainel = new JPanel();
        sulPainel.setLayout(new BoxLayout(sulPainel, BoxLayout.Y_AXIS));
        sulPainel.add(lblAviso);
        sulPainel.add(pnlPaginas);

        painelStock.add(sulPainel, BorderLayout.SOUTH);
        
        // ações dos botões de paginação
        btnAnteriorPagina.addActionListener(e -> {
            if(paginaAtual > 0) {
                paginaAtual--;
                atualizarTabelaStock();
            }
        });
        btnProximaPagina.addActionListener(e -> {
            if((paginaAtual + 1) * POR_PAGINA < produtosFiltrados.size()) {
                paginaAtual++;
                atualizarTabelaStock();
            }
        });

        return painelStock;
    }

    private void configurarBotoesLaterais() {
        // alterna entre os painéis principais
        btnDashboard.addActionListener(e -> { cardLayout.show(contentPanel,"dashboard"); setBotaoAtivo(btnDashboard); });
        btnStock.addActionListener(e -> { cardLayout.show(contentPanel,"stock"); setBotaoAtivo(btnStock); });
        btnVendas.addActionListener(e -> { cardLayout.show(contentPanel,"vendas"); setBotaoAtivo(btnVendas); });
        btnHistorico.addActionListener(e -> { painelHistorico.carregarHistorico(); cardLayout.show(contentPanel,"historico"); setBotaoAtivo(btnHistorico); });
        btnRelatorios.addActionListener(e -> { cardLayout.show(contentPanel,"relatorios"); setBotaoAtivo(btnRelatorios); });
        
        // botão de logout volta ao ecrã de login
        btnLogout.addActionListener(e -> {
            Container parent = this.getParent();
            if(parent instanceof JPanel panelParent && panelParent.getLayout() instanceof CardLayout cl){
                cl.show(panelParent,"login");
                for(Component c: panelParent.getComponents()){
                    if(c instanceof LoginPanel login) login.reset();
                }
            }
            setBotaoAtivo(btnLogout);
        });
    }

    private void configurarPesquisaStock() {
        // filtro dinâmico de pesquisa no stock
        tfPesquisa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                String texto = tfPesquisa.getText().toLowerCase().trim();
                produtosFiltrados = todosProdutos.stream()
                        .filter(p -> p.getNome().toLowerCase().contains(texto) ||
                                     p.getPlataforma().toLowerCase().contains(texto))
                        .toList();
                paginaAtual = 0;
                atualizarTabelaStock();
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e){update();}
            public void removeUpdate(javax.swing.event.DocumentEvent e){update();}
            public void changedUpdate(javax.swing.event.DocumentEvent e){update();}
        });
    }

    private void atualizarTabelaStock(){
        // atualiza a tabela com base na página atual
        DefaultTableModel model = (DefaultTableModel) tabelaStock.getModel();
        model.setRowCount(0);

        int inicio = paginaAtual * POR_PAGINA;
        int fim = Math.min(inicio + POR_PAGINA, produtosFiltrados.size());

        for(int i = inicio; i < fim; i++){
            Produto p = produtosFiltrados.get(i);
            model.addRow(new Object[]{p.getId(), p.getNome(), p.getPlataforma(), p.getPreco(), p.getStock()});
        }

        lblContagem.setText(fim + "/" + produtosFiltrados.size() + " " + I18n.t("registos"));
    }

    // ---------------- MÉTODO PARA DEFINIR QUAL BOTÃO ESTÁ ACTIVO ----------------
    private void setBotaoAtivo(JButton botaoAtivo){
        JButton[] botoes = {btnDashboard, btnStock, btnVendas, btnHistorico, btnRelatorios, btnLogout};
        for(JButton b : botoes){
            b.setBackground(corNormal);
            b.setForeground(Color.BLACK);
        }
        botaoAtivo.setBackground(corAtiva);
        botaoAtivo.setForeground(Color.WHITE);
    }

    // ---------------- RELOAD TEXTOS ----------------
    public void reloadTexts() {
        // Atualiza textos dos botões laterais
        btnDashboard.setText(I18n.t("dashboard"));
        btnStock.setText(I18n.t("stock"));
        btnVendas.setText(I18n.t("vendas"));
        btnHistorico.setText(I18n.t("historico"));
        btnRelatorios.setText(I18n.t("relatorios"));
        btnLogout.setText(I18n.t("logout"));

        // Actualiza labels gerais
        lblTotalProdutos.setText(I18n.t("totalProdutos")+": " + lblTotalProdutos.getText().split(": ")[1]);
        lblContagem.setText(tabelaStock.getRowCount() + " " + I18n.t("registos"));
        lblPesquisar.setText(I18n.t("pesquisar")+": ");

        // Actualiza cabeçalhos das tabelas
        ((DefaultTableModel)tabelaUltimos5.getModel()).setColumnIdentifiers(new String[]{
            I18n.t("id"), I18n.t("nome"), I18n.t("plataforma"), I18n.t("preco")
        });
        ((DefaultTableModel)tabelaStock.getModel()).setColumnIdentifiers(new String[]{
            I18n.t("id"), I18n.t("nome"), I18n.t("plataforma"), I18n.t("preco"), I18n.t("stock")
        });

        // Actualiza textos dos botões CRUD
        btnAdicionar.setText(I18n.t("adicionar"));
        btnEditar.setText(I18n.t("editar"));
        btnApagar.setText(I18n.t("apagar"));

        // Actualiza labels do CRUD inline
        lblNomeCRUD.setText(I18n.t("nome")+":");
        lblPlataformaCRUD.setText(I18n.t("plataforma")+":");
        lblPrecoCRUD.setText(I18n.t("preco")+":");
        lblStockCRUD.setText(I18n.t("stock")+":");

        // Actualiza botões de salvar/cancelar do CRUD inline
        btnSalvarInline.setText(I18n.t("salvar"));
        btnCancelarInline.setText(I18n.t("cancelar"));
        
        // Actualiza painel de vendas
        painelVenda.atualizarTextos();

        // Actualiza painel de histórico
        if(painelHistorico != null) painelHistorico.reloadTexts();

        // Actualiza painel de relatórios
        if(painelRelatorio != null) painelRelatorio.reloadTexts();

        // Reaplicar última mensagem de aviso
        lblAviso.setForeground(corUltimaMensagem);
        lblAviso.setText(ultimaMensagemAviso);

        lblAvisoCRUD.setForeground(corUltimaMensagemCRUD);
        lblAvisoCRUD.setText(ultimaMensagemCRUD);

        revalidate();
        repaint();
    }

    // ---------------- CONFIGURAR CRUD ----------------
    private void configurarCRUD(JButton btnAdicionar, JButton btnEditar, JButton btnApagar) {
        
        // Ação do botão Adicionar
        btnAdicionar.addActionListener(e -> {
            lblAvisoCRUD.setText(" ");
            tfNome.setText(""); tfPrecoCRUD.setText(""); tfStockCRUD.setText("");
            cbPlataformaCRUD.removeAllItems();
            for(String p : produtoDAO.getTodasPlataformas()) cbPlataformaCRUD.addItem(p);
            painelCRUD.setVisible(true);
            tfNome.requestFocus();

            // limpa listeners antigos para evitar duplicações
            for (ActionListener al : btnSalvarInline.getActionListeners()) btnSalvarInline.removeActionListener(al);
            for (ActionListener al : btnCancelarInline.getActionListeners()) btnCancelarInline.removeActionListener(al);

            btnSalvarInline.addActionListener(ev -> salvarProdutoInline(null));
            btnCancelarInline.addActionListener(ev -> painelCRUD.setVisible(false));
        });

        // Ação do botão Editar
        btnEditar.addActionListener(e -> {
            int linha = tabelaStock.getSelectedRow();
            if(linha == -1){
                mostrarAvisoTemporario(lblAviso, I18n.t("selecionarProdutoEditar"), Color.RED, 7000);
                return;
            }
            int modeloLinha = tabelaStock.convertRowIndexToModel(linha);
            DefaultTableModel model = (DefaultTableModel)tabelaStock.getModel();
            int id = (int) model.getValueAt(modeloLinha,0);
            Produto p = produtoDAO.getProdutoById(id);
            if(p == null) return;

            // preenche os campos com os dados do produto selecionado
            tfNome.setText(model.getValueAt(modeloLinha,1).toString());
            tfPrecoCRUD.setText(model.getValueAt(modeloLinha,3).toString());
            tfStockCRUD.setText(model.getValueAt(modeloLinha,4).toString());
            cbPlataformaCRUD.removeAllItems();
            for(String pl : produtoDAO.getTodasPlataformas()) cbPlataformaCRUD.addItem(pl);
            cbPlataformaCRUD.setSelectedItem(p.getPlataforma());
            painelCRUD.setVisible(true);

            // limpa listeners antigos
            for (ActionListener al : btnSalvarInline.getActionListeners()) btnSalvarInline.removeActionListener(al);
            for (ActionListener al : btnCancelarInline.getActionListeners()) btnCancelarInline.removeActionListener(al);

            btnSalvarInline.addActionListener(ev -> salvarProdutoInline(p));
            btnCancelarInline.addActionListener(ev -> painelCRUD.setVisible(false));
        });

        // Ação do botão Apagar
        btnApagar.addActionListener(e -> {
            int linha = tabelaStock.getSelectedRow();
            if(linha == -1){
                mostrarAvisoTemporario(lblAviso, I18n.t("selecionarProdutoApagar"), Color.RED, 7000);
                return;
            }
            int modeloLinha = tabelaStock.convertRowIndexToModel(linha);
            DefaultTableModel model = (DefaultTableModel)tabelaStock.getModel();
            int id = (int) model.getValueAt(modeloLinha,0);
            Produto p = produtoDAO.getProdutoById(id);

            // confirmação antes de apagar
            if(p != null){
                int resposta = JOptionPane.showConfirmDialog(
                        framePrincipal,
                        I18n.t("confirmarApagarProduto"),
                        I18n.t("confirmarApagar"),
                        JOptionPane.YES_NO_OPTION
                );
                if(resposta == JOptionPane.YES_OPTION){
                    produtoDAO.apagarProduto(p);
                    carregarTabelaStock();
                    carregarDashboard();
                    mostrarAvisoTemporario(lblAviso, I18n.t("produtoApagado"), new Color(0,128,0), 7000);
                }
            }
        });
    }

    // ---------------- SALVAR INLINE ----------------
    private void salvarProdutoInline(Produto p) {
        // lê e valida os campos do CRUD inline
        String nome = tfNome.getText().trim();
        String plat = (String) cbPlataformaCRUD.getSelectedItem();
        double preco; int stock;

        try {
            preco = Double.parseDouble(tfPrecoCRUD.getText().trim());
            stock = Integer.parseInt(tfStockCRUD.getText().trim());
        } catch(NumberFormatException ex) {
            lblAvisoCRUD.setText(I18n.t("precoStockInvalidos"));
            return;
        }

        // cria ou atualiza o produto conforme o caso
        if(p == null){
            Produto novo = new Produto();
            novo.setNome(nome);
            novo.setPlataforma(plat);
            novo.setPreco(preco);
            novo.setStock(stock);
            produtoDAO.criarProduto(novo);
            mostrarAvisoTemporario(lblAviso, I18n.t("produtoAdicionado"), new Color(0,128,0), 7000);
        } else {
            p.setNome(nome);
            p.setPlataforma(plat);
            p.setPreco(preco);
            p.setStock(stock);
            produtoDAO.atualizarProduto(p);
            mostrarAvisoTemporario(lblAviso, I18n.t("produtoEditado"), new Color(0,128,0), 7000);
        }

        carregarTabelaStock();
        carregarDashboard();
        painelCRUD.setVisible(false);

        // atualiza a lista de produtos do painel de vendas
        if(painelVenda != null){
            painelVenda.atualizarProdutos();
        }
    }

    // ---------------- MÉTODO AUXILIAR PARA AVISOS TEMPORÁRIOS ----------------
    private void mostrarAvisoTemporario(JLabel label, String mensagem, Color cor, int tempoMs){
        label.setForeground(cor);
        label.setText(mensagem);

        // guarda a última mensagem (para reloadTexts)
        if(label == lblAviso){
            ultimaMensagemAviso = mensagem;
            corUltimaMensagem = cor;
        } else if(label == lblAvisoCRUD){
            ultimaMensagemCRUD = mensagem;
            corUltimaMensagemCRUD = cor;
        }

        // limpa a mensagem após o tempo definido
        javax.swing.Timer timer = new javax.swing.Timer(tempoMs, ev -> label.setText(" "));
        timer.setRepeats(false);
        timer.start();
    }

    // ---------- MÉTODOS AUXILIARES ----------

    public void atualizarStockEDashboard(){
        carregarTabelaStock();
        carregarDashboard();
    }

    // atualiza o painel principal com estatísticas
    private void carregarDashboard(){
        try(Connection conn = ConexaoDB.getConnection();
            Statement stmt = conn.createStatement()){

            // total de produtos
            ResultSet rsTotal = stmt.executeQuery("SELECT COUNT(*) AS total FROM produto");
            if(rsTotal.next()) lblTotalProdutos.setText(I18n.t("totalProdutos")+": "+rsTotal.getInt("total"));

            // últimos 5 produtos adicionados
            ResultSet rsUltimos = stmt.executeQuery(
                "SELECT id, nome, plataforma, preco FROM produto ORDER BY data_adicionado DESC LIMIT 5"
            );

            DefaultTableModel model = (DefaultTableModel)tabelaUltimos5.getModel();
            model.setRowCount(0);
            while(rsUltimos.next()){
                Vector<Object> row = new Vector<>();
                row.add(rsUltimos.getInt("id"));
                row.add(rsUltimos.getString("nome"));
                row.add(rsUltimos.getString("plataforma"));
                row.add(rsUltimos.getDouble("preco"));
                model.addRow(row);
            }

        } catch(SQLException e){
            lblTotalProdutos.setText(I18n.t("erroCarregarDashboard"));
            e.printStackTrace();
        }
    }

    // carrega os produtos da DB para a tabela de stock
    private void carregarTabelaStock(){
        todosProdutos.clear();
        try(Connection conn = ConexaoDB.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT id, nome, plataforma, preco, stock FROM produto"
            )){

            while(rs.next()){
                Produto p = new Produto();
                p.setId(rs.getInt("id"));
                p.setNome(rs.getString("nome"));
                p.setPlataforma(rs.getString("plataforma"));
                p.setPreco(rs.getDouble("preco"));
                p.setStock(rs.getInt("stock"));
                todosProdutos.add(p);
            }

            produtosFiltrados = new ArrayList<>(todosProdutos); // inicialmente sem filtro
            paginaAtual = 0;
            atualizarTabelaStock(); // carrega a primeira página

        } catch(SQLException e){
            lblAviso.setText(I18n.t("erroCarregarStock"));
            e.printStackTrace();
        }
    }

    // recarrega histórico de vendas
    public void carregarHistorico(){
        painelHistorico.carregarHistorico();
    }

    // mostra o dashboard no ecrã principal
    public void mostrarDashboard() {
        cardLayout.show(contentPanel, "dashboard");
    }
}