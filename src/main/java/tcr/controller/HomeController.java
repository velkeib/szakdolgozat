package tcr.controller;

import tcr.Application;
import tcr.dto.DateTimeRecordDto;
import tcr.dto.RecordDto;
import tcr.entity.*;
import tcr.repository.*;

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

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
@Controller

public class HomeController {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @RequestMapping(value = "/home", method=RequestMethod.GET)
    public String getHome(Model model, Principal principal, @RequestParam(required = false) String success){

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

        Iterable<Reservation> records = reservationRepository.getByDay(calendar, calendarEnd);
        List<RecordDto> rec = new ArrayList<>();
        for(Reservation s: records){
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
                model.addAttribute("success", "Reservation successfully updated!");
            }else if(success.equals("delete")){
                model.addAttribute("success", "Reservation successfully deleted!");
            }
        }
        model.addAttribute("everyReason", Arrays.asList(Reservation.Reason.class.getEnumConstants()));
        model.addAttribute("everyUser", userRepository.findAll());

        return "home";
    }

    @Transactional
    @RequestMapping(value = "/home", method=RequestMethod.POST)
    public String createReservation(@ModelAttribute(value="timeRecord") DateTimeRecordDto timeRecord, Principal principal, Model model) {
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

        if(userRepository.findByUsername(principal.getName()).get().getRole() != User.Role.ROLE_GUEST) {
            if ((calendarEnd.getTimeInMillis() - calendarStart.getTimeInMillis()) / 1000 < 0) {
                model.addAttribute("error", "The start of the reservation has to be earlier than the end!");
            } else if ((new GregorianCalendar().getTimeInMillis() - calendarStart.getTimeInMillis()) / 1000 > 0) {
                model.addAttribute("error", "The reservation has to be in the future!");
            } else if (calendarEnd.get(Calendar.YEAR) != calendarStart.get(Calendar.YEAR) || calendarEnd.get(Calendar.MONTH) != calendarStart.get(Calendar.MONTH)
                    || calendarEnd.get(Calendar.DAY_OF_MONTH) != calendarStart.get(Calendar.DAY_OF_MONTH)
                    || calendarStart.get(Calendar.HOUR_OF_DAY) < 8 || calendarEnd.get(Calendar.HOUR_OF_DAY) > 21) {
                model.addAttribute("error", "The reservation has to be in open hours!");
            } else if (reservationRepository.findInInterval(calendarStart, calendarEnd, timeRecord.getCourtID(), Long.valueOf(0)).size() > 0) {
                model.addAttribute("error", "The court is reserved at this time!");
            } else {

                Reservation timeRecordEntity = new Reservation(userRepository.findByUsername(principal.getName()).get().getId(), calendarStart, calendarEnd, timeRecord.getCourtID(), timeRecord.getReason());
                reservationRepository.save(timeRecordEntity);

                List<Participant> participants = participantRepository.deleteByRecordId(timeRecordEntity.getId());

                for (int i = 0; i < timeRecord.getParticipants().size(); i++) {

                    Participant participant = new Participant();

                    participant.setRecordId(timeRecordEntity.getId());
                    participant.setUserId(Long.parseLong(timeRecord.getParticipants().get(i)));

                    participantRepository.save(participant);
                }
                model.addAttribute("success", "Reservation successfully created!");
            }
        }else{
            model.addAttribute("error", "Start your membership to book!");
        }

        Iterable<Reservation> records = reservationRepository.getByDay(calendarStartFilter, calendarEndFilter);
        String name = "";
        List<RecordDto> rec = new ArrayList<>();
        for(Reservation s: records){
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
        model.addAttribute("everyReason", Arrays.asList(Reservation.Reason.class.getEnumConstants()));
        model.addAttribute("everyUser", userRepository.findAll());


        return "/home";
    }



}
