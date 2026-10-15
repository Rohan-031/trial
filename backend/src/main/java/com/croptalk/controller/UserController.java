package com.croptalk.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.croptalk.model.User;
import com.croptalk.service.UserService;
import com.croptalk.util.JsonUtil;

/** Port of routes/users.js (mounted at /api/users). */
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/userdata/{clerkid}")
    public User userInfo(@PathVariable String clerkid) {
        return userService.userInfo(clerkid);
    }

    @GetMapping("/finduser/{username}")
    public User findUser(@PathVariable String username) {
        return userService.findUser(username);
    }

    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/login/{uname}")
    public Map<String, Object> userLogin(@PathVariable String uname) {
        return userService.userLogin(uname);
    }

    @PostMapping("/register")
    public void userRegister() {
        userService.userRegister();
    }

    @PostMapping("/insert")
    public ResponseEntity<Object> insertUser(@RequestBody(required = false) Map<String, Object> body) {
        UserService.ResponseHolder result = userService.insertUser(JsonUtil.orEmpty(body));
        return ResponseEntity.status(result.status()).body(result.body());
    }
}
