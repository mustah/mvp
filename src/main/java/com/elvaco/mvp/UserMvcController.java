package com.elvaco.mvp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserMvcController {
    private final UserRepository userRepository;
    @Autowired
    UserMvcController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @RequestMapping("/users")
    String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }
}
