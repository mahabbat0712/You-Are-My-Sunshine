package com.fitnesscenter.dao.impl;

import com.fitnesscenter.dao.OrderDao;
import com.fitnesscenter.dao.TrainingCycleDao;
import com.fitnesscenter.dao.UserDao;
import com.fitnesscenter.jdbc.DatabaseManager;
import com.fitnesscenter.model.Order;
import com.fitnesscenter.model.TrainingCycle;
import com.fitnesscenter.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC Implementation of OrderDao
 */
@Repository
public class OrderDaoImpl implements OrderDao {
    private static final Logger logger = LogManager.getLogger(OrderDaoImpl.class);
    
    private final DatabaseManager databaseManager;
    private final UserDao userDao;
    private final TrainingCycleDao trainingCycleDao;
    
    @Autowired
    public OrderDaoImpl(DatabaseManager databaseManager, UserDao userDao, TrainingCycleDao trainingCycleDao) {
        this.databaseManager = databaseManager;
        this.userDao = userDao;
        this.trainingCycleDao = trainingCycleDao;
        logger.info("OrderDaoImpl initialized");
    }
    
    @Override
    public Optional<Order> findById(Long id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = databaseManager.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToOrder(rs));
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding order by ID: {}", id, e);
            return Optional.empty();
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    @Override
    public List<Order> findAll() {
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Order> orders = new ArrayList<>();
        
        try {
            conn = databaseManager.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
            
            return orders;
        } catch (SQLException e) {
            logger.error("Error finding all orders", e);
            return orders;
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    @Override
    public List<Order> findAll(int page, int size) {
        String sql = "SELECT * FROM orders ORDER BY order_date DESC LIMIT ? OFFSET ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Order> orders = new ArrayList<>();
        
        try {
            conn = databaseManager.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, size);
            stmt.setInt(2, (page - 1) * size);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
            
            return orders;
        } catch (SQLException e) {
            logger.error("Error finding orders with pagination: page={}, size={}", page, size, e);
            return orders;
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    @Override
    public List<Order> findByClientId(Long clientId) {
        String sql = "SELECT * FROM orders WHERE client_id = ? ORDER BY order_date DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Order> orders = new ArrayList<>();
        
        try {
            conn = databaseManager.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, clientId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
            
            return orders;
        } catch (SQLException e) {
            logger.error("Error finding orders for client: {}", clientId, e);
            return orders;
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    @Override
    public List<Order> findByTrainerId(Long trainerId) {
        String sql = "SELECT * FROM orders WHERE trainer_id = ? ORDER BY order_date DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Order> orders = new ArrayList<>();
        
        try {
            conn = databaseManager.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, trainerId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
            
            return orders;
        } catch (SQLException e) {
            logger.error("Error finding orders for trainer: {}", trainerId, e);
            return orders;
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    @Override
    public Order save(Order order) {
        String sql = "INSERT INTO orders (client_id, trainer_id, training_cycle_id, original_price, " +
                "discount_percentage, final_price, status, order_date, payment_date, completion_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = databaseManager.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setLong(1, order.getClient().getId());
            
            if (order.getTrainer() != null) {
                stmt.setLong(2, order.getTrainer().getId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }
            
            stmt.setLong(3, order.getTrainingCycle().getId());
            stmt.setBigDecimal(4, order.getOriginalPrice());
            stmt.setBigDecimal(5, order.getDiscountPercentage());
            stmt.setBigDecimal(6, order.getFinalPrice());
            stmt.setString(7, order.getStatus().getValue());
            
            // Set timestamps
            stmt.setTimestamp(8, Timestamp.valueOf(order.getOrderDate()));
            
            if (order.getPaymentDate() != null) {
                stmt.setTimestamp(9, Timestamp.valueOf(order.getPaymentDate()));
            } else {
                stmt.setNull(9, Types.TIMESTAMP);
            }
            
            if (order.getCompletionDate() != null) {
                stmt.setTimestamp(10, Timestamp.valueOf(order.getCompletionDate()));
            } else {
                stmt.setNull(10, Types.TIMESTAMP);
            }
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                order.setId(rs.getLong("id"));
                return order;
            } else {
                throw new SQLException("Failed to insert order, no ID obtained.");
            }
            
        } catch (SQLException e) {
            logger.error("Error saving order", e);
            throw new RuntimeException("Failed to save order", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    @Override
    public Order update(Order order) {
        String sql = "UPDATE orders SET client_id = ?, trainer_id = ?, training_cycle_id = ?, " +
                "original_price = ?, discount_percentage = ?, final_price = ?, status = ?, " +
                "order_date = ?, payment_date = ?, completion_date = ? WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = databaseManager.getConnection();
            stmt = conn.prepareStatement(sql);
            
            stmt.setLong(1, order.getClient().getId());
            
            if (order.getTrainer() != null) {
                stmt.setLong(2, order.getTrainer().getId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }
            
            stmt.setLong(3, order.getTrainingCycle().getId());
            stmt.setBigDecimal(4, order.getOriginalPrice());
            stmt.setBigDecimal(5, order.getDiscountPercentage());
            stmt.setBigDecimal(6, order.getFinalPrice());
            stmt.setString(7, order.getStatus().getValue());
            
            // Set timestamps
            stmt.setTimestamp(8, Timestamp.valueOf(order.getOrderDate()));
            
            if (order.getPaymentDate() != null) {
                stmt.setTimestamp(9, Timestamp.valueOf(order.getPaymentDate()));
            } else {
                stmt.setNull(9, Types.TIMESTAMP);
            }
            
            if (order.getCompletionDate() != null) {
                stmt.setTimestamp(10, Timestamp.valueOf(order.getCompletionDate()));
            } else {
                stmt.setNull(10, Types.TIMESTAMP);
            }
            
            stmt.setLong(11, order.getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                return order;
            } else {
                throw new SQLException("Failed to update order, no rows affected.");
            }
            
        } catch (SQLException e) {
            logger.error("Error updating order: {}", order.getId(), e);
            throw new RuntimeException("Failed to update order", e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM orders WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = databaseManager.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            logger.error("Error deleting order: {}", id, e);
            return false;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
    
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM orders";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = databaseManager.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting orders", e);
            return 0;
        } finally {
            closeResources(conn, stmt, rs);
        }
    }
    
    /**
     * Map a ResultSet row to an Order object
     */
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        
        // Load client
        Long clientId = rs.getLong("client_id");
        User client = userDao.findById(clientId).orElse(null);
        order.setClient(client);
        
        // Load trainer if exists
        Long trainerId = rs.getLong("trainer_id");
        if (!rs.wasNull()) {
            User trainer = userDao.findById(trainerId).orElse(null);
            order.setTrainer(trainer);
        }
        
        // Load training cycle
        Long cycleId = rs.getLong("training_cycle_id");
        TrainingCycle cycle = trainingCycleDao.findById(cycleId).orElse(null);
        order.setTrainingCycle(cycle);
        
        order.setOriginalPrice(rs.getBigDecimal("original_price"));
        order.setDiscountPercentage(rs.getBigDecimal("discount_percentage"));
        order.setFinalPrice(rs.getBigDecimal("final_price"));
        
        // Parse status
        String statusStr = rs.getString("status");
        order.setStatus(Order.OrderStatus.fromValue(statusStr));
        
        // Set dates
        order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
        
        Timestamp paymentDate = rs.getTimestamp("payment_date");
        if (paymentDate != null) {
            order.setPaymentDate(paymentDate.toLocalDateTime());
        }
        
        Timestamp completionDate = rs.getTimestamp("completion_date");
        if (completionDate != null) {
            order.setCompletionDate(completionDate.toLocalDateTime());
        }
        
        return order;
    }
    
    /**
     * Close JDBC resources
     */
    private void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error("Error closing ResultSet", e);
            }
        }
        
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error("Error closing Statement", e);
            }
        }
        
        if (conn != null) {
            databaseManager.releaseConnection(conn);
        }
    }
} 