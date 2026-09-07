package com.example.Spring_Coffee_Shop_Project.controller;

import com.example.Spring_Coffee_Shop_Project.constant.CommonResponse;
import com.example.Spring_Coffee_Shop_Project.dto.CustomerDTO;
import com.example.Spring_Coffee_Shop_Project.enumeration.CustomerStatus;
import com.example.Spring_Coffee_Shop_Project.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CommonResponse> saveCustomer(@RequestBody CustomerDTO customerDTO) {
        customerService.saveCustomer(customerDTO);
        return ResponseEntity.ok(new CommonResponse(200, "Customer created successfully"));
    }

    @PutMapping
    public ResponseEntity<CommonResponse> updateCustomer(@RequestBody CustomerDTO customerDTO) {
        customerService.updateCustomer(customerDTO);
        return ResponseEntity.ok(new CommonResponse(200, "Customer updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse> deleteCustomer(@PathVariable long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(new CommonResponse(200, "Customer status updated to INACTIVE successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/statuses")
    public ResponseEntity<CustomerStatus[]> getCustomerStatuses() {
        return ResponseEntity.ok(CustomerStatus.values());
    }

    @GetMapping("/search/name")
    public List<CustomerDTO> searchByName(@RequestParam String query) {
        return customerService.searchCustomersByName(query);
    }

    @GetMapping("/search/phone")
    public List<CustomerDTO> searchByPhone(@RequestParam String phone) {
        return customerService.searchCustomersByPhone(phone);
    }
}