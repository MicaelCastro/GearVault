package com.mycompany.videogamesstock;

import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;                                 // Para exportação CSV
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class PainelRelatorio extends JPanel {

    // Spinners para selecionar datas de início e fim
    private JSpinner spinnerInicio, spinnerFim;
    private JButton btnExportStock, btnExportVendas;       // Botões de exportação
    private JLabel lblAviso;                               // Mensagens de sucesso/erro
    private JLabel lblInicio, lblFim;                      // Labels para os spinners

    public PainelRelatorio() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Labels
        lblInicio = new JLabel(I18n.t("dataInicio") + ":");
        lblFim = new JLabel(I18n.t("dataFim") + ":");

        // Spinners de data (dia/mês/ano)
        spinnerInicio = new JSpinner(new SpinnerDateModel());
        spinnerFim = new JSpinner(new SpinnerDateModel());
        spinnerInicio.setEditor(new JSpinner.DateEditor(spinnerInicio, "dd/MM/yyyy"));
        spinnerFim.setEditor(new JSpinner.DateEditor(spinnerFim, "dd/MM/yyyy"));

        // Botões de exportação
        btnExportStock = new JButton(I18n.t("exportarStockCSV"));
        btnExportVendas = new JButton(I18n.t("exportarVendasCSV"));

        // Layout: adiciona labels e spinners
        gbc.gridx = 0; gbc.gridy = 0; add(lblInicio, gbc);
        gbc.gridx = 1; add(spinnerInicio, gbc);

        gbc.gridx = 0; gbc.gridy = 1; add(lblFim, gbc);
        gbc.gridx = 1; add(spinnerFim, gbc);

        gbc.gridx = 0; gbc.gridy = 2; add(btnExportStock, gbc);
        gbc.gridx = 1; add(btnExportVendas, gbc);

        // Label de aviso (inicialmente vazio)
        lblAviso = new JLabel(I18n.t("mensagemVazia"));
        lblAviso.setForeground(Color.RED);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        add(lblAviso, gbc);

        // Ações dos botões
        btnExportStock.addActionListener(e -> exportarStock());
        btnExportVendas.addActionListener(e -> exportarVendas());
    }

    // Atualiza textos quando muda o idioma
    public void reloadTexts() {
        lblInicio.setText(I18n.t("dataInicio") + ":");
        lblFim.setText(I18n.t("dataFim") + ":");
        btnExportStock.setText(I18n.t("exportarStockCSV"));
        btnExportVendas.setText(I18n.t("exportarVendasCSV"));
        lblAviso.setText(I18n.t("mensagemVazia"));
        revalidate();
        repaint();
    }

    // Valida se a data final não é antes da inicial
    private boolean validarDatas(Date inicio, Date fim) {
        if (fim.before(inicio)) {
            lblAviso.setText(I18n.t("dataFimAntesInicio"));
            return false;
        } else {
            lblAviso.setText(I18n.t("mensagemVazia"));
        }
        return true;
    }

    // Exporta os produtos do stock dentro do período selecionado
    private void exportarStock() {
        try {
            // Define início do dia para a data inicial
            Calendar calInicio = Calendar.getInstance();
            calInicio.setTime((Date) spinnerInicio.getValue());
            calInicio.set(Calendar.HOUR_OF_DAY, 0);
            calInicio.set(Calendar.MINUTE, 0);
            calInicio.set(Calendar.SECOND, 0);
            calInicio.set(Calendar.MILLISECOND, 0);

            // Define fim do dia para a data final
            Calendar calFim = Calendar.getInstance();
            calFim.setTime((Date) spinnerFim.getValue());
            calFim.set(Calendar.HOUR_OF_DAY, 23);
            calFim.set(Calendar.MINUTE, 59);
            calFim.set(Calendar.SECOND, 59);
            calFim.set(Calendar.MILLISECOND, 999);

            Date inicio = calInicio.getTime();
            Date fim = calFim.getTime();

            if (!validarDatas(inicio, fim)) return;

            // SQL para buscar produtos adicionados dentro do período
            String sql = "SELECT id, nome, plataforma, preco, stock, data_adicionado " +
                         "FROM produto WHERE data_adicionado BETWEEN ? AND ?";

            try (Connection conn = ConexaoDB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setTimestamp(1, new java.sql.Timestamp(inicio.getTime()));
                ps.setTimestamp(2, new java.sql.Timestamp(fim.getTime()));
                ResultSet rs = ps.executeQuery();

                // Cria nome de ficheiro baseado na data/hora atual
                SimpleDateFormat sdfFile = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
                String defaultFileName = "Relatorio_Stock_" + sdfFile.format(new Date()) + ".csv";

                // Diálogo para guardar ficheiro
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle(I18n.t("guardarRelatorioStock"));
                fc.setSelectedFile(new java.io.File(defaultFileName));
                if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

                // Cria CSV
                FileWriter fw = new FileWriter(fc.getSelectedFile());
                SimpleDateFormat sdfHeader = new SimpleDateFormat("dd/MM/yyyy");

                // Cabeçalho
                fw.write("\"" + I18n.t("relatorioStock") + "\"\n");
                fw.write("\"" + I18n.t("periodo") + ": " + sdfHeader.format(inicio) + " " + I18n.t("ate") + " " + sdfHeader.format(fim) + "\"\n");
                fw.write("\"ID\";\"" + I18n.t("nome") + "\";\"" + I18n.t("plataforma") + "\";\"" + I18n.t("preco") + "\";\"" + I18n.t("stock") + "\";\"" + I18n.t("data") + "\"\n");

                // Dados do stock
                while (rs.next()) {
                    fw.write("\"" + rs.getInt("id") + "\";"
                            + "\"" + rs.getString("nome") + "\";"
                            + "\"" + rs.getString("plataforma") + "\";"
                            + "\"" + rs.getDouble("preco") + "\";"
                            + "\"" + rs.getInt("stock") + "\";"
                            + "\"" + sdfHeader.format(rs.getTimestamp("data_adicionado")) + "\"\n");
                }

                fw.close();
                lblAviso.setForeground(new Color(0,128,0));
                lblAviso.setText(I18n.t("stockExportSuccess"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            lblAviso.setForeground(Color.RED);
            lblAviso.setText(I18n.t("erroExportStock"));
        }
    }

    // Exporta as vendas dentro do período selecionado
    private void exportarVendas() {
        try {
            // Define início e fim do dia
            Calendar calInicio = Calendar.getInstance();
            calInicio.setTime((Date) spinnerInicio.getValue());
            calInicio.set(Calendar.HOUR_OF_DAY, 0);
            calInicio.set(Calendar.MINUTE, 0);
            calInicio.set(Calendar.SECOND, 0);
            calInicio.set(Calendar.MILLISECOND, 0);

            Calendar calFim = Calendar.getInstance();
            calFim.setTime((Date) spinnerFim.getValue());
            calFim.set(Calendar.HOUR_OF_DAY, 23);
            calFim.set(Calendar.MINUTE, 59);
            calFim.set(Calendar.SECOND, 59);
            calFim.set(Calendar.MILLISECOND, 999);

            Date inicio = calInicio.getTime();
            Date fim = calFim.getTime();

            if (!validarDatas(inicio, fim)) return;

            // SQL para buscar vendas e itens
            String sql = "SELECT pi.pedido_id, c.nome AS cliente, p.nome AS produto, pi.quantidade, pi.preco_unit, pe.data " +
                         "FROM pedido_item pi " +
                         "JOIN produto p ON pi.produto_id = p.id " +
                         "JOIN pedido pe ON pi.pedido_id = pe.id " +
                         "JOIN cliente c ON pe.cliente_id = c.id " +
                         "WHERE pe.data BETWEEN ? AND ? " +
                         "ORDER BY pe.data";

            try (Connection conn = ConexaoDB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setTimestamp(1, new java.sql.Timestamp(inicio.getTime()));
                ps.setTimestamp(2, new java.sql.Timestamp(fim.getTime()));
                ResultSet rs = ps.executeQuery();

                // Nome do ficheiro baseado na data/hora
                SimpleDateFormat sdfFile = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
                String defaultFileName = "Relatorio_Vendas_" + sdfFile.format(new Date()) + ".csv";

                // Dialogo para guardar
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle(I18n.t("guardarRelatorioVendas"));
                fc.setSelectedFile(new java.io.File(defaultFileName));
                if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

                FileWriter fw = new FileWriter(fc.getSelectedFile());
                SimpleDateFormat sdfDataHora = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                SimpleDateFormat sdfData = new SimpleDateFormat("dd/MM/yyyy");

                // Cabeçalho
                fw.write("\"" + I18n.t("relatorioVendas") + "\"\n");
                fw.write("\"" + I18n.t("periodo") + ": " + sdfData.format(inicio) + " " + I18n.t("ate") + " " + sdfData.format(fim) + "\"\n");
                fw.write("\"" + I18n.t("pedidoID") + "\";\"" + I18n.t("cliente") + "\";\"" + I18n.t("produto") + "\";\"" + I18n.t("quantidade") + "\";\"" + I18n.t("precoUnit") + "\";\"" + I18n.t("total") + "\";\"" + I18n.t("dataHora") + "\"\n");

                double totalFaturado = 0;
                Set<String> clientes = new HashSet<>();

                // Dados das vendas
                while (rs.next()) {
                    int quantidade = rs.getInt("quantidade");
                    double preco = rs.getDouble("preco_unit");
                    double totalLinha = quantidade * preco;
                    totalFaturado += totalLinha;

                    String cliente = rs.getString("cliente");
                    clientes.add(cliente);

                    fw.write("\"" + rs.getInt("pedido_id") + "\";"
                            + "\"" + cliente + "\";"
                            + "\"" + rs.getString("produto") + "\";"
                            + "\"" + quantidade + "\";"
                            + "\"" + preco + "\";"
                            + "\"" + totalLinha + "\";"
                            + "\"" + sdfDataHora.format(rs.getTimestamp("data")) + "\"\n");
                }

                // Resumo no final do CSV
                fw.write("\n"); 
                fw.write("\"" + I18n.t("totalFaturado") + "\";\"" + totalFaturado + "\"\n");
                fw.write("\"" + I18n.t("numeroClientes") + "\";\"" + clientes.size() + "\"\n");

                fw.close();
                lblAviso.setForeground(new Color(0,128,0));
                lblAviso.setText(I18n.t("vendasExportSuccess"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            lblAviso.setForeground(Color.RED);
            lblAviso.setText(I18n.t("erroExportVendas"));
        }
    }
}