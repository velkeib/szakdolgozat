package tcr.controller;

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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
@Controller
public class ReservationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @RequestMapping(value = "/reservation/{id}", method=RequestMethod.GET)
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

        User user = userRepository.findById(reservationRepository.findById(Long.valueOf(id)).get().getCustomer()).get();
        Reservation timeRecord = reservationRepository.findById(Long.valueOf(id)).get();

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

        Iterable<Reservation> records = reservationRepository.getByDay(calendarStartFilter, calendarEndFilter);
        String filtername = "";
        List<RecordDto> rec = new ArrayList<>();
        for(Reservation s: records){
            filtername = userRepository.findById(s.getCustomer()).get().getFirstName() + " " + userRepository.findById(s.getCustomer()).get().getLastName();

            SimpleDateFormat filterformatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            rec.add(new RecordDto(s.getId().toString(), filtername, filterformatter.format(s.getStartDate().getTime()),
                    filterformatter.format(s.getEndDate().getTime()), s.getCourtID(),
                    s.getReason(), participantRepository.findByRecordId(s.getId())));
        }

        if(reservationRepository.findById(Long.valueOf(id)).get().getCustomer() != userRepository.findByUsername(principal.getName()).get().getId()){
            model.addAttribute("notMy", "You are not allowed to modify other person's reservation");
        }

        model.addAttribute("reservations", rec);
        model.addAttribute("tennisCourts", courtRepository.findAll());
        model.addAttribute("authenticatedUser", userRepository.findByUsername(principal.getName()).get());
        model.addAttribute("reservation", new RecordDto(timeRecord.getId().toString(), name, formatter.format(timeRecord.getStartDate().getTime()),
                formatter.format(timeRecord.getEndDate().getTime()), timeRecord.getCourtID(),
                timeRecord.getReason(), participantRepository.findByRecordId(timeRecord.getId())));
        model.addAttribute("everyReason", Arrays.asList(Reservation.Reason.class.getEnumConstants()));
        model.addAttribute("everyUser", userRepository.findAll());

        return "reservation";
    }

    //ADMIN OR RESERVATION DELETE UPDATE-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @RequestMapping(value = "/deleteRecord/{id}", method = RequestMethod.POST)
    @ResponseBody public String deleteReservation(@PathVariable String id, Principal principal) throws Exception {
        if(reservationRepository.findById(Long.valueOf(id)).get().getCustomer() == userRepository.findByUsername(principal.getName()).get().getId()){
            reservationRepository.deleteById(Long.valueOf(id));
            return "Success";
        }else{
            if(userRepository.findByUsername(principal.getName()).get().getRole() == User.Role.ROLE_ADMIN){
                reservationRepository.deleteById(Long.valueOf(id));
                return "Success";
            }else{
                throw new Exception("norights");
            }
        }
    }

    @Transactional
    @RequestMapping(value = "/updateRecord", method = RequestMethod.POST)
    @ResponseBody public String updateReservation(@RequestParam String Id, @RequestParam(required = false) String customerId, @RequestParam String startDate, @RequestParam String startTime, @RequestParam String endDate
            ,@RequestParam String endTime, @RequestParam String reason, @RequestParam String courtId, Principal principal, @RequestParam(value="participants[]", required = false) List<String> participants){


        Reservation timeRecord = reservationRepository.findById(Long.valueOf(Id)).get();

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
            } else if (reservationRepository.findInInterval(calendarStart, calendarEnd, timeRecord.getCourtID(), Long.valueOf(Id)).size() > 0) {
                return "?error=reserved";
            } else {
                timeRecord.setStartDate(calendarStart);
                timeRecord.setEndDate(calendarEnd);
                timeRecord.setCourtID(Long.valueOf(courtId.split(" - ")[0]));
                timeRecord.setReason(reason);

                reservationRepository.save(timeRecord);

                participantRepository.deleteByRecordId(timeRecord.getId());

                if(participants != null) {

                    for (int i = 0; i < participants.size(); i++) {
                        Participant participant = new Participant();

                        participant.setRecordId(timeRecord.getId());
                        participant.setUserId(Long.parseLong(participants.get(i)));

                        participantRepository.save(participant);
                    }
                }
                return "success";
            }
        }else{
            return "?error=missingmembership";
        }
    }


}
