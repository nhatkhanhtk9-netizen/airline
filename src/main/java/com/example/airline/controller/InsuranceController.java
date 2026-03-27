package com.example.airline.controller;

import com.example.airline.model.Insurance;
import com.example.airline.model.Users;
import com.example.airline.repository.InsuranceRepository;
import com.example.airline.repository.UsersRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/insurance")
public class InsuranceController {

    private final InsuranceRepository insuranceRepository;
    private final UsersRepository usersRepository;

    public InsuranceController(InsuranceRepository insuranceRepository, UsersRepository usersRepository) {
        this.insuranceRepository = insuranceRepository;
        this.usersRepository = usersRepository;
    }

    // Hiển thị danh sách bảo hiểm
    @GetMapping
    public String listInsurance(Model model) {
        model.addAttribute("insurances", insuranceRepository.findAll());
        return "insurance/list"; // trả về file insurance/list.html
    }

    // Hiển thị form tạo mới
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("insurance", new Insurance());
        model.addAttribute("users", usersRepository.findAll());
        return "insurance/create"; // create.html
    }

    // Lưu bảo hiểm mới
    @PostMapping("/save")
    public String saveInsurance(@ModelAttribute("insurance") Insurance insurance) {

        if (insurance.getUser() != null && insurance.getUser().getId() != null) {
            Users user = usersRepository.findById(Long.valueOf(insurance.getUser().getId()))
                    .orElse(null);
            insurance.setUser(user);
        }

        insuranceRepository.save(insurance);
        return "redirect:/insurance";
    }

    // Hiển thị form sửa
    @GetMapping("/edit/{id}")
    public String editInsurance(@PathVariable Integer id, Model model) {

        Insurance insurance = insuranceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insurance not found"));

        model.addAttribute("insurance", insurance);
        model.addAttribute("users", usersRepository.findAll());
        return "insurance/edit"; // edit.html
    }

    // Cập nhật
    @PostMapping("/update/{id}")
    public String updateInsurance(@PathVariable Integer id,
                                  @ModelAttribute("insurance") Insurance insuranceForm) {

        Insurance existing = insuranceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insurance not found"));

        existing.setType(insuranceForm.getType());
        existing.setStartDate(insuranceForm.getStartDate());
        existing.setEndDate(insuranceForm.getEndDate());
        existing.setPrice(insuranceForm.getPrice());
        existing.setCustomerName(insuranceForm.getCustomerName());

        if (insuranceForm.getUser() != null && insuranceForm.getUser().getId() != null) {
            Users user = usersRepository.findById(Long.valueOf(insuranceForm.getUser().getId()))
                    .orElse(null);
            existing.setUser(user);
        }

        insuranceRepository.save(existing);
        return "redirect:/insurance";
    }

    // Xóa
    @GetMapping("/delete/{id}")
    public String deleteInsurance(@PathVariable Integer id) {
        insuranceRepository.deleteById(id);
        return "redirect:/insurance";
    }
}
