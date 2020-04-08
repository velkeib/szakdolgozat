package hello;

import hello.dto.DateTimeRecordDto;
import hello.dto.RecordDto;
import hello.entity.*;
import hello.entity.Payment;
import hello.repository.*;
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
import org.springframework.transaction.annotation.Transactional;

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
import sun.util.calendar.Gregorian;

@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
@Controller
public class AppController {


    private String stripePublicKey = "pk_test_aHSlpmgRfXA7IysM2RwvbYYv00P57xFRGs";

    @Autowired
    private StripeService paymentsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private UserInformationRepository userInformationRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TimeRecordRepository recordRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    //REGISTRATION PAGE
    @GetMapping("/")
    public String index(Model model) {

        model.addAttribute("error", "");
        model.addAttribute("user", new User());
        return "index";
    }

    @RequestMapping(value = "/", method=RequestMethod.POST)
    public String processForm(@ModelAttribute(value="user") User user, Model model) {
        Optional<User> oUser = userRepository.findByUsername(user.getUsername());
        model.addAttribute("error", "");

        if (oUser.isPresent()) {
            model.addAttribute("error", "Email address already in use!");
            model.addAttribute("user", new User());
            return "/index";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        user.setRole(User.Role.ROLE_GUEST);
        userRepository.save(user);

        model.addAttribute("user", new User());

        return "/index";
    }

    //LOGIN PAGE
    @RequestMapping("/login")
    public String login(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("error", "");
        if(error != null){
            log.info(error);
            model.addAttribute("error", "Email address and/or password is wrong!");
        }

        return "login";
    }

    //HOME PAGE---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @GetMapping("/home")
    public String home(Model model, Principal principal, @RequestParam(required = false) String success){

        model.addAttribute("success", "");
        GregorianCalendar calendar = new GregorianCalendar();

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        GregorianCalendar calendarEnd = new GregorianCalendar();

        calendarEnd.set(Calendar.HOUR_OF_DAY, 0);
        calendarEnd.set(Calendar.MINUTE, 0);
        calendarEnd.set(Calendar.SECOND, 0);

        calendarEnd.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);

        String name = "";

        Iterable<TimeRecord> records = recordRepository.getByDay(calendar, calendarEnd);
        List<RecordDto> rec = new ArrayList<>();
        for(TimeRecord s: records){
            name = userRepository.findById(s.getCustomer()).get().getFirstName() + " " + userRepository.findById(s.getCustomer()).get().getLastName();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            rec.add(new RecordDto(s.getId().toString(), name, formatter.format(s.getStartDate().getTime()),
                    formatter.format(s.getEndDate().getTime()), s.getCourtID(),
                    s.getReason(), participantRepository.findByRecordId(s.getId())));
        }

        model.addAttribute("authenticatedUser", userRepository.findByUsername(principal.getName()).get());
        model.addAttribute("tennisCourts", courtRepository.findAll());
        model.addAttribute("reservations", rec);
        model.addAttribute("timeRecord", new DateTimeRecordDto());
        model.addAttribute("error", "");
        if(success != null) {
            if(success.equals("update")) {
                model.addAttribute("success", "Reservation updated");
            }else if(success.equals("delete")){
                model.addAttribute("success", "Reservation deleted");
            }
        }
        model.addAttribute("everyReason", Arrays.asList(TimeRecord.Reason.class.getEnumConstants()));
        model.addAttribute("everyUser", userRepository.findAll());

        return "home";
    }

    @Transactional
    @RequestMapping(value = "/home", method=RequestMethod.POST)
    public String processTimeRecordForm(@ModelAttribute(value="timeRecord") DateTimeRecordDto timeRecord, Principal principal, Model model) {
        model.addAttribute("error", "");
        model.addAttribute("success", "");

        GregorianCalendar calendarStart = new GregorianCalendar();
        GregorianCalendar calendarStartFilter = new GregorianCalendar();

        calendarStartFilter.set(Calendar.HOUR_OF_DAY, 0);
        calendarStartFilter.set(Calendar.MINUTE, 0);
        calendarStartFilter.set(Calendar.SECOND, 0);

        GregorianCalendar calendarEnd = new GregorianCalendar();
        GregorianCalendar calendarEndFilter = new GregorianCalendar();

        calendarEndFilter.set(Calendar.HOUR_OF_DAY, 0);
        calendarEndFilter.set(Calendar.MINUTE, 0);
        calendarEndFilter.set(Calendar.SECOND, 0);

        calendarEndFilter.set(Calendar.DAY_OF_MONTH, calendarStart.get(Calendar.DAY_OF_MONTH) + 1);

        calendarStart.set(Calendar.YEAR, Integer.parseInt(timeRecord.getStartDate().split("/")[2]));
        calendarStart.set(Calendar.MONTH, Integer.parseInt(timeRecord.getStartDate().split("/")[0]) - 1);
        calendarStart.set(Calendar.DAY_OF_MONTH, Integer.parseInt(timeRecord.getStartDate().split("/")[1]));
        calendarStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeRecord.getStartTime().split(":")[0]));
        calendarStart.set(Calendar.MINUTE, Integer.parseInt(timeRecord.getStartTime().split(":")[1]));
        calendarStart.set(Calendar.SECOND, 0);
        calendarStart.set(Calendar.MILLISECOND, 0);

        calendarEnd.set(Calendar.YEAR, Integer.parseInt(timeRecord.getEndDate().split("/")[2]));
        calendarEnd.set(Calendar.MONTH, Integer.parseInt(timeRecord.getEndDate().split("/")[0]) - 1);
        calendarEnd.set(Calendar.DAY_OF_MONTH, Integer.parseInt(timeRecord.getEndDate().split("/")[1]));
        calendarEnd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeRecord.getEndTime().split(":")[0]));
        calendarEnd.set(Calendar.MINUTE, Integer.parseInt(timeRecord.getEndTime().split(":")[1]));
        calendarEnd.set(Calendar.SECOND, 0);
        calendarEnd.set(Calendar.MILLISECOND, 0);

        log.info(calendarStart.get(Calendar.HOUR_OF_DAY) + " " + calendarStart.get(Calendar.MINUTE)+ " " + calendarStart.get(Calendar.SECOND));

        log.info(calendarEnd.get(Calendar.HOUR_OF_DAY) + " " + calendarEnd.get(Calendar.MINUTE)+ " " + calendarEnd.get(Calendar.SECOND));
        //if(recordRepository.findInInterval(calendarStart, calendarEnd, timeRecord.getCourtID()).size()>0) {
        //    log.info(recordRepository.findInInterval(calendarStart, calendarEnd, timeRecord.getCourtID()).get(0).getStartDate().get(Calendar.HOUR_OF_DAY) + " " + recordRepository.findInInterval(calendarStart, calendarEnd, timeRecord.getCourtID()).get(0).getStartDate().get(Calendar.MINUTE) + " " + recordRepository.findInInterval(calendarStart, calendarEnd, timeRecord.getCourtID()).get(0).getStartDate().get(Calendar.SECOND));
        //    log.info(recordRepository.findInInterval(calendarStart, calendarEnd, timeRecord.getCourtID()).get(0).getEndDate().get(Calendar.HOUR_OF_DAY) + " " + recordRepository.findInInterval(calendarStart, calendarEnd, timeRecord.getCourtID()).get(0).getEndDate().get(Calendar.MINUTE) + " " + recordRepository.findInInterval(calendarStart, calendarEnd, timeRecord.getCourtID()).get(0).getEndDate().get(Calendar.SECOND));
        //}

        if(userRepository.findByUsername(principal.getName()).get().getRole() != User.Role.ROLE_GUEST) {
            if ((calendarEnd.getTimeInMillis() - calendarStart.getTimeInMillis()) / 1000 < 0) {
                model.addAttribute("error", "The start of the reservation has to be earlier than the end!");
            } else if ((new GregorianCalendar().getTimeInMillis() - calendarStart.getTimeInMillis()) / 1000 > 0) {
                model.addAttribute("error", "The reservation has to be in the future!");
            } else if (calendarEnd.get(Calendar.YEAR) != calendarStart.get(Calendar.YEAR) || calendarEnd.get(Calendar.MONTH) != calendarStart.get(Calendar.MONTH)
                    || calendarEnd.get(Calendar.DAY_OF_MONTH) != calendarStart.get(Calendar.DAY_OF_MONTH)
                    || calendarStart.get(Calendar.HOUR_OF_DAY) < 8 || calendarEnd.get(Calendar.HOUR_OF_DAY) > 21) {
                model.addAttribute("error", "The reservation has to be in open hours!");
            } else if (recordRepository.findInInterval(calendarStart, calendarEnd, timeRecord.getCourtID(), Long.valueOf(0)).size() > 0) {
                model.addAttribute("error", "The court is reserved at this time!");
            } else {
                log.info(timeRecord.getParticipants().toString());

                TimeRecord timeRecordEntity = new TimeRecord(userRepository.findByUsername(principal.getName()).get().getId(), calendarStart, calendarEnd, timeRecord.getCourtID(), timeRecord.getReason());
                recordRepository.save(timeRecordEntity);

                List<Participant> participants = participantRepository.deleteByRecordId(timeRecordEntity.getId());

                for (int i = 0; i < timeRecord.getParticipants().size(); i++) {

                    Participant participant = new Participant();

                    participant.setRecordId(timeRecordEntity.getId());
                    participant.setUserId(Long.parseLong(timeRecord.getParticipants().get(i)));

                    participantRepository.save(participant);
                }

                model.addAttribute("success", "Reservation created");

            }
        }else{
            model.addAttribute("error", "Start your membership to book!");
        }


        Iterable<TimeRecord> records = recordRepository.getByDay(calendarStartFilter, calendarEndFilter);
        String name = "";
        List<RecordDto> rec = new ArrayList<>();
        for(TimeRecord s: records){
            name = userRepository.findById(s.getCustomer()).get().getFirstName() + " " + userRepository.findById(s.getCustomer()).get().getLastName();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            rec.add(new RecordDto(s.getId().toString(), name, formatter.format(s.getStartDate().getTime()),
                    formatter.format(s.getEndDate().getTime()), s.getCourtID(),
                    s.getReason(), participantRepository.findByRecordId(s.getId())));
        }

        timeRecord.setCustomer(userRepository.findByUsername(principal.getName()).get().getId().toString());

        model.addAttribute("reservations", rec);
        model.addAttribute("timeRecord", new DateTimeRecordDto());
        model.addAttribute("authenticatedUser", userRepository.findByUsername(principal.getName()).get());
        model.addAttribute("tennisCourts", courtRepository.findAll());
        model.addAttribute("everyReason", Arrays.asList(TimeRecord.Reason.class.getEnumConstants()));
        model.addAttribute("everyUser", userRepository.findAll());


        return "/home";
    }

    /*@RequestMapping(value="/logout", method=RequestMethod.GET)
    public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "/index";
    }*/


    //RESERVATION PAGE---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @GetMapping("/reservation/{id}")
    public String getReservation(@PathVariable String id, Principal principal, Model model, @RequestParam(required = false) String error){

        model.addAttribute("error", "");
        model.addAttribute("notMy", "");

        if(error != null){
            switch (error) {
                case "startend":
                    model.addAttribute("error", "The start of the reservation has to be earlier than the end!");
                    break;
                case "past":
                    model.addAttribute("error", "The reservation has to be in the future!");
                    break;
                case "closed":
                    model.addAttribute("error", "The reservation has to be in open hours!");
                    break;
                case "reserved":
                    model.addAttribute("error", "The court is reserved at this time!");
                    break;
                case "missingmembership":
                    model.addAttribute("error", "Start your membership to book!");
                    break;
            }
        }

        User user = userRepository.findById(recordRepository.findById(Long.valueOf(id)).get().getCustomer()).get();
        TimeRecord timeRecord = recordRepository.findById(Long.valueOf(id)).get();

        String name = user.getFirstName() + " " + user.getLastName();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        GregorianCalendar calendarStartFilter = new GregorianCalendar();

        calendarStartFilter.set(Calendar.MONTH, timeRecord.getStartDate().get(Calendar.MONTH));
        calendarStartFilter.set(Calendar.DAY_OF_MONTH, timeRecord.getStartDate().get(Calendar.DAY_OF_MONTH));
        calendarStartFilter.set(Calendar.HOUR_OF_DAY, 0);
        calendarStartFilter.set(Calendar.MINUTE, 0);
        calendarStartFilter.set(Calendar.SECOND, 0);

        GregorianCalendar calendarEndFilter = new GregorianCalendar();

        calendarEndFilter.set(Calendar.MONTH, timeRecord.getStartDate().get(Calendar.MONTH) + 1);
        calendarEndFilter.set(Calendar.HOUR_OF_DAY, 0);
        calendarEndFilter.set(Calendar.MINUTE, 0);
        calendarEndFilter.set(Calendar.SECOND, 0);

        calendarEndFilter.set(Calendar.DAY_OF_MONTH, calendarStartFilter.get(Calendar.DAY_OF_MONTH) + 1);

        Iterable<TimeRecord> records = recordRepository.getByDay(calendarStartFilter, calendarEndFilter);
        String filtername = "";
        List<RecordDto> rec = new ArrayList<>();
        for(TimeRecord s: records){
            filtername = userRepository.findById(s.getCustomer()).get().getFirstName() + " " + userRepository.findById(s.getCustomer()).get().getLastName();

            SimpleDateFormat filterformatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            rec.add(new RecordDto(s.getId().toString(), filtername, filterformatter.format(s.getStartDate().getTime()),
                    filterformatter.format(s.getEndDate().getTime()), s.getCourtID(),
                    s.getReason(), participantRepository.findByRecordId(s.getId())));
        }

        if(recordRepository.findById(Long.valueOf(id)).get().getCustomer() == userRepository.findByUsername(principal.getName()).get().getId()){
            model.addAttribute("notMy", "You are not allowed to modify other person's reservation");
        }

        model.addAttribute("reservations", rec);
        model.addAttribute("tennisCourts", courtRepository.findAll());
        model.addAttribute("authenticatedUser", userRepository.findByUsername(principal.getName()).get());
        model.addAttribute("reservation", new RecordDto(timeRecord.getId().toString(), name, formatter.format(timeRecord.getStartDate().getTime()),
                formatter.format(timeRecord.getEndDate().getTime()), timeRecord.getCourtID(),
                timeRecord.getReason(), participantRepository.findByRecordId(timeRecord.getId())));
        model.addAttribute("everyReason", Arrays.asList(TimeRecord.Reason.class.getEnumConstants()));
        model.addAttribute("everyUser", userRepository.findAll());

        return "reservation";
    }

    //ADMIN OR RESERVATION DELETE UPDATE-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @RequestMapping(value = "/deleteRecord/{id}", method = RequestMethod.POST)
    @ResponseBody public String deleteRecord(@PathVariable String id, Principal principal) throws Exception {

        if(recordRepository.findById(Long.valueOf(id)).get().getCustomer() == userRepository.findByUsername(principal.getName()).get().getId()){
            recordRepository.deleteById(Long.valueOf(id));
            return "Success";
        }else{
            if(userRepository.findByUsername(principal.getName()).get().getRole() == User.Role.ROLE_ADMIN){
                recordRepository.deleteById(Long.valueOf(id));
                return "Success";
            }else{
                throw new Exception("norights");
            }
        }
    }

    @Transactional
    @RequestMapping(value = "/updateRecord", method = RequestMethod.POST)
    @ResponseBody public String updateRecord(@RequestParam String Id, @RequestParam(required = false) String customerId, @RequestParam String startDate, @RequestParam String startTime, @RequestParam String endDate
            ,@RequestParam String endTime, @RequestParam String reason, @RequestParam String courtId, Principal principal, HttpServletRequest request, @RequestParam(value="participants[]", required = false) List<String> participants){

        log.info(request.getRequestURL().toString());

        TimeRecord timeRecord = recordRepository.findById(Long.valueOf(Id)).get();

        if(customerId != null){
            timeRecord.setCustomer(Long.valueOf(customerId));
        }

        GregorianCalendar calendarStart = new GregorianCalendar(Integer.parseInt(startDate.split("/")[2]), Integer.parseInt(startDate.split("/")[0])-1, Integer.parseInt(startDate.split("/")[1]),
                Integer.parseInt(startTime.split(":")[0]), Integer.parseInt(startTime.split(":")[1]));
        calendarStart.set(Calendar.SECOND, 0);
        calendarStart.set(Calendar.MILLISECOND, 0);

        GregorianCalendar calendarEnd = new GregorianCalendar(Integer.parseInt(endDate.split("/")[2]), Integer.parseInt(endDate.split("/")[0])-1, Integer.parseInt(endDate.split("/")[1]),
                Integer.parseInt(endTime.split(":")[0]), Integer.parseInt(endTime.split(":")[1]));
        calendarEnd.set(Calendar.SECOND, 0);
        calendarEnd.set(Calendar.MILLISECOND, 0);


        if(userRepository.findByUsername(principal.getName()).get().getRole() != User.Role.ROLE_GUEST) {
            if ((calendarEnd.getTimeInMillis() - calendarStart.getTimeInMillis()) / 1000 < 0) {
                return "?error=startend";
            } else if ((new GregorianCalendar().getTimeInMillis() - calendarStart.getTimeInMillis()) / 1000 > 0) {
                return "?error=past";
            } else if (calendarEnd.get(Calendar.YEAR) != calendarStart.get(Calendar.YEAR) || calendarEnd.get(Calendar.MONTH) != calendarStart.get(Calendar.MONTH)
                    || calendarEnd.get(Calendar.DAY_OF_MONTH) != calendarStart.get(Calendar.DAY_OF_MONTH)
                    || calendarStart.get(Calendar.HOUR_OF_DAY) < 8 || calendarEnd.get(Calendar.HOUR_OF_DAY) > 21) {
                return "?error=closed";
            } else if (recordRepository.findInInterval(calendarStart, calendarEnd, timeRecord.getCourtID(), Long.valueOf(Id)).size() > 0) {
                return "?error=reserved";
            } else {

                timeRecord.setStartDate(calendarStart);
                timeRecord.setEndDate(calendarEnd);
                timeRecord.setCourtID(Long.valueOf(courtId.split(" - ")[0]));
                timeRecord.setReason(reason);

                recordRepository.save(timeRecord);

                participantRepository.deleteByRecordId(timeRecord.getId());

                for(int i =  0; i < participants.size(); i++){

                    Participant participant = new Participant();

                    participant.setRecordId(timeRecord.getId());
                    participant.setUserId(Long.parseLong(participants.get(i)));

                    participantRepository.save(participant);
                }

                return "success";
            }
        }else{
            return "?error=missingmembership";
        }
    }

    //ADMIN PAGE------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @GetMapping("/admin")
    public String admin(Principal principal, Model model, @RequestParam(required = false) String error) {

        model.addAttribute("notAdmin", "");

        if(userRepository.findByUsername(principal.getName()).get().getRole() != User.Role.ROLE_ADMIN) {
            model.addAttribute("notAdmin", "Only administrators can access this site");
        }

         model.addAttribute("error", "");

         if (error != null) {
             model.addAttribute("error", error);
         }


         GregorianCalendar calendar = new GregorianCalendar();

         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);

         GregorianCalendar calendarEnd = new GregorianCalendar();

         calendarEnd.set(Calendar.HOUR_OF_DAY, 0);
         calendarEnd.set(Calendar.MINUTE, 0);
         calendarEnd.set(Calendar.SECOND, 0);

         calendarEnd.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);

         String name = "";

         Iterable<TimeRecord> records = recordRepository.getByDay(calendar, calendarEnd);
         List<RecordDto> rec = new ArrayList<>();
         for (TimeRecord s : records) {
             name = userRepository.findById(s.getCustomer()).get().getFirstName() + " " + userRepository.findById(s.getCustomer()).get().getLastName();

             SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

             rec.add(new RecordDto(s.getId().toString(), name, formatter.format(s.getStartDate().getTime()),
                     formatter.format(s.getEndDate().getTime()), s.getCourtID(),
                     s.getReason(), participantRepository.findByRecordId(s.getId())));
         }

         model.addAttribute("tennisCourts", courtRepository.findAll());
         model.addAttribute("reservations", rec);
         model.addAttribute("authenticatedUser", userRepository.findByUsername(principal.getName()).get());
         model.addAttribute("everyUser", userRepository.findAll());
         model.addAttribute("everyRole", Arrays.asList(User.Role.class.getEnumConstants()));
         model.addAttribute("everyReason", Arrays.asList(TimeRecord.Reason.class.getEnumConstants()));


        return "/admin";
    }

    @RequestMapping(value = "/changeAuthorization", method = RequestMethod.POST)
    @ResponseBody public Iterable<User> changeAuthorization(@RequestParam Integer userID, @RequestParam String role){

        User user = userRepository.findById(Long.valueOf(userID)).get();
        User.Role roleEnum = User.Role.valueOf(role);

        user.setRole(roleEnum);
        userRepository.save(user);

        return userRepository.findAll();
    }

    @GetMapping("/getReservation")
    @ResponseBody public RecordDto getReservationById(@RequestParam("id") String id) {

        TimeRecord timeRecord = recordRepository.findById(Long.valueOf(id)).get();

        List<Participant> participants = participantRepository.findByRecordId(Long.valueOf(id));

        SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm");


        return new RecordDto(timeRecord.getId().toString(), timeRecord.getCustomer().toString(),
                formatterDate.format(timeRecord.getStartDate().getTime()) + "T" + formatterTime.format(timeRecord.getStartDate().getTime()),
                formatterDate.format(timeRecord.getEndDate().getTime()) + "T" + formatterTime.format(timeRecord.getEndDate().getTime()),
                timeRecord.getCourtID(), timeRecord.getReason(), participants);
    }

    //USER DETAIL PAGE-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @RequestMapping("/userdetail")
    public String getUserInformation(Principal principal, Model model){

        User user = userRepository.findByUsername(principal.getName()).get();

        model.addAttribute("tennisCourts", courtRepository.findAll());
        model.addAttribute("amount", 30 * 100); // in cents
        model.addAttribute("stripePublicKey", stripePublicKey);
        model.addAttribute("currency", ChargeRequest.Currency.EUR);
        model.addAttribute("id", "");
        model.addAttribute("myFutureReservations", recordRepository.getFutureReservationsByUser(new GregorianCalendar(), user.getId()));

        if(userInformationRepository.findByUserId(user.getId()).isPresent()){
            log.info(userInformationRepository.findByUserId(user.getId()).get().toString());
            model.addAttribute("userInformation", userInformationRepository.findByUserId(user.getId()).get());
            if(userInformationRepository.findByUserId(user.getId()).get().getIsMember() != null) {

                log.info(membershipRepository.getAllMembershipByUserId(user.getId()).toString());
                model.addAttribute("memberships", membershipRepository.getAllMembershipByUserId(user.getId()));
                model.addAttribute("isMember", true);
            }else{
                model.addAttribute("isMember", false);
            }

        }else{
            model.addAttribute("userInformation", new UserInformation());
        }

        model.addAttribute("authenticatedUser", userRepository.findByUsername(principal.getName()).get());

        return "userdetail";
    }

    @PostMapping("userdetail")
    public String setUserInformations(@ModelAttribute("userInformation") UserInformation userInformation, Principal principal, Model model){

        User user = userRepository.findByUsername(principal.getName()).get();


        model.addAttribute("tennisCourts", courtRepository.findAll());
        model.addAttribute("amount", 30 * 100); // in cents
        model.addAttribute("stripePublicKey", stripePublicKey);
        model.addAttribute("currency", ChargeRequest.Currency.EUR);
        model.addAttribute("id", "");
        model.addAttribute("myFutureReservations", recordRepository.getFutureReservationsByUser(new GregorianCalendar(), user.getId()).size() > 10 ? recordRepository.getFutureReservationsByUser(new GregorianCalendar(), user.getId()).subList(0, 10) : recordRepository.getFutureReservationsByUser(new GregorianCalendar(), user.getId()));

        model.addAttribute("authenticatedUser", user);

        userInformation.setUserId(user.getId());

        if(userInformationRepository.findByUserId(user.getId()).isPresent()){
            userInformation.setIsMember(userInformationRepository.findByUserId(user.getId()).get().getIsMember());
            userInformation.setStartOfMembership(userInformationRepository.findByUserId(user.getId()).get().getStartOfMembership());

        }
        log.info(userInformation.toString());

        userInformationRepository.save(userInformation);

        if(userInformationRepository.findByUserId(user.getId()).isPresent()){
            log.info(userInformationRepository.findByUserId(user.getId()).get().toString());
            model.addAttribute("userInformation", userInformationRepository.findByUserId(user.getId()).get());

            if(userInformationRepository.findByUserId(user.getId()).get().getIsMember() != null) {
                model.addAttribute("memberships", membershipRepository.getAllMembershipByUserId(user.getId()));
                model.addAttribute("isMember", true);
            }else{
                model.addAttribute("isMember", false);
            }

            log.info(membershipRepository.getAllMembershipByUserId(user.getId()).toString());
        }else{
            model.addAttribute("userInformation", new UserInformation());
        }

        return "userdetail";
    }

    @PostMapping("/charge")
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

        return "result";
    }

    @ExceptionHandler(StripeException.class)
    public String handleError(Model model, StripeException ex) {
        model.addAttribute("error", ex.getMessage());
        return "result";
    }

    @RequestMapping(value = "/startMembership", method = RequestMethod.POST)
    @ResponseBody public String startMembership(@RequestParam String id, Principal principal){

        if(userInformationRepository.findByUserId(Long.valueOf(id)).isPresent()) {
            UserInformation userInformation = userInformationRepository.findByUserId(Long.valueOf(id)).get();
            if(userInformation.getStartOfMembership() == null) {
                userInformation.setIsMember("true");
                userInformation.setStartOfMembership(new GregorianCalendar());

                userInformationRepository.save(userInformation);

                Membership membership = new Membership();

                GregorianCalendar gregorianCalendar = new GregorianCalendar();

                gregorianCalendar.set(gregorianCalendar.get(Calendar.YEAR), gregorianCalendar.get(Calendar.MONTH) + 1, gregorianCalendar.get(Calendar.DAY_OF_MONTH));

                membership.setStartOfMembership(new GregorianCalendar());
                membership.setEndOfMembership(gregorianCalendar);
                membership.setUserId(Long.valueOf(id));

                membershipRepository.save(membership);

                if(userRepository.findByUsername(principal.getName()).get().getRole() == User.Role.ROLE_GUEST) {
                    userRepository.findByUsername(principal.getName()).get().setRole(User.Role.ROLE_USER);
                }

                return "Success";
            }else{
                return "You already started your membership!";
            }
        }else{
            return "Fill in your informations before you can start the membership!";
        }
    }

    //CHANGE DAY FILTER

    @RequestMapping("/api/search")
    @ResponseBody List<RecordDto> getSearchResultViaAjax(@RequestParam(required=false) String filterDay, Principal principal, Model model) {

        GregorianCalendar startDateFilter = new GregorianCalendar(Integer.parseInt(filterDay.split("/")[2]),
                Integer.parseInt(filterDay.split("/")[0]) - 1,
                Integer.parseInt(filterDay.split("/")[1]),0,0,0);
        GregorianCalendar endDateFilter = new GregorianCalendar(Integer.parseInt(filterDay.split("/")[2]),
                Integer.parseInt(filterDay.split("/")[0]) - 1,
                Integer.parseInt(filterDay.split("/")[1]), 23,59,59);

        Iterable<TimeRecord> records = recordRepository.getByDay(startDateFilter, endDateFilter);

        String name = "";
        List<RecordDto> rec = new ArrayList<>();

        for(TimeRecord s: records){
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
