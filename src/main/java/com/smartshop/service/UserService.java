package com.smartshop.service;

import com.smartshop.entity.user.Role;
import com.smartshop.entity.user.User;
import com.smartshop.repository.RoleRepository;
import com.smartshop.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Lấy tất cả người dùng
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Lấy người dùng theo ID
     */
    public User getUserById(Long userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    /**
     * Kích hoạt/vô hiệu hóa tài khoản người dùng
     */
    public User toggleUserActiveStatus(Long userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        user.setActive(!user.isActive());
        return userRepository.save(user);
    }

    /**
     * Đặt trạng thái active cho người dùng
     */
    public User setUserActiveStatus(Long userId, boolean isActive) {
        Objects.requireNonNull(userId, "userId must not be null");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        user.setActive(isActive);
        return userRepository.save(user);
    }

    /**
     * Lấy tất cả các vai trò (roles) có sẵn
     */
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    /**
     * Thêm vai trò cho người dùng
     */
    public User addRoleToUser(Long userId, Long roleId) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(roleId, "roleId must not be null");
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        
        // Kiểm tra xem user đã có role này chưa
        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            return userRepository.save(user);
        }
        
        return user;
    }

    /**
     * Xóa vai trò khỏi người dùng
     */
    public User removeRoleFromUser(Long userId, Long roleId) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(roleId, "roleId must not be null");
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        
        // Xóa role khỏi user
        user.getRoles().remove(role);
        return userRepository.save(user);
    }

    /**
     * Thêm vai trò cho người dùng theo tên role
     */
    public User addRoleToUserByName(Long userId, String roleName) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(roleName, "roleName must not be null");
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));
        
        // Kiểm tra xem user đã có role này chưa
        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            return userRepository.save(user);
        }
        
        return user;
    }

    /**
     * Xóa vai trò khỏi người dùng theo tên role
     */
    public User removeRoleFromUserByName(Long userId, String roleName) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(roleName, "roleName must not be null");
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));
        
        // Xóa role khỏi user
        user.getRoles().remove(role);
        return userRepository.save(user);
    }

    /**
     * Thay đổi vai trò của người dùng (xóa tất cả roles cũ và thêm role mới)
     */
    public User changeUserRole(Long userId, Long roleId) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(roleId, "roleId must not be null");
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        
        // Xóa tất cả roles cũ và thêm role mới
        user.getRoles().clear();
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    /**
     * Thay đổi vai trò của người dùng theo tên role
     */
    public User changeUserRoleByName(Long userId, String roleName) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(roleName, "roleName must not be null");
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));
        
        // Xóa tất cả roles cũ và thêm role mới
        user.getRoles().clear();
        user.getRoles().add(role);
        return userRepository.save(user);
    }
}

