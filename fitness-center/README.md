# Fitness Center Web Application - User Flow Guide

## Overview

The Fitness Center web application is a comprehensive platform for managing fitness training programs. It supports three main user roles:

1. **Client** - Purchases training programs, receives and manages assignments from trainers, and provides reviews
2. **Trainer** - Creates and manages personalized training plans for clients
3. **Administrator** - Manages users, discounts, and training cycles

This document explains the typical user flow for each role.

## Client Flow

### Registration and Login
1. Navigate to the home page
2. Click "Register" in the navigation bar
3. Fill in the registration form with personal details
4. Submit the form to create an account
5. Log in with the newly created username and password

### Viewing and Purchasing Training Programs
1. After login, navigate to "Orders" in the navigation bar
2. Click the "Purchase Training Program" button
3. Browse the available training cycles
4. Select a program of interest to view details
5. Click "Select" to choose this program
6. Review the order preview page, which shows:
   - Program details
   - Original price
   - Applied discounts (based on completed cycles or account type)
   - Final price
7. Click "Proceed to Payment"
8. Enter payment details on the payment page
9. Confirm payment to complete the purchase
10. View the order confirmation page

### Managing Orders and Assignments
1. Navigate to "Orders" to see all current and past orders
2. Click on an order to view its details
3. For pending orders, options include:
   - Pay now
   - Cancel order
4. For paid orders, view assigned trainer information
5. Navigate to "Assignments" to view all training assignments
6. Click on an assignment to see details:
   - Exercise program
   - Equipment recommendations
   - Dietary recommendations
7. For each assignment, options include:
   - Request changes
   - Decline specific parts
   - Mark as completed

### Providing Reviews
1. Navigate to "Orders"
2. Click on a completed order
3. If not yet reviewed, a review form will be available
4. Rate the program (1-5 stars)
5. Add comments
6. Submit the review

## Trainer Flow

### Login
1. Log in with trainer credentials provided by the administrator

### Managing Clients and Orders
1. After login, view dashboard showing:
   - Number of assigned clients
   - Pending assignments
   - Completed assignments
2. Navigate to "Orders" to see all assigned orders
3. Click on an order to view details

### Creating and Managing Assignments
1. From order details page, click "Create Assignment"
2. Fill in the assignment form:
   - Exercise program details
   - Equipment recommendations
   - Dietary recommendations
   - Training schedule
3. Submit to create the assignment
4. Navigate to "Assignments" to view all created assignments
5. Update assignments as needed:
   - Modify existing details
   - Respond to client change requests
   - Mark assignments as completed

## Administrator Flow

### Login
1. Log in with admin credentials

### Managing Users
1. Navigate to "Admin" → "Users"
2. View all registered users
3. Actions include:
   - Create new users (including trainers)
   - Edit user details
   - Deactivate/reactivate users
   - Assign roles to users

### Managing Training Cycles
1. Navigate to "Admin" → "Training Cycles"
2. View all training programs
3. Actions include:
   - Create new training cycles
   - Edit existing cycles
   - Deactivate/reactivate cycles

### Managing Orders
1. Navigate to "Admin" → "Orders" or just "Orders"
2. View all orders across the system
3. Actions include:
   - Mark orders as paid (for manual payments)
   - Assign trainers to paid orders
   - Mark orders as completed
   - Cancel orders if needed

### Configuring Discounts
1. Navigate to "Admin" → "Account Types"
2. Create or modify account types with specific discount percentages
3. Navigate to "Admin" → "Settings" to configure:
   - Loyalty discounts based on completed cycles
   - Special promotional discounts

## Key Features

### Session Management
- Secure session tracking across the application
- Session timeout after extended inactivity
- Session regeneration for security

### Discount System
- Automatic discounts for loyal clients based on completed training cycles
- Special discounts for corporate accounts
- Discount stack protection to prevent excessive discounting

### Assignment Management
- Detailed training plans
- Equipment recommendations
- Dietary guidance
- Progress tracking

### Review System
- Rating system for completed orders
- Feedback collection for program improvement

## Technical Notes

- The application uses a Java Spring MVC architecture
- Thymeleaf templates for the frontend
- JDBC for database interaction
- Log4j2 for comprehensive logging
- BCrypt for secure password handling 