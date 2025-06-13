package com.fitnesscenter.dao.impl;

import com.fitnesscenter.dao.TrainerProfileDao;
import com.fitnesscenter.dao.UserDao;
import com.fitnesscenter.model.TrainerProfile;
import com.fitnesscenter.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * JDBC implementation of TrainerProfileDao
 */
@Repository
public class TrainerProfileDaoImpl implements TrainerProfileDao {
    private static final Logger logger = LogManager.getLogger(TrainerProfileDaoImpl.class);
    
    private final JdbcTemplate jdbcTemplate;
    private final UserDao userDao;
    
    @Autowired
    public TrainerProfileDaoImpl(JdbcTemplate jdbcTemplate, UserDao userDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.userDao = userDao;
        logger.info("TrainerProfileDaoImpl initialized");
    }
    
    @Override
    public Optional<TrainerProfile> findByUserId(Long userId) {
        logger.debug("Finding trainer profile for user ID: {}", userId);
        try {
            String query = "SELECT user_id, specialization, experience_years, hourly_rate, bio " +
                          "FROM trainer_profiles WHERE user_id = ?";
                          
            TrainerProfile profile = jdbcTemplate.queryForObject(query, new Object[]{userId}, new TrainerProfileMapper());
            return Optional.ofNullable(profile);
        } catch (DataAccessException e) {
            logger.debug("No trainer profile found for user ID: {}", userId);
            return Optional.empty();
        }
    }
    
    @Override
    public TrainerProfile save(TrainerProfile profile) {
        logger.debug("Saving trainer profile for user ID: {}", profile.getUser().getId());
        
        String sql = "INSERT INTO trainer_profiles (user_id, specialization, experience_years, hourly_rate, bio) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON CONFLICT (user_id) DO UPDATE " +
                    "SET specialization = ?, experience_years = ?, hourly_rate = ?, bio = ?";
                    
        Long userId = profile.getUser().getId();
        jdbcTemplate.update(sql, 
            userId, 
            profile.getSpecialization(),
            profile.getExperienceYears(), 
            profile.getHourlyRate(),
            profile.getBio(),
            profile.getSpecialization(),
            profile.getExperienceYears(), 
            profile.getHourlyRate(),
            profile.getBio());
            
        return profile;
    }
    
    /**
     * RowMapper for TrainerProfile
     */
    private class TrainerProfileMapper implements RowMapper<TrainerProfile> {
        @Override
        public TrainerProfile mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long userId = rs.getLong("user_id");
            Optional<User> userOpt = userDao.findById(userId);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                TrainerProfile profile = new TrainerProfile(user);
                profile.setSpecialization(rs.getString("specialization"));
                profile.setExperienceYears(rs.getInt("experience_years"));
                profile.setHourlyRate(rs.getBigDecimal("hourly_rate"));
                profile.setBio(rs.getString("bio"));
                
                user.setTrainerProfile(profile);
                return profile;
            }
            
            return null;
        }
    }
} 