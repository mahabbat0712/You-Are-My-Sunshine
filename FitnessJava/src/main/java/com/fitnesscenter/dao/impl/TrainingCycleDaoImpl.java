package com.fitnesscenter.dao.impl;

import com.fitnesscenter.dao.AbstractDao;
import com.fitnesscenter.dao.TrainingCycleDao;
import com.fitnesscenter.model.TrainingCycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of TrainingCycleDao interface using JDBC
 */
@Repository
public class TrainingCycleDaoImpl extends AbstractDao<TrainingCycle, Long> implements TrainingCycleDao {
    private static final Logger logger = LogManager.getLogger(TrainingCycleDaoImpl.class);
    
    private static final String FIND_BY_ID_SQL = 
            "SELECT id, name, description, duration_days, price, active, created_at " +
            "FROM training_cycles WHERE id = ?";
    
    private static final String FIND_ALL_SQL = 
            "SELECT id, name, description, duration_days, price, active, created_at " +
            "FROM training_cycles ORDER BY id";
    
    private static final String FIND_ALL_PAGINATED_SQL = 
            "SELECT id, name, description, duration_days, price, active, created_at " +
            "FROM training_cycles ORDER BY id LIMIT ? OFFSET ?";
    
    private static final String FIND_ACTIVE_SQL = 
            "SELECT id, name, description, duration_days, price, active, created_at " +
            "FROM training_cycles WHERE active = true ORDER BY id";
    
    private static final String FIND_ACTIVE_PAGINATED_SQL = 
            "SELECT id, name, description, duration_days, price, active, created_at " +
            "FROM training_cycles WHERE active = true ORDER BY id LIMIT ? OFFSET ?";
    
    private static final String INSERT_SQL = 
            "INSERT INTO training_cycles (name, description, duration_days, price, active, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE training_cycles " +
            "SET name = ?, description = ?, duration_days = ?, price = ?, active = ? " +
            "WHERE id = ?";
    
    private static final String DELETE_SQL = 
            "DELETE FROM training_cycles WHERE id = ?";
    
    private static final String COUNT_SQL = 
            "SELECT COUNT(*) FROM training_cycles";
    
    private static final String ACTIVATE_SQL = 
            "UPDATE training_cycles SET active = true WHERE id = ?";
    
    private static final String DEACTIVATE_SQL = 
            "UPDATE training_cycles SET active = false WHERE id = ?";
    
    @Override
    public Optional<TrainingCycle> findById(Long id) {
        logger.debug("Finding training cycle by ID: {}", id);
        return findOne(FIND_BY_ID_SQL, id);
    }
    
    @Override
    public List<TrainingCycle> findAll() {
        logger.debug("Finding all training cycles");
        return findMany(FIND_ALL_SQL);
    }
    
    @Override
    public List<TrainingCycle> findAll(int offset, int limit) {
        logger.debug("Finding training cycles with pagination: offset={}, limit={}", offset, limit);
        return findMany(FIND_ALL_PAGINATED_SQL, limit, offset);
    }
    
    @Override
    public List<TrainingCycle> findActive() {
        logger.debug("Finding active training cycles");
        return findMany(FIND_ACTIVE_SQL);
    }
    
    @Override
    public List<TrainingCycle> findActive(int offset, int limit) {
        logger.debug("Finding active training cycles with pagination: offset={}, limit={}", offset, limit);
        return findMany(FIND_ACTIVE_PAGINATED_SQL, limit, offset);
    }
    
    @Override
    public TrainingCycle save(TrainingCycle trainingCycle) {
        if (trainingCycle.getId() == null) {
            return insert(trainingCycle);
        } else {
            return update(trainingCycle);
        }
    }
    
    private TrainingCycle insert(TrainingCycle trainingCycle) {
        logger.debug("Inserting new training cycle: {}", trainingCycle);
        
        LocalDateTime createdAt = trainingCycle.getCreatedAt() != null ? 
                trainingCycle.getCreatedAt() : LocalDateTime.now();
        
        Optional<Long> generatedId = updateAndGetGeneratedKey(INSERT_SQL,
                trainingCycle.getName(),
                trainingCycle.getDescription(),
                trainingCycle.getDurationDays(),
                trainingCycle.getPrice(),
                trainingCycle.isActive(),
                createdAt
        );
        
        if (generatedId.isPresent()) {
            trainingCycle.setId(generatedId.get());
            return trainingCycle;
        } else {
            throw new RuntimeException("Failed to insert training cycle: " + trainingCycle);
        }
    }
    
    private TrainingCycle update(TrainingCycle trainingCycle) {
        logger.debug("Updating training cycle: {}", trainingCycle);
        
        int rowsAffected = update(UPDATE_SQL,
                trainingCycle.getName(),
                trainingCycle.getDescription(),
                trainingCycle.getDurationDays(),
                trainingCycle.getPrice(),
                trainingCycle.isActive(),
                trainingCycle.getId()
        );
        
        if (rowsAffected > 0) {
            return trainingCycle;
        } else {
            throw new RuntimeException("Failed to update training cycle: " + trainingCycle);
        }
    }
    
    @Override
    public boolean deleteById(Long id) {
        logger.debug("Deleting training cycle by ID: {}", id);
        int rowsAffected = update(DELETE_SQL, id);
        return rowsAffected > 0;
    }
    
    @Override
    public long count() {
        logger.debug("Counting all training cycles");
        return count(COUNT_SQL);
    }
    
    @Override
    public boolean activate(Long id) {
        logger.debug("Activating training cycle: {}", id);
        int rowsAffected = update(ACTIVATE_SQL, id);
        return rowsAffected > 0;
    }
    
    @Override
    public boolean deactivate(Long id) {
        logger.debug("Deactivating training cycle: {}", id);
        int rowsAffected = update(DEACTIVATE_SQL, id);
        return rowsAffected > 0;
    }
    
    @Override
    protected TrainingCycle mapRow(ResultSet resultSet) throws SQLException {
        TrainingCycle cycle = new TrainingCycle();
        cycle.setId(resultSet.getLong("id"));
        cycle.setName(resultSet.getString("name"));
        cycle.setDescription(resultSet.getString("description"));
        cycle.setDurationDays(resultSet.getInt("duration_days"));
        cycle.setPrice(resultSet.getBigDecimal("price"));
        cycle.setActive(resultSet.getBoolean("active"));
        cycle.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        return cycle;
    }
} 