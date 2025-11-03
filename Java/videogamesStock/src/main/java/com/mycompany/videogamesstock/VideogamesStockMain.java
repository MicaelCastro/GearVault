package com.mycompany.videogamesstock;

import javax.swing.*;
import java.awt.*;

/**
 * Classe principal da aplicação.
 */

public class VideogamesStockMain {

    public static void main(String[] args) {
        // Garante que a interface gráfica será criada na EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            // Cria o JFrame principal
            JFrame frame = new JFrame("Gear Vault");
            
            // Adiciona favicon
            ImageIcon favicon = new ImageIcon(VideogamesStockMain.class.getResource("/images/favicon.png"));
            frame.setIconImage(favicon.getImage());

            // Define comportamento ao fechar (usamos DO_NOTHING para controlar com confirmação)
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.setSize(1000, 650);                                // Define tamanho inicial
            frame.setLocationRelativeTo(null);                       // Centraliza na tela

            // Layout em cartões para alternar entre login e painel principal
            CardLayout cardLayout = new CardLayout();
            JPanel mainPanel = new JPanel(cardLayout);

            // Cria os painéis
            LoginPanel loginPanel = new LoginPanel(cardLayout, mainPanel);
            PainelPrincipal painelPrincipal = new PainelPrincipal(frame);

            // Adiciona os painéis ao mainPanel com "nomes" únicos
            mainPanel.add(loginPanel, "login");
            mainPanel.add(painelPrincipal, "painelPrincipal");

            // Mostra primeiro o login
            cardLayout.show(mainPanel, "login");

            // Adiciona o painel principal ao frame
            frame.add(mainPanel);

            // Confirmação ao sair da aplicação
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    int resposta = JOptionPane.showConfirmDialog(
                            frame,
                            I18n.t("confirmExitMessage"),   
                            I18n.t("confirmExitTitle"),   
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (resposta == JOptionPane.YES_OPTION) {
                        frame.dispose();                      // Fecha a aplicação
                    }
                }
            });

            // Torna o frame visível
            frame.setVisible(true);
        });
    }
}