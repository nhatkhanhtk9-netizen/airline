package com.example.airline.controller;

import com.example.airline.model.BookingKS;
import com.example.airline.model.Users;
import com.example.airline.repository.BookingKSRepository;
import com.example.airline.repository.UsersRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class BookingKSController {

    @Autowired
    private BookingKSRepository bookingKSRepository;

    @Autowired
    private UsersRepository usersRepository;

    // Hiển thị form qua đường dẫn /hotels hoặc /bookingKS/form
    @GetMapping({"/hotels", "/bookingKS/form"})
    public String showForm(@RequestParam(name = "query", required = false) String query,
                           HttpSession session,
                           Model model) {
        Users loggedInUser = (Users) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        BookingKS bookingKS = new BookingKS();
        if (loggedInUser != null) {
            bookingKS.setFullName(loggedInUser.getFullName());
            bookingKS.setEmail(loggedInUser.getEmail());
            bookingKS.setPhone(loggedInUser.getPhone());
        }
        
        model.addAttribute("bookingKS", bookingKS);
        model.addAttribute("loggedInUser", loggedInUser);
        if (query != null && !query.isBlank()) {
            model.addAttribute("searchQuery", query.trim());
        }
        return "bookingKSForm"; // Tên file HTML
    }

    // Xử lý submit form và hiển thị kết quả tìm kiếm
    @PostMapping("/bookingKS/submit")
    public String submitForm(@ModelAttribute BookingKS bookingKS, HttpSession session, Model model) {
        Users loggedInUser = (Users) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        bookingKSRepository.save(bookingKS);
        
        // Mocking some hotel results for demonstration
        model.addAttribute("booking", bookingKS);
        model.addAttribute("loggedInUser", loggedInUser);
        return "hotelResults"; // Chuyển sang trang kết quả tìm kiếm
    }

    @GetMapping("/hotel/detail/{id}")
    public String hotelDetail(@PathVariable String id, HttpSession session, Model model) {
        Users loggedInUser = (Users) session.getAttribute("loggedInUser");

        // Mock data for hotel detail based on ID
        Map<String, Object> hotel = new HashMap<>();
        if ("1".equals(id)) {
            hotel.put("name", "Nature Hotel - Le Hong Phong");
            hotel.put("imageUrl", "https://images.unsplash.com/photo-1566073771259-6a8506099945?q=80&w=800");
            hotel.put("stars", 4);
            hotel.put("ratingCount", 73);
            hotel.put("ratingValue", 8.7);
            hotel.put("location", "Phường 4, Đà Lạt");
            hotel.put("price", 406536.0);
            hotel.put("description", "Khách sạn tọa lạc tại vị trí đắc địa, không gian sang trọng và hiện đại, dịch vụ đẳng cấp quốc tế.");
            hotel.put("pros", List.of("Vị trí trung tâm, dễ di chuyển", "Gần nhiều quán ăn ngon", "Phòng ốc sạch sẽ, mới"));
            hotel.put("cons", List.of("Thường xuyên hết phòng vào cuối tuần", "Tiếng ồn từ phố vào ban đêm"));
        } else {
            hotel.put("name", "The Luxe Hotel Dalat");
            hotel.put("imageUrl", "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?q=80&w=800");
            hotel.put("stars", 3);
            hotel.put("ratingCount", 1400);
            hotel.put("ratingValue", 8.5);
            hotel.put("location", "Phường 3, Đà Lạt");
            hotel.put("price", 405800.0);
            hotel.put("description", "Khách sạn phong cách tân cổ điển, mang lại trải nghiệm ấm cúng và sang trọng giữa lòng Đà Lạt.");
            hotel.put("pros", List.of("Kiến trúc đẹp, sang trọng", "Giá cả hợp lý", "Nhân viên nhiệt tình"));
            hotel.put("cons", List.of("Không gian hơi hẹp", "Wifi đôi khi không ổn định"));
        }

        model.addAttribute("hotel", hotel);
        model.addAttribute("loggedInUser", loggedInUser);
        return "hotel-detail";
    }
}
