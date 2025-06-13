package com.fitnesscenter.service.impl;

import com.fitnesscenter.dao.TrainerProfileDao;
import com.fitnesscenter.dao.UserDao;
import com.fitnesscenter.model.Role;
import com.fitnesscenter.model.TrainerProfile;
import com.fitnesscenter.model.User;
import com.fitnesscenter.service.TrainerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of TrainerService
 */
@Service
public class TrainerServiceImpl implements TrainerService {
    private static final Logger logger = LogManager.getLogger(TrainerServiceImpl.class);
    
    private final UserDao userDao;
    private final TrainerProfileDao trainerProfileDao;
    
    @Autowired
    public TrainerServiceImpl(UserDao userDao, TrainerProfileDao trainerProfileDao) {
        this.userDao = userDao;
        this.trainerProfileDao = trainerProfileDao;
        logger.info("TrainerServiceImpl initialized");
    }
    
    @Override
    public List<User> findAllTrainers(int page, int pageSize) {
        logger.debug("Finding all trainers with page={}, pageSize={}", page, pageSize);
        int offset = (page - 1) * pageSize;
        try {
            List<User> trainers = userDao.findByRole(Role.ROLE_TRAINER, offset, pageSize);
            // Load trainer profiles for each trainer
            trainers.forEach(this::loadTrainerProfile);
            return trainers;
        } catch (Exception e) {
            logger.error("Error finding trainers", e);
            // В случае ошибки возвращаем пустой список
            return List.of();
        }
    }
    
    @Override
    public Optional<User> findTrainerById(Long id) {
        logger.debug("Finding trainer with id={}", id);
        try {
            Optional<User> userOpt = userDao.findById(id);
            
            // Проверяем, что найденный пользователь является тренером
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                boolean isTrainer = user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals(Role.ROLE_TRAINER));
                
                if (isTrainer) {
                    // Load trainer profile if the user is a trainer
                    loadTrainerProfile(user);
                    return userOpt;
                }
            }
            
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding trainer by id={}", id, e);
            return Optional.empty();
        }
    }
    
    @Override
    public int countTrainers() {
        logger.debug("Counting trainers");
        try {
            return (int) userDao.countByRole(Role.ROLE_TRAINER);
        } catch (Exception e) {
            logger.error("Error counting trainers", e);
            return 0;
        }
    }
    
    /**
     * Load trainer profile for a user
     * 
     * @param user User to load trainer profile for
     */
    private void loadTrainerProfile(User user) {
        if (user != null && user.getTrainerProfile() == null) {
            logger.debug("Loading trainer profile for user ID: {}", user.getId());
            Optional<TrainerProfile> profileOpt = trainerProfileDao.findByUserId(user.getId());
            profileOpt.ifPresent(user::setTrainerProfile);
        }
    }
} 