package com.example.Spring_Coffee_Shop_Project.service;

import com.example.Spring_Coffee_Shop_Project.dto.CustomerDTO;

import java.util.List;

public interface CustomerService {

    void saveCustomer(CustomerDTO customerDTO);

    void updateCustomer(CustomerDTO customerDTO);

    void deleteCustomer(long id);

    CustomerDTO getCustomerById(long id);

    List<CustomerDTO> getAllCustomers();

    List<CustomerDTO> searchCustomersByName(String query);

    List<CustomerDTO> searchCustomersByPhone(String phone);
}