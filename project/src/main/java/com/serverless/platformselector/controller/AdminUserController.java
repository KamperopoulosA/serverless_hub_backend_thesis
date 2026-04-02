package com.serverless.platformselector.controller;

import com.serverless.platformselector.dto.ReqRes;
import com.serverless.platformselector.entity.OurUsers;
import com.serverless.platformselector.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUserController {

    @Autowired
    private UserManagementService userManagementService;

    // 1. Get all users
    @GetMapping
    public ResponseEntity<ReqRes> getAllUsers() {
        return ResponseEntity.ok(userManagementService.getAllUsers());
    }

    // 2. Get single user
    @GetMapping("/{id}")
    public ResponseEntity<ReqRes> getUser(@PathVariable Integer id) {
        return ResponseEntity.ok(userManagementService.getUsersById(id));
    }

    // 3. Update user
    @PutMapping("/{id}")
    public ResponseEntity<ReqRes> updateUser(@PathVariable Integer id,
                                             @RequestBody OurUsers user) {
        return ResponseEntity.ok(userManagementService.updateUser(id, user));
    }

    // 4. Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<ReqRes> deleteUser(@PathVariable Integer id) {
        return ResponseEntity.ok(userManagementService.deleteUser(id));
    }

    // 5. Activate / deactivate
    @PutMapping("/{id}/toggle")
    public ResponseEntity<ReqRes> toggleActive(@PathVariable Integer id) {
        return ResponseEntity.ok(userManagementService.toggleActive(id));
    }
}
