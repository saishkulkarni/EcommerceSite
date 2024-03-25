package com.s13sh.myshop.service;

import java.io.IOException;

import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.s13sh.myshop.dto.Product;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

public interface AdminService {

    String loadDashboard(HttpSession session, HttpServletResponse response) throws IOException;

    String loadAddProduct(HttpSession session, ModelMap map, HttpServletResponse response) throws IOException;

    String addProduct(Product product, BindingResult result, MultipartFile picture, HttpSession session, ModelMap map,
            HttpServletResponse response) throws IOException;

    String manageproducts(HttpSession session, ModelMap map, HttpServletResponse response) throws IOException;

    String deleteProduct(int id, HttpSession session, HttpServletResponse response) throws IOException;

    String editProduct(int id, HttpSession session, ModelMap map, HttpServletResponse response) throws IOException;

    String updateProduct(@Valid Product product, BindingResult result, MultipartFile picture, HttpSession session,
            ModelMap map, HttpServletResponse response) throws IOException;

    String createAdmin(String email, String password, HttpSession session, HttpServletResponse response)
            throws IOException;

}
