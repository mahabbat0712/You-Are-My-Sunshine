package com.fitnesscenter.service.impl;

import com.fitnesscenter.dao.TrainingCycleDao;
import com.fitnesscenter.model.TrainingCycle;
import com.fitnesscenter.service.TrainingCycleService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of TrainingCycleService
 */
@Service
public class TrainingCycleServiceImpl implements TrainingCycleService {
    private static final Logger logger = LogManager.getLogger(TrainingCycleServiceImpl.class);
    
    private final TrainingCycleDao trainingCycleDao;
    
    @Autowired
    public TrainingCycleServiceImpl(TrainingCycleDao trainingCycleDao) {
        this.trainingCycleDao = trainingCycleDao;
    }
    
    @Override
    public Optional<TrainingCycle> findById(Long id) {
        logger.debug("Finding training cycle by ID: {}", id);
        return trainingCycleDao.findById(id);
    }
    
    @Override
    public List<TrainingCycle> findAll() {
        logger.debug("Finding all training cycles");
        return trainingCycleDao.findAll();
    }
    
    @Override
    public List<TrainingCycle> findAll(int page, int size) {
        logger.debug("Finding training cycles with pagination: page={}, size={}", page, size);
        int offset = Math.max(0, (page - 1) * size);
        return trainingCycleDao.findAll(offset, size);
    }
    
    @Override
    public List<TrainingCycle> findActive() {
        logger.debug("Finding active training cycles");
        return trainingCycleDao.findActive();
    }
    
    @Override
    public List<TrainingCycle> findActive(int page, int size) {
        logger.debug("Finding active training cycles with pagination: page={}, size={}", page, size);
        int offset = Math.max(0, (page - 1) * size);
        return trainingCycleDao.findActive(offset, size);
    }
    
    @Override
    public TrainingCycle createTrainingCycle(TrainingCycle trainingCycle) {
        logger.debug("Creating training cycle: {}", trainingCycle);
        
        // Set default values if not provided
        if (trainingCycle.getCreatedAt() == null) {
            trainingCycle.setCreatedAt(LocalDateTime.now());
        }
        
        return trainingCycleDao.save(trainingCycle);
    }
    
    @Override
    public TrainingCycle updateTrainingCycle(TrainingCycle trainingCycle) {
        logger.debug("Updating training cycle: {}", trainingCycle);
        
        // Ensure cycle exists
        TrainingCycle existingCycle = trainingCycleDao.findById(trainingCycle.getId())
                .orElseThrow(() -> new IllegalArgumentException("Training cycle not found with ID: " + trainingCycle.getId()));
        
        // Preserve creation date
        trainingCycle.setCreatedAt(existingCycle.getCreatedAt());
        
        return trainingCycleDao.save(trainingCycle);
    }
    
    @Override
    public boolean activateTrainingCycle(Long id) {
        logger.debug("Activating training cycle: {}", id);
        
        // Ensure cycle exists
        trainingCycleDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Training cycle not found with ID: " + id));
        
        return trainingCycleDao.activate(id);
    }
    
    @Override
    public boolean deactivateTrainingCycle(Long id) {
        logger.debug("Deactivating training cycle: {}", id);
        
        // Ensure cycle exists
        trainingCycleDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Training cycle not found with ID: " + id));
        
        return trainingCycleDao.deactivate(id);
    }
    
    @Override
    public boolean deleteTrainingCycle(Long id) {
        logger.debug("Deleting training cycle: {}", id);
        return trainingCycleDao.deleteById(id);
    }
    
    @Override
    public long countTrainingCycles() {
        logger.debug("Counting all training cycles");
        return trainingCycleDao.count();
    }
    
    @Override
    public List<TrainingCycle> findActiveTrainingCycles() {
        logger.debug("Finding all active training cycles");
        return trainingCycleDao.findActive();
    }
    
    @Override
    public Optional<TrainingCycle> findTrainingCycleById(Long id) {
        logger.debug("Finding training cycle by ID: {}", id);
        return trainingCycleDao.findById(id);
    }
    
    @Override
    public List<TrainingCycle> findAllTrainingCycles() {
        logger.debug("Finding all training cycles (findAllTrainingCycles alias)");
        return findAll();
    }
    
    @Override
    public List<TrainingCycle> findByActive(boolean active, int offset, int limit) {
        logger.debug("Finding training cycles by active status: active={}, offset={}, limit={}", active, offset, limit);
        // Ensure offset is not negative
        offset = Math.max(0, offset);
        if (active) {
            return trainingCycleDao.findActive(offset, limit);
        } else {
            // Предполагаем, что у нас нет специального метода для неактивных циклов
            // Поэтому реализуем через фильтрацию всех циклов
            return trainingCycleDao.findAll(offset, limit).stream()
                    .filter(cycle -> !cycle.isActive())
                    .toList();
        }
    }
    
    @Override
    public long countByActive(boolean active) {
        logger.debug("Counting training cycles by active status: active={}", active);
        if (active) {
            return trainingCycleDao.findActive().size();
        } else {
            // Аналогично, фильтруем все циклы
            return trainingCycleDao.findAll().stream()
                    .filter(cycle -> !cycle.isActive())
                    .count();
        }
    }
    
    @Override
    public List<TrainingCycle> search(String query, int offset, int limit) {
        logger.debug("Searching training cycles: query={}, offset={}, limit={}", query, offset, limit);
        // Ensure offset is not negative
        offset = Math.max(0, offset);
        // Простая реализация поиска - фильтруем по имени и описанию
        // В реальном приложении здесь стоит использовать полноценный поиск в базе данных
        String lowerQuery = query.toLowerCase();
        return trainingCycleDao.findAll().stream()
                .filter(cycle -> 
                    (cycle.getName() != null && cycle.getName().toLowerCase().contains(lowerQuery)) ||
                    (cycle.getDescription() != null && cycle.getDescription().toLowerCase().contains(lowerQuery))
                )
                .skip(offset)
                .limit(limit)
                .toList();
    }
    
    @Override
    public long countBySearch(String query) {
        logger.debug("Counting training cycles by search: query={}", query);
        // Аналогично поиску, фильтруем и считаем
        String lowerQuery = query.toLowerCase();
        return trainingCycleDao.findAll().stream()
                .filter(cycle -> 
                    (cycle.getName() != null && cycle.getName().toLowerCase().contains(lowerQuery)) ||
                    (cycle.getDescription() != null && cycle.getDescription().toLowerCase().contains(lowerQuery))
                )
                .count();
    }
} 