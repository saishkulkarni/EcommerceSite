package com.s13sh.myshop.service.implementation;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.s13sh.myshop.dao.CustomerDao;
import com.s13sh.myshop.dao.ItemDao;
import com.s13sh.myshop.dao.ProductDao;
import com.s13sh.myshop.dao.ShoppingOrderDao;
import com.s13sh.myshop.dto.Cart;
import com.s13sh.myshop.dto.Customer;
import com.s13sh.myshop.dto.Item;
import com.s13sh.myshop.dto.Product;
import com.s13sh.myshop.dto.ShoppingOrder;
import com.s13sh.myshop.helper.AES;
import com.s13sh.myshop.helper.MailSendingHelper;
import com.s13sh.myshop.service.CustomerService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerDao customerDao;

	@Autowired
	ProductDao productDao;

	@Autowired
	MailSendingHelper mailHelper;

	@Autowired
	ItemDao itemDao;

	@Autowired
	ShoppingOrderDao orderDao;

	@Override
	public String save(Customer customer, BindingResult result, HttpServletResponse response,ModelMap map) throws IOException {
		if (customerDao.checkEmailDuplicate(customer.getEmail()))
			result.rejectValue("email", "error.email", "* Account Already Exists with this Email");
		if (customerDao.checkMobileDuplicate(customer.getMobile()))
			result.rejectValue("mobile", "error.mobile", "* Account Already Exists with this Mobile");

		if (result.hasErrors())
			return "Signup";
		else {
			customer.setPassword(AES.encrypt(customer.getPassword(), "123"));
			customer.setRole("USER");
			
			customerDao.save(customer);
			//sendOtp(customer.getId(), map);
			response.sendRedirect("/send-otp/" + customer.getId());
			return null;
		}
	}

	@Override
	public String verifyOtp(int id, int otp, ModelMap map, HttpSession session, HttpServletResponse response)
			throws IOException {
		Customer customer = customerDao.findById(id);
		System.out.println("*******2********");
		if (customer.getOtp() == otp) {
			customer.setVerified(true);
			customerDao.save(customer);
			session.setAttribute("successMessage", "Account Created Success");
			response.sendRedirect("/signin");
			return null;
		} else {
			map.put("failMessage", "Invalid Otp, Try Again!");
			map.put("id", id);
			return "VerifyOtp";
		}
	}

	@Override
	public String sendOtp(int id, ModelMap map) {
		Customer customer = customerDao.findById(id);
		customer.setOtp(new Random().nextInt(100000, 999999));
		customerDao.save(customer);
		//mailHelper.sendOtp(customer);
		map.put("id", id);
		map.put("successMessage", "Otp Sent Success");
		return "VerifyOtp";
	}

	@Override
	public String resendOtp(int id, ModelMap map) {
		Customer customer = customerDao.findById(id);
		customer.setOtp(new Random().nextInt(100000, 999999));
		customerDao.save(customer);
		// mailHelper.resendOtp(customer);
		map.put("id", id);
		map.put("successMessage", "Otp Resent Success");
		return "VerifyOtp";
	}

	@Override
	public String login(String email, String password, ModelMap map, HttpSession session, HttpServletResponse response)
			throws IOException {
		Customer customer = customerDao.findByEmail(email);
		if (customer == null)
			session.setAttribute("failMessage", "Invalid Email");
		else {
			if (AES.decrypt(customer.getPassword(), "123").equals(password)) {
				if (customer.isVerified()) {
					session.setAttribute("customer", customer);
					session.setAttribute("successMessage", "Login Success");
					response.sendRedirect("/");
					return null;
				} else {
					return resendOtp(customer.getId(), map);
				}
			} else
				session.setAttribute("failMessage", "Invalid Password");
		}
		return "Login";
	}

	@Override
	public String viewProducts(HttpSession session, ModelMap map, HttpServletResponse response) throws IOException {
		List<Product> products = productDao.fetchAll();
		if (products.isEmpty()) {
			session.setAttribute("failMessage", "No Products Present");
			response.sendRedirect("/");
			return null;
		} else {
			map.put("products", products);
			return "ViewProducts";
		}
	}

	@Override
	public String addToCart(int id, HttpSession session, HttpServletResponse response) throws IOException {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer == null) {
			session.setAttribute("failMessage", "Invalid Session");
			response.sendRedirect("/");
			return null;
		} else {
			Product product = productDao.findById(id);
			if (product.getStock() > 0) {
				Cart cart = customer.getCart();
				List<Item> items = cart.getItems();
				if (items.isEmpty()) {
					Item item = new Item();
					item.setCategory(product.getCategory());
					item.setDescription(product.getDescription());
					item.setImagePath(product.getImagePath());
					item.setName(product.getName());
					item.setPrice(product.getPrice());
					item.setQuantity(1);
					items.add(item);
					session.setAttribute("successMessage", "Item added to Cart Success");
				} else {
					boolean flag = true;
					for (Item item : items) {
						if (item.getName().equals(product.getName())) {
							flag = false;
							if (item.getQuantity() < product.getStock()) {
								item.setQuantity(item.getQuantity() + 1);
								item.setPrice(item.getPrice() + product.getPrice());
								session.setAttribute("successMessage", "Item added to Cart Success");
							} else {
								session.setAttribute("failMessage", "Out of Stock");
							}
							break;
						}
					}
					if (flag) {
						Item item = new Item();
						item.setCategory(product.getCategory());
						item.setDescription(product.getDescription());
						item.setImagePath(product.getImagePath());
						item.setName(product.getName());
						item.setPrice(product.getPrice());
						item.setQuantity(1);
						items.add(item);
						session.setAttribute("successMessage", "Item added to Cart Success");
					}
				}
				customerDao.save(customer);
				session.setAttribute("customer", customerDao.findById(customer.getId()));
				response.sendRedirect("/products");
				return null;
			} else {
				session.setAttribute("failMessage", "Out of Stock");
				response.sendRedirect("/");
				return null;
			}
		}
	}

	@Override
	public String viewCart(ModelMap map, HttpSession session, HttpServletResponse response) throws IOException {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer == null) {
			session.setAttribute("failMessage", "Invalid Session");
			response.sendRedirect("/signin");
			return null;
		} else {
			Cart cart = customer.getCart();
			List<Item> items = cart.getItems();
			if (items.isEmpty()) {
				session.setAttribute("failMessage", "No Items in cart");
				response.sendRedirect("/");
				return null;
			} else {
				map.put("items", items);
				return "ViewCart";
			}
		}
	}

	@Override
	public String removeFromCart(int id, HttpSession session, HttpServletResponse response) throws IOException {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer == null) {
			session.setAttribute("failMessage", "Invalid Session");
			response.sendRedirect("/signin");
			return null;
		} else {
			Item item = itemDao.findById(id);
			if (item.getQuantity() == 1) {
				customer.getCart().getItems().remove(item);
				customerDao.save(customer);
				session.setAttribute("customer", customerDao.findById(customer.getId()));
				itemDao.delete(item);
				session.setAttribute("successMessage", "Item Removed from Cart");

			} else {
				item.setPrice(item.getPrice() - (item.getPrice() / item.getQuantity()));
				item.setQuantity(item.getQuantity() - 1);
				itemDao.save(item);
				session.setAttribute("successMessage", "Item Quantity Reduced By 1");
			}
			session.setAttribute("customer", customerDao.findById(customer.getId()));
			response.sendRedirect("/cart");
			return null;
		}
	}

	@Override
	public String paymentPage(HttpSession session, ModelMap map, HttpServletResponse response) throws IOException {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer == null) {
			session.setAttribute("failMessage", "Invalid Session");
			response.sendRedirect("/signin");
			return null;
		} else {

			List<Item> items = customer.getCart().getItems();
			if (items.isEmpty()) {
				session.setAttribute("failMessage", "Nothing to Buy");
				response.sendRedirect("/");
				return null;
			} else {
				double price = items.stream().mapToDouble(x -> x.getPrice()).sum();
				try {
					RazorpayClient razorpay = new RazorpayClient("rzp_test_NL2VDewmKxugHZ", "OYO8g3i8aCPLCfn8piTOYlbi");

					JSONObject orderRequest = new JSONObject();
					orderRequest.put("amount", price * 100);
					orderRequest.put("currency", "INR");

					Order order = razorpay.orders.create(orderRequest);

					ShoppingOrder myOrder = new ShoppingOrder();
					myOrder.setDateTime(LocalDateTime.now());
					myOrder.setItems(items);
					myOrder.setOrderId(order.get("id"));
					myOrder.setStatus(order.get("status"));
					myOrder.setTotalPrice(price);

					orderDao.saveOrder(myOrder);

					map.put("key", "rzp_test_NL2VDewmKxugHZ");
					map.put("myOrder", myOrder);
					map.put("customer", customer);

					customer.getOrders().add(myOrder);
					customerDao.save(customer);
					session.setAttribute("customer", customerDao.findById(customer.getId()));
					return "PaymentPage";

				} catch (RazorpayException e) {
					e.printStackTrace();
					response.sendRedirect("/");
					return null;
				}
			}
		}
	}

	@Override
	public String confirmOrder(HttpSession session, int id, String razorpay_payment_id, HttpServletResponse response)
			throws IOException {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer == null) {
			session.setAttribute("failMessage", "Invalid Session");
			response.sendRedirect("/signin");
			return null;
		} else {
			for (Item item : customer.getCart().getItems()) {
				Product product = productDao.findByName(item.getName());
				product.setStock(product.getStock() - item.getQuantity());
				productDao.save(product);
			}
			ShoppingOrder order = orderDao.findOrderById(id);
			order.setPaymentId(razorpay_payment_id);
			order.setStatus("success");
			orderDao.saveOrder(order);
			customer.getCart().setItems(new ArrayList<Item>());
			customerDao.save(customer);
			session.setAttribute("customer", customerDao.findById(customer.getId()));
			session.setAttribute("successMessage", "Order Placed Success");
			response.sendRedirect("/");
			return null;
		}
	}

	@Override
	public String viewOrders(HttpSession session, ModelMap map, HttpServletResponse response) throws IOException {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer == null) {
			session.setAttribute("failMessage", "Invalid Session");
			response.sendRedirect("/");
			return null;
		} else {
			List<ShoppingOrder> orders = customer.getOrders();
			if (orders == null || orders.isEmpty()) {
				session.setAttribute("failMessage", "No Orders Yet");
				response.sendRedirect("/");
				return null;
			} else {
				map.put("orders", orders);
				return "ViewOrders";
			}
		}
	}

}
