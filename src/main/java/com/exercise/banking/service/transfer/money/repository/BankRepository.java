package com.exercise.banking.service.transfer.money.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.exercise.banking.service.transfer.money.model.Bank;

@Repository
public interface BankRepository extends JpaRepository<Bank,String>{

}
