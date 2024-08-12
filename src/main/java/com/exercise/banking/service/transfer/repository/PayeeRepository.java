package com.exercise.banking.service.transfer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.exercise.banking.service.transfer.model.Payee;

@Repository
public interface PayeeRepository extends JpaRepository<Payee,Long>{

	Optional<Payee> findByPayerAccount_AccNumAndAccNum(String payerAccountNum, String payeeAccountNum);

}
