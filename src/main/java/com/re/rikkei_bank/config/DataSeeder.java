package com.re.rikkei_bank.config;

import com.re.rikkei_bank.model.*;
import com.re.rikkei_bank.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ═══════════════════════════════════════════════════════════════
 *  DataSeeder - Tự động tạo dữ liệu mẫu khi khởi động ứng dụng
 * ═══════════════════════════════════════════════════════════════
 *
 * Dữ liệu sẽ được tạo:
 *   • 3 Roles    (ROLE_ADMIN, ROLE_STAFF, ROLE_CUSTOMER)
 *   • 10 Users   (1 Admin, 2 Staff, 7 Customers)
 *   • 10 Accounts
 *   • 7 KYC Profiles (đa dạng trạng thái)
 *   • 15 Transactions mẫu
 *
 * ═══════════════════════════════════════════════════════════════
 *  BẢNG ĐĂNG NHẬP - Mật khẩu tất cả: Admin@1234 | PIN: 123456
 * ═══════════════════════════════════════════════════════════════
 *  Username      | Role     | Account Number | Balance (VND)
 *  ────────────────────────────────────────────────────────────
 *  admin         | ADMIN    | 9000000001     | 999,999,999
 *  staff01       | STAFF    | 9000000002     | 50,000,000
 *  staff02       | STAFF    | 9000000003     | 45,000,000
 *  customer01    | CUSTOMER | 1001000001     | 75,000,000   ✅ KYC CONFIRM
 *  customer02    | CUSTOMER | 1001000002     | 32,500,000   ✅ KYC CONFIRM
 *  customer03    | CUSTOMER | 1001000003     | 120,000,000  ✅ KYC CONFIRM
 *  customer04    | CUSTOMER | 1001000004     | 8,500,000    ✅ KYC CONFIRM
 *  customer05    | CUSTOMER | 1001000005     | 15,000,000   ⏳ KYC PENDING
 *  customer06    | CUSTOMER | 1001000006     | 5,000,000    ❌ KYC REJECT
 *  customer07    | CUSTOMER | 1001000007     | 22,000,000   🔒 USER LOCKED
 * ═══════════════════════════════════════════════════════════════
 *
 * Nếu database ĐÃ CÓ dữ liệu (roles table > 0 row) → sẽ TỰ ĐỘNG BỎ QUA.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final KycProfileRepository kycProfileRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "Admin@1234";
    private static final String DEFAULT_PIN = "123456";

    @Override
    @Transactional
    public void run(String... args) {
        // Kiểm tra nếu đã có dữ liệu → bỏ qua
        if (roleRepository.count() > 0) {
            log.info("════════════════════════════════════════════════════");
            log.info("  ⏭️  Database đã có dữ liệu → bỏ qua seed data.");
            log.info("════════════════════════════════════════════════════");
            return;
        }

        log.info("════════════════════════════════════════════════════");
        log.info("  🌱 BẮT ĐẦU SEED DỮ LIỆU MẪU...");
        log.info("════════════════════════════════════════════════════");

        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);
        String encodedPin = passwordEncoder.encode(DEFAULT_PIN);

        // ══════════════════════════════════════════
        // 1. TẠO ROLES
        // ══════════════════════════════════════════
        Role adminRole = roleRepository.save(
                Role.builder().name("ROLE_ADMIN").description("Quản trị viên hệ thống - toàn quyền quản lý").build());
        Role staffRole = roleRepository.save(
                Role.builder().name("ROLE_STAFF").description("Nhân viên ngân hàng - duyệt KYC, xem tài khoản").build());
        Role customerRole = roleRepository.save(
                Role.builder().name("ROLE_CUSTOMER").description("Khách hàng - giao dịch, xem số dư, đổi PIN").build());
        log.info("  ✅ Đã tạo 3 roles");

        // ══════════════════════════════════════════
        // 2. TẠO USERS
        // ══════════════════════════════════════════

        // --- Admin ---
        User admin = saveUser("admin", encodedPassword, "admin@rikkeibank.vn", "0900000001", adminRole, true, true);

        // --- Staff ---
        User staff01 = saveUser("staff01", encodedPassword, "staff01@rikkeibank.vn", "0900000002", staffRole, true, true);
        User staff02 = saveUser("staff02", encodedPassword, "staff02@rikkeibank.vn", "0900000003", staffRole, true, true);

        // --- Customers (đa dạng trạng thái) ---
        User cust01 = saveUser("customer01", encodedPassword, "nguyenvanan@gmail.com",  "0912345001", customerRole, true, true);
        User cust02 = saveUser("customer02", encodedPassword, "trranthibich@gmail.com", "0912345002", customerRole, true, true);
        User cust03 = saveUser("customer03", encodedPassword, "lequangcuong@gmail.com", "0912345003", customerRole, true, true);
        User cust04 = saveUser("customer04", encodedPassword, "phamthidung@gmail.com",  "0912345004", customerRole, true, true);
        User cust05 = saveUser("customer05", encodedPassword, "hoangvanem@gmail.com",   "0912345005", customerRole, true, false);  // KYC chưa duyệt
        User cust06 = saveUser("customer06", encodedPassword, "dangthiphuong@gmail.com","0912345006", customerRole, true, false);  // KYC bị từ chối
        User cust07 = saveUser("customer07", encodedPassword, "vothigiang@gmail.com",   "0912345007", customerRole, false, false); // User bị khóa

        log.info("  ✅ Đã tạo 10 users (1 admin, 2 staff, 7 customers)");

        // ══════════════════════════════════════════
        // 3. TẠO ACCOUNTS
        // ══════════════════════════════════════════
        Account accAdmin   = saveAccount("9000000001", admin,   encodedPin, new BigDecimal("999999999"), AccountStatus.ACTIVE);
        Account accStaff01 = saveAccount("9000000002", staff01, encodedPin, new BigDecimal("50000000"),  AccountStatus.ACTIVE);
        Account accStaff02 = saveAccount("9000000003", staff02, encodedPin, new BigDecimal("45000000"),  AccountStatus.ACTIVE);

        Account accCust01 = saveAccount("1001000001", cust01, encodedPin, new BigDecimal("75000000"),  AccountStatus.ACTIVE);
        Account accCust02 = saveAccount("1001000002", cust02, encodedPin, new BigDecimal("32500000"),  AccountStatus.ACTIVE);
        Account accCust03 = saveAccount("1001000003", cust03, encodedPin, new BigDecimal("120000000"), AccountStatus.ACTIVE);
        Account accCust04 = saveAccount("1001000004", cust04, encodedPin, new BigDecimal("8500000"),   AccountStatus.ACTIVE);
        Account accCust05 = saveAccount("1001000005", cust05, encodedPin, new BigDecimal("15000000"),  AccountStatus.ACTIVE);
        Account accCust06 = saveAccount("1001000006", cust06, encodedPin, new BigDecimal("5000000"),   AccountStatus.ACTIVE);
        Account accCust07 = saveAccount("1001000007", cust07, encodedPin, new BigDecimal("22000000"),  AccountStatus.LOCKED);

        log.info("  ✅ Đã tạo 10 accounts");

        // ══════════════════════════════════════════
        // 4. TẠO KYC PROFILES
        // ══════════════════════════════════════════
        // 4 hồ sơ đã duyệt (CONFIRM)
        saveKyc(cust01, "Nguyễn Văn An",   "079201001001", LocalDate.of(1995, 3, 15),
                Gender.MALE,   "123 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM",
                KycStatus.CONFIRM, null);

        saveKyc(cust02, "Trần Thị Bích",   "079201002002", LocalDate.of(1998, 7, 22),
                Gender.FEMALE, "456 Lê Lợi, Phường Bến Thành, Quận 1, TP.HCM",
                KycStatus.CONFIRM, null);

        saveKyc(cust03, "Lê Quang Cường",  "079201003003", LocalDate.of(1992, 11, 8),
                Gender.MALE,   "789 Trần Hưng Đạo, Phường 2, Quận 5, TP.HCM",
                KycStatus.CONFIRM, null);

        saveKyc(cust04, "Phạm Thị Dung",   "079201004004", LocalDate.of(2000, 1, 30),
                Gender.FEMALE, "101 Nguyễn Trãi, Phường 3, Quận 5, TP.HCM",
                KycStatus.CONFIRM, null);

        // 1 hồ sơ đang chờ duyệt
        saveKyc(cust05, "Hoàng Văn Em",    "079201005005", LocalDate.of(1997, 5, 12),
                Gender.MALE,   "202 Lý Tự Trọng, Phường Bến Thành, Quận 1, TP.HCM",
                KycStatus.PENDING, null);

        // 1 hồ sơ bị từ chối
        saveKyc(cust06, "Đặng Thị Phương", "079201006006", LocalDate.of(1999, 9, 5),
                Gender.FEMALE, "303 Hai Bà Trưng, Phường Tân Định, Quận 1, TP.HCM",
                KycStatus.REJECT, "Ảnh CCCD mặt trước bị mờ, không đọc được thông tin. Vui lòng chụp lại ảnh rõ nét hơn.");

        // 1 hồ sơ PENDING + user bị khóa
        saveKyc(cust07, "Võ Thị Giang",    "079201007007", LocalDate.of(1996, 12, 18),
                Gender.FEMALE, "404 Điện Biên Phủ, Phường 25, Quận Bình Thạnh, TP.HCM",
                KycStatus.PENDING, null);

        log.info("  ✅ Đã tạo 7 KYC profiles (4 CONFIRM, 1 PENDING, 1 REJECT, 1 PENDING+locked)");

        // ══════════════════════════════════════════
        // 5. TẠO TRANSACTIONS (15 giao dịch mẫu)
        // ══════════════════════════════════════════
        LocalDateTime now = LocalDateTime.now();

        // --- Ngày 25/06 ---
        saveTxn("TXN20260625001", accCust01, accCust02, new BigDecimal("5000000"),
                "Chuyển tiền ăn trưa tháng 6", TransactionStatus.SUCCESS, TransactionType.INTERNAL);
        saveTxn("TXN20260625002", accCust03, accCust01, new BigDecimal("12000000"),
                "Trả tiền mua laptop cũ", TransactionStatus.SUCCESS, TransactionType.INTERNAL);

        // --- Ngày 26/06 ---
        saveTxn("TXN20260626001", accCust01, accCust04, new BigDecimal("3500000"),
                "Cho mượn tiền đóng học phí", TransactionStatus.SUCCESS, TransactionType.INTERNAL);
        saveTxn("TXN20260626002", accCust02, accCust03, new BigDecimal("1500000"),
                "Tiền góp quỹ team building", TransactionStatus.SUCCESS, TransactionType.INTERNAL);

        // --- Ngày 27/06 ---
        saveTxn("TXN20260627001", accCust04, accCust01, new BigDecimal("2000000"),
                "Trả tiền vay tuần trước", TransactionStatus.SUCCESS, TransactionType.INTERNAL);
        saveTxn("TXN20260627002", accCust01, accCust03, new BigDecimal("8000000"),
                "Thanh toán tiền thuê nhà tháng 7", TransactionStatus.SUCCESS, TransactionType.INTERNAL);

        // --- Ngày 28/06 ---
        saveTxn("TXN20260628001", accCust03, accCust02, new BigDecimal("4500000"),
                "Chuyển tiền mua vé máy bay đi Đà Nẵng", TransactionStatus.SUCCESS, TransactionType.INTERNAL);
        saveTxn("TXN20260628002", accCust02, accCust04, new BigDecimal("750000"),
                "Tiền cà phê nhóm tuần này", TransactionStatus.SUCCESS, TransactionType.INTERNAL);

        // --- Ngày 29/06 ---
        saveTxn("TXN20260629001", accCust01, accCust02, new BigDecimal("15000000"),
                "Đặt cọc mua xe máy Honda Wave", TransactionStatus.SUCCESS, TransactionType.INTERNAL);
        saveTxn("TXN20260629002", accCust04, accCust03, new BigDecimal("6000000"),
                "Tiền học phí khóa tiếng Anh giao tiếp", TransactionStatus.SUCCESS, TransactionType.INTERNAL);

        // --- Ngày 30/06 ---
        saveTxn("TXN20260630001", accCust03, accCust01, new BigDecimal("2500000"),
                "Hoàn tiền đặt cọc phòng trọ tháng 5", TransactionStatus.SUCCESS, TransactionType.INTERNAL);
        saveTxn("TXN20260630002", accCust02, accCust01, new BigDecimal("10000000"),
                "Trả nợ tháng 6 - đợt cuối", TransactionStatus.SUCCESS, TransactionType.INTERNAL);

        // --- Hôm nay 01/07 ---
        saveTxn("TXN20260701001", accCust01, accCust04, new BigDecimal("1000000"),
                "Mừng sinh nhật bạn Dung", TransactionStatus.SUCCESS, TransactionType.INTERNAL);
        saveTxn("TXN20260701002", accCust04, accCust02, new BigDecimal("50000000"),
                "Chuyển tiền lớn - SỐ DƯ KHÔNG ĐỦ", TransactionStatus.FAILED, TransactionType.INTERNAL);
        saveTxn("TXN20260701003", accCust01, accCust03, new BigDecimal("3000000"),
                "Tiền ăn uống team cuối tuần", TransactionStatus.SUCCESS, TransactionType.INTERNAL);

        log.info("  ✅ Đã tạo 15 transactions (13 SUCCESS, 1 FAILED, 1 SUCCESS hôm nay)");

        // ══════════════════════════════════════════
        // TỔNG KẾT
        // ══════════════════════════════════════════
        log.info("════════════════════════════════════════════════════════════════════");
        log.info("  🎉 SEED DỮ LIỆU HOÀN TẤT!");
        log.info("════════════════════════════════════════════════════════════════════");
        log.info("  📊 Tổng kết:");
        log.info("     • 3 Roles       (ROLE_ADMIN, ROLE_STAFF, ROLE_CUSTOMER)");
        log.info("     • 10 Users      (1 admin, 2 staff, 7 customers)");
        log.info("     • 10 Accounts   (9 ACTIVE, 1 LOCKED)");
        log.info("     • 7 KYC Profiles (4 CONFIRM, 1 PENDING, 1 REJECT, 1 PENDING+locked)");
        log.info("     • 15 Transactions (13 SUCCESS, 1 FAILED)");
        log.info("  ────────────────────────────────────────────────────────────────");
        log.info("  🔑 Đăng nhập:");
        log.info("     Mật khẩu: Admin@1234 (tất cả tài khoản)");
        log.info("     PIN:      123456     (tất cả tài khoản)");
        log.info("  ────────────────────────────────────────────────────────────────");
        log.info("  👤 Tài khoản test nhanh:");
        log.info("     admin      → ADMIN    | 9000000001 | 999,999,999 VND");
        log.info("     customer01 → CUSTOMER | 1001000001 | 75,000,000  VND ✅");
        log.info("     customer02 → CUSTOMER | 1001000002 | 32,500,000  VND ✅");
        log.info("     customer05 → CUSTOMER | 1001000005 | 15,000,000  VND ⏳ KYC PENDING");
        log.info("     customer06 → CUSTOMER | 1001000006 | 5,000,000   VND ❌ KYC REJECT");
        log.info("     customer07 → CUSTOMER | 1001000007 | 22,000,000  VND 🔒 LOCKED");
        log.info("════════════════════════════════════════════════════════════════════");
    }

    // ═══════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════

    private User saveUser(String username, String encodedPassword, String email,
                          String phone, Role role, boolean isActive, boolean isKyc) {
        User user = User.builder()
                .username(username)
                .password(encodedPassword)
                .email(email)
                .phoneNumber(phone)
                .role(role)
                .isActive(isActive)
                .isKyc(isKyc)
                .build();
        return userRepository.save(user);
    }

    private Account saveAccount(String accountNumber, User user, String encodedPin,
                                BigDecimal balance, AccountStatus status) {
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .user(user)
                .transactionPin(encodedPin)
                .balance(balance)
                .currency("VND")
                .status(status)
                .active(true)
                .build();
        return accountRepository.save(account);
    }

    private void saveKyc(User user, String fullName, String idNumber, LocalDate dob,
                          Gender gender, String address, KycStatus status, String rejectReason) {
        KycProfile kyc = KycProfile.builder()
                .user(user)
                .fullName(fullName)
                .idNumber(idNumber)
                .dob(dob)
                .gender(gender)
                .address(address)
                .cccdFrontUrl("https://res.cloudinary.com/rikkeibank/image/upload/kyc/" + idNumber + "_front.jpg")
                .cccdBackUrl("https://res.cloudinary.com/rikkeibank/image/upload/kyc/" + idNumber + "_back.jpg")
                .selfieUrl("https://res.cloudinary.com/rikkeibank/image/upload/kyc/" + idNumber + "_selfie.jpg")
                .status(status)
                .rejectReason(rejectReason)
                .verifiedAt(status == KycStatus.CONFIRM ? LocalDateTime.now() : null)
                .build();
        kycProfileRepository.save(kyc);
    }

    private void saveTxn(String txnCode, Account from, Account to,
                          BigDecimal amount, String description,
                          TransactionStatus status, TransactionType type) {
        Transaction txn = Transaction.builder()
                .transactionCode(txnCode)
                .fromAccount(from)
                .toAccount(to)
                .amount(amount)
                .description(description)
                .status(status)
                .type(type)
                .build();
        transactionRepository.save(txn);
    }
}
