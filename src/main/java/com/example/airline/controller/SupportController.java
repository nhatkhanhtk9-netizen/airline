package com.example.airline.controller;

import com.example.airline.model.SupportMessage;
import com.example.airline.model.Users;
import com.example.airline.repository.SupportRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SupportController {

    private final SupportRepository supportRepository;

    public SupportController(SupportRepository supportRepository) {
        this.supportRepository = supportRepository;
    }

    @GetMapping("/support")
    public String showSupportPage(Model model, HttpSession session) {
        SupportMessage message = new SupportMessage();
        
        // Nếu đã đăng nhập, tự động điền thông tin
        Users loggedInUser = (Users) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            message.setFullName(loggedInUser.getFullName());
            message.setEmail(loggedInUser.getEmail());
            message.setPhone(loggedInUser.getPhone());
        }
        
        model.addAttribute("supportMessage", message);
        return "support";
    }

    @PostMapping("/support/send")
    public String receiveSupportMessage(@ModelAttribute SupportMessage supportMessage, RedirectAttributes redirectAttributes) {
        try {
            supportRepository.save(supportMessage);
            redirectAttributes.addFlashAttribute("successMessage", "Yêu cầu của bạn đã được gửi thành công! Chúng tôi sẽ phản hồi sớm nhất có thể.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi gửi yêu cầu. Vui lòng thử lại sau.");
        }
        return "redirect:/support";
    }
}
