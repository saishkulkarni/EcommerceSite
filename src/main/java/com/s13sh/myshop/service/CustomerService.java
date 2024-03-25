package com.s13sh.myshop.service;

import java.io.IOException;

import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import com.s13sh.myshop.dto.Customer;

import ch.qos.logback.core.model.Model;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public interface CustomerService {
	String save(Customer customer, BindingResult result,HttpServletResponse response,ModelMap map)throws IOException;

	String verifyOtp(int id, int otp, ModelMap map, HttpSession session,HttpServletResponse response)throws IOException;

	String sendOtp(int id, ModelMap map);

	String resendOtp(int id, ModelMap map);

	String login(String email, String password, ModelMap map, HttpSession session,HttpServletResponse response)throws IOException;

    String viewProducts(HttpSession session, ModelMap map,HttpServletResponse response)throws IOException;

    String addToCart(int id, HttpSession session,HttpServletResponse response)throws IOException;

	String viewCart(ModelMap map, HttpSession session,HttpServletResponse response)throws IOException;

	String removeFromCart(int id, HttpSession session,HttpServletResponse response)throws IOException;

	String paymentPage(HttpSession session, ModelMap map,HttpServletResponse response)throws IOException;

	String confirmOrder(HttpSession session, int id, String razorpay_payment_id,HttpServletResponse response)throws IOException;

	String viewOrders(HttpSession session, ModelMap map,HttpServletResponse response)throws IOException;
}
