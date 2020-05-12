package tcr.controller;

import tcr.Application;
import tcr.StripeService;
import tcr.dto.DateTimeRecordDto;
import tcr.dto.RecordDto;
import tcr.entity.*;
import tcr.entity.Payment;
import tcr.repository.*;
import tcr.dto.ChargeRequest;

import tcr.dto.ChargeRequest.Currency;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.*;
import java.util.*;
import java.text.SimpleDateFormat;
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

    @Autowired
    private StripeService paymentsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private ParticipantRepository participantRepository;


    private static final Logger log = LoggerFactory.getLogger(Application.class);


    @RequestMapping(value = "/charge", method = RequestMethod.POST)
    public String charge(ChargeRequest chargeRequest, Model model, Principal principal)
            throws StripeException {

        log.info(chargeRequest.getId());

        chargeRequest.setDescription("Payment");
        chargeRequest.setCurrency(Currency.EUR);
        Charge charge = paymentsService.charge(chargeRequest);
        model.addAttribute("id", charge.getId());
        model.addAttribute("status", charge.getStatus());
        model.addAttribute("chargeId", charge.getId());
        model.addAttribute("balance_transaction", charge.getBalanceTransaction());


        Payment payment = new Payment();
        payment.setChargeId(charge.getId());
        payment.setUserId(userRepository.findByUsername(principal.getName()).get().getId());
        payment.setPaymentDate(new GregorianCalendar());

        Membership membership = membershipRepository.findById(Long.valueOf(chargeRequest.getId())).get();

        paymentRepository.save(payment);

        membership.setPaymentId(payment.getId());

        membershipRepository.save(membership);

        model.addAttribute("authenticatedUser", userRepository.findByUsername(principal.getName()).get());

        return "result";
    }

    @ExceptionHandler(StripeException.class)
    public String handleError(Model model, StripeException ex, Principal principal) {
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("authenticatedUser", userRepository.findByUsername(principal.getName()).get());
        return "result";
    }

    @RequestMapping(value = "/api/search", method = RequestMethod.GET)
    @ResponseBody List<RecordDto> getReservationsByDay(@RequestParam(required=false) String filterDay, Principal principal, Model model) {

        GregorianCalendar startDateFilter = new GregorianCalendar(Integer.parseInt(filterDay.split("/")[2]),
                Integer.parseInt(filterDay.split("/")[0]) - 1,
                Integer.parseInt(filterDay.split("/")[1]),0,0,0);
        GregorianCalendar endDateFilter = new GregorianCalendar(Integer.parseInt(filterDay.split("/")[2]),
                Integer.parseInt(filterDay.split("/")[0]) - 1,
                Integer.parseInt(filterDay.split("/")[1]), 23,59,59);

        Iterable<Reservation> records = reservationRepository.getByDay(startDateFilter, endDateFilter);

        String name = "";
        List<RecordDto> rec = new ArrayList<>();

        for(Reservation s: records){
            name = userRepository.findById(s.getCustomer()).get().getFirstName() + " " + userRepository.findById(s.getCustomer()).get().getLastName();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            rec.add(new RecordDto(s.getId().toString(), name, formatter.format(s.getStartDate().getTime()),
                    formatter.format(s.getEndDate().getTime()), s.getCourtID(),
                    s.getReason(), participantRepository.findByRecordId(s.getId())));
        }

        model.addAttribute("reservations", rec);

        return rec;
    }

    @Bean
    public CommandLineRunner demo(UserRepository repository, CourtRepository courtRepository, ReservationRepository recordRepository) {
        return (args) -> {


            courtRepository.save(new Court(1L, "North"));
            courtRepository.save(new Court(2L, "West"));
            courtRepository.save(new Court(3L, "South"));


            //PW asd
            User user = new User( "velkeib@gmail.com", "Bence", "Velkei", "$2a$10$IT.xwh1CFa.k9xqzJsK3xOzYClrtvfHBm9PinY5UEAPvSU2CAmirO");
            user.setRole(User.Role.ROLE_ADMIN);

            User user2 = new User( "velkeics@gmail.com", "Csaba", "Velkei", "$2a$10$IT.xwh1CFa.k9xqzJsK3xOzYClrtvfHBm9PinY5UEAPvSU2CAmirO");

            repository.save(user);
            repository.save(user2);

            // save a couple of customer
            /*repository.save(new User("sad@sad.hu", "Jack", "Sparrow", "$2a$04$YDiv9c./ytEGZQopFfExoOgGlJL6/o0er0K.hiGb5TGKHUL8Ebn.."));
            repository.save(new User( "test@stest.hu", "Bence", "Velkei", "$2a$10$OCdR/doDzVLvDpJ1QtJO3ONJR80EQHeVXhnA7SB6qrlR0vwecTfhy"));
            repository.save(new User( "test", "Geza", "Nagy", "$2a$10$iKieGnwWVKdjRmRLnv/qG.4IZQ761q7/xORG4hAfbF5NvMW.jOovq"));
            repository.save(new User( "fefe@fefe.hu", "Joe", "Smith", "$2a$10$iKieGnwWVKdjRmRLnv/qG.4IZQ761q7/xORG4hAfbF5NvMW.jOovq"));
            repository.save(new User( "lol@lol.hu", "Alajos", "Zsanér", "$2a$10$iKieGnwWVKdjRmRLnv/qG.4IZQ761q7/xORG4hAfbF5NvMW.jOovq"));
            repository.save(new User( "asd", "asd", "asd", "$2a$10$IT.xwh1CFa.k9xqzJsK3xOzYClrtvfHBm9PinY5UEAPvSU2CAmirO"));


            recordRepository.save(new TimeRecord(1L, new GregorianCalendar(2020, 1, 27, 13, 00), new GregorianCalendar(2020, 1, 27, 14, 00), 2L, "training"));
            recordRepository.save(new TimeRecord(3L, new GregorianCalendar(2020, 1, 27, 11, 00), new GregorianCalendar(2020, 1, 27, 12, 00), 2L, "tournament"));
            recordRepository.save(new TimeRecord(2L, new GregorianCalendar(2020, 1, 27, 16, 30), new GregorianCalendar(2020, 1, 27, 17, 30), 1L, "friendly match"));
            recordRepository.save(new TimeRecord(3L, new GregorianCalendar(2020, 1, 27, 17, 30), new GregorianCalendar(2020, 1, 27, 18, 30), 1L, "training"));
            recordRepository.save(new TimeRecord(6L, new GregorianCalendar(2020, 1, 27, 10, 30), new GregorianCalendar(2020, 1, 27, 11, 30), 3L, "friendly match"));

            membershipRepository.save(new Membership(2L, 6L, null, new GregorianCalendar(2020,01,01), new GregorianCalendar(2020, 01, 01)));
*/

            //membershipRepository.save(new Membership(6L, 5L, null, new GregorianCalendar(2019,11,01), new GregorianCalendar(2020, 00, 01)));


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


            for(Reservation record : recordRepository.find(new GregorianCalendar(2019, 03, 04, 11, 00))){
                log.info(record.toString());
            }

            log.info("TimeRecords found with findAll():");
            log.info("-------------------------------");

            for(Reservation record : recordRepository.findAll()){
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
