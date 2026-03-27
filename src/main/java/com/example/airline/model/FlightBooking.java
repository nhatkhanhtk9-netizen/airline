package com.example.airline.model;

import jakarta.persistence.*;

@Entity
public class FlightBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String bookingCode;

    private String fromLocation;
    private String toLocation;
    private String departureDate;
    private String returnDate;
    private int passenger;
    private String classType;
    
    // Thông tin khách hàng
    private String fullName;
    private String email;
    private String phone;
    private String idCard;

    // Giá tiền
    private Double totalPrice;

    // Trạng thái thanh toán
    private String status = "PENDING"; // PENDING, CONFIRMED, PAID

    // Các tiện ích bổ sung
    private Boolean insurance = false;
    private Integer baggageWeight = 0; // kg
    private String voucherCode;
    private Double discountAmount = 0.0;
    private Integer pointsEarned = 0;

    @PostLoad
    @PrePersist
    @PreUpdate
    private void normalizeNulls() {
        if (insurance == null) insurance = false;
        if (baggageWeight == null) baggageWeight = 0;
        if (discountAmount == null) discountAmount = 0.0;
        if (pointsEarned == null) pointsEarned = 0;
    }

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
    
    public Boolean isInsurance() { return insurance; }
    public void setInsurance(Boolean insurance) { this.insurance = insurance; }

    public Integer getBaggageWeight() { return baggageWeight; }
    public void setBaggageWeight(Integer baggageWeight) { this.baggageWeight = baggageWeight; }

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }

    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }

    public Integer getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(Integer pointsEarned) { this.pointsEarned = pointsEarned; }

    public String getFromLocation() { return fromLocation; }
    public void setFromLocation(String fromLocation) { this.fromLocation = fromLocation; }

    public String getToLocation() { return toLocation; }
    public void setToLocation(String toLocation) { this.toLocation = toLocation; }

    public String getDepartureDate() { return departureDate; }
    public void setDepartureDate(String departureDate) { this.departureDate = departureDate; }

    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }

    public int getPassenger() { return passenger; }
    public void setPassenger(int passenger) { this.passenger = passenger; }

    public String getClassType() { return classType; }
    public void setClassType(String classType) { this.classType = classType; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
