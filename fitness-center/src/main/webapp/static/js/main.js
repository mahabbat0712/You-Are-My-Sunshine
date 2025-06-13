/**
 * Main JavaScript file for Fitness Center application
 */

// Wait for DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
    // Initialize Bootstrap tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Initialize Bootstrap popovers
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    var popoverList = popoverTriggerList.map(function(popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
    
    // Auto-hide alerts after 5 seconds
    setTimeout(function() {
        var alerts = document.querySelectorAll('.alert');
        alerts.forEach(function(alert) {
            var bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    }, 5000);
    
    // Form validation
    var forms = document.querySelectorAll('.needs-validation');
    Array.prototype.slice.call(forms).forEach(function(form) {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });
    
    // Password strength meter
    var passwordInput = document.getElementById('password');
    if (passwordInput) {
        passwordInput.addEventListener('input', function() {
            var password = passwordInput.value;
            var strength = 0;
            
            // Length check
            if (password.length >= 8) {
                strength += 1;
            }
            
            // Contains lowercase
            if (password.match(/[a-z]+/)) {
                strength += 1;
            }
            
            // Contains uppercase
            if (password.match(/[A-Z]+/)) {
                strength += 1;
            }
            
            // Contains number
            if (password.match(/[0-9]+/)) {
                strength += 1;
            }
            
            // Contains special character
            if (password.match(/[$@#&!]+/)) {
                strength += 1;
            }
            
            var strengthMeter = document.getElementById('password-strength');
            if (strengthMeter) {
                // Update strength meter
                strengthMeter.className = 'progress-bar';
                strengthMeter.style.width = (strength * 20) + '%';
                
                if (strength < 2) {
                    strengthMeter.classList.add('bg-danger');
                    strengthMeter.textContent = 'Weak';
                } else if (strength < 4) {
                    strengthMeter.classList.add('bg-warning');
                    strengthMeter.textContent = 'Medium';
                } else {
                    strengthMeter.classList.add('bg-success');
                    strengthMeter.textContent = 'Strong';
                }
            }
        });
    }
    
    // Toggle show all training cycles
    var showAllSwitch = document.getElementById('showAllSwitch');
    if (showAllSwitch) {
        showAllSwitch.addEventListener('change', function() {
            var showAll = showAllSwitch.checked;
            window.location.href = '/training-cycles?showAll=' + showAll;
        });
    }
    
    // Confirmation dialogs
    var confirmButtons = document.querySelectorAll('[data-confirm]');
    confirmButtons.forEach(function(button) {
        button.addEventListener('click', function(event) {
            if (!confirm(button.getAttribute('data-confirm'))) {
                event.preventDefault();
            }
        });
    });
}); 