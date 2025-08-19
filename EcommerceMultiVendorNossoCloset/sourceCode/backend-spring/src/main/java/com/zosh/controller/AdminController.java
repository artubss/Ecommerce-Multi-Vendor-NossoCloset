package com.zosh.controller;

import com.zosh.domain.AccountStatus;
import com.zosh.exception.SellerException;
import com.zosh.model.HomeCategory;
import com.zosh.model.Seller;
import com.zosh.model.User;
import com.zosh.model.Order;
import com.zosh.model.Product;
import com.zosh.service.HomeCategoryService;
import com.zosh.service.SellerService;
import com.zosh.service.UserService;
import com.zosh.service.OrderService;
import com.zosh.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final SellerService sellerService;
    private final HomeCategoryService homeCategoryService;
    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;

    public AdminController(SellerService sellerService, 
                         HomeCategoryService homeCategoryService,
                         UserService userService,
                         OrderService orderService,
                         ProductService productService) {     
        this.sellerService = sellerService;
        this.homeCategoryService = homeCategoryService;
        this.userService = userService;
        this.orderService = orderService;
        this.productService = productService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Admin service is running!");
    }

    // ===== GESTÃO DE FORNECEDORES (SELLERS) =====
    @PatchMapping("/seller/{id}/status/{status}")
    public ResponseEntity<Seller> updateSellerStatus(
            @PathVariable Long id,
            @PathVariable AccountStatus status) throws SellerException {

        Seller updatedSeller = sellerService.updateSellerAccountStatus(id, status);
        return ResponseEntity.ok(updatedSeller);
    }

    @GetMapping("/sellers")
    public ResponseEntity<List<Seller>> getAllSellers() {
        List<Seller> sellers = sellerService.getAllSellers();
        return ResponseEntity.ok(sellers);
    }

    @GetMapping("/seller/{id}")
    public ResponseEntity<Seller> getSellerById(@PathVariable Long id) throws SellerException {
        Seller seller = sellerService.findSellerById(id);
        return ResponseEntity.ok(seller);
    }

    // ===== GESTÃO DE CLIENTES =====
    @GetMapping("/customers")
    public ResponseEntity<List<User>> getAllCustomers() {
        List<User> customers = userService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/customer/{id}")
    public ResponseEntity<User> getCustomerById(@PathVariable Long id) {
        User customer = userService.getUserById(id);
        return ResponseEntity.ok(customer);
    }

    @PatchMapping("/customer/{id}/status")
    public ResponseEntity<User> updateCustomerStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        User updatedCustomer = userService.updateUserStatus(id, status);
        return ResponseEntity.ok(updatedCustomer);
    }

    // ===== GESTÃO DE PEDIDOS =====
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/order/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/order/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Order updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    // ===== GESTÃO DE PRODUTOS =====
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PatchMapping("/product/{id}/status")
    public ResponseEntity<Product> updateProductStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Product updatedProduct = productService.updateProductStatus(id, status);
        return ResponseEntity.ok(updatedProduct);
    }

    // ===== GESTÃO DE CATEGORIAS DA PÁGINA INICIAL =====
    @GetMapping("/home-category")
    public ResponseEntity<List<HomeCategory>> getHomeCategory() throws Exception {
        List<HomeCategory> categories = homeCategoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/home-category")
    public ResponseEntity<HomeCategory> createHomeCategory(
            @RequestBody HomeCategory homeCategory) throws Exception {
        HomeCategory createdCategory = homeCategoryService.createCategory(homeCategory);
        return ResponseEntity.ok(createdCategory);
    }

    @PatchMapping("/home-category/{id}")
    public ResponseEntity<HomeCategory> updateHomeCategory(
            @PathVariable Long id,
            @RequestBody HomeCategory homeCategory) throws Exception {
        HomeCategory updatedCategory = homeCategoryService.updateCategory(homeCategory, id);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/home-category/{id}")
    public ResponseEntity<String> deleteHomeCategory(@PathVariable Long id) throws Exception {
        homeCategoryService.deleteCategory(id);
        return ResponseEntity.ok("Category deleted successfully");
    }

    // ===== DASHBOARD E ESTATÍSTICAS =====
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Object> getDashboardStats() {
        // Implementar lógica para retornar estatísticas do dashboard
        return ResponseEntity.ok("Dashboard stats");
    }

    @GetMapping("/revenue")
    public ResponseEntity<Object> getRevenueStats() {
        // Implementar lógica para retornar estatísticas de receita
        return ResponseEntity.ok("Revenue stats");
    }
}
