/**
 * Author: Le Nguyen Minh Quy
 */
package fashion.mock.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fashion.mock.model.Category;
import fashion.mock.model.Product;
import fashion.mock.model.User;
import fashion.mock.service.CartItemService;
import fashion.mock.service.CategoryService;
import fashion.mock.service.ProductService;
import fashion.mock.service.UserService;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/shop")
public class ProductUserController {
	@Autowired
	private ProductService productService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private CartItemService cartItemService;
	private UserService userService;

	@GetMapping
	public String listProducts(Model model,HttpSession session, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "9") int max, @RequestParam(required = false) String searchTerm,
			@RequestParam(required = false) String sortBy, @RequestParam(required = false) String color,
			@RequestParam(required = false) String size, @RequestParam(required = false) String priceRange,
			@RequestParam(required = false) Long categoryId) {
		// Lấy danh sách tất cả các danh mục
		List<Category> categories = categoryService.getAllCategories();
		List<Category> aoCategories = categories.stream()
				.filter(category -> category.getCategoryName().startsWith("Áo")).collect(Collectors.toList());
		List<Category> quanCategories = categories.stream()
				.filter(category -> category.getCategoryName().startsWith("Quần"))// thay Quần bằng Quan
				.collect(Collectors.toList());
		
		String categoryName = null;
		if (categoryId != null) {
			// Sử dụng Optional để xử lý kết quả trả về từ getCategoryById
			Optional<Category> categoryOpt = categoryService.getCategoryById(categoryId);
			Category category = categoryOpt.orElse(null); // Trả về null nếu không có danh mục
			categoryName = category != null ? category.getCategoryName() : null;
		}

		// Lấy các sản phẩm theo bộ lọc và danh mục
		PageRequest pageRequest = PageRequest.of(page, max);
		Page<Product> productPage = productService.getFilteredProducts(searchTerm, sortBy, color, size, priceRange,
				categoryId, pageRequest);

		List<Product> products = productPage.getContent();
		List<Boolean> productsOnDiscount = products.stream().map(productService::isProductOnDiscount)
				.collect(Collectors.toList());
		List<Double> discountedPrices = products.stream().map(productService::getDiscountedPrice)
				.collect(Collectors.toList());
		
		User user = (User) session.getAttribute("user");
        boolean isAdmin = false; // Initialize isAdmin

        if (user != null) {
            isAdmin = userService.isAdmin(user.getId());
            model.addAttribute("user", user);
        } else {
            // Handle the case where user is null (e.g., redirect, set an error message, etc.)
        }
        model.addAttribute("isAdmin", isAdmin);

		model.addAttribute("products", products);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", productPage.getTotalPages());
		model.addAttribute("totalItems", productPage.getTotalElements());
		model.addAttribute("searchTerm", searchTerm);
		model.addAttribute("sortBy", sortBy);
		model.addAttribute("color", color);
		model.addAttribute("size", size);
		model.addAttribute("priceRange", priceRange);
		model.addAttribute("categoryId", categoryId);
		model.addAttribute("aoCategories", aoCategories);
		model.addAttribute("quanCategories", quanCategories);
		model.addAttribute("categories", categories);
		model.addAttribute("productsOnDiscount", productsOnDiscount);
		model.addAttribute("discountedPrices", discountedPrices);
		model.addAttribute("categoryName", categoryName);
		
		model.addAttribute("totalCartItems", cartItemService.getCount());
		return "shop";
		
	}
}