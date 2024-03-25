package com.s13sh.myshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.s13sh.myshop.dto.Item;

public interface ItemRepository extends JpaRepository<Item, Integer>
{

}
