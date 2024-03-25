package com.s13sh.myshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.s13sh.myshop.dto.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

	boolean existsByName(String name);

	Product findByName(String name);

}
