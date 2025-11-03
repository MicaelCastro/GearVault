package com.mycompany.videogamesstock;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Classe responsável pelo acesso aos dados (Data Access Object)
public class PedidoDAO {

    private ProdutoDAO produtoDAO = new ProdutoDAO();    // DAO de produtos, para validar stock e preços
    private ClienteDAO clienteDAO = new ClienteDAO();    // DAO de clientes, para associar pedidos

    // ---------------- LISTAR TODOS OS PEDIDOS ----------------
    public List<Pedido> listarPedidos() {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT * FROM pedido ORDER BY data DESC";       // ordena por data decrescente

        try (Connection conn = ConexaoDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Busca todos os clientes para associar aos pedidos
            List<Cliente> clientes = clienteDAO.listarClientes();

            while (rs.next()) {
                Pedido p = new Pedido();
                p.setId(rs.getInt("id"));

                // Associa o cliente ao pedido usando o ID
                int clienteId = rs.getInt("cliente_id");
                for (Cliente c : clientes) {
                    if (c.getId() == clienteId) {
                        p.setCliente(c);
                        break;
                    }
                }

                // Converte timestamp SQL para LocalDateTime
                p.setData(rs.getTimestamp("data").toLocalDateTime());
                
                // Carrega os itens do pedido
                p.setItens(listarItensPorPedido(p.getId()));
                
                pedidos.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pedidos;
    }

    // ---------------- CRIAR UM NOVO PEDIDO ----------------
    public void criarPedido(Pedido pedido) {
        String sqlPedido = "INSERT INTO pedido (cliente_id, data) VALUES (?, ?)";
        String sqlItem = "INSERT INTO pedido_item (pedido_id, produto_id, quantidade, preco_unit) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexaoDB.getConnection()) {
            conn.setAutoCommit(false); // inicia transação

            // Verifica stock e garante preco_unit antes de criar pedido
            for (PedidoItem item : pedido.getItens()) {
                Produto p = produtoDAO.getProdutoById(item.getProduto().getId());
                if (p == null) {
                    throw new RuntimeException("Produto não encontrado: ID " + item.getProduto().getId());
                }

                if (item.getQuantidade() > p.getStock()) {
                    throw new RuntimeException("Stock insuficiente para o produto: " + p.getNome());
                }

                // Garante que preco_unit não é nulo ou 0
                if (item.getPrecoUnit() <= 0) {
                    item.setPrecoUnit(p.getPreco());
                }

                // Atualiza o produto do item para ter stock e preço corretos
                item.setProduto(p);
            }

            // Inserir pedido
            try (PreparedStatement ps = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, pedido.getCliente().getId());
                ps.setTimestamp(2, Timestamp.valueOf(pedido.getData()));
                ps.executeUpdate();

                // Obtém ID gerado pela DB
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int pedidoId = rs.getInt(1);
                        pedido.setId(pedidoId);

                        // Inserir itens do pedido
                        try (PreparedStatement psItem = conn.prepareStatement(sqlItem)) {
                            for (PedidoItem item : pedido.getItens()) {
                                psItem.setInt(1, pedidoId);
                                psItem.setInt(2, item.getProduto().getId());
                                psItem.setInt(3, item.getQuantidade());
                                psItem.setDouble(4, item.getPrecoUnit());
                                psItem.addBatch();                               
                            }
                            psItem.executeBatch();
                        }
                    }
                }
            }

            conn.commit();              // confirma a transação

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao criar pedido: " + e.getMessage(), e);
        }
    }

    // ---------------- LISTAR ITENS DE UM PEDIDO ----------------
    private List<PedidoItem> listarItensPorPedido(int pedidoId) {
        List<PedidoItem> itens = new ArrayList<>();
        String sql = "SELECT i.produto_id, i.quantidade, i.preco_unit, p.nome, p.plataforma " +
                     "FROM pedido_item i " +
                     "JOIN produto p ON i.produto_id = p.id " +
                     "WHERE i.pedido_id = ?";

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Produto produto = new Produto();
                    produto.setId(rs.getInt("produto_id"));
                    produto.setNome(rs.getString("nome"));
                    produto.setPlataforma(rs.getString("plataforma"));
                    produto.setPreco(rs.getDouble("preco_unit"));

                    PedidoItem item = new PedidoItem();
                    item.setProduto(produto);
                    item.setQuantidade(rs.getInt("quantidade"));
                    item.setPrecoUnit(rs.getDouble("preco_unit"));

                    itens.add(item);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return itens;
    }
}