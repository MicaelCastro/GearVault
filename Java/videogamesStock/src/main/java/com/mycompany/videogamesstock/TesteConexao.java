package com.mycompany.videogamesstock;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Classe de teste para verificar se a ligação à base de dados funciona corretamente.
 */

public class TesteConexao {
    public static void main(String[] args) {
        // Tenta obter uma ligação à base de dados usando ConexaoDB
        try (Connection conn = ConexaoDB.getConnection()) {
            
            // Se a ligação não for nula, imprime sucesso
            if (conn != null) {
                System.out.println("Ligação à base de dados realizada com sucesso!");
            }
        } catch (SQLException e) {
            // Captura qualquer erro de ligação e imprime mensagem + stack trace
            System.err.println("Erro ao ligar à base de dados:");
            e.printStackTrace();
        }
    }
}