package com.s13sh.myshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.s13sh.myshop.dto.ShoppingOrder;

public interface ShoppingOrderRepository extends JpaRepository<ShoppingOrder, Integer> {

}
