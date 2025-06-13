package com.fitnesscenter.dao.impl;

import com.fitnesscenter.dao.AssignmentDao;
import com.fitnesscenter.dao.EquipmentDao;
import com.fitnesscenter.dao.OrderDao;
import com.fitnesscenter.dao.UserDao;
import com.fitnesscenter.jdbc.DatabaseManager;
import com.fitnesscenter.model.Assignment;
import com.fitnesscenter.model.AssignmentCategory;
import com.fitnesscenter.model.Equipment;
import com.fitnesscenter.model.Order;
import com.fitnesscenter.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of AssignmentDao interface
 */
@Repository
public class AssignmentDaoImpl implements AssignmentDao {

    private static final Logger logger = LogManager.getLogger(AssignmentDaoImpl.class);
    
    private final DatabaseManager databaseManager;
    private final OrderDao orderDao;
    private final UserDao userDao;
    private final EquipmentDao equipmentDao;
    
    @Autowired
    public AssignmentDaoImpl(DatabaseManager databaseManager, OrderDao orderDao, UserDao userDao, EquipmentDao equipmentDao) {
        this.databaseManager = databaseManager;
        this.orderDao = orderDao;
        this.userDao = userDao;
        this.equipmentDao = equipmentDao;
        logger.info("AssignmentDaoImpl initialized");
    }
    
    @Override
    public Optional<Assignment> findById(Long id) {
        logger.debug("Finding assignment by ID: {}", id);
        
        String sql = "SELECT a.id, a.order_id, a.category_id, a.trainer_id, a.title, a.description, a.schedule, " +
                     "a.status, a.created_at, a.updated_at FROM assignments a WHERE a.id = ?";
        
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, id);
            
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Assignment assignment = mapResultSetToAssignment(resultSet, connection);
                return Optional.of(assignment);
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding assignment by ID: {}", id, e);
            throw new RuntimeException("Error finding assignment by ID: " + id, e);
        } finally {
            if (connection != null) {
                databaseManager.releaseConnection(connection);
            }
        }
    }
    
    @Override
    public List<Assignment> findAll() {
        logger.debug("Finding all assignments");
        
        String sql = "SELECT a.id, a.order_id, a.category_id, a.trainer_id, a.title, a.description, a.schedule, " +
                     "a.status, a.created_at, a.updated_at FROM assignments a ORDER BY a.created_at DESC";
        
        Connection connection = null;
        List<Assignment> assignments = new ArrayList<>();
        
        try {
            connection = databaseManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Assignment assignment = mapResultSetToAssignment(resultSet, connection);
                assignments.add(assignment);
            }
            
            return assignments;
        } catch (SQLException e) {
            logger.error("Error finding all assignments", e);
            throw new RuntimeException("Error finding all assignments", e);
        } finally {
            if (connection != null) {
                databaseManager.releaseConnection(connection);
            }
        }
    }
    
    @Override
    public List<Assignment> findByTrainerId(Long trainerId) {
        logger.debug("Finding assignments by trainer ID: {}", trainerId);
        
        String sql = "SELECT a.id, a.order_id, a.category_id, a.trainer_id, a.title, a.description, a.schedule, " +
                     "a.status, a.created_at, a.updated_at FROM assignments a WHERE a.trainer_id = ? ORDER BY a.created_at DESC";
        
        Connection connection = null;
        List<Assignment> assignments = new ArrayList<>();
        
        try {
            connection = databaseManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, trainerId);
            
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Assignment assignment = mapResultSetToAssignment(resultSet, connection);
                assignments.add(assignment);
            }
            
            return assignments;
        } catch (SQLException e) {
            logger.error("Error finding assignments by trainer ID: {}", trainerId, e);
            throw new RuntimeException("Error finding assignments by trainer ID: " + trainerId, e);
        } finally {
            if (connection != null) {
                databaseManager.releaseConnection(connection);
            }
        }
    }
    
    @Override
    public List<Assignment> findByClientId(Long clientId) {
        logger.debug("Finding assignments by client ID: {}", clientId);
        
        String sql = "SELECT a.id, a.order_id, a.category_id, a.trainer_id, a.title, a.description, a.schedule, " +
                     "a.status, a.created_at, a.updated_at FROM assignments a " +
                     "JOIN orders o ON a.order_id = o.id " +
                     "WHERE o.client_id = ? ORDER BY a.created_at DESC";
        
        Connection connection = null;
        List<Assignment> assignments = new ArrayList<>();
        
        try {
            connection = databaseManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, clientId);
            
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Assignment assignment = mapResultSetToAssignment(resultSet, connection);
                assignments.add(assignment);
            }
            
            return assignments;
        } catch (SQLException e) {
            logger.error("Error finding assignments by client ID: {}", clientId, e);
            throw new RuntimeException("Error finding assignments by client ID: " + clientId, e);
        } finally {
            if (connection != null) {
                databaseManager.releaseConnection(connection);
            }
        }
    }
    
    @Override
    public List<Assignment> findByOrderId(Long orderId) {
        logger.debug("Finding assignments by order ID: {}", orderId);
        // TODO: Implement this method fully
        return new ArrayList<>();
    }
    
    @Override
    public Assignment save(Assignment assignment) {
        logger.debug("Saving assignment: {}", assignment);
        
        Connection connection = null;
        try {
            logger.info("Starting database operation for saving assignment");
            connection = databaseManager.getConnection();
            logger.info("Database connection established");
            
            // Start transaction
            connection.setAutoCommit(false);
            logger.info("Transaction started");
            
            // Ensure assignment_categories table exists
            createCategoryTableIfNotExists(connection);
            
            // Insert into assignments table
            String sql = "INSERT INTO assignments (order_id, category_id, trainer_id, title, description, schedule, status, created_at, updated_at) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            logger.info("Preparing SQL statement: {}", sql);
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, assignment.getOrder().getId());
            statement.setInt(2, assignment.getCategory().getId());
            statement.setLong(3, assignment.getTrainer().getId());
            statement.setString(4, assignment.getTitle());
            statement.setString(5, assignment.getDescription());
            statement.setString(6, assignment.getSchedule());
            statement.setString(7, assignment.getStatus());
            statement.setTimestamp(8, Timestamp.valueOf(assignment.getCreatedAt()));
            statement.setTimestamp(9, Timestamp.valueOf(assignment.getUpdatedAt()));
            logger.info("Statement parameters set successfully");
            
            int affectedRows = statement.executeUpdate();
            logger.info("Statement executed, affected rows: {}", affectedRows);
            
            if (affectedRows == 0) {
                logger.error("Creating assignment failed, no rows affected");
                throw new SQLException("Creating assignment failed, no rows affected.");
            }
            
            // Get the generated ID
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                assignment.setId(generatedKeys.getLong(1));
                logger.debug("Generated assignment ID: {}", assignment.getId());
            } else {
                logger.error("Creating assignment failed, no ID obtained");
                throw new SQLException("Creating assignment failed, no ID obtained.");
            }
            
            // Insert equipment relations if any
            if (assignment.getRequiredEquipment() != null && !assignment.getRequiredEquipment().isEmpty()) {
                logger.info("Saving equipment relations for assignment");
                insertAssignmentEquipment(connection, assignment.getId(), assignment.getRequiredEquipment());
            }
            
            // Commit transaction
            connection.commit();
            logger.info("Transaction committed successfully");
            
            return assignment;
            
        } catch (SQLException e) {
            logger.error("Error saving assignment", e);
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.debug("Transaction rolled back");
                } catch (SQLException ex) {
                    logger.error("Error rolling back transaction", ex);
                }
            }
            throw new RuntimeException("Error saving assignment", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    databaseManager.releaseConnection(connection);
                    logger.info("Database connection released");
                } catch (SQLException e) {
                    logger.error("Error resetting auto-commit", e);
                }
            }
        }
    }
    
    /**
     * Helper method to insert assignment-equipment relations
     */
    private void insertAssignmentEquipment(Connection connection, Long assignmentId, Set<Equipment> equipment) throws SQLException {
        String sql = "INSERT INTO assignment_equipment (assignment_id, equipment_id) VALUES (?, ?)";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Equipment item : equipment) {
                statement.setLong(1, assignmentId);
                statement.setLong(2, item.getId());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
    
    @Override
    public boolean update(Assignment assignment) {
        logger.debug("Updating assignment: {}", assignment);
        
        String sql = "UPDATE assignments SET order_id = ?, category_id = ?, trainer_id = ?, " +
                     "title = ?, description = ?, schedule = ?, status = ?, updated_at = ? " +
                     "WHERE id = ?";
        
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            
            // Start transaction
            connection.setAutoCommit(false);
            
            // Update assignment
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, assignment.getOrder().getId());
            statement.setInt(2, assignment.getCategory().getId());
            statement.setLong(3, assignment.getTrainer().getId());
            statement.setString(4, assignment.getTitle());
            statement.setString(5, assignment.getDescription());
            statement.setString(6, assignment.getSchedule());
            statement.setString(7, assignment.getStatus());
            statement.setTimestamp(8, Timestamp.valueOf(assignment.getUpdatedAt()));
            statement.setLong(9, assignment.getId());
            
            int rowsUpdated = statement.executeUpdate();
            
            // Update equipment relations if any
            if (assignment.getRequiredEquipment() != null) {
                // Delete existing relations
                deleteAssignmentEquipment(connection, assignment.getId());
                
                // Insert new relations
                if (!assignment.getRequiredEquipment().isEmpty()) {
                    insertAssignmentEquipment(connection, assignment.getId(), assignment.getRequiredEquipment());
                }
            }
            
            // Commit transaction
            connection.commit();
            
            return rowsUpdated > 0;
            
        } catch (SQLException e) {
            logger.error("Error updating assignment: {}", assignment.getId(), e);
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.debug("Transaction rolled back");
                } catch (SQLException ex) {
                    logger.error("Error rolling back transaction", ex);
                }
            }
            throw new RuntimeException("Error updating assignment: " + assignment.getId(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    databaseManager.releaseConnection(connection);
                } catch (SQLException e) {
                    logger.error("Error resetting auto-commit", e);
                }
            }
        }
    }
    
    /**
     * Helper method to delete assignment-equipment relations
     */
    private void deleteAssignmentEquipment(Connection connection, Long assignmentId) throws SQLException {
        String sql = "DELETE FROM assignment_equipment WHERE assignment_id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, assignmentId);
            statement.executeUpdate();
        }
    }
    
    @Override
    public boolean delete(Long id) {
        logger.debug("Deleting assignment by ID: {}", id);
        
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            
            // Start transaction
            connection.setAutoCommit(false);
            
            // Delete equipment relations first
            deleteAssignmentEquipment(connection, id);
            
            // Delete assignment
            String sql = "DELETE FROM assignments WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, id);
            
            int rowsDeleted = statement.executeUpdate();
            
            // Commit transaction
            connection.commit();
            
            return rowsDeleted > 0;
            
        } catch (SQLException e) {
            logger.error("Error deleting assignment by ID: {}", id, e);
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.debug("Transaction rolled back");
                } catch (SQLException ex) {
                    logger.error("Error rolling back transaction", ex);
                }
            }
            throw new RuntimeException("Error deleting assignment: " + id, e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    databaseManager.releaseConnection(connection);
                } catch (SQLException e) {
                    logger.error("Error resetting auto-commit", e);
                }
            }
        }
    }
    
    /**
     * Helper method to map ResultSet to Assignment
     */
    private Assignment mapResultSetToAssignment(ResultSet rs, Connection connection) throws SQLException {
        Assignment assignment = new Assignment();
        assignment.setId(rs.getLong("id"));
        assignment.setTitle(rs.getString("title"));
        assignment.setDescription(rs.getString("description"));
        assignment.setSchedule(rs.getString("schedule"));
        assignment.setStatus(rs.getString("status"));
        assignment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        assignment.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        // Load order
        Long orderId = rs.getLong("order_id");
        orderDao.findById(orderId).ifPresent(assignment::setOrder);
        
        // Load trainer
        Long trainerId = rs.getLong("trainer_id");
        userDao.findById(trainerId).ifPresent(assignment::setTrainer);
        
        // Load category
        Integer categoryId = rs.getInt("category_id");
        // Create assignment category based on ID
        AssignmentCategory category = new AssignmentCategory();
        category.setId(categoryId);
        switch(categoryId) {
            case 1:
                category.setName(AssignmentCategory.CATEGORY_EXERCISE);
                break;
            case 2:
                category.setName(AssignmentCategory.CATEGORY_EQUIPMENT);
                break;
            case 3:
                category.setName(AssignmentCategory.CATEGORY_DIET);
                break;
            default:
                category.setName("UNKNOWN");
        }
        assignment.setCategory(category);
        
        // Load equipment
        loadAssignmentEquipment(connection, assignment);
        
        return assignment;
    }
    
    /**
     * Helper method to load equipment for an assignment
     */
    private void loadAssignmentEquipment(Connection connection, Assignment assignment) throws SQLException {
        String sql = "SELECT equipment_id FROM assignment_equipment WHERE assignment_id = ?";
        
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setLong(1, assignment.getId());
        
        ResultSet resultSet = statement.executeQuery();
        Set<Equipment> equipment = new HashSet<>();
        
        while (resultSet.next()) {
            Long equipmentId = resultSet.getLong("equipment_id");
            equipmentDao.findById(equipmentId).ifPresent(equipment::add);
        }
        
        assignment.setRequiredEquipment(equipment);
    }

    /**
     * Check if the assignment_categories table exists
     */
    private boolean doesCategoryTableExist(Connection connection) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet tables = meta.getTables(null, null, "assignment_categories", null);
        boolean exists = tables.next();
        tables.close();
        return exists;
    }

    /**
     * Create the assignment_categories table if it doesn't exist
     */
    private void createCategoryTableIfNotExists(Connection connection) throws SQLException {
        logger.info("Checking if assignment_categories table exists");
        if (!doesCategoryTableExist(connection)) {
            logger.info("Creating assignment_categories table");
            String sql = "CREATE TABLE IF NOT EXISTS assignment_categories (" +
                         "id SERIAL PRIMARY KEY, " +
                         "name VARCHAR(50) NOT NULL UNIQUE)";
            
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
            
            // Insert default categories
            String insertSql = "INSERT INTO assignment_categories (name) VALUES (?)";
            try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                statement.setString(1, "EXERCISE");
                statement.executeUpdate();
                
                statement.setString(1, "EQUIPMENT");
                statement.executeUpdate();
                
                statement.setString(1, "DIET");
                statement.executeUpdate();
            }
            logger.info("Default categories created");
        }
    }
} 