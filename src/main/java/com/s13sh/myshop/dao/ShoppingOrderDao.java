package com.s13sh.myshop.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.s13sh.myshop.dto.ShoppingOrder;
import com.s13sh.myshop.repository.ShoppingOrderRepository;

@Repository
public class ShoppingOrderDao {

	@Autowired
	ShoppingOrderRepository orderRepository;

	public void saveOrder(ShoppingOrder order) {
		orderRepository.save(order);
	}

	public ShoppingOrder findOrderById(int id) {
		return orderRepository.findById(id).orElseThrow();
	}
}
