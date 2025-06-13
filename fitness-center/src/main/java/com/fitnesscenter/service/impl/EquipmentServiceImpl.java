package com.fitnesscenter.service.impl;

import com.fitnesscenter.dao.EquipmentDao;
import com.fitnesscenter.model.Equipment;
import com.fitnesscenter.service.EquipmentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of the EquipmentService interface
 */
@Service
public class EquipmentServiceImpl implements EquipmentService {
    
    private static final Logger logger = LogManager.getLogger(EquipmentServiceImpl.class);
    
    private final EquipmentDao equipmentDao;
    
    @Autowired
    public EquipmentServiceImpl(EquipmentDao equipmentDao) {
        this.equipmentDao = equipmentDao;
        logger.info("EquipmentServiceImpl initialized");
    }
    
    @Override
    public List<Equipment> findAllEquipment() {
        logger.debug("Finding all equipment");
        return equipmentDao.findAll();
    }
    
    @Override
    public Equipment findEquipmentById(Long id) {
        logger.debug("Finding equipment by ID: {}", id);
        return equipmentDao.findById(id).orElse(null);
    }
    
    @Override
    public Set<Equipment> findEquipmentByIds(List<Long> ids) {
        logger.debug("Finding equipment by IDs: {}", ids);
        return equipmentDao.findByIds(ids);
    }
    
    @Override
    public Equipment createEquipment(Equipment equipment) {
        logger.debug("Creating equipment: {}", equipment);
        return equipmentDao.save(equipment);
    }
    
    @Override
    public Equipment updateEquipment(Equipment equipment) {
        logger.debug("Updating equipment: {}", equipment);
        boolean updated = equipmentDao.update(equipment);
        return updated ? equipment : null;
    }
    
    @Override
    public boolean deleteEquipment(Long id) {
        logger.debug("Deleting equipment by ID: {}", id);
        return equipmentDao.delete(id);
    }
} 