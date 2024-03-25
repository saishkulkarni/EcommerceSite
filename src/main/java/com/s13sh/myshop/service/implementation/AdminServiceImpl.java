package com.s13sh.myshop.service.implementation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.s13sh.myshop.dao.CustomerDao;
import com.s13sh.myshop.dao.ProductDao;
import com.s13sh.myshop.dto.Customer;
import com.s13sh.myshop.dto.Product;
import com.s13sh.myshop.helper.AES;
import com.s13sh.myshop.service.AdminService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Service
public class AdminServiceImpl implements AdminService {

	@Autowired
	Product product;

	@Autowired
	ProductDao productDao;

	@Autowired
	CustomerDao customerDao;

	@Override
	public String loadDashboard(HttpSession session,HttpServletResponse response) throws IOException {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer == null) {
			session.setAttribute("failMessage", "Invalid Session");
			response.sendRedirect("/signin");
			return "null";
		} else {
			if (customer.getRole().equals("ADMIN"))
				return "AdminDashBoard";
			else {
				session.setAttribute("failMessage", "You are Unauthorized to access his URL");
				response.sendRedirect("/");
				return "null";
			}
		}
	}

	@Override
	public String loadAddProduct(HttpSession session, ModelMap map,HttpServletResponse response) throws IOException {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer == null) {
			session.setAttribute("failMessage", "Invalid Session");
			response.sendRedirect("/signin");
			return null;
		} else {
			if (customer.getRole().equals("ADMIN")) {
				map.put("product", product);
				return "AddProduct";
			} else {
				session.setAttribute("failMessage", "You are Unauthorized to access his URL");
				response.sendRedirect("/");
				return null;
			}
		}
	}

	@Override
	public String addProduct(Product product, BindingResult result, MultipartFile picture, HttpSession session,
			ModelMap map,HttpServletResponse response) throws IOException {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer == null) {
			session.setAttribute("failMessage", "Invalid Session");
			response.sendRedirect("/signin");
			return null;
		} else {
			if (customer.getRole().equals("ADMIN")) {
				if (productDao.checkName(product.getName()))
					result.rejectValue("name", "error.name", "Product with Same Name Already Exists");

				if (result.hasErrors())
					return "AddProduct";
				else {
					// try {
					// byte[] image = new byte[picture.getInputStream().available()];
					// picture.getInputStream().read(image);
					//
					// product.setImage(image);
					product.setImagePath("/images/" + product.getName() + ".jpg");
					productDao.save(product);

					File file = new File("src/main/resources/static/images");
					if (!file.isDirectory())
						file.mkdir();

					try {
						Files.write(Paths.get("src/main/resources/static/images", product.getName() + ".jpg"),
								picture.getBytes());
					} catch (IOException e) {
						session.setAttribute("failMessage", "You are Unauthorized to access his URL");
						response.sendRedirect("/");
						return null;
					}
					session.setAttribute("successMessage", "Product Added Success");
					response.sendRedirect("/admin");
					return null;

					// } catch (IOException e) {
					// session.setAttribute("failMessage", "You are Unauthorized to access his
					// URL");
					// return "redirect:/";
					// }
				}
			} else {
				session.setAttribute("failMessage", "You are Unauthorized to access his URL");
				response.sendRedirect("/");
				return null;
			}
		}
	}

	@Override
	public String manageproducts(HttpSession session, ModelMap map,HttpServletResponse response) throws IOException {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer == null) {
			session.setAttribute("failMessage", "Invalid Session");
			response.sendRedirect("/signin");
			return null;
		} else {
			if (customer.getRole().equals("ADMIN")) {
				List<Product> products = productDao.fetchAll();
				if (products.isEmpty()) {
					session.setAttribute("failMessage", "No Products Present");
					response.sendRedirect("/admin");
					return null;
				} else {
					map.put("products", products);
					return "ManageProducts";
				}
			} else {
				session.setAttribute("failMessage", "You are Unauthorized to access his URL");
				response.sendRedirect("/");
				return null;
			}
		}
	}

	@Override
	public String deleteProduct(int id, HttpSession session,HttpServletResponse response)throws IOException {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer == null) {
			session.setAttribute("failMessage", "Invalid Session");
			response.sendRedirect("/signin");
			return null;
		} else {
			if (customer.getRole().equals("ADMIN")) {
				Product product = productDao.findById(id);
				File file = new File("src/main/resources/static" + product.getImagePath());
				if (file.exists())
					file.delete();
				productDao.delete(product);
				session.setAttribute("successMessage", "Product Deleted Success");
				response.sendRedirect("/admin/manage-products");
				return null;
			} else {
				session.setAttribute("failMessage", "You are Unauthorized to access his URL");
				response.sendRedirect("/");
				return null;
			}
		}
	}

	@Override
	public String editProduct(int id, HttpSession session, ModelMap map,HttpServletResponse response) throws IOException{
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer == null) {
			session.setAttribute("failMessage", "Invalid Session");
			response.sendRedirect("/signin");
			return null;
		} else {
			if (customer.getRole().equals("ADMIN")) {
				Product product = productDao.findById(id);
				map.put("product", product);
				return "EditProduct";
			} else {
				session.setAttribute("failMessage", "You are Unauthorized to access his URL");
				response.sendRedirect("/");
				return null;
			}
		}

	}

	@Override
	public String updateProduct(@Valid Product product, BindingResult result, MultipartFile picture,
			HttpSession session, ModelMap map,HttpServletResponse response) throws IOException{
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer == null) {
			session.setAttribute("failMessage", "Invalid Session");
			response.sendRedirect("/signin");
			return null;
		} else {
			if (customer.getRole().equals("ADMIN")) {

				if (result.hasErrors())
					return "EditProduct";
				else {
					product.setImagePath("/images/" + product.getName() + ".jpg");
					productDao.save(product);

					File file = new File("src/main/resources/static/images");
					if (!file.isDirectory())
						file.mkdir();

					try {
						Files.write(Paths.get("src/main/resources/static/images", product.getName() + ".jpg"),
								picture.getBytes());
					} catch (IOException e) {
						session.setAttribute("failMessage", "You are Unauthorized to access his URL");
						response.sendRedirect("/");
						return null;
					}
					session.setAttribute("successMessage", "Product Updated Success");
					response.sendRedirect("/admin");
					return null;
				}
			} else {
				session.setAttribute("failMessage", "You are Unauthorized to access his URL");
				response.sendRedirect("/");
				return null;
			}
		}
	}

	@Override
	public String createAdmin(String email, String password, HttpSession session,HttpServletResponse response) throws IOException{
		if(!customerDao.checkEmailDuplicate(email)) {
		Customer customer = new Customer();
		customer.setEmail(email);
		customer.setPassword(AES.encrypt(password, "123"));
		customer.setRole("ADMIN");
		customer.setVerified(true);
		customerDao.save(customer);
		session.setAttribute("successMessage", "Admin Account creation success");
		response.sendRedirect("/");
		return null;
		}else {
			session.setAttribute("failMessage", "Admin Account Already Exists");
			response.sendRedirect("/");
			return null;
		}
	}
}
