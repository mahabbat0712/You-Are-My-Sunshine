package com.fitnesscenter.dao.impl;

import com.fitnesscenter.dao.AbstractDao;
import com.fitnesscenter.dao.AccountTypeDao;
import com.fitnesscenter.dao.DiscountRuleDao;
import com.fitnesscenter.model.AccountType;
import com.fitnesscenter.model.DiscountRule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of DiscountRuleDao interface using JDBC
 */
@Repository
public class DiscountRuleDaoImpl extends AbstractDao<DiscountRule, Integer> implements DiscountRuleDao {
    
    private static final Logger logger = LogManager.getLogger(DiscountRuleDaoImpl.class);
    
    private final AccountTypeDao accountTypeDao;
    
    @Autowired
    public DiscountRuleDaoImpl(AccountTypeDao accountTypeDao) {
        this.accountTypeDao = accountTypeDao;
    }
    
    private static final String FIND_BY_ID_SQL = 
            "SELECT id, name, account_type_id, min_completed_cycles, discount_percentage, active " +
            "FROM discount_rules WHERE id = ?";
    
    private static final String FIND_ALL_SQL = 
            "SELECT id, name, account_type_id, min_completed_cycles, discount_percentage, active " +
            "FROM discount_rules ORDER BY account_type_id, min_completed_cycles DESC";
    
    private static final String FIND_ALL_PAGINATED_SQL = 
            "SELECT id, name, account_type_id, min_completed_cycles, discount_percentage, active " +
            "FROM discount_rules ORDER BY account_type_id, min_completed_cycles DESC LIMIT ? OFFSET ?";
    
    private static final String FIND_BY_ACCOUNT_TYPE_SQL = 
            "SELECT id, name, account_type_id, min_completed_cycles, discount_percentage, active " +
            "FROM discount_rules WHERE account_type_id = ? " +
            "ORDER BY min_completed_cycles DESC";
    
    private static final String FIND_ACTIVE_BY_ACCOUNT_TYPE_SQL = 
            "SELECT id, name, account_type_id, min_completed_cycles, discount_percentage, active " +
            "FROM discount_rules WHERE account_type_id = ? AND active = TRUE " +
            "ORDER BY min_completed_cycles DESC";
    
    private static final String FIND_BEST_DISCOUNT_SQL = 
            "SELECT id, name, account_type_id, min_completed_cycles, discount_percentage, active " +
            "FROM discount_rules " +
            "WHERE account_type_id = ? AND min_completed_cycles <= ? AND active = TRUE " +
            "ORDER BY discount_percentage DESC LIMIT 1";
    
    private static final String INSERT_SQL = 
            "INSERT INTO discount_rules (name, account_type_id, min_completed_cycles, discount_percentage, active) " +
            "VALUES (?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
            "UPDATE discount_rules SET name = ?, account_type_id = ?, " +
            "min_completed_cycles = ?, discount_percentage = ?, active = ? " +
            "WHERE id = ?";
    
    private static final String SET_ACTIVE_SQL = 
            "UPDATE discount_rules SET active = ? WHERE id = ?";
    
    private static final String DELETE_SQL = 
            "DELETE FROM discount_rules WHERE id = ?";
    
    private static final String COUNT_SQL = 
            "SELECT COUNT(*) FROM discount_rules";
    
    @Override
    public DiscountRule findBestDiscount(Integer accountTypeId, Integer completedCycles) {
        logger.debug("Finding best discount for account type: {}, completed cycles: {}", 
                accountTypeId, completedCycles);
        
        List<DiscountRule> discounts = findMany(FIND_BEST_DISCOUNT_SQL, 
                accountTypeId, completedCycles);
        
        if (discounts.isEmpty()) {
            logger.debug("No discount found for account type: {}, completed cycles: {}", 
                    accountTypeId, completedCycles);
            return null;
        } else {
            DiscountRule bestDiscount = discounts.get(0);
            logger.debug("Found best discount: {}", bestDiscount);
            return bestDiscount;
        }
    }
    
    @Override
    public List<DiscountRule> findByAccountType(Integer accountTypeId) {
        logger.debug("Finding discount rules by account type: {}", accountTypeId);
        return findMany(FIND_BY_ACCOUNT_TYPE_SQL, accountTypeId);
    }
    
    @Override
    public List<DiscountRule> findActiveByAccountType(Integer accountTypeId) {
        logger.debug("Finding active discount rules by account type: {}", accountTypeId);
        return findMany(FIND_ACTIVE_BY_ACCOUNT_TYPE_SQL, accountTypeId);
    }
    
    @Override
    public boolean setActive(Integer id, boolean active) {
        logger.debug("Setting discount rule active status. ID: {}, active: {}", id, active);
        int rowsAffected = update(SET_ACTIVE_SQL, active, id);
        return rowsAffected > 0;
    }
    
    @Override
    public DiscountRule save(DiscountRule discountRule) {
        if (discountRule.getId() == null) {
            return insert(discountRule);
        } else {
            return update(discountRule);
        }
    }
    
    private DiscountRule insert(DiscountRule discountRule) {
        logger.debug("Inserting new discount rule: {}", discountRule);
        
        Integer accountTypeId = discountRule.getAccountType() != null ? 
                discountRule.getAccountType().getId() : null;
        
        Optional<Long> generatedId = updateAndGetGeneratedKey(INSERT_SQL, 
                discountRule.getName(),
                accountTypeId,
                discountRule.getMinCompletedCycles(),
                discountRule.getDiscountPercentage(),
                discountRule.isActive());
        
        if (generatedId.isPresent()) {
            discountRule.setId(generatedId.get().intValue());
            return discountRule;
        } else {
            throw new RuntimeException("Failed to insert discount rule: " + discountRule);
        }
    }
    
    private DiscountRule update(DiscountRule discountRule) {
        logger.debug("Updating discount rule: {}", discountRule);
        
        Integer accountTypeId = discountRule.getAccountType() != null ? 
                discountRule.getAccountType().getId() : null;
        
        int rowsAffected = update(UPDATE_SQL, 
                discountRule.getName(),
                accountTypeId,
                discountRule.getMinCompletedCycles(),
                discountRule.getDiscountPercentage(),
                discountRule.isActive(),
                discountRule.getId());
        
        if (rowsAffected > 0) {
            return discountRule;
        } else {
            throw new RuntimeException("Failed to update discount rule: " + discountRule);
        }
    }
    
    @Override
    public Optional<DiscountRule> findById(Integer id) {
        logger.debug("Finding discount rule by ID: {}", id);
        return findOne(FIND_BY_ID_SQL, id);
    }
    
    @Override
    public List<DiscountRule> findAll() {
        logger.debug("Finding all discount rules");
        return findMany(FIND_ALL_SQL);
    }
    
    @Override
    public List<DiscountRule> findAll(int offset, int limit) {
        logger.debug("Finding discount rules with pagination: offset={}, limit={}", offset, limit);
        return findMany(FIND_ALL_PAGINATED_SQL, limit, offset);
    }
    
    @Override
    public boolean deleteById(Integer id) {
        logger.debug("Deleting discount rule by ID: {}", id);
        int rowsAffected = update(DELETE_SQL, id);
        return rowsAffected > 0;
    }
    
    @Override
    public long count() {
        logger.debug("Counting all discount rules");
        return count(COUNT_SQL);
    }
    
    @Override
    protected DiscountRule mapRow(ResultSet resultSet) throws SQLException {
        DiscountRule discountRule = new DiscountRule();
        discountRule.setId(resultSet.getInt("id"));
        discountRule.setName(resultSet.getString("name"));
        
        Integer accountTypeId = resultSet.getObject("account_type_id", Integer.class);
        if (accountTypeId != null) {
            accountTypeDao.findById(accountTypeId).ifPresent(discountRule::setAccountType);
        }
        
        discountRule.setMinCompletedCycles(resultSet.getInt("min_completed_cycles"));
        discountRule.setDiscountPercentage(resultSet.getBigDecimal("discount_percentage"));
        discountRule.setActive(resultSet.getBoolean("active"));
        
        return discountRule;
    }
} 