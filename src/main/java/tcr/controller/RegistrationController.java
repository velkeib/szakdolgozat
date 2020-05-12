package tcr.controller;

import tcr.Application;
import tcr.entity.*;
import tcr.repository.*;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.*;
import java.util.*;
import java.lang.*;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindingResult;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.validation.*;
import javax.xml.ws.RequestWrapper;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.security.Principal;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
@Controller
public class RegistrationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @RequestMapping(value = "/", method=RequestMethod.GET)
    public String getIndex(Model model) {

        model.addAttribute("user", new User());
        model.addAttribute("success", "");
        model.addAttribute("error", "");

        return "index";
    }

    @RequestMapping(value = "/", method=RequestMethod.POST)
    public String createUser(@ModelAttribute(value="user") User user, Model model) {
        Optional<User> oUser = userRepository.findByUsername(user.getUsername());
        model.addAttribute("error", "");

        if (oUser.isPresent()) {
            model.addAttribute("error", "Email address already in use!");
            model.addAttribute("user", new User());
            model.addAttribute("success", "");

            return "index";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        user.setRole(User.Role.ROLE_GUEST);
        userRepository.save(user);

        model.addAttribute("success", "Account successfully created");
        model.addAttribute("user", new User());

        return "index";
    }

    @RequestMapping("/login")
    public String login(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("error", "");
        if(error != null){
            model.addAttribute("error", "Email address and/or password is wrong!");
        }

        return "login";
    }
}
