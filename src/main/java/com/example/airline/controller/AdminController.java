package com.example.airline.controller;

import com.example.airline.model.Booking;
import com.example.airline.model.BookingKS;
import com.example.airline.model.Flight;
import com.example.airline.model.FlightBooking;
import com.example.airline.model.Users;
import com.example.airline.model.SupportMessage;
import com.example.airline.repository.BookingKSRepository;
import com.example.airline.repository.BookingRepository;
import com.example.airline.repository.FlightBookingRepository;
import com.example.airline.repository.FlightRepository;
import com.example.airline.repository.SupportRepository;
import com.example.airline.repository.UsersRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UsersRepository usersRepository;
    private final FlightBookingRepository flightBookingRepository;
    private final BookingKSRepository bookingKSRepository;
    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final SupportRepository supportRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String mailFrom;
    private final String mailHost;

    public AdminController(
            UsersRepository usersRepository,
            FlightBookingRepository flightBookingRepository,
            BookingKSRepository bookingKSRepository,
            BookingRepository bookingRepository,
            FlightRepository flightRepository,
            SupportRepository supportRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String mailFrom,
            @Value("${spring.mail.host:}") String mailHost
    ) {
        this.usersRepository = usersRepository;
        this.flightBookingRepository = flightBookingRepository;
        this.bookingKSRepository = bookingKSRepository;
        this.bookingRepository = bookingRepository;
        this.flightRepository = flightRepository;
        this.supportRepository = supportRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.mailHost = mailHost;
    }

    /**
     * Simple check: if no session or not admin, redirect to login.
     */
    private boolean isAdmin(HttpSession session) {
        Object o = session.getAttribute("loggedInUser");
        if (o instanceof Users) {
            return ((Users) o).isAdmin();
        }
        return false;
    }

    // Trang đăng nhập riêng cho Admin
    @GetMapping("/login")
    public String showAdminLoginForm(HttpSession session) {
        if (isAdmin(session)) {
            return "redirect:/admin";
        }
        return "admin/login";
    }

    @PostMapping("/login")
    public String adminLogin(@RequestParam String email,
                             @RequestParam String password,
                             HttpSession session,
                             Model model) {
        String lowerEmail = email != null ? email.trim().toLowerCase() : "";
        System.out.println(">>> Admin Login Attempt: " + lowerEmail);
        
        Users user = usersRepository.findByEmailIgnoreCase(lowerEmail);
        if (user != null && user.isAdmin()) {
            if (passwordEncoder.matches(password, user.getPassword())) {
                System.out.println(">>> Admin Login Success: " + lowerEmail);
                session.setAttribute("loggedInUser", user);
                return "redirect:/admin";
            } else {
                System.out.println(">>> Admin Login Failed: Wrong password for " + lowerEmail);
                model.addAttribute("error", "Mật khẩu quản trị không đúng!");
                return "admin/login";
            }
        } else {
            System.out.println(">>> Admin Login Failed: Not an admin or user not found: " + lowerEmail);
            model.addAttribute("error", "Email quản trị không tồn tại hoặc không có quyền!");
            return "admin/login";
        }
    }

    @GetMapping
    public String adminHome(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }
        long flightCount = flightRepository.count();
        long userCount = usersRepository.count();
        long flightBookingCount = flightBookingRepository.count();
        long hotelBookingCount = bookingKSRepository.count();
        long generalBookingCount = bookingRepository.count();
        long bookingCount = flightBookingCount + hotelBookingCount + generalBookingCount;

        List<FlightBooking> flightBookings = flightBookingRepository.findAll();
        List<BookingKS> hotelBookings = bookingKSRepository.findAll();
        List<Booking> generalBookings = bookingRepository.findAll();

        double flightRevenue = flightBookings.stream()
                .map(FlightBooking::getTotalPrice)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        long generalRevenue = generalBookings.stream()
                .map(Booking::getTotalPrice)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();

        double totalRevenue = flightRevenue + generalRevenue;

        Map<String, Long> flightBookingStatusCounts = flightBookings.stream()
                .map(b -> normalizeStatus(b.getStatus()))
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        Map<String, Long> sortedStatusCounts = flightBookingStatusCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        List<MonthSummary> monthSummaries = buildLast4MonthsSummary(flightBookings, hotelBookings, generalBookings);

        Sort latestFirst = Sort.by(Sort.Direction.DESC, "id");
        model.addAttribute("recentFlightBookings", flightBookingRepository.findAll(latestFirst).stream().limit(5).toList());
        model.addAttribute("recentUsers", usersRepository.findAll(latestFirst).stream().limit(5).toList());

        model.addAttribute("flightCount", flightCount);
        model.addAttribute("userCount", userCount);
        model.addAttribute("bookingCount", bookingCount);
        model.addAttribute("flightBookingCount", flightBookingCount);
        model.addAttribute("hotelBookingCount", hotelBookingCount);
        model.addAttribute("generalBookingCount", generalBookingCount);
        model.addAttribute("flightRevenue", flightRevenue);
        model.addAttribute("generalRevenue", generalRevenue);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("flightBookingStatusCounts", sortedStatusCounts);
        model.addAttribute("monthSummaries", monthSummaries);
        return "admin/index";
    }

    private static String normalizeStatus(String status) {
        if (status == null) return "UNKNOWN";
        String s = status.trim();
        if (s.isEmpty()) return "UNKNOWN";
        return s.toUpperCase(Locale.ROOT);
    }

    private static LocalDate safeParseIsoDate(String value) {
        if (value == null) return null;
        String s = value.trim();
        if (s.isEmpty()) return null;
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static boolean isInMonth(LocalDate date, YearMonth yearMonth) {
        if (date == null) return false;
        return date.getYear() == yearMonth.getYear() && date.getMonthValue() == yearMonth.getMonthValue();
    }

    private static List<MonthSummary> buildLast4MonthsSummary(
            List<FlightBooking> flightBookings,
            List<BookingKS> hotelBookings,
            List<Booking> generalBookings
    ) {
        YearMonth now = YearMonth.now();
        List<YearMonth> months = new ArrayList<>();
        for (int i = 3; i >= 0; i--) {
            months.add(now.minusMonths(i));
        }

        List<long[]> raw = new ArrayList<>();
        for (YearMonth ym : months) {
            long flightCount = flightBookings.stream()
                    .map(b -> safeParseIsoDate(b.getDepartureDate()))
                    .filter(d -> isInMonth(d, ym))
                    .count();

            long hotelCount = hotelBookings.stream()
                    .map(BookingKS::getCheckIn)
                    .filter(d -> isInMonth(d, ym))
                    .count();

            long generalCount = generalBookings.stream()
                    .map(b -> safeParseIsoDate(b.getDate()))
                    .filter(d -> isInMonth(d, ym))
                    .count();

            long total = flightCount + hotelCount + generalCount;
            raw.add(new long[]{flightCount, hotelCount, generalCount, total});
        }

        long maxTotal = raw.stream().mapToLong(a -> a[3]).max().orElse(0L);

        List<MonthSummary> out = new ArrayList<>();
        for (int i = 0; i < months.size(); i++) {
            YearMonth ym = months.get(i);
            long flight = raw.get(i)[0];
            long hotel = raw.get(i)[1];
            long general = raw.get(i)[2];
            long total = raw.get(i)[3];

            int heightPx = 0;
            if (maxTotal > 0) {
                heightPx = (int) Math.round((total * 160.0) / maxTotal);
            }

            String label = "Tháng " + ym.getMonthValue();
            out.add(new MonthSummary(label, total, heightPx, flight, hotel, general));
        }
        return out;
    }

    public static class MonthSummary {
        private final String label;
        private final long total;
        private final int heightPx;
        private final long flight;
        private final long hotel;
        private final long general;

        public MonthSummary(String label, long total, int heightPx, long flight, long hotel, long general) {
            this.label = label;
            this.total = total;
            this.heightPx = heightPx;
            this.flight = flight;
            this.hotel = hotel;
            this.general = general;
        }

        public String getLabel() {
            return label;
        }

        public long getTotal() {
            return total;
        }

        public int getHeightPx() {
            return heightPx;
        }

        public long getFlight() {
            return flight;
        }

        public long getHotel() {
            return hotel;
        }

        public long getGeneral() {
            return general;
        }
    }

    // --- QUẢN LÝ CHUYẾN BAY ---
    @GetMapping("/flights")
    public String manageFlights(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        model.addAttribute("flights", flightRepository.findAll());
        return "admin/flights";
    }

    @GetMapping("/flights/add")
    public String showAddFlightForm(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        model.addAttribute("flight", new Flight());
        return "admin/flight-form";
    }

    @PostMapping("/flights/save")
    public String saveFlight(@ModelAttribute Flight flight, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        flightRepository.save(flight);
        return "redirect:/admin/flights";
    }

    @GetMapping("/flights/edit/{id}")
    public String showEditFlightForm(@PathVariable Long id, Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        Flight flight = flightRepository.findById(id).orElseThrow();
        model.addAttribute("flight", flight);
        return "admin/flight-form";
    }

    @GetMapping("/flights/delete/{id}")
    public String deleteFlight(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        flightRepository.deleteById(id);
        return "redirect:/admin/flights";
    }

    // --- QUẢN LÝ NGƯỜI DÙNG ---
    @GetMapping("/users")
    public String manageUsers(@RequestParam(name = "q", required = false) String q, Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        List<Users> users;
        if (q != null && !q.isBlank()) {
            String keyword = q.trim();
            users = usersRepository.findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrPhoneContainingIgnoreCase(
                    keyword,
                    keyword,
                    keyword
            );
            model.addAttribute("q", keyword);
        } else {
            users = usersRepository.findAll();
            model.addAttribute("q", "");
        }
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/users/add")
    public String showAddUserForm(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        model.addAttribute("user", new Users());
        model.addAttribute("isEdit", false);
        return "admin/user-form";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        Users user = usersRepository.findById(id).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("isEdit", true);
        return "admin/user-form";
    }

    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute Users user,
                           @RequestParam(name = "newPassword", required = false) String newPassword,
                           HttpSession session,
                           Model model) {
        if (!isAdmin(session)) return "redirect:/admin/login";

        Object o = session.getAttribute("loggedInUser");
        Users currentAdmin = (o instanceof Users) ? (Users) o : null;

        Users existing = usersRepository.findByEmailIgnoreCase(user.getEmail());
        if (existing != null && (user.getId() == null || !existing.getId().equals(user.getId()))) {
            model.addAttribute("user", user);
            model.addAttribute("isEdit", user.getId() != null);
            model.addAttribute("error", "Email đã tồn tại!");
            return "admin/user-form";
        }

        Users toSave;
        if (user.getId() != null) {
            toSave = usersRepository.findById(user.getId()).orElseThrow();
        } else {
            toSave = new Users();
        }

        toSave.setFullName(user.getFullName());
        toSave.setEmail(user.getEmail());
        toSave.setPhone(user.getPhone());

        boolean adminFlag = user.isAdmin();
        if (currentAdmin != null && toSave.getId() != null && currentAdmin.getId().equals(toSave.getId())) {
            adminFlag = true;
        }

        if (toSave.getId() != null && toSave.isAdmin() && !adminFlag) {
            long adminCount = usersRepository.findByAdminTrue().size();
            if (adminCount <= 1) {
                model.addAttribute("user", user);
                model.addAttribute("isEdit", true);
                model.addAttribute("error", "Không thể bỏ quyền admin của admin cuối cùng!");
                return "admin/user-form";
            }
        }

        toSave.setAdmin(adminFlag);

        if (newPassword != null && !newPassword.isBlank()) {
            toSave.setPassword(passwordEncoder.encode(newPassword));
        }

        usersRepository.save(toSave);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        usersRepository.deleteById(id);
        return "redirect:/admin/users";
    }

    // --- QUẢN LÝ ĐẶT CHỖ ---
    @GetMapping("/bookings")
    public String manageBookings(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        Sort latestFirst = Sort.by(Sort.Direction.DESC, "id");
        model.addAttribute("flightBookings", flightBookingRepository.findAll(latestFirst));
        model.addAttribute("hotelBookings", bookingKSRepository.findAll(latestFirst));
        model.addAttribute("generalBookings", bookingRepository.findAll(latestFirst));
        return "admin/bookings";
    }

    @PostMapping("/bookings/flight/update-status/{id}")
    public String updateFlightBookingStatus(@PathVariable Long id, @RequestParam String status, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        FlightBooking booking = flightBookingRepository.findById(id).orElseThrow();
        booking.setStatus(status);
        flightBookingRepository.save(booking);
        return "redirect:/admin/bookings";
    }

    @PostMapping("/bookings/flight/update-code/{id}")
    public String updateFlightBookingCode(@PathVariable Long id, @RequestParam String bookingCode, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        FlightBooking booking = flightBookingRepository.findById(id).orElseThrow();
        String code = bookingCode == null ? "" : bookingCode.trim().toUpperCase();
        if (code.isBlank()) {
            booking.setBookingCode(null);
            flightBookingRepository.save(booking);
            return "redirect:/admin/bookings";
        }
        if (!code.equals(booking.getBookingCode()) && flightBookingRepository.existsByBookingCode(code)) {
            return "redirect:/admin/bookings";
        }
        booking.setBookingCode(code);
        flightBookingRepository.save(booking);
        return "redirect:/admin/bookings";
    }

    @PostMapping("/bookings/flight/approve-payment/{id}")
    public String approveFlightPayment(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        FlightBooking booking = flightBookingRepository.findById(id).orElseThrow();
        booking.setStatus("PAID");
        flightBookingRepository.save(booking);
        Long bookingId = booking.getId();
        new Thread(() -> {
            try {
                FlightBooking latest = flightBookingRepository.findById(bookingId).orElse(null);
                if (latest != null) {
                    sendPaymentApprovedEmail(latest);
                }
            } catch (Exception e) {
                System.err.println("Lỗi gửi email nền: " + e.getMessage());
            }
        }).start();
        redirectAttributes.addFlashAttribute("successMessage", "Đã phê duyệt thanh toán thành công và gửi thông báo cho khách hàng.");
        return "redirect:/admin/bookings";
    }

    private void sendPaymentApprovedEmail(FlightBooking booking) {
        if (booking == null) return;
        String to = booking.getEmail();
        if (to == null || to.isBlank()) return;
        if (mailHost == null || mailHost.isBlank() || mailFrom == null || mailFrom.isBlank()) {
            System.err.println("Chưa cấu hình SMTP (spring.mail.host / spring.mail.username). Bỏ qua gửi email.");
            return;
        }

        String code = booking.getBookingCode();
        if (code == null || code.isBlank()) {
            code = "#" + booking.getId();
        }

        String total = booking.getTotalPrice() == null ? "0" : String.format("%,.0f", booking.getTotalPrice());

        String subject = "Xác nhận thanh toán thành công - Mã đặt chỗ: " + code;
        String text =
                "Xin chào " + (booking.getFullName() == null ? "Quý khách" : booking.getFullName()) + ",\n\n" +
                "FlyBooking xin thông báo: Đơn đặt vé của bạn đã được XÁC NHẬN THANH TOÁN THÀNH CÔNG.\n\n" +
                "Mã đặt chỗ: " + code + "\n" +
                "Hành trình: " + (booking.getFromLocation() == null ? "" : booking.getFromLocation()) + " -> " + (booking.getToLocation() == null ? "" : booking.getToLocation()) + "\n" +
                "Ngày đi: " + (booking.getDepartureDate() == null ? "" : booking.getDepartureDate()) + "\n" +
                "Tổng tiền: " + total + " VNĐ\n\n" +
                "Cảm ơn bạn đã tin tưởng và sử dụng dịch vụ của FlyBooking.\n";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to.trim());
            if (mailFrom != null && !mailFrom.isBlank()) {
                message.setFrom(mailFrom.trim());
            }
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Lỗi gửi email phê duyệt thanh toán: " + e.getMessage());
        }
    }

    @GetMapping("/bookings/flight/delete/{id}")
    public String deleteFlightBooking(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        flightBookingRepository.deleteById(id);
        return "redirect:/admin/bookings";
    }

    @GetMapping("/bookings/hotel/delete/{id}")
    public String deleteHotelBooking(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        bookingKSRepository.deleteById(id);
        return "redirect:/admin/bookings";
    }

    @GetMapping("/bookings/general/delete/{id}")
    public String deleteGeneralBooking(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        bookingRepository.deleteById(id);
        return "redirect:/admin/bookings";
    }

    // --- SUPPORT MESSAGE MANAGEMENT ---

    @GetMapping("/support")
    public String listSupportMessages(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        
        List<SupportMessage> messages = supportRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("messages", messages);
        return "admin/support";
    }

    @PostMapping("/support/reply")
    public String replyToSupportMessage(@RequestParam Long id, 
                                       @RequestParam String adminReply, 
                                       HttpSession session, 
                                       RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        
        SupportMessage message = supportRepository.findById(id).orElse(null);
        if (message != null) {
            message.setAdminReply(adminReply);
            message.setRepliedAt(LocalDateTime.now());
            message.setStatus("RESOLVED");
            supportRepository.save(message);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi phản hồi thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tin nhắn hỗ trợ.");
        }
        
        return "redirect:/admin/support";
    }

    @GetMapping("/support/delete/{id}")
    public String deleteSupportMessage(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        
        try {
            supportRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tin nhắn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa tin nhắn.");
        }
        
        return "redirect:/admin/support";
    }
}
