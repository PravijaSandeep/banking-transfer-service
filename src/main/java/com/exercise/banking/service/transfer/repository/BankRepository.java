package com.exercise.banking.service.transfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.exercise.banking.service.transfer.model.Bank;

@Repository
public interface BankRepository extends JpaRepository<Bank,String>{

}
