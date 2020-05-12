package tcr.controller;

import tcr.StripeService;
import tcr.entity.*;
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

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
@Controller
public class UserInformationController {

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
    private ReservationRepository reservationRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @RequestMapping(value = "/userdetail", method=RequestMethod.GET)
    public String getUserInformation(Principal principal, Model model, @RequestParam(required = false) String error, @RequestParam(required = false) String success){

        User user = userRepository.findByUsername(principal.getName()).get();

        model.addAttribute("error", "");
        model.addAttribute("success", "");
        if(error != null) {
            if (error.equals("alreadymember")) {
                model.addAttribute("error", "You are already a member");
            } else if (error.equals("missinginformation")) {
                model.addAttribute("error", "Fill out the information form");
            }
        }

        if(success != null) {
            if (success.equals("informationupdated")) {
                model.addAttribute("success", "Information successfully updated!");
            } else if (success.equals("membershipstarted")) {
                model.addAttribute("success", "Membership successfully started!");
            }
        }

        model.addAttribute("tennisCourts", courtRepository.findAll());
        model.addAttribute("amount", 30 * 100); // in cents
        model.addAttribute("stripePublicKey", stripePublicKey);
        model.addAttribute("currency", ChargeRequest.Currency.EUR);
        model.addAttribute("id", "");
        model.addAttribute("myFutureReservations", reservationRepository.getFutureReservationsByUser(new GregorianCalendar(), user.getId()));

        if(userInformationRepository.findByUserId(user.getId()).isPresent()){
            model.addAttribute("userInformation", userInformationRepository.findByUserId(user.getId()).get());
            if(userInformationRepository.findByUserId(user.getId()).get().getIsMember().equals("true")){

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

    @RequestMapping(value = "/userdetail", method=RequestMethod.POST)
    public String setUserInformation(@ModelAttribute("userInformation") UserInformation userInformation, Principal principal, Model model){

        User user = userRepository.findByUsername(principal.getName()).get();

        model.addAttribute("error", "");
        model.addAttribute("success", "");

        model.addAttribute("tennisCourts", courtRepository.findAll());
        model.addAttribute("amount", 30 * 100); // in cents
        model.addAttribute("stripePublicKey", stripePublicKey);
        model.addAttribute("currency", ChargeRequest.Currency.EUR);
        model.addAttribute("id", "");
        model.addAttribute("myFutureReservations", reservationRepository.getFutureReservationsByUser(new GregorianCalendar(), user.getId()).size() > 10 ? reservationRepository.getFutureReservationsByUser(new GregorianCalendar(), user.getId()).subList(0, 10) : reservationRepository.getFutureReservationsByUser(new GregorianCalendar(), user.getId()));

        model.addAttribute("authenticatedUser", user);

        userInformation.setUserId(user.getId());

        if(userInformationRepository.findByUserId(user.getId()).isPresent()){
            userInformation.setIsMember(userInformationRepository.findByUserId(user.getId()).get().getIsMember());
            userInformation.setStartOfMembership(userInformationRepository.findByUserId(user.getId()).get().getStartOfMembership());

        }

        userInformationRepository.save(userInformation);

        if(userInformationRepository.findByUserId(user.getId()).isPresent()){
            model.addAttribute("userInformation", userInformationRepository.findByUserId(user.getId()).get());

            if(userInformationRepository.findByUserId(user.getId()).get().getIsMember() != null) {
                model.addAttribute("memberships", membershipRepository.getAllMembershipByUserId(user.getId()));
                model.addAttribute("isMember", true);
            }else{
                model.addAttribute("isMember", false);
            }
        }else{
            model.addAttribute("userInformation", new UserInformation());
        }

        model.addAttribute("success", "User information successfully updated!");

        return "userdetail";
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
                    User modifiedUser = userRepository.findByUsername(principal.getName()).get();
                    modifiedUser.setRole(User.Role.ROLE_USER);
                    userRepository.save(modifiedUser);
                }

                return "?success=membershipstarted";
            }else{
                return "?error=alreadymember";
            }
        }else{
            return "?error=missinginformation";
        }
    }


}
