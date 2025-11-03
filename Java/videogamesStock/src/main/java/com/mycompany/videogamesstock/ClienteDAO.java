package com.mycompany.videogamesstock;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Classe responsável pelo acesso aos dados (Data Access Object)
public class ClienteDAO {

    // Método para criar um novo cliente na base de dados
    public int criarCliente(Cliente c) {
        try (Connection conn = ConexaoDB.getConnection()) {
            String sql = "INSERT INTO cliente (nome, contacto, email) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, c.getNome());
            ps.setString(2, c.getContacto());
            ps.setString(3, c.getEmail());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next()) return rs.getInt(1);
        } catch(SQLException e) { e.printStackTrace(); }
        return -1;
    }

    // Método para listar todos os clientes existentes na base de dados
    public List<Cliente> listarClientes() {
        List<Cliente> lista = new ArrayList<>();                                 // Lista onde os clientes serão guardados
        try (Connection conn = ConexaoDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM cliente")) {

            while(rs.next()) {
                Cliente c = new Cliente();
                c.setId(rs.getInt("id"));
                c.setNome(rs.getString("nome"));
                c.setContacto(rs.getString("contacto"));
                c.setEmail(rs.getString("email"));
                lista.add(c);
            }
        } catch(SQLException e) { e.printStackTrace(); }
        return lista;                                                            // Retorna a lista completa de clientes
    }
}