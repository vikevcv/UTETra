package com.ute.trasua.service.impl;

import com.ute.trasua.dto.EmployeeRequest;
import com.ute.trasua.dto.EmployeeResponse;
import com.ute.trasua.dto.PosAccountRequest;
import com.ute.trasua.dto.PosAccountResponse;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.Employee;
import com.ute.trasua.entity.PosAccount;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.BranchRepository;
import com.ute.trasua.repository.EmployeeRepository;
import com.ute.trasua.repository.PosAccountRepository;
import com.ute.trasua.repository.UserRepository;
import com.ute.trasua.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service implementation handling Employee management and POS accounts at Branch (UC16).
 */
@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;
    private final PosAccountRepository posAccountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               BranchRepository branchRepository,
                               PosAccountRepository posAccountRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        super();
        this.employeeRepository = employeeRepository;
        this.branchRepository = branchRepository;
        this.posAccountRepository = posAccountRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ==========================================
    // EMPLOYEE MANAGEMENT (UC16)
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        return EmployeeResponse.fromEntity(getEmployeeEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Employee getEmployeeEntityById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> searchEmployees(Long branchId,
                                                  String keyword,
                                                  String status,
                                                  Pageable pageable) {
        Page<Employee> page = employeeRepository.searchEmployees(branchId, keyword, status, pageable);
        return page.map(EmployeeResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllByBranch(Long branchId) {
        return employeeRepository.findByBranchId(branchId).stream()
                .map(EmployeeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (request.getEmployeeCode() == null || request.getEmployeeCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee code must not be empty!");
        }

        String code = request.getEmployeeCode().trim().toUpperCase();
        if (employeeRepository.existsByEmployeeCode(code)) {
            throw new IllegalArgumentException("Employee code [" + code + "] already exists!");
        }

        if (request.getBranchId() == null) {
            throw new IllegalArgumentException("Please select a branch for the employee!");
        }

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new IllegalArgumentException("Assigned branch does not exist!"));

        Employee emp = Employee.builder()
                .employeeCode(code)
                .fullName(request.getFullName().trim())
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .citizenId(request.getCitizenId() != null ? request.getCitizenId().trim() : null)
                .email(request.getEmail() != null ? request.getEmail().trim() : null)
                .salary(request.getSalary() != null ? request.getSalary() : 0.0)
                .status(request.getStatus() != null ? request.getStatus().trim() : "ACTIVE")
                .hireDate(request.getHireDate() != null ? request.getHireDate() : java.time.LocalDate.now())
                .branch(branch)
                .build();

        Employee saved = employeeRepository.save(emp);
        if (userRepository != null && saved.getEmail() != null && !saved.getEmail().isBlank()) {
            userRepository.findByEmail(saved.getEmail().trim().toLowerCase()).ifPresent(u -> {
                if (u.getBranch() == null) {
                    u.setBranch(branch);
                    userRepository.save(u);
                }
            });
        }
        log.info(">>> Onboarded new employee: [{}] {} - Branch: {}", saved.getEmployeeCode(), saved.getFullName(), branch.getName());
        return EmployeeResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee emp = getEmployeeEntityById(id);

        if (request.getEmployeeCode() != null && !request.getEmployeeCode().trim().isEmpty()) {
            String code = request.getEmployeeCode().trim().toUpperCase();
            if (employeeRepository.existsByEmployeeCodeAndIdNot(code, id)) {
                throw new IllegalArgumentException("Employee code [" + code + "] belongs to another employee!");
            }
            emp.setEmployeeCode(code);
        }

        if (request.getFullName() != null) emp.setFullName(request.getFullName().trim());
        if (request.getPhone() != null) emp.setPhone(request.getPhone().trim());
        if (request.getCitizenId() != null) emp.setCitizenId(request.getCitizenId().trim());
        if (request.getEmail() != null) emp.setEmail(request.getEmail().trim());
        if (request.getSalary() != null) emp.setSalary(request.getSalary());
        if (request.getStatus() != null) emp.setStatus(request.getStatus().trim());
        if (request.getHireDate() != null) emp.setHireDate(request.getHireDate());

        if (request.getBranchId() != null && !request.getBranchId().equals(emp.getBranch().getId())) {
            Branch newBranch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new IllegalArgumentException("Transferred branch does not exist!"));
            emp.setBranch(newBranch);
            if (userRepository != null && emp.getEmail() != null && !emp.getEmail().isBlank()) {
                userRepository.findByEmail(emp.getEmail().trim().toLowerCase()).ifPresent(u -> {
                    u.setBranch(newBranch);
                    userRepository.save(u);
                });
            }
            log.info(">>> Transferred employee ID {} to branch {}", id, newBranch.getName());
        }

        Employee updated = employeeRepository.save(emp);
        return EmployeeResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        Employee emp = getEmployeeEntityById(id);
        emp.setStatus(status);
        employeeRepository.save(emp);
        log.info(">>> Updated employee [{}] status to: {}", emp.getEmployeeCode(), status);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee emp = getEmployeeEntityById(id);
        employeeRepository.delete(emp);
        log.info(">>> Deleted employee profile ID {}: [{}] {}", id, emp.getEmployeeCode(), emp.getFullName());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByBranch(Long branchId) {
        return employeeRepository.countByBranchId(branchId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveByBranch(Long branchId) {
        return employeeRepository.countByBranchIdAndStatus(branchId, "ACTIVE");
    }

    // ==========================================
    // POS ACCOUNT MANAGEMENT (UC16)
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<PosAccountResponse> getPosAccountsByBranch(Long branchId) {
        return posAccountRepository.findByBranchId(branchId).stream()
                .map(PosAccountResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PosAccountResponse createPosAccount(PosAccountRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("POS account username must not be empty!");
        }

        String username = request.getUsername().trim().toLowerCase();
        if (posAccountRepository.existsByUsername(username) || userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username [" + username + "] is already in use in the system!");
        }

        if (request.getBranchId() == null) {
            throw new IllegalArgumentException("Please select a branch for the POS account!");
        }

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new IllegalArgumentException("Branch does not exist!"));

        String rawPassword = request.getPassword() != null && !request.getPassword().trim().isEmpty()
                ? request.getPassword().trim() : "Pos@123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 1. Tạo và đồng bộ bản ghi User trong bảng users để xác thực Spring Security
        String email = username + "@pos.trasua.local";
        if (userRepository.existsByEmail(email)) {
            email = username + "." + branch.getId() + "@pos.trasua.local";
        }

        User user = User.builder()
                .username(username)
                .password(encodedPassword)
                .fullName(request.getCounterName() != null ? request.getCounterName().trim() : "Quầy POS - " + branch.getName())
                .email(email)
                .phone(branch.getHotline())
                .branch(branch)
                .enabled(request.isActive())
                .locked(!request.isActive())
                .roles(new HashSet<>(Set.of("ROLE_STAFF", "ROLE_POS")))
                .createdAt(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(user);

        // 2. Tạo bản ghi PosAccount liên kết với User và Branch
        PosAccount pos = PosAccount.builder()
                .counterName(request.getCounterName() != null ? request.getCounterName().trim() : "Counter POS")
                .username(username)
                .password(encodedPassword)
                .active(request.isActive())
                .note(request.getNote() != null ? request.getNote().trim() : null)
                .branch(branch)
                .user(savedUser)
                .build();

        PosAccount saved = posAccountRepository.save(pos);
        log.info(">>> Configured POS account: [{}] for branch {} (Synchronized User ID: {})", saved.getUsername(), branch.getName(), savedUser.getId());
        return PosAccountResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public boolean togglePosAccount(Long id) {
        PosAccount pos = posAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("POS account not found with ID: " + id));
        boolean newStatus = !pos.isActive();
        pos.setActive(newStatus);
        posAccountRepository.save(pos);

        // Đồng bộ trạng thái khóa/mở khóa sang bảng users
        userRepository.findByUsername(pos.getUsername()).ifPresent(user -> {
            user.setEnabled(newStatus);
            user.setLocked(!newStatus);
            user.setLockReason(!newStatus ? "Tạm khóa theo trạng thái máy POS chi nhánh" : null);
            userRepository.save(user);
        });

        log.info(">>> Toggled POS account [{}] to: {} (Synced to users table)", pos.getUsername(), newStatus ? "Active" : "Locked");
        return newStatus;
    }

    @Override
    @Transactional
    public void deletePosAccount(Long id) {
        PosAccount pos = posAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("POS account not found with ID: " + id));
        String username = pos.getUsername();
        posAccountRepository.delete(pos);

        // Xóa tài khoản tương ứng trong bảng users
        userRepository.findByUsername(username).ifPresent(user -> {
            userRepository.delete(user);
            log.info(">>> Deleted corresponding User record: [{}]", username);
        });

        log.info(">>> Deleted POS account ID {}: [{}]", id, username);
    }
}
