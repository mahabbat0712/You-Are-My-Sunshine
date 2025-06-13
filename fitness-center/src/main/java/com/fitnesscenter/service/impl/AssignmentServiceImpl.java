package com.fitnesscenter.service.impl;

import com.fitnesscenter.dao.AssignmentDao;
import com.fitnesscenter.model.Assignment;
import com.fitnesscenter.service.AssignmentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the AssignmentService interface
 */
@Service
public class AssignmentServiceImpl implements AssignmentService {
    
    private static final Logger logger = LogManager.getLogger(AssignmentServiceImpl.class);
    
    private final AssignmentDao assignmentDao;
    
    @Autowired
    public AssignmentServiceImpl(AssignmentDao assignmentDao) {
        this.assignmentDao = assignmentDao;
        logger.info("AssignmentServiceImpl initialized");
    }
    
    @Override
    public Optional<Assignment> findAssignmentById(Long id) {
        logger.debug("Finding assignment by ID: {}", id);
        return assignmentDao.findById(id);
    }
    
    @Override
    public List<Assignment> findAllAssignments() {
        logger.debug("Finding all assignments");
        return assignmentDao.findAll();
    }
    
    @Override
    public List<Assignment> findAssignmentsByTrainer(Long trainerId) {
        logger.debug("Finding assignments by trainer ID: {}", trainerId);
        return assignmentDao.findByTrainerId(trainerId);
    }
    
    @Override
    public List<Assignment> findAssignmentsByClient(Long clientId) {
        logger.debug("Finding assignments by client ID: {}", clientId);
        return assignmentDao.findByClientId(clientId);
    }
    
    @Override
    public List<Assignment> findAssignmentsByOrder(Long orderId) {
        logger.debug("Finding assignments by order ID: {}", orderId);
        return assignmentDao.findByOrderId(orderId);
    }
    
    @Override
    public Assignment createAssignment(Assignment assignment) {
        logger.debug("Creating assignment: {}", assignment);
        
        // Ensure created and updated timestamps are set
        if (assignment.getCreatedAt() == null) {
            assignment.setCreatedAt(LocalDateTime.now());
        }
        
        if (assignment.getUpdatedAt() == null) {
            assignment.setUpdatedAt(LocalDateTime.now());
        }
        
        // Ensure status is set
        if (assignment.getStatus() == null || assignment.getStatus().isEmpty()) {
            assignment.setStatus("ASSIGNED");
        }
        
        return assignmentDao.save(assignment);
    }
    
    @Override
    public Assignment saveAssignment(Assignment assignment) {
        logger.debug("Saving assignment: {}", assignment);
        
        try {
            logger.info("Preparing to save assignment: {}", assignment);
            Assignment savedAssignment = createAssignment(assignment);
            
            if (savedAssignment != null) {
                logger.info("Assignment saved successfully with ID: {}", savedAssignment.getId());
            } else {
                logger.error("Failed to save assignment - returned null");
            }
            
            return savedAssignment;
        } catch (Exception e) {
            logger.error("Exception occurred while saving assignment", e);
            throw e;
        }
    }
    
    @Override
    public Assignment updateAssignment(Assignment assignment) {
        logger.debug("Updating assignment: {}", assignment);
        
        // Update timestamp
        assignment.setUpdatedAt(LocalDateTime.now());
        
        boolean updated = assignmentDao.update(assignment);
        if (updated) {
            return assignment;
        } else {
            logger.warn("Failed to update assignment: {}", assignment.getId());
            return null;
        }
    }
    
    @Override
    public boolean updateAssignmentStatus(Long id, String status) {
        logger.debug("Updating assignment status: id={}, status={}", id, status);
        
        Optional<Assignment> assignmentOpt = findAssignmentById(id);
        if (!assignmentOpt.isPresent()) {
            logger.warn("Assignment not found for status update: {}", id);
            return false;
        }
        
        Assignment assignment = assignmentOpt.get();
        assignment.setStatus(status);
        assignment.setUpdatedAt(LocalDateTime.now());
        
        return assignmentDao.update(assignment);
    }
    
    @Override
    public boolean deleteAssignment(Long id) {
        logger.debug("Deleting assignment by ID: {}", id);
        return assignmentDao.delete(id);
    }
} 