-- Create salary_structures table
CREATE TABLE salary_structures (
    structure_id VARCHAR(36) PRIMARY KEY,
    employee_id VARCHAR(36) NOT NULL,
    base_salary DECIMAL(12,2) NOT NULL,
    bonus_details JSON,
    effective_from DATE NOT NULL,
    effective_to DATE NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_salary_structures_employee_id ON salary_structures(employee_id);
CREATE INDEX idx_salary_structures_effective_dates ON salary_structures(effective_from, effective_to);

-- Add constraint to ensure effective_from is before effective_to
ALTER TABLE salary_structures 
ADD CONSTRAINT chk_salary_effective_dates 
CHECK (effective_to IS NULL OR effective_from <= effective_to);
