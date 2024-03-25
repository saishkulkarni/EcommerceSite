package com.s13sh.myshop.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.s13sh.myshop.dto.Product;
import com.s13sh.myshop.service.AdminService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	AdminService adminService;

	@GetMapping
	public String loadDashboard(HttpSession session,HttpServletResponse response) throws IOException {
		return adminService.loadDashboard(session, response);
	}

	@GetMapping("/add-product")
	public String loadAddProdcut(HttpSession session, ModelMap map,HttpServletResponse response) throws IOException {
		return adminService.loadAddProduct(session, map, response);
	}

	@PostMapping("/add-product")
	public String addProdcut(@Valid Product product, BindingResult result, @RequestParam MultipartFile picture,
			HttpSession session, ModelMap map,HttpServletResponse response) throws IOException {
		return adminService.addProduct(product, result, picture, session, map, response);
	}

	@PostMapping("/update-product")
	public String updateProdcut(@Valid Product product, BindingResult result, @RequestParam MultipartFile picture,
			HttpSession session, ModelMap map,HttpServletResponse response) throws IOException {
		return adminService.updateProduct(product, result, picture, session, map, response);
	}

	@GetMapping("/manage-products")
	public String manageProducts(HttpSession session, ModelMap map,HttpServletResponse response) throws IOException {
		return adminService.manageproducts(session, map, response);
	}

	@GetMapping("/delete/{id}")
	public String deleteProduct(@PathVariable int id, HttpSession session,HttpServletResponse response) throws IOException {
		return adminService.deleteProduct(id, session, response);
	}

	@GetMapping("/edit/{id}")
	public String editProduct(@PathVariable int id, HttpSession session, ModelMap map,HttpServletResponse response) throws IOException {
		return adminService.editProduct(id, session, map, response);
	}

	@GetMapping("/create-admin/{email}/{password}")
	public String createAdmin(@PathVariable String email, @PathVariable String password,HttpSession session,HttpServletResponse response) throws IOException{
		return adminService.createAdmin(email,password,session, response);
	}

}
