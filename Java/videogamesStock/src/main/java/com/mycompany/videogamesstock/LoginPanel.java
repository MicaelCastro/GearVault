package com.mycompany.videogamesstock;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public class LoginPanel extends JPanel {
    // Campos de interface gráfica
    private JTextField tfUsername;       // Campo de texto para username
    private JPasswordField pfPassword;   // Campo de texto para password
    private JButton btnLogin;            // Botão de login
    private JLabel lblMessage;           // Label para mensagens de erro/sucesso
    private JCheckBox cbMostrarSenha;    // Checkbox para mostrar/esconder a password

    // Referências ao layout e painel principal
    private CardLayout cardLayout;       // CardLayout usado no painel principal
    private JPanel mainPanel;            // Painel principal que contém todos os painéis
    
    private JToggleButton btnPt, btnEn;  // Botões de seleção de idioma (PT/UK)
    
    // Variável para guardar idioma atual
    private Locale idiomaAtual = new Locale("pt","PT");

    // Construtor recebe o CardLayout e painel principal
    public LoginPanel(CardLayout cardLayout, JPanel mainPanel) {
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;
        initComponents();                // Inicializa a interface gráfica
    }

    // Inicializa a interface gráfica
    private void initComponents() {
        this.removeAll();
        this.setLayout(new BorderLayout());
       
    // --- Painel topo com bandeiras centradas ---
   add(criarPainelIdiomas(), BorderLayout.NORTH);

    // --- Painel centro: logo + formulário ---
    JPanel centralPanel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(10,10,10,10);
    c.anchor = GridBagConstraints.CENTER;

    // Logo
    ImageIcon icon = new ImageIcon(getClass().getResource("/images/Logo.png"));
    JLabel lblLogo = new JLabel(icon);
    lblLogo.setHorizontalAlignment(JLabel.CENTER);
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.5;
    c.weighty = 0.5;
    c.fill = GridBagConstraints.NONE;
    centralPanel.add(lblLogo, c);

    // Formulário de Login
    JPanel formPanel = new JPanel(new GridBagLayout());
    GridBagConstraints g = new GridBagConstraints();
    g.insets = new Insets(5,5,5,5);
    g.fill = GridBagConstraints.HORIZONTAL;
    g.anchor = GridBagConstraints.CENTER;

    // Username
    g.gridx = 0; g.gridy = 0;
    formPanel.add(new JLabel(I18n.t("username")), g);
    tfUsername = new JTextField(15);
    g.gridx = 1;
    formPanel.add(tfUsername, g);

    // Password
    g.gridx = 0; g.gridy = 1;
    formPanel.add(new JLabel(I18n.t("password")), g);
    pfPassword = new JPasswordField(15);
    g.gridx = 1;
    formPanel.add(pfPassword, g);

    // Mostrar senha
    cbMostrarSenha = new JCheckBox(I18n.t("showPassword"));
    g.gridx = 1; g.gridy = 2;
    formPanel.add(cbMostrarSenha, g);
    cbMostrarSenha.addActionListener(e -> pfPassword.setEchoChar(cbMostrarSenha.isSelected() ? (char)0 : '\u2022'));

    // Botão login
    btnLogin = new JButton(I18n.t("login"));
    g.gridx = 0; g.gridy = 3; g.gridwidth = 2;
    formPanel.add(btnLogin, g);

    // Mensagem de feedback
    lblMessage = new JLabel("", JLabel.CENTER);
    lblMessage.setForeground(Color.RED);
    g.gridy = 4;
    formPanel.add(lblMessage, g);

    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 0.5;
    centralPanel.add(formPanel, c);

    this.add(centralPanel, BorderLayout.CENTER);

    // Ações do botão de login
    btnLogin.addActionListener(e -> autenticar());     
    

    revalidate();
    repaint();
}
    
    // --- Painel de idiomas com bandeiras ---
    private JPanel criarPainelIdiomas() {
        JPanel langPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        btnPt = criarToggleButton("/images/pt.png");
        btnEn = criarToggleButton("/images/uk.png");

        // Agrupar para permitir apenas uma seleção
        ButtonGroup group = new ButtonGroup();
        group.add(btnPt);
        group.add(btnEn);

        // Define o botão selecionado conforme o idioma atual
        if(idiomaAtual.getLanguage().equals("pt")) {
            btnPt.setSelected(true);
            atualizarContornoBandeiras(btnPt);
        } else {
            btnEn.setSelected(true);
            atualizarContornoBandeiras(btnEn);
        }

        // Ações de troca de idioma
        btnPt.addActionListener(e -> {
            idiomaAtual = new Locale("pt","PT");
            atualizarContornoBandeiras(btnPt);
            mudarIdioma(idiomaAtual);
        });

        btnEn.addActionListener(e -> {
            idiomaAtual = new Locale("en","UK");
            atualizarContornoBandeiras(btnEn);
            mudarIdioma(idiomaAtual);
        });

        langPanel.add(btnPt);
        langPanel.add(btnEn);

        return langPanel;
    }

    // --- Cria um JToggleButton com ícone fixo e tamanho definido ---
    private JToggleButton criarToggleButton(String iconPath) {
        JToggleButton btn = new JToggleButton(new ImageIcon(getClass().getResource(iconPath)));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(50, 30));
        return btn;
    }

    // --- Atualiza o contorno verde dos botões ---
    private void atualizarContornoBandeiras(JToggleButton selecionado) {
        JToggleButton[] botoes = {btnPt, btnEn};
        for (JToggleButton b : botoes) {
            if (b == selecionado) {
                b.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
            } else {
                b.setBorder(UIManager.getBorder("Button.border"));
            }
        }
    }

    // Método para fazer autenticação do utilizador
    private void autenticar() {
        String username = tfUsername.getText().trim();          // Lê username
        String password = new String(pfPassword.getPassword()); // Lê password

        if(username.isEmpty() || password.isEmpty()){
            lblMessage.setText(I18n.t("fillFields"));
            return;
        }

        try(Connection conn = ConexaoDB.getConnection()){       // Liga à DB
            String sql = "SELECT password_hash, salt FROM users WHERE username = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if(rs.next()){  // Se utilizador existe
                String storedHash = rs.getString("password_hash");
                String salt = rs.getString("salt");
                String hashInput = PasswordUtils.hashPassword(password, salt);

                if(hashInput.equals(storedHash)){
                    // Login bem-sucedido
                    lblMessage.setForeground(new Color(0,128,0));
                    lblMessage.setText(I18n.t("loginSuccess"));

                    // Forçar o PainelPrincipal a mostrar o Dashboard inicial
                    for(Component c : mainPanel.getComponents()){
                        if(c instanceof PainelPrincipal painel){
                            painel.mostrarDashboard();
                        }
                    }

                    // Mostra o painel principal
                    cardLayout.show(mainPanel,"painelPrincipal");
                } else {
                    lblMessage.setForeground(Color.RED);
                    lblMessage.setText(I18n.t("wrongPassword"));
                }
            } else{
                lblMessage.setForeground(Color.RED);
                lblMessage.setText(I18n.t("userNotFound"));
            }

        } catch(SQLException ex){
            lblMessage.setForeground(Color.RED);
            lblMessage.setText(I18n.t("dbError"));
            ex.printStackTrace();
        }
    }

    // --- Método para fazer reset ao painel de login ---
    public void reset() {
        lblMessage.setText("");             // Limpa mensagem
        lblMessage.setForeground(Color.RED); 
        tfUsername.setText("");             // Limpa username
        pfPassword.setText("");             // Limpa password
        cbMostrarSenha.setSelected(false);  // Desmarca checkbox
        pfPassword.setEchoChar('\u2022');   // Garante que a password fica escondida
    }

    // --- Recarrega o texto de acordo com o idioma ---
    public void reloadTexts() {
        removeAll();
        initComponents();
        revalidate();
        repaint();
    }

    // --- Muda o idioma de todos os painéis ---
    private void mudarIdioma(Locale locale) {
        I18n.setLocale(locale);
        
        for (Component c : mainPanel.getComponents()) {
            if (c instanceof LoginPanel) ((LoginPanel) c).reloadTexts();
            if (c instanceof PainelPrincipal) ((PainelPrincipal) c).reloadTexts();
        }
    }
}
    