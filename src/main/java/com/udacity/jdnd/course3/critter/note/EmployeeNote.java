package com.udacity.jdnd.course3.critter.note;

import com.udacity.jdnd.course3.critter.user.Employee;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class EmployeeNote extends Note {

    @ManyToOne(targetEntity = Employee.class, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public EmployeeNote validate() {
        super.validate();
        return this;
    }
}
