package com.example.airline.config;

import com.example.airline.model.CompanyInfo;
import com.example.airline.model.Flight;
import com.example.airline.model.FlightBooking;
import com.example.airline.model.Users;
import com.example.airline.repository.CompanyInfoRepository;
import com.example.airline.repository.FlightBookingRepository;
import com.example.airline.repository.FlightRepository;
import com.example.airline.repository.UsersRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsersRepository usersRepository;
    private final FlightRepository flightRepository;
    private final FlightBookingRepository flightBookingRepository;
    private final CompanyInfoRepository companyInfoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsersRepository usersRepository, 
                           FlightRepository flightRepository, 
                           FlightBookingRepository flightBookingRepository,
                           CompanyInfoRepository companyInfoRepository,
                           PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.flightRepository = flightRepository;
        this.flightBookingRepository = flightBookingRepository;
        this.companyInfoRepository = companyInfoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> Checking for Admin User...");
        // Initialize Admin User if not exists
        if (usersRepository.findByEmailIgnoreCase("admin@airline.com") == null) {
            Users admin = new Users();
            admin.setFullName("Admin User");
            admin.setEmail("admin@airline.com");
            admin.setPhone("0123456789");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setAdmin(true);
            usersRepository.save(admin);
            System.out.println(">>> Created Admin User: admin@airline.com / admin123");
        } else {
            System.out.println(">>> Admin User already exists.");
        }

        // Initialize a Normal User for testing index1 redirect
        if (usersRepository.findByEmailIgnoreCase("user@example.com") == null) {
            Users user = new Users();
            user.setFullName("Normal User");
            user.setEmail("user@example.com");
            user.setPhone("0987654321");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setAdmin(false);
            usersRepository.save(user);
        }

        // Initialize some flights if none exist
        if (flightRepository.count() == 0) {
            Flight f1 = new Flight();
            f1.setFlightNumber("VN123");
            f1.setOrigin("Hà Nội");
            f1.setDestination("TP. Hồ Chí Minh");
            f1.setDepartureTime(LocalDateTime.now().plusDays(1));
            f1.setPrice(1500000.0);
            f1.setSeatsAvailable(100);
            flightRepository.save(f1);

            Flight f2 = new Flight();
            f2.setFlightNumber("VJ456");
            f2.setOrigin("Đà Nẵng");
            f2.setDestination("Hà Nội");
            f2.setDepartureTime(LocalDateTime.now().plusDays(2));
            f2.setPrice(800000.0);
            f2.setSeatsAvailable(50);
            flightRepository.save(f2);
        }

        // Initialize a sample booking if empty
        if (flightBookingRepository.count() == 0) {
            FlightBooking sample = new FlightBooking();
            sample.setFromLocation("Hà Nội");
            sample.setToLocation("TP. Hồ Chí Minh");
            sample.setDepartureDate("2026-03-28");
            sample.setPassenger(1);
            sample.setFullName("Khách Hàng Mẫu");
            sample.setEmail("nhatkhanhtk9@gmail.com");
            sample.setTotalPrice(1500000.0);
            sample.setStatus("PENDING");
            sample.setBookingCode("SAMPLE001");
            flightBookingRepository.save(sample);
            System.out.println(">>> Created Sample Booking for: nhatkhanhtk9@gmail.com");
        }

        // Initialize Company Info if empty
        if (companyInfoRepository.count() == 0) {
            companyInfoRepository.save(new CompanyInfo("about", "Giới thiệu về FlyBooking", 
                "FlyBooking là nền tảng đặt vé máy bay và khách sạn hàng đầu tại Việt Nam. Chúng tôi cam kết mang đến trải nghiệm du lịch tuyệt vời nhất với giá cả cạnh tranh."));
            companyInfoRepository.save(new CompanyInfo("news", "Tin tức Du lịch", 
                "Cập nhật những thông tin mới nhất về các chuyến bay, khuyến mãi khách sạn và xu hướng du lịch trên thế giới."));
            companyInfoRepository.save(new CompanyInfo("careers", "Cơ hội nghề nghiệp", 
                "Hãy gia nhập đội ngũ FlyBooking để cùng nhau xây dựng tương lai của ngành du lịch số. Chúng tôi luôn tìm kiếm những tài năng đam mê và sáng tạo."));
            companyInfoRepository.save(new CompanyInfo("terms", "Điều khoản sử dụng", 
                "Bằng việc sử dụng dịch vụ của FlyBooking, bạn đồng ý với các điều khoản và điều kiện được quy định nhằm đảm bảo quyền lợi cho cả hai bên."));
            companyInfoRepository.save(new CompanyInfo("privacy", "Chính sách bảo mật", 
                "FlyBooking cam kết bảo vệ thông tin cá nhân của bạn một cách tuyệt đối. Chúng tôi sử dụng các công nghệ bảo mật tiên tiến nhất để giữ an toàn dữ liệu."));
            System.out.println(">>> Initialized Company Info records.");
        }
    }
}