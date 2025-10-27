package com.serverless.platformselector.service;

import com.serverless.platformselector.dto.ReqRes;
import com.serverless.platformselector.entity.OurUsers;
import com.serverless.platformselector.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class UserManagementService {

    @Autowired
    private UsersRepo usersRepo;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register a new user
     */
    public ReqRes register(ReqRes registrationRequest) {
        ReqRes resp = new ReqRes();
        try {
            OurUsers user = new OurUsers();
            user.setEmail(registrationRequest.getEmail());
            user.setCity(registrationRequest.getCity());
            user.setName(registrationRequest.getName());

            // ✅ Default role if none provided
            String role = registrationRequest.getRole();
            if (role == null || role.trim().isEmpty()) {
                role = "USER";
            }
            user.setRole(role.toUpperCase());

            user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));

            OurUsers savedUser = usersRepo.save(user);
            if (savedUser.getId() > 0) {
                resp.setOurUsers(savedUser);
                resp.setMessage("User registered successfully with role: " + role);
                resp.setStatusCode(201);
            }
        } catch (Exception e) {
            resp.setStatusCode(500);
            resp.setError("Error registering user: " + e.getMessage());
        }
        return resp;
    }

    /**
     * Login existing user and issue JWT + Refresh token
     */
    public ReqRes login(ReqRes loginRequest) {
        ReqRes response = new ReqRes();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            OurUsers user = usersRepo.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Generate JWT and Refresh tokens with proper expiration
            String jwt = jwtUtils.generateToken(user);
            String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

            response.setStatusCode(200);
            response.setToken(jwt);
            response.setRefreshToken(refreshToken);
            response.setExpirationTime("1 hour");
            response.setMessage("Login successful");
        } catch (Exception e) {
            response.setStatusCode(401);
            response.setMessage("Invalid credentials: " + e.getMessage());
        }
        return response;
    }

    /**
     * Refresh JWT token
     */
    public ReqRes refreshToken(ReqRes refreshTokenRequest) {
        ReqRes response = new ReqRes();
        try {
            String email = jwtUtils.extractUsername(refreshTokenRequest.getToken());
            OurUsers user = usersRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (jwtUtils.isTokenValid(refreshTokenRequest.getToken(), user)) {
                String newJwt = jwtUtils.generateToken(user);
                response.setStatusCode(200);
                response.setToken(newJwt);
                response.setRefreshToken(refreshTokenRequest.getToken());
                response.setExpirationTime("1 hour");
                response.setMessage("Token refreshed successfully");
            } else {
                response.setStatusCode(401);
                response.setMessage("Invalid or expired refresh token");
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error refreshing token: " + e.getMessage());
        }
        return response;
    }

    /**
     * Retrieve all users
     */
    public ReqRes getAllUsers() {
        ReqRes reqRes = new ReqRes();
        try {
            List<OurUsers> users = usersRepo.findAll();
            if (!users.isEmpty()) {
                reqRes.setOurUsersList(users);
                reqRes.setStatusCode(200);
                reqRes.setMessage("Success");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("No users found");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error retrieving users: " + e.getMessage());
        }
        return reqRes;
    }

    /**
     * Retrieve user by ID
     */
    public ReqRes getUsersById(Integer id) {
        ReqRes reqRes = new ReqRes();
        try {
            OurUsers user = usersRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            reqRes.setOurUsers(user);
            reqRes.setStatusCode(200);
            reqRes.setMessage("User found");
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error fetching user: " + e.getMessage());
        }
        return reqRes;
    }

    /**
     * Delete user
     */
    public ReqRes deleteUser(Integer userId) {
        ReqRes reqRes = new ReqRes();
        try {
            Optional<OurUsers> user = usersRepo.findById(userId);
            if (user.isPresent()) {
                usersRepo.deleteById(userId);
                reqRes.setStatusCode(200);
                reqRes.setMessage("User deleted successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error deleting user: " + e.getMessage());
        }
        return reqRes;
    }

    /**
     * Update user info
     */
    public ReqRes updateUser(Integer userId, OurUsers updateUser) {
        ReqRes reqRes = new ReqRes();
        try {
            Optional<OurUsers> userOpt = usersRepo.findById(userId);
            if (userOpt.isPresent()) {
                OurUsers existingUser = userOpt.get();
                existingUser.setEmail(updateUser.getEmail());
                existingUser.setName(updateUser.getName());
                existingUser.setCity(updateUser.getCity());
                existingUser.setRole(updateUser.getRole() != null ? updateUser.getRole() : existingUser.getRole());

                if (updateUser.getPassword() != null && !updateUser.getPassword().isEmpty()) {
                    existingUser.setPassword(passwordEncoder.encode(updateUser.getPassword()));
                }
                usersRepo.save(existingUser);

                reqRes.setStatusCode(200);
                reqRes.setMessage("User updated successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error updating user: " + e.getMessage());
        }
        return reqRes;
    }

    /**
     * Get info for current logged-in user
     */
    public ReqRes getMyInfo(String email) {
        ReqRes reqRes = new ReqRes();
        try {
            Optional<OurUsers> userOpt = usersRepo.findByEmail(email);
            if (userOpt.isPresent()) {
                reqRes.setOurUsers(userOpt.get());
                reqRes.setStatusCode(200);
                reqRes.setMessage("User info retrieved");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error getting user info: " + e.getMessage());
        }
        return reqRes;
    }
}
