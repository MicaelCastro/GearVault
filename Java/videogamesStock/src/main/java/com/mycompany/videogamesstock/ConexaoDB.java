package com.mycompany.videogamesstock;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexaoDB {

    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        // Caminho para o ficheiro externo
        String caminhoConfig = "config/db.properties";

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(caminhoConfig)) {
            props.load(fis);
            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");
        } catch (IOException e) {
            System.err.println("Erro ao ler o ficheiro de configuração: " + caminhoConfig);
            e.printStackTrace();
        }

        // Forçar carregamento do driver MySQL
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL não encontrado!");
            e.printStackTrace();
        }
    }

    // Método que devolve uma ligação ativa à base de dados
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}