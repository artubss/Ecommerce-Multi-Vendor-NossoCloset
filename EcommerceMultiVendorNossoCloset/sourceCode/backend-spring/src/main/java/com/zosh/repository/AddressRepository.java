package com.nossocloset.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nossocloset.model.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {

}
