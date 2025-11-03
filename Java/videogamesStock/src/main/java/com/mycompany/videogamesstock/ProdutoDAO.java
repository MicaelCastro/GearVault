package com.mycompany.videogamesstock;

import java.sql.*;
import java.util.*;

// Classe responsável pelo acesso aos dados (Data Access Object)
public class ProdutoDAO {

    // ---------------- CREATE ----------------
    // Cria um novo produto na base de dados
    public boolean criarProduto(Produto p) {
        String sql = "INSERT INTO produto (nome, plataforma, preco, stock) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getNome());
            ps.setString(2, p.getPlataforma());
            ps.setDouble(3, p.getPreco());
            ps.setInt(4, p.getStock());

            int linhas = ps.executeUpdate();                        // Executa insert
            if (linhas > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) p.setId(rs.getInt(1));               // Actualiza ID do produto criado
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

   // ---------------- READ ALL ----------------
    // Lista todos os produtos existentes
    public List<Produto> listarProdutos() {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT id, nome, plataforma, preco, stock FROM produto";
        try (Connection conn = ConexaoDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Produto p = new Produto();
                p.setId(rs.getInt("id"));
                p.setNome(rs.getString("nome"));
                p.setPlataforma(rs.getString("plataforma"));
                p.setPreco(rs.getDouble("preco"));
                p.setStock(rs.getInt("stock"));                
                produtos.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return produtos;
    }

    // ---------------- UPDATE ----------------
    // Atualiza os dados de um produto existente
    public boolean atualizarProduto(Produto p) {
        String sql = "UPDATE produto SET nome = ?, plataforma = ?, preco = ?, stock = ? WHERE id = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNome());
            ps.setString(2, p.getPlataforma());
            ps.setDouble(3, p.getPreco());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------------- DELETE ----------------
    // Apaga um produto pelo ID
    public boolean apagarProduto(Produto p) {
        String sql = "DELETE FROM produto WHERE id = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, p.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------------- READ BY ID ----------------
    // Retorna um produto específico pelo ID
    public Produto getProdutoById(int id) {
        String sql = "SELECT * FROM produto WHERE id = ?";
        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Produto p = new Produto();
                p.setId(rs.getInt("id"));
                p.setNome(rs.getString("nome"));
                p.setPlataforma(rs.getString("plataforma"));
                p.setPreco(rs.getDouble("preco"));
                p.setStock(rs.getInt("stock"));               
                return p;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ---------------- SEARCH BY NAME ----------------
    // Retorna produtos cujo nome contém a string fornecida
    public List<Produto> buscarProdutosPorNome(String nome) {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT id, nome, plataforma, preco, stock FROM produto WHERE nome LIKE ?";

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + nome + "%"); // busca parcial
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Produto p = new Produto();
                p.setId(rs.getInt("id"));
                p.setNome(rs.getString("nome")); // atributo temporário
                p.setPlataforma(rs.getString("plataforma"));
                p.setPreco(rs.getDouble("preco"));
                p.setStock(rs.getInt("stock"));
                produtos.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return produtos;
    }

    // ---------------- UNIQUE PLATFORMS ----------------
    // Retorna todas as plataformas únicas (para ComboBox, filtros, etc)
    public Set<String> getTodasPlataformas() {
        Set<String> plataformas = new TreeSet<>();
        String sql = "SELECT DISTINCT plataforma FROM produto";
        try (Connection conn = ConexaoDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                plataformas.add(rs.getString("plataforma"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plataformas;
    }
}