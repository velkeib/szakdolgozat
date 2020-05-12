package tcr.controller;

import tcr.Application;
import tcr.entity.*;
import tcr.repository.*;
import tcr.dto.*;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.*;
import java.util.*;
import java.lang.*;
import java.text.SimpleDateFormat;

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
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @RequestMapping(value = "/admin", method=RequestMethod.GET)
    public String getAdmin(Principal principal, Model model, @RequestParam(required = false) String error, @RequestParam(required = false) String success) {

        model.addAttribute("notAdmin", "");
        model.addAttribute("success", "");
        model.addAttribute("error", "");

        if(userRepository.findByUsername(principal.getName()).get().getRole() != User.Role.ROLE_ADMIN) {
            model.addAttribute("notAdmin", "Only administrators can access this site");
        }

        if (error != null) {
            model.addAttribute("error", error);
        }

        if(success != null){
            model.addAttribute("success", "Role successfully changed!");
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

        Iterable<Reservation> records = reservationRepository.getByDay(calendar, calendarEnd);
        List<RecordDto> rec = new ArrayList<>();
        for (Reservation s : records) {
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
        model.addAttribute("everyReason", Arrays.asList(Reservation.Reason.class.getEnumConstants()));

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

    @RequestMapping(value = "/getReservation", method=RequestMethod.GET)
    @ResponseBody public RecordDto getReservationById(@RequestParam("id") String id) {

        Reservation timeRecord = reservationRepository.findById(Long.valueOf(id)).get();

        List<Participant> participants = participantRepository.findByRecordId(Long.valueOf(id));

        SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm");


        return new RecordDto(timeRecord.getId().toString(), timeRecord.getCustomer().toString(),
                formatterDate.format(timeRecord.getStartDate().getTime()) + "T" + formatterTime.format(timeRecord.getStartDate().getTime()),
                formatterDate.format(timeRecord.getEndDate().getTime()) + "T" + formatterTime.format(timeRecord.getEndDate().getTime()),
                timeRecord.getCourtID(), timeRecord.getReason(), participants);
    }


}
