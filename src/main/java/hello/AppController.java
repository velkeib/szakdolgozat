package hello;

import hello.dto.DateTimeRecordDto;
import hello.dto.RecordDto;
import hello.entity.Court;
import hello.entity.TimeRecord;
import hello.entity.User;
import hello.repository.CourtRepository;
import hello.repository.TimeRecordRepository;
import hello.repository.UserRepository;
import hello.repository.UserInformationRepository;
import hello.entity.UserInformation;
import hello.dto.ChargeRequest;
import hello.dto.*;

import hello.dto.ChargeRequest.Currency;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.lang.*;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import javax.xml.ws.RequestWrapper;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
@Controller
public class AppController {


    private String stripePublicKey = "pk_test_aHSlpmgRfXA7IysM2RwvbYYv00P57xFRGs";



    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private UserInformationRepository userInformationRepository;

    @Autowired
    private TimeRecordRepository recordRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final Logger log = LoggerFactory.getLogger(Application.class);


/*
    public Iterable<TimeRecord> getRecords(){



        return
    }
*/

    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        //log.info(principal.getName());

        Date date = new Date();

        GregorianCalendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        GregorianCalendar calendarEnd = new GregorianCalendar();

        calendarEnd.setTime(date);

        calendarEnd.set(Calendar.HOUR_OF_DAY, 0);
        calendarEnd.set(Calendar.MINUTE, 0);
        calendarEnd.set(Calendar.SECOND, 0);

        calendarEnd.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);

        String name = "";

        Iterable<TimeRecord> recordstest = recordRepository.findAll();

        //log.info(recordstest.toString());

        Iterable<TimeRecord> records = recordRepository.getByDay(calendar, calendarEnd);
        List<RecordDto> rec = new ArrayList<>();
        for(TimeRecord s: records){
            //log.info(s.getCustomer().toString());
            //log.info(userRepository.findById(s.getCustomer()).toString());
            //log.info(userRepository.findById(s.getCustomer()).get().getFirstName() + " " + userRepository.findById(s.getCustomer()).get().getLastName());
            name = userRepository.findById(s.getCustomer()).get().getFirstName() + " " + userRepository.findById(s.getCustomer()).get().getLastName();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            rec.add(new RecordDto(s.getId().toString(), name, formatter.format(s.getStartDate().getTime()),
                    formatter.format(s.getEndDate().getTime()), s.getCourtID(),
                    s.getReason()));
        }

        //log.info(rec.toString());

        model.addAttribute("authenticatedUser", userRepository.findByUsername(principal.getName()).get());
        model.addAttribute("tennisCourts", courtRepository.findAll());
        //model.addAttribute("test", recordRepository.findAll());
        model.addAttribute("reservations", rec);
        model.addAttribute("timeRecord", new DateTimeRecordDto());
        //model.addAttribute("map", map);

        return "home";
    }

    @GetMapping("/test")
    public String test(){
        return "test";
    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    /*@RequestMapping(value="/logout", method=RequestMethod.GET)
    public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "/index";
    }*/

    @RequestMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("user", new User());
        for (User customer : userRepository.findAll()) {
            //log.info(customer.toString());
        }
        return "index";
    }

    /*@GetMapping("/reservation/{id}")
    public String reservation(@PathVariable String id, Model model) {
        model.addAttribute("court", new Court());
        log.info(id);

        return "reservation";
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
        user.setRole(User.Role.ROLE_GUEST);
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


    @RequestMapping(value = "/home", method=RequestMethod.POST)
    public String processTimeRecordForm(@ModelAttribute(value="timeRecord") DateTimeRecordDto timeRecord, Principal principal, Model model) {
        Date date = new Date();

        GregorianCalendar calendarStart = new GregorianCalendar();
        GregorianCalendar calendarStartFilter = new GregorianCalendar();

        calendarStartFilter.setTime(date);

        calendarStartFilter.set(Calendar.HOUR_OF_DAY, 0);
        calendarStartFilter.set(Calendar.MINUTE, 0);
        calendarStartFilter.set(Calendar.SECOND, 0);

        GregorianCalendar calendarEnd = new GregorianCalendar();
        GregorianCalendar calendarEndFilter = new GregorianCalendar();

        calendarEnd.setTime(date);

        calendarEndFilter.set(Calendar.HOUR_OF_DAY, 0);
        calendarEndFilter.set(Calendar.MINUTE, 0);
        calendarEndFilter.set(Calendar.SECOND, 0);

        calendarEndFilter.set(Calendar.DAY_OF_MONTH, calendarStart.get(Calendar.DAY_OF_MONTH) + 1);

        calendarStart.set(Calendar.YEAR, Integer.parseInt(timeRecord.getStartDate().split("/")[2]));
        calendarStart.set(Calendar.MONTH, Integer.parseInt(timeRecord.getStartDate().split("/")[0]) - 1);
        calendarStart.set(Calendar.DAY_OF_MONTH, Integer.parseInt(timeRecord.getStartDate().split("/")[1]));
        calendarStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeRecord.getStartTime().split(":")[0]));
        calendarStart.set(Calendar.MINUTE, Integer.parseInt(timeRecord.getStartTime().split(":")[1]));


        calendarEnd.set(Calendar.YEAR, Integer.parseInt(timeRecord.getEndDate().split("/")[2]));
        calendarEnd.set(Calendar.MONTH, Integer.parseInt(timeRecord.getEndDate().split("/")[0]) - 1);
        calendarEnd.set(Calendar.DAY_OF_MONTH, Integer.parseInt(timeRecord.getEndDate().split("/")[1]));
        calendarEnd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeRecord.getEndTime().split(":")[0]));
        calendarEnd.set(Calendar.MINUTE, Integer.parseInt(timeRecord.getEndTime().split(":")[1]));

        recordRepository.save(new TimeRecord(userRepository.findByUsername(principal.getName()).get().getId(), calendarStart, calendarEnd, timeRecord.getCourtID(), timeRecord.getReason()));


        Iterable<TimeRecord> records = recordRepository.getByDay(calendarStartFilter, calendarEndFilter);
        String name = "";
        List<RecordDto> rec = new ArrayList<>();
        for(TimeRecord s: records){
            name = userRepository.findById(s.getCustomer()).get().getFirstName() + " " + userRepository.findById(s.getCustomer()).get().getLastName();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            rec.add(new RecordDto(s.getId().toString(), name, formatter.format(s.getStartDate().getTime()),
                    formatter.format(s.getEndDate().getTime()), s.getCourtID(),
                    s.getReason()));
        }




        timeRecord.setCustomer(userRepository.findByUsername(principal.getName()).get().getId().toString());

        model.addAttribute("reservations", rec);
        model.addAttribute("timeRecord", new DateTimeRecordDto());
        model.addAttribute("authenticatedUser", userRepository.findByUsername(principal.getName()).get());
        model.addAttribute("tennisCourts", courtRepository.findAll());

        //user.setPassword(passwordEncoder.encode(user.getPassword()));
        //user.setEnabled(true);
        //user.setRole(User.Role.ROLE_USER);
        //userRepository.save(user);
        return "/home";
    }

    @PostMapping("login")
    public String loginResponse() {
        return "home";
    }

    @Bean
    public CommandLineRunner demo(UserRepository repository, CourtRepository courtRepository, TimeRecordRepository recordRepository) {
        return (args) -> {
            // save a couple of customer
            /*repository.save(new User("sad@sad.hu", "Jack", "Sparrow", "$2a$04$YDiv9c./ytEGZQopFfExoOgGlJL6/o0er0K.hiGb5TGKHUL8Ebn.."));
            repository.save(new User( "test@stest.hu", "Bence", "Velkei", "$2a$10$OCdR/doDzVLvDpJ1QtJO3ONJR80EQHeVXhnA7SB6qrlR0vwecTfhy"));
            repository.save(new User( "test", "Geza", "Nagy", "$2a$10$iKieGnwWVKdjRmRLnv/qG.4IZQ761q7/xORG4hAfbF5NvMW.jOovq"));
            repository.save(new User( "fefe@fefe.hu", "Joe", "Smith", "$2a$10$iKieGnwWVKdjRmRLnv/qG.4IZQ761q7/xORG4hAfbF5NvMW.jOovq"));
            repository.save(new User( "lol@lol.hu", "Alajos", "Zsanér", "$2a$10$iKieGnwWVKdjRmRLnv/qG.4IZQ761q7/xORG4hAfbF5NvMW.jOovq"));
            repository.save(new User( "asd", "asd", "asd", "$2a$10$IT.xwh1CFa.k9xqzJsK3xOzYClrtvfHBm9PinY5UEAPvSU2CAmirO"));

            courtRepository.save(new Court(1L, "North"));
            courtRepository.save(new Court(2L, "West"));
            courtRepository.save(new Court(3L, "South"));

            recordRepository.save(new TimeRecord(1L, new GregorianCalendar(2020, 1, 27, 13, 00), new GregorianCalendar(2020, 1, 27, 14, 00), 2L, "training"));
            recordRepository.save(new TimeRecord(3L, new GregorianCalendar(2020, 1, 27, 11, 00), new GregorianCalendar(2020, 1, 27, 12, 00), 2L, "tournament"));
            recordRepository.save(new TimeRecord(2L, new GregorianCalendar(2020, 1, 27, 16, 30), new GregorianCalendar(2020, 1, 27, 17, 30), 1L, "friendly match"));
            recordRepository.save(new TimeRecord(3L, new GregorianCalendar(2020, 1, 27, 17, 30), new GregorianCalendar(2020, 1, 27, 18, 30), 1L, "training"));
            recordRepository.save(new TimeRecord(6L, new GregorianCalendar(2020, 1, 27, 10, 30), new GregorianCalendar(2020, 1, 27, 11, 30), 3L, "friendly match"));
*/


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

    @RequestMapping("/api/search")
    @ResponseBody List<RecordDto> getSearchResultViaAjax(@RequestParam(required=false, defaultValue="World") String filterDay, Principal principal, Model model) {
        //log.info(filterDay);

        GregorianCalendar startDateFilter = new GregorianCalendar(Integer.parseInt(filterDay.split("/")[2]),
                Integer.parseInt(filterDay.split("/")[0]) - 1,
                Integer.parseInt(filterDay.split("/")[1]),0,0,0);
        GregorianCalendar endDateFilter = new GregorianCalendar(Integer.parseInt(filterDay.split("/")[2]),
                Integer.parseInt(filterDay.split("/")[0]) - 1,
                Integer.parseInt(filterDay.split("/")[1]), 23,59,59);

        Iterable<TimeRecord> records = recordRepository.getByDay(startDateFilter, endDateFilter);

        //log.info(startDateFilter.toString());
        //log.info(endDateFilter.toString());

        String name = "";
        List<RecordDto> rec = new ArrayList<>();

        for(TimeRecord s: records){
            name = userRepository.findById(s.getCustomer()).get().getFirstName() + " " + userRepository.findById(s.getCustomer()).get().getLastName();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            rec.add(new RecordDto(s.getId().toString(), name, formatter.format(s.getStartDate().getTime()),
                    formatter.format(s.getEndDate().getTime()), s.getCourtID(),
                    s.getReason()));
        }

        //log.info(rec.toString());
        //log.info(model.toString());

        model.addAttribute("reservations", rec);

        //log.info(model.toString());

        return rec;
    }

    @RequestMapping("/userdetail")
    public String getUserInformation(Principal principal, Model model){

        model.addAttribute("amount", 30 * 100); // in cents
        model.addAttribute("stripePublicKey", stripePublicKey);
        model.addAttribute("currency", ChargeRequest.Currency.EUR);

        User user = userRepository.findByUsername(principal.getName()).get();
        if(userInformationRepository.findByUserId(user.getId()).isPresent()){
            log.info(userInformationRepository.findByUserId(user.getId()).get().toString());
            model.addAttribute("userInformation", userInformationRepository.findByUserId(user.getId()).get());

            //GregorianCalendar startOfMembership =  userInformationRepository.findByUserId(user.getId()).get().getStartOfMembership();
            //GregorianCalendar currentDate = new GregorianCalendar();



        }else{
            model.addAttribute("userInformation", new UserInformation());
        }

        model.addAttribute("authenticatedUser", userRepository.findByUsername(principal.getName()).get());

        return "userDetail";
    }

    @PostMapping("userdetail")
    public String setUserInformations(@ModelAttribute("userInformation") UserInformation userInformation, Principal principal, Model model){

        model.addAttribute("amount", 30 * 100); // in cents
        model.addAttribute("stripePublicKey", stripePublicKey);
        model.addAttribute("currency", ChargeRequest.Currency.EUR);

        User user = userRepository.findByUsername(principal.getName()).get();

        model.addAttribute("authenticatedUser", user);
        //model.addAttribute("userInformation", new UserInformation());

        //userInformation.setUserId();



        userInformation.setUserId(user.getId());
        userInformation.setIsMember("false");

        log.info(userInformation.toString());

        /*
        if(userInformation.getIsMember() != null){
            if(userInformationRepository.findByUserId(user.getId()).ifPresent()) {
                if (userInformationRepository.findByUserId(user.getId()).get().getStartOfMembership() == null) {
                    userInformation.setStartOfMembership(new GregorianCalendar());
                }
            }
        }
        */
        log.info(userInformation.toString());

        userInformationRepository.save(userInformation);

        //userInformationRepository.save(userInformation);

        if(userInformationRepository.findByUserId(user.getId()).isPresent()){
            log.info(userInformationRepository.findByUserId(user.getId()).get().toString());
            model.addAttribute("userInformation", userInformationRepository.findByUserId(user.getId()).get());
        }else{
            model.addAttribute("userInformation", new UserInformation());
        }


        return "userDetail";
    }

    @RequestMapping("/checkout")
    public String checkout(Model model) {
        model.addAttribute("amount", 30 * 100); // in cents
        model.addAttribute("stripePublicKey", stripePublicKey);
        model.addAttribute("currency", ChargeRequest.Currency.EUR);
        return "checkout";
    }

    @Autowired
    private StripeService paymentsService;

    @PostMapping("/charge")
    public String charge(ChargeRequest chargeRequest, Model model)
            throws StripeException {

        chargeRequest.setDescription("Payment");
        chargeRequest.setCurrency(Currency.EUR);
        Charge charge = paymentsService.charge(chargeRequest);
        model.addAttribute("id", charge.getId());
        model.addAttribute("status", charge.getStatus());
        model.addAttribute("chargeId", charge.getId());
        model.addAttribute("balance_transaction", charge.getBalanceTransaction());

        return "result";
    }

    @ExceptionHandler(StripeException.class)
    public String handleError(Model model, StripeException ex) {
        model.addAttribute("error", ex.getMessage());
        return "result";
    }



    @GetMapping("/reservation/{id}")
    public String getReservation(@PathVariable String id, Principal principal, Model model){

        User user = userRepository.findById(recordRepository.findById(Long.valueOf(id)).get().getCustomer()).get();

        TimeRecord timeRecord = recordRepository.findById(Long.valueOf(id)).get();




        String name = user.getFirstName() + " " + user.getLastName();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Date date = new Date();

        GregorianCalendar calendarStart = new GregorianCalendar();
        GregorianCalendar calendarStartFilter = new GregorianCalendar();

        calendarStartFilter.setTime(date);

        calendarStartFilter.set(Calendar.DAY_OF_MONTH, timeRecord.getStartDate().get(Calendar.DAY_OF_MONTH));
        calendarStartFilter.set(Calendar.HOUR_OF_DAY, 0);
        calendarStartFilter.set(Calendar.MINUTE, 0);
        calendarStartFilter.set(Calendar.SECOND, 0);

        GregorianCalendar calendarEnd = new GregorianCalendar();
        GregorianCalendar calendarEndFilter = new GregorianCalendar();

        calendarEnd.setTime(date);

        calendarEndFilter.set(Calendar.DAY_OF_MONTH, timeRecord.getStartDate().get(Calendar.DAY_OF_MONTH) + 1);
        calendarEndFilter.set(Calendar.HOUR_OF_DAY, 0);
        calendarEndFilter.set(Calendar.MINUTE, 0);
        calendarEndFilter.set(Calendar.SECOND, 0);

        calendarEndFilter.set(Calendar.DAY_OF_MONTH, calendarStart.get(Calendar.DAY_OF_MONTH) + 1);

        Iterable<TimeRecord> records = recordRepository.getByDay(calendarStartFilter, calendarEndFilter);
        String filtername = "";
        List<RecordDto> rec = new ArrayList<>();
        for(TimeRecord s: records){
            filtername = userRepository.findById(s.getCustomer()).get().getFirstName() + " " + userRepository.findById(s.getCustomer()).get().getLastName();

            SimpleDateFormat filterformatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            rec.add(new RecordDto(s.getId().toString(), filtername, filterformatter.format(s.getStartDate().getTime()),
                    filterformatter.format(s.getEndDate().getTime()), s.getCourtID(),
                    s.getReason()));
        }

        model.addAttribute("reservations", rec);
        model.addAttribute("tennisCourts", courtRepository.findAll());
        model.addAttribute("authenticatedUser", userRepository.findByUsername(principal.getName()).get());
        model.addAttribute("reservation", new RecordDto(timeRecord.getId().toString(), name, formatter.format(timeRecord.getStartDate().getTime()),
                formatter.format(timeRecord.getEndDate().getTime()), timeRecord.getCourtID(),
                timeRecord.getReason()));

        return "reservation";
    }

    /*public List<RecordDto> getReservationForDay(int year, int, month, int day, int hour, int minute){
        GregorianCalendar start = new GregorianCalendar()
        GregorianCalendar start = new GregorianCalendar()


    }*/

    @GetMapping("/admin")
    public String admin(Principal principal, Model model) {

        Date date = new Date();
        GregorianCalendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        GregorianCalendar calendarEnd = new GregorianCalendar();

        calendarEnd.setTime(date);

        calendarEnd.set(Calendar.HOUR_OF_DAY, 0);
        calendarEnd.set(Calendar.MINUTE, 0);
        calendarEnd.set(Calendar.SECOND, 0);

        calendarEnd.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);

        String name = "";

        Iterable<TimeRecord> records = recordRepository.getByDay(calendar, calendarEnd);
        List<RecordDto> rec = new ArrayList<>();
        for(TimeRecord s: records){
            //log.info(s.getCustomer().toString());
            //log.info(userRepository.findById(s.getCustomer()).toString());
            //log.info(userRepository.findById(s.getCustomer()).get().getFirstName() + " " + userRepository.findById(s.getCustomer()).get().getLastName());
            name = userRepository.findById(s.getCustomer()).get().getFirstName() + " " + userRepository.findById(s.getCustomer()).get().getLastName();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            rec.add(new RecordDto(s.getId().toString(), name, formatter.format(s.getStartDate().getTime()),
                    formatter.format(s.getEndDate().getTime()), s.getCourtID(),
                    s.getReason()));
        }

        model.addAttribute("tennisCourts", courtRepository.findAll());
        model.addAttribute("reservations", rec);
        model.addAttribute("authenticatedUser", userRepository.findByUsername(principal.getName()).get());
        model.addAttribute("everyUser", userRepository.findAll());
        model.addAttribute("everyRole", Arrays.asList(User.Role.class.getEnumConstants()));

        //log.info(userRepository.findAll().toString());

        return "/admin";
    }

    @RequestMapping(value = "/deleteRecord/{id}", method = RequestMethod.POST)
    @ResponseBody public String deleteRecord(@PathVariable String id){

        recordRepository.deleteById(Long.valueOf(id));

        return "Success";
    }

    @GetMapping("/getReservation")
    @ResponseBody public TimeRecord getReservationById(@RequestParam("id") String id) {

        TimeRecord timeRecord = recordRepository.findById(Long.valueOf(id)).get();

        return timeRecord;
    }


    @RequestMapping(value = "/updateRecord", method = RequestMethod.POST)
    @ResponseBody public String updateRecord(@RequestParam String Id, @RequestParam(required = false) String customerId, @RequestParam String startDate, @RequestParam String startTime, @RequestParam String endDate
            ,@RequestParam String endTime, @RequestParam String reason, @RequestParam String courtId){

        TimeRecord timeRecord = recordRepository.findById(Long.valueOf(Id)).get();

        if(customerId != null){
            timeRecord.setCustomer(Long.valueOf(customerId));
        }

        //log.info();


        timeRecord.setStartDate(new GregorianCalendar(Integer.parseInt(startDate.split("/")[2]), Integer.parseInt(startDate.split("/")[0])-1, Integer.parseInt(startDate.split("/")[1]),
                Integer.parseInt(startTime.split(":")[0]), Integer.parseInt(startTime.split(":")[1])));
        timeRecord.setEndDate(new GregorianCalendar(Integer.parseInt(endDate.split("/")[2]), Integer.parseInt(endDate.split("/")[0])-1, Integer.parseInt(endDate.split("/")[1]),
                Integer.parseInt(endTime.split(":")[0]), Integer.parseInt(endTime.split(":")[1])));
        timeRecord.setCourtID(Long.valueOf(courtId.split(" - ")[0]));
        timeRecord.setReason(reason);


        //log.info(timeRecord.toString());

        recordRepository.save(timeRecord);

        return "Success";
    }


    @RequestMapping(value = "/changeAuthorization", method = RequestMethod.POST)
    @ResponseBody public Iterable<User> changeAuthorization(@RequestParam Integer userID, @RequestParam String role){

        User user = userRepository.findById(Long.valueOf(userID)).get();
        User.Role roleEnum = User.Role.valueOf(role);
        //log.info(roleEnum.toString());
        user.setRole(roleEnum);
        userRepository.save(user);
        //log.info(user.toString());

        return userRepository.findAll();
    }


    @RequestMapping(value = "/api/save", method = RequestMethod.POST)
    public String saveRecord(@RequestParam String courtID, @RequestParam String reason) throws Exception {
        //Employee employee = new Employee();
        //log.info(courtID + ", " + reason);
        //String lastName = request.getParameter("lastName");
        /*String email = request.getParameter("email");
        employee.setEmail(email);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);*/
        return courtID + ", " + reason;
    }

    @RequestMapping(value = "/startMembership", method = RequestMethod.POST)
    @ResponseBody public String startMembership(@RequestParam String id){

        if(userInformationRepository.findByUserId(Long.valueOf(id)).isPresent()) {
            UserInformation userInformation = userInformationRepository.findByUserId(Long.valueOf(id)).get();
            if(userInformation.getStartOfMembership() == null) {
                userInformation.setIsMember("true");
                userInformation.setStartOfMembership(new GregorianCalendar());

                userInformationRepository.save(userInformation);

                return "Success";
            }else{
                return "You already started your membership!";
            }
        }else{
            return "Fill in you informations before you can start the membership!";
        }
    }

}
