package com.udacity.jdnd.course3.critter.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerVerification {

    @Autowired
    private CustomerService customerService;

    public Customer verifyCustomer(Long customerId) {
        Customer customer = customerService.findById(customerId);
        return customer;
    }
}
