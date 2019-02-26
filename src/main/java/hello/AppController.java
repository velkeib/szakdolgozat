package hello;

import hello.entity.User;
import hello.repository.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindingResult;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.validation.*;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@Controller
public class AppController {

    @Autowired
    private UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(Application.class);


    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        log.info(principal.getName());

        model.addAttribute("authenticatedUser", userRepository.findByUsername(principal.getName()));

        return "home";
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("user", new User());
        for (User customer : userRepository.findAll()) {
            log.info(customer.toString());
        }
        return "index";
    }

    /*@PostMapping("/")
    public String indexSubmit(@ModelAttribute User user) {
        return "/";
    }*/

    @RequestMapping(value = "/", method=RequestMethod.POST)
    public String processForm(@ModelAttribute(value="user") User user) {
        Optional<User> oUser = userRepository.findByUsername(user.getUsername());

        //log.info(user.toString());

        if (oUser.isPresent()) {
            return "/index";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        user.setRole(User.Role.ROLE_USER);
        userRepository.save(user);
        return "/index";
    }

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("register")
    public ResponseEntity<User> register(@RequestBody User user) {
        Optional<User> oUser = userRepository.findByUsername(user.getUsername());
        if (oUser.isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        user.setRole(User.Role.ROLE_USER);
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PostMapping("login")
    public ResponseEntity login(@RequestBody User user) {
        return ResponseEntity.ok().build();
    }

    @Bean
    public CommandLineRunner demo(UserRepository repository) {
        return (args) -> {
            // save a couple of customer
            repository.save(new User(1L, "sad@sad.hu", "Jack", "Sparrow", "$2a$04$YDiv9c./ytEGZQopFfExoOgGlJL6/o0er0K.hiGb5TGKHUL8Ebn..", true, User.Role.ROLE_USER));
            repository.save(new User(2L, "test@stest.hu", "Bence", "Velkei", "$2a$10$OCdR/doDzVLvDpJ1QtJO3ONJR80EQHeVXhnA7SB6qrlR0vwecTfhy", true, User.Role.ROLE_USER));
            repository.save(new User(3L, "test", "test", "test", "$2a$10$iKieGnwWVKdjRmRLnv/qG.4IZQ761q7/xORG4hAfbF5NvMW.jOovq", true, User.Role.ROLE_USER));

            //repository.save(new User("Chloe", "O'Brian", "asd@sad.hu"));
            //repository.save(new User("Kim", "Bauer", "asd@sad.hu"));
            //repository.save(new User("David", "Palmer", "asd@sad.hu"));
            //repository.save(new User("Michelle", "Dessler", "asd@sad.hu"));

            // fetch all customers
            log.info("Customers found with findAll():");
            log.info("-------------------------------");
            for (User customer : repository.findAll()) {
                log.info(customer.toString());
            }
            log.info("");

            // fetch an individual customer by ID
            repository.findById(2L)
                    .ifPresent(customer -> {
                        log.info("Customer found with findById(1L):");
                        log.info("--------------------------------");
                        log.info(customer.toString());
                        log.info("");
                    });

            // fetch customers by last name
            //log.info("Customer found with findByLastName('Bauer'):");
            //log.info("--------------------------------------------");

            // for (Customer bauer : repository.findByLastName("Bauer")) {
            // 	log.info(bauer.toString());
            // }
            log.info("");
        };
    }


}
