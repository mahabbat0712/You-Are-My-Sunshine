package com.fitnesscenter.dao.impl;

import com.fitnesscenter.dao.EquipmentDao;
import com.fitnesscenter.jdbc.DatabaseManager;
import com.fitnesscenter.model.Equipment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of EquipmentDao interface
 */
@Repository
public class EquipmentDaoImpl implements EquipmentDao {

    private static final Logger logger = LogManager.getLogger(EquipmentDaoImpl.class);
    
    private final DatabaseManager databaseManager;
    
    @Autowired
    public EquipmentDaoImpl(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        logger.info("EquipmentDaoImpl initialized");
    }
    
    @Override
    public Optional<Equipment> findById(Long id) {
        logger.debug("Finding equipment by ID: {}", id);
        
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            
            String sql = "SELECT id, name, description FROM equipment WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, id);
            
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Equipment equipment = new Equipment();
                equipment.setId(resultSet.getLong("id"));
                equipment.setName(resultSet.getString("name"));
                equipment.setDescription(resultSet.getString("description"));
                
                return Optional.of(equipment);
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding equipment by ID", e);
            throw new RuntimeException("Error finding equipment by ID", e);
        } finally {
            if (connection != null) {
                databaseManager.releaseConnection(connection);
            }
        }
    }
    
    @Override
    public List<Equipment> findAll() {
        logger.debug("Finding all equipment");
        
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            
            String sql = "SELECT id, name, description FROM equipment ORDER BY name";
            PreparedStatement statement = connection.prepareStatement(sql);
            
            ResultSet resultSet = statement.executeQuery();
            List<Equipment> equipmentList = new ArrayList<>();
            
            while (resultSet.next()) {
                Equipment equipment = new Equipment();
                equipment.setId(resultSet.getLong("id"));
                equipment.setName(resultSet.getString("name"));
                equipment.setDescription(resultSet.getString("description"));
                
                equipmentList.add(equipment);
            }
            
            return equipmentList;
        } catch (SQLException e) {
            logger.error("Error finding all equipment", e);
            throw new RuntimeException("Error finding all equipment", e);
        } finally {
            if (connection != null) {
                databaseManager.releaseConnection(connection);
            }
        }
    }
    
    @Override
    public Set<Equipment> findByIds(List<Long> ids) {
        logger.debug("Finding equipment by IDs: {}", ids);
        
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            
            // Create SQL with placeholders for each ID
            StringBuilder sqlBuilder = new StringBuilder("SELECT id, name, description FROM equipment WHERE id IN (");
            for (int i = 0; i < ids.size(); i++) {
                if (i > 0) {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append("?");
            }
            sqlBuilder.append(")");
            
            PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString());
            
            // Set parameters
            for (int i = 0; i < ids.size(); i++) {
                statement.setLong(i + 1, ids.get(i));
            }
            
            ResultSet resultSet = statement.executeQuery();
            Set<Equipment> equipmentSet = new HashSet<>();
            
            while (resultSet.next()) {
                Equipment equipment = new Equipment();
                equipment.setId(resultSet.getLong("id"));
                equipment.setName(resultSet.getString("name"));
                equipment.setDescription(resultSet.getString("description"));
                
                equipmentSet.add(equipment);
            }
            
            return equipmentSet;
        } catch (SQLException e) {
            logger.error("Error finding equipment by IDs", e);
            throw new RuntimeException("Error finding equipment by IDs", e);
        } finally {
            if (connection != null) {
                databaseManager.releaseConnection(connection);
            }
        }
    }
    
    @Override
    public Equipment save(Equipment equipment) {
        logger.debug("Saving equipment: {}", equipment);
        
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            
            String sql = "INSERT INTO equipment (name, description) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            statement.setString(1, equipment.getName());
            statement.setString(2, equipment.getDescription());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating equipment failed, no rows affected.");
            }
            
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                equipment.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("Creating equipment failed, no ID obtained.");
            }
            
            return equipment;
        } catch (SQLException e) {
            logger.error("Error saving equipment", e);
            throw new RuntimeException("Error saving equipment", e);
        } finally {
            if (connection != null) {
                databaseManager.releaseConnection(connection);
            }
        }
    }
    
    @Override
    public boolean update(Equipment equipment) {
        logger.debug("Updating equipment: {}", equipment);
        
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            
            String sql = "UPDATE equipment SET name = ?, description = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            
            statement.setString(1, equipment.getName());
            statement.setString(2, equipment.getDescription());
            statement.setLong(3, equipment.getId());
            
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error updating equipment", e);
            throw new RuntimeException("Error updating equipment", e);
        } finally {
            if (connection != null) {
                databaseManager.releaseConnection(connection);
            }
        }
    }
    
    @Override
    public boolean delete(Long id) {
        logger.debug("Deleting equipment by ID: {}", id);
        
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            
            String sql = "DELETE FROM equipment WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            
            statement.setLong(1, id);
            
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting equipment", e);
            throw new RuntimeException("Error deleting equipment", e);
        } finally {
            if (connection != null) {
                databaseManager.releaseConnection(connection);
            }
        }
    }
} 