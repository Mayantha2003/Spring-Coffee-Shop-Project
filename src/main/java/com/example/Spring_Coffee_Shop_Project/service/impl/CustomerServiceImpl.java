package com.example.Spring_Coffee_Shop_Project.service.impl;

import com.example.Spring_Coffee_Shop_Project.dto.CustomerDTO;
import com.example.Spring_Coffee_Shop_Project.dto.LoyaltyPointDTO;
import com.example.Spring_Coffee_Shop_Project.entity.Customer;
import com.example.Spring_Coffee_Shop_Project.entity.LoyaltyPoint;
import com.example.Spring_Coffee_Shop_Project.enumeration.CustomerStatus;
import com.example.Spring_Coffee_Shop_Project.exception.CustomerException;
import com.example.Spring_Coffee_Shop_Project.repository.CustomerRepository;
import com.example.Spring_Coffee_Shop_Project.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public void saveCustomer(CustomerDTO dto) {
        log.info("Executing Save Customer method...");

        if (dto.getPhone() == null || dto.getPhone().isBlank()) {
            throw new CustomerException(400, "Phone number is required");
        }
        if (customerRepository.existsByPhone(dto.getPhone().trim())) {
            throw new CustomerException(409, "A customer with this phone already exists");
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()
                && customerRepository.existsByEmail(dto.getEmail().trim())) {
            throw new CustomerException(409, "A customer with this email already exists");
        }

        Customer customer = mapToEntity(dto);
        customer.setRegisteredAt(LocalDateTime.now());

        LoyaltyPoint loyaltyPoint = new LoyaltyPoint();
        loyaltyPoint.setAvailablePoints(0);
        loyaltyPoint.setTotalEarned(0);
        loyaltyPoint.setTotalRedeemed(0);
        loyaltyPoint.setCustomer(customer);
        customer.setLoyaltyPoint(loyaltyPoint);

        Customer saved = customerRepository.save(customer);
        log.info("Customer saved successfully with id: {}", saved.getCustomerId());
    }

    @Override
    @Transactional
    public void updateCustomer(CustomerDTO dto) {
        log.info("Executing Update Customer ID: {}", dto.getCustomerId());

        Optional<Customer> optional = customerRepository.findById(dto.getCustomerId());
        if (optional.isEmpty()) {
            throw new CustomerException(404, "Customer not found with id: " + dto.getCustomerId());
        }

        if (dto.getPhone() != null && !dto.getPhone().isBlank()
                && customerRepository.existsByPhoneAndCustomerIdNot(dto.getPhone().trim(), dto.getCustomerId())) {
            throw new CustomerException(409, "A customer with this phone already exists");
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()
                && customerRepository.existsByEmailAndCustomerIdNot(dto.getEmail().trim(), dto.getCustomerId())) {
            throw new CustomerException(409, "A customer with this email already exists");
        }

        Customer customer = optional.get();
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : customer.getPhone());
        customer.setEmail(dto.getEmail() != null && !dto.getEmail().isBlank() ? dto.getEmail().trim() : null);
        customer.setAddress(dto.getAddress());
        customer.setCustomerStatus(
                dto.getCustomerStatus() != null ? dto.getCustomerStatus() : CustomerStatus.ACTIVE
        );
        customer.setVerified(dto.isVerified());

        customerRepository.save(customer);
        log.info("Customer updated successfully with id: {}", customer.getCustomerId());
    }

    @Override
    @Transactional
    public void deleteCustomer(long id) {
        log.info("Executing Soft Delete for Customer ID: {}", id);

        Optional<Customer> optional = customerRepository.findById(id);
        if (optional.isEmpty()) {
            throw new CustomerException(404, "Customer not found with id: " + id);
        }

        Customer customer = optional.get();
        customer.setCustomerStatus(CustomerStatus.INACTIVE);
        customerRepository.save(customer);

        log.info("Customer marked as INACTIVE for id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(long id) {
        log.info("Fetching customer by ID: {}", id);

        Optional<Customer> optional = customerRepository.findById(id);
        if (optional.isEmpty()) {
            throw new CustomerException(404, "Customer not found with id: " + id);
        }
        return mapToDTO(optional.get());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers() {
        log.info("Fetching all customers...");

        List<Customer> customers = customerRepository.findAll();
        List<CustomerDTO> list = new ArrayList<>();
        for (Customer c : customers) {
            list.add(mapToDTO(c));
        }
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> searchCustomersByName(String query) {
        log.info("Searching customers by name prefix: {}", query);

        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String prefix = query.trim();
        List<Customer> customers = customerRepository
                .findByFirstNameStartingWithIgnoreCaseOrLastNameStartingWithIgnoreCase(prefix, prefix);

        List<CustomerDTO> result = new ArrayList<>();
        for (Customer c : customers) {
            result.add(mapToDTO(c));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> searchCustomersByPhone(String phone) {
        log.info("Searching customers by phone: {}", phone);

        if (phone == null || phone.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String cleaned = phone.trim().replaceAll("\\s+", "");
        List<Customer> customers = customerRepository.findByPhoneContaining(cleaned);

        List<CustomerDTO> result = new ArrayList<>();
        for (Customer c : customers) {
            result.add(mapToDTO(c));
        }
        return result;
    }

    private CustomerDTO mapToDTO(Customer customer) {
        LoyaltyPoint lp = customer.getLoyaltyPoint();
        LoyaltyPointDTO lpDto = null;
        if (lp != null) {
            lpDto = LoyaltyPointDTO.builder()
                    .loyaltyPointId(lp.getLoyaltyPointId())
                    .availablePoints(lp.getAvailablePoints())
                    .totalEarned(lp.getTotalEarned())
                    .totalRedeemed(lp.getTotalRedeemed())
                    .build();
        }

        return CustomerDTO.builder()
                .customerId(customer.getCustomerId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .customerStatus(customer.getCustomerStatus())
                .verified(customer.isVerified())
                .registeredAt(customer.getRegisteredAt())
                .loyaltyPoint(lpDto)
                .build();
    }

    private Customer mapToEntity(CustomerDTO dto) {
        Customer customer = new Customer();
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);
        customer.setEmail(dto.getEmail() != null && !dto.getEmail().isBlank() ? dto.getEmail().trim() : null);
        customer.setAddress(dto.getAddress());
        customer.setCustomerStatus(
                dto.getCustomerStatus() != null ? dto.getCustomerStatus() : CustomerStatus.ACTIVE
        );
        customer.setVerified(dto.isVerified());
        return customer;
    }
}