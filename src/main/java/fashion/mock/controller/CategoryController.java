package fashion.mock.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fashion.mock.model.Category;
import fashion.mock.service.CategoryService;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    
    @GetMapping("/nav")
    public String listNavigation(Model model) {
        // Lấy tất cả danh mục
        List<Category> categories = categoryService.getAllCategories();

        // Phân loại danh mục dựa trên tên bắt đầu bằng "Áo" hoặc "Quần"
        List<Category> aoCategories = categories.stream()
            .filter(category -> category.getCategoryName().startsWith("Áo"))
            .collect(Collectors.toList());

        List<Category> quanCategories = categories.stream()
            .filter(category -> category.getCategoryName().startsWith("Quan"))
            .collect(Collectors.toList());

        // Thêm danh sách "Áo" và "Quần" vào model
        model.addAttribute("aoCategories", aoCategories);
        model.addAttribute("quanCategories", quanCategories);

        return "navigation";
    }

    // Hiển thị danh sách category
//  @GetMapping
//  public String listCategories(Model model) {
//      List<Category> categories = categoryService.getAllCategories();
//      model.addAttribute("categories", categories);
//      return "adminlistcategory";
//  } 
    
    // Hiển thị danh sách category
    @GetMapping
    public String listCategories(Model model, 
                                 @RequestParam(defaultValue = "0") int page, 
                                 @RequestParam(defaultValue = "3") int size) {
        Page<Category> categoryPage = categoryService.getAllCategories(PageRequest.of(page, size));
        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("totalItems", categoryPage.getTotalElements());
        return "adminlistcategory";
    }

    // Hiển thị form thêm mới category
    @GetMapping("/new")
    public String showAddCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "adminformcategory";
    }

    // Hiển thị form sửa category
    @GetMapping("/edit/{id}")
    public String showUpdateCategoryForm(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new RuntimeException("Category không tồn tại"));
        model.addAttribute("category", category);
        return "adminformcategory";
    }

//    // Xử lý thêm category
    @PostMapping
    public String addCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        try {
            categoryService.addCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục thành công!");
            return "redirect:/categories";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("category", category);
            return "redirect:/categories/new";
        }
    }

//    // Xử lý cập nhật category
    @PostMapping("/update")
    public String updateCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        try {
            categoryService.updateCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công!");
            return "redirect:/categories";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("category", category);
            return "redirect:/categories/edit/" + category.getId();
        }
    }

    // Xóa category
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean deleted = categoryService.deleteCategory(id);
        if (!deleted) {
            redirectAttributes.addFlashAttribute("message", "Category không tồn tại");
            redirectAttributes.addFlashAttribute("messageType", "error");
        } else {
            redirectAttributes.addFlashAttribute("message", "Category đã được xóa thành công");
            redirectAttributes.addFlashAttribute("messageType", "success");
        }
        return "redirect:/categories";
    }
    
    // Tìm kiếm
    @GetMapping("/search")
    public String searchCategories(@RequestParam(value = "searchTerm", required = false) String searchTerm, 
                                   @RequestParam(defaultValue = "0") int page, 
                                   @RequestParam(defaultValue = "3") int size,
                                   Model model) {
    	  if (page < 0) {
    	        page = 0;
    	    }
        Page<Category> categoryPage = categoryService.searchCategories(searchTerm, PageRequest.of(page, size));
        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("totalItems", categoryPage.getTotalElements());
        model.addAttribute("searchTerm", searchTerm);
        return "adminlistcategory";
    }
}