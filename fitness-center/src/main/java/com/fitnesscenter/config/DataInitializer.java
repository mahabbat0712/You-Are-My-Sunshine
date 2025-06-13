package com.fitnesscenter.config;

import com.fitnesscenter.model.Equipment;
import com.fitnesscenter.service.EquipmentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Component to initialize sample data when the application starts
 */
@Component
public class DataInitializer {
    
    private static final Logger logger = LogManager.getLogger(DataInitializer.class);
    
    private final EquipmentService equipmentService;
    
    @Autowired
    public DataInitializer(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }
    
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("Initializing sample data");
        initEquipment();
    }
    
    private void initEquipment() {
        // Check if we already have equipment
        List<Equipment> existingEquipment = equipmentService.findAllEquipment();
        if (!existingEquipment.isEmpty()) {
            logger.info("Equipment data already exists, skipping initialization");
            return;
        }
        
        logger.info("Initializing equipment data");
        
        // Create sample equipment
        Equipment dumbbell = new Equipment("Dumbbells Set", "Set of various weight dumbbells");
        Equipment treadmill = new Equipment("Treadmill", "Standard treadmill for cardio exercises");
        Equipment yogaMat = new Equipment("Yoga Mat", "Comfortable mat for floor exercises");
        Equipment resistanceBands = new Equipment("Resistance Bands", "Set of resistance bands of different strengths");
        Equipment kettlebell = new Equipment("Kettlebell", "Kettlebell for strength training");
        
        // Save equipment
        equipmentService.createEquipment(dumbbell);
        equipmentService.createEquipment(treadmill);
        equipmentService.createEquipment(yogaMat);
        equipmentService.createEquipment(resistanceBands);
        equipmentService.createEquipment(kettlebell);
        
        logger.info("Equipment data initialized successfully");
    }
} 