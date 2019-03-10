package hello;

import hello.dto.RecordDto;
import hello.entity.Court;
import hello.entity.TimeRecord;
import hello.entity.User;
import hello.repository.CourtRepository;
import hello.repository.TimeRecordRepository;
import hello.repository.UserRepository;

import java.util.*;
import java.text.SimpleDateFormat;
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

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private TimeRecordRepository recordRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final Logger log = LoggerFactory.getLogger(Application.class);


    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        log.info(principal.getName());

        String name = "";
        Iterable<TimeRecord> records = recordRepository.findAll();
        List<RecordDto> rec = new ArrayList<>();
        for(TimeRecord s: records){
            log.info(s.getCustomer().toString());
            log.info(userRepository.findById(s.getCustomer()).toString());
            log.info(userRepository.findById(s.getCustomer()).get().getFirstName() + " " + userRepository.findById(s.getCustomer()).get().getLastName());
            name = userRepository.findById(s.getCustomer()).get().getFirstName() + " " + userRepository.findById(s.getCustomer()).get().getLastName();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            rec.add(new RecordDto(s.getId(), name, formatter.format(s.getStartDate().getTime()),
                    formatter.format(s.getStartDate().getTime()), s.getCourtID(),
                    s.getReason()));

    }


        model.addAttribute("authenticatedUser", userRepository.findByUsername(principal.getName()).get());
        model.addAttribute("tennisCourts", courtRepository.findAll());
        model.addAttribute("test", recordRepository.findAll());
        model.addAttribute("reservations", rec);
        //model.addAttribute("map", map);

        return "home";
    }

    @RequestMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("user", new User());
        for (User customer : userRepository.findAll()) {
            log.info(customer.toString());
        }
        return "index";
    }

    @GetMapping("/reservation/{id}")
    public String reservation(@PathVariable String id, Model model) {
        model.addAttribute("court", new Court());
        log.info(id);

        return "reservation";
    }

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
    public CommandLineRunner demo(UserRepository repository, CourtRepository courtRepository, TimeRecordRepository recordRepository) {
        return (args) -> {
            // save a couple of customer
            repository.save(new User("sad@sad.hu", "Jack", "Sparrow", "$2a$04$YDiv9c./ytEGZQopFfExoOgGlJL6/o0er0K.hiGb5TGKHUL8Ebn.."));
            repository.save(new User( "test@stest.hu", "Bence", "Velkei", "$2a$10$OCdR/doDzVLvDpJ1QtJO3ONJR80EQHeVXhnA7SB6qrlR0vwecTfhy"));
            repository.save(new User( "test", "Geza", "Nagy", "$2a$10$iKieGnwWVKdjRmRLnv/qG.4IZQ761q7/xORG4hAfbF5NvMW.jOovq"));
            repository.save(new User( "fefe@fefe.hu", "Joe", "Smith", "$2a$10$iKieGnwWVKdjRmRLnv/qG.4IZQ761q7/xORG4hAfbF5NvMW.jOovq"));
            repository.save(new User( "lol@lol.hu", "Alajos", "Zsanér", "$2a$10$iKieGnwWVKdjRmRLnv/qG.4IZQ761q7/xORG4hAfbF5NvMW.jOovq"));

            courtRepository.save(new Court(1L, "North"));
            courtRepository.save(new Court(2L, "West"));
            courtRepository.save(new Court(3L, "South"));

            recordRepository.save(new TimeRecord(1L, new GregorianCalendar(2019, 03, 04, 13, 00), new GregorianCalendar(2019, 03, 04, 14, 00), 2L, "training"));
            recordRepository.save(new TimeRecord(3L, new GregorianCalendar(2019, 03, 04, 11, 00), new GregorianCalendar(2019, 03, 04, 12, 00), 2L, "tournament"));
            recordRepository.save(new TimeRecord(2L, new GregorianCalendar(2019, 03, 03, 16, 30), new GregorianCalendar(2019, 03, 03, 17, 30), 1L, "friendly match"));
            recordRepository.save(new TimeRecord(3L, new GregorianCalendar(2019, 03, 03, 17, 30), new GregorianCalendar(2019, 03, 03, 18, 30), 1L, "training"));
            recordRepository.save(new TimeRecord(1L, new GregorianCalendar(2019, 02, 23, 10, 30), new GregorianCalendar(2019, 02, 23, 11, 30), 3L, "friendly match"));



            //repository.save(new User("Chloe", "O'Brian", "asd@sad.hu"));
            //repository.save(new User("Kim", "Bauer", "asd@sad.hu"));
            //repository.save(new User("David", "Palmer", "asd@sad.hu"));
            //repository.save(new User("Michelle", "Dessler", "asd@sad.hu"));

            log.info("Tennis courts found with findAll()");
            log.info("-------------------------------");
            for(Court court : courtRepository.findAll()){
                log.info(court.toString());
            }

            log.info("-------------------------------");
            // fetch all customers
            log.info("Customers found with findAll():");
            log.info("-------------------------------");
            for (User customer : repository.findAll()) {
                log.info(customer.toString());
            }
            log.info("TimeRecords found with find():");
            log.info("-------------------------------");

            Calendar calendar = Calendar.getInstance();
            int week = calendar.get(Calendar.WEEK_OF_YEAR);
            int year = calendar.get(Calendar.YEAR);
            calendar.clear();
            calendar.set(Calendar.WEEK_OF_YEAR, week);
            calendar.set(Calendar.YEAR, year);

            Date date = calendar.getTime();

            log.info(calendar.getTime().toString());


            for(TimeRecord record : recordRepository.find(new GregorianCalendar(2019, 03, 04, 11, 00))){
                log.info(record.toString());
            }

            log.info("TimeRecords found with findAll():");
            log.info("-------------------------------");

            for(TimeRecord record : recordRepository.findAll()){
                log.info(record.toString());
            }

            log.info("");

            // fetch an individual customer by ID
            repository.findById(2L)
                    .ifPresent(customer -> {
                        log.info("Customer found with findById(2L):");
                        log.info("--------------------------------");
                        log.info(customer.toString());
                    });
        };
    }


}
