package tcr.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.http.MediaType;
import java.security.Principal;
import tcr.entity.User;
import tcr.entity.Court;
import tcr.entity.Reservation;
import tcr.entity.Participant;
import tcr.dto.RecordDto;
import tcr.repository.UserRepository;
import tcr.repository.CourtRepository;
import tcr.repository.ReservationRepository;
import tcr.repository.ParticipantRepository;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.Commit;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.junit.Before;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
public class TestingWebApplicationTests {

    private final String USER_NAME = "oroszg@gmail.com";
    private final Long USER_ID = 4L;
    private final Long COURT_ID = 4L;

    @Autowired
    private AppController appController;

    @Autowired
    private HomeController homeController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private UserInformationRepository userInformationRepository;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private RequestMappingHandlerAdapter handlerAdapter;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Test
    public void contextLoads() throws Exception {
        assertThat(appController).isNotNull();
    }

    //USER CREATION
    @Test
    public void userInserted() throws Exception {
        User user = new User(3L, USER_NAME, "Bence", "Velkei", "$2a$10$IT.xwh1CFa.k9xqzJsK3xOzYClrtvfHBm9PinY5UEAPvSU2CAmirO", true, User.Role.ROLE_ADMIN);
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByUsername(USER_NAME);

        assertThat(foundUser.isPresent()).isEqualTo(true);

        assertThat(foundUser
                .get()
                .getUsername()).isEqualTo(USER_NAME);
    }

    //COURT CREATION
    @Test
    public void courtInserted() throws Exception {
        Court court = new Court(COURT_ID, "East");
        courtRepository.save(court);

        Optional<Court> foundCourt = courtRepository.findById(COURT_ID);

        assertThat(foundCourt.isPresent()).isEqualTo(true);

        assertThat(foundCourt
                .get()
                .getId()).isEqualTo(COURT_ID);
    }

    //RESERVATION CREATION
    @Test
    public void reservationInserted() throws Exception {
        Reservation reservation = new Reservation(1L, 1L, new GregorianCalendar(2020,01,22,11,0), new GregorianCalendar(2020,01,22,12,0), 2L, Reservation.Reason.OTHER.toString());
        reservationRepository.save(reservation);

        List<Reservation> foundReservation = reservationRepository.getByDay(new GregorianCalendar(2020,01,22), new GregorianCalendar(2020,01,23));

        assertThat(foundReservation.size()).isEqualTo(1);

    }

    @Test
    public void userinformationInserted() throws Exception {
        Reservation reservation = new Reservation(1L, 1L, new GregorianCalendar(2020,01,22,11,0), new GregorianCalendar(2020,01,22,12,0), 2L, Reservation.Reason.OTHER.toString());
        reservationRepository.save(reservation);

        List<Reservation> foundReservation = reservationRepository.getByDay(new GregorianCalendar(2020,01,22), new GregorianCalendar(2020,01,23));

        assertThat(foundReservation.size()).isEqualTo(1);

    }

    //INDEX GET
    @Test
    public void indexGet() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        // request init here

        MockHttpServletResponse response = new MockHttpServletResponse();
        Object handler = handlerMapping.getHandler(request).getHandler();
        ModelAndView modelAndView = handlerAdapter.handle(request, response, handler);
        assertThat("index").isEqualTo(modelAndView.getViewName());
    }

    //INDEX POST
    @Test
    public void indexPost() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/");
        request.addParameter("username", "username");
        request.addParameter("firstName", "firstName");
        request.addParameter("lastName", "lastName");
        request.addParameter("password", "password");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Object handler = handlerMapping.getHandler(request).getHandler();
        ModelAndView modelAndView = handlerAdapter.handle(request, response, handler);

        Optional<User> foundUser = userRepository.findByUsername("username");

        assertThat(foundUser.isPresent()).isEqualTo(true);
        assertThat(foundUser.get().getUsername()).isEqualTo("username");
        assertThat("index").isEqualTo(modelAndView.getViewName());
        assertThat("Account successfully created").isEqualTo(modelAndView.getModel().get("success"));
    }

    //LOGIN GET
    @Test
    public void loginGet() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        //request.setSession(new MockHttpSession());
        //request.addHeader("authToken", "aa");

        Object handler = handlerMapping.getHandler(request).getHandler();
            ModelAndView modelAndView = handlerAdapter.handle(request, response, handler);
        assertThat("login").isEqualTo(modelAndView.getViewName());
    }

    protected MockMvc mockMvc;

    @Test
    //@WithUserDetails(value="asd", userDetailsServiceBeanName="myUserDetailsService")
    public void homeGet() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/home");
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setUserPrincipal(new Principal() {
            @Override
            public String getName() {
                return "velkeib@gmail.com";
            }
        });

        Object handler = handlerMapping.getHandler(request).getHandler();
        ModelAndView modelAndView = handlerAdapter.handle(request, response, handler);
        assertThat("home").isEqualTo(modelAndView.getViewName());
        assertThat(userRepository.findByUsername("velkeib@gmail.com").get()).isEqualTo(modelAndView.getModel().get("authenticatedUser"));
    }

    @Test
    //@WithUserDetails(value="asd", userDetailsServiceBeanName="myUserDetailsService")
    public void homePost() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/home");
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addParameter("customer", "1L");
        request.addParameter("startDate", "11/12/2020");
        request.addParameter("startTime", "10:00");
        request.addParameter("endDate", "11/12/2020");
        request.addParameter("endTime", "11:00");
        request.addParameter("courtID", "1");
        request.addParameter("reason", "OTHER");
        request.addParameter("participants", "");

        request.setUserPrincipal(new Principal() {
            @Override
            public String getName() {
                return "velkeib@gmail.com";
            }
        });

        Object handler = handlerMapping.getHandler(request).getHandler();
        ModelAndView modelAndView = handlerAdapter.handle(request, response, handler);
        assertThat("/home").isEqualTo(modelAndView.getViewName());
        assertThat(userRepository.findByUsername("velkeib@gmail.com").get()).isEqualTo(modelAndView.getModel().get("authenticatedUser"));
        assertThat(reservationRepository.getFutureReservationsByUser(new GregorianCalendar(), 1L).get(0).getCourtID()).isEqualTo(1L);
    }

    @Test
    public void homePostFutureError() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/home");
        MockHttpServletResponse response = new MockHttpServletResponse();

        reservationRepository.save(new Reservation(10L, 1L, new GregorianCalendar(2020, 3,1,0,0), new GregorianCalendar(2020, 5,1,0,0), 2L, Reservation.Reason.OTHER.toString()));

        request.addParameter("customer", "1L");
        request.addParameter("startDate", "04/12/2020");
        request.addParameter("startTime", "10:00");
        request.addParameter("endDate", "04/12/2020");
        request.addParameter("endTime", "11:00");
        request.addParameter("courtID", "2");
        request.addParameter("reason", "OTHER");
        request.addParameter("participants", "");

        request.setUserPrincipal(new Principal() {
            @Override
            public String getName() {
                return "velkeib@gmail.com";
            }
        });

        Object handler = handlerMapping.getHandler(request).getHandler();
        ModelAndView modelAndView = handlerAdapter.handle(request, response, handler);
        assertThat("/home").isEqualTo(modelAndView.getViewName());
        assertThat(userRepository.findByUsername("velkeib@gmail.com").get()).isEqualTo(modelAndView.getModel().get("authenticatedUser"));
        assertThat("The reservation has to be in the future!").isEqualTo(modelAndView.getModel().get("error"));
    }

    @Test
    public void homePostReservedError() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/home");
        MockHttpServletResponse response = new MockHttpServletResponse();

        reservationRepository.save(new Reservation(10L, 1L, new GregorianCalendar(2021, 3,1,0,0), new GregorianCalendar(2021, 10,1,0,0), 2L, Reservation.Reason.OTHER.toString()));

        request.addParameter("customer", "1L");
        request.addParameter("startDate", "06/12/2021");
        request.addParameter("startTime", "10:00");
        request.addParameter("endDate", "06/12/2021");
        request.addParameter("endTime", "11:00");
        request.addParameter("courtID", "2");
        request.addParameter("reason", "OTHER");
        request.addParameter("participants", "");

        request.setUserPrincipal(new Principal() {
            @Override
            public String getName() {
                return "velkeib@gmail.com";
            }
        });

        Object handler = handlerMapping.getHandler(request).getHandler();
        ModelAndView modelAndView = handlerAdapter.handle(request, response, handler);
        assertThat("/home").isEqualTo(modelAndView.getViewName());
        assertThat(userRepository.findByUsername("velkeib@gmail.com").get()).isEqualTo(modelAndView.getModel().get("authenticatedUser"));
        assertThat("The court is reserved at this time!").isEqualTo(modelAndView.getModel().get("error"));
    }

    @Test
    public void homePostMembershipError() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/home");
        MockHttpServletResponse response = new MockHttpServletResponse();

        reservationRepository.save(new Reservation(10L, 1L, new GregorianCalendar(2021, 3,1,0,0), new GregorianCalendar(2021, 10,1,0,0), 2L, Reservation.Reason.OTHER.toString()));

        request.addParameter("customer", "1L");
        request.addParameter("startDate", "06/12/2021");
        request.addParameter("startTime", "10:00");
        request.addParameter("endDate", "06/12/2021");
        request.addParameter("endTime", "11:00");
        request.addParameter("courtID", "2");
        request.addParameter("reason", "OTHER");
        request.addParameter("participants", "");

        request.setUserPrincipal(new Principal() {
            @Override
            public String getName() {
                return "velkeics@gmail.com";
            }
        });

        Object handler = handlerMapping.getHandler(request).getHandler();
        ModelAndView modelAndView = handlerAdapter.handle(request, response, handler);
        assertThat("/home").isEqualTo(modelAndView.getViewName());
        assertThat(userRepository.findByUsername("velkeics@gmail.com").get()).isEqualTo(modelAndView.getModel().get("authenticatedUser"));
        assertThat("Start your membership to book!").isEqualTo(modelAndView.getModel().get("error"));
    }

    @Test
    public void homePostEarlyError() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/home");
        MockHttpServletResponse response = new MockHttpServletResponse();

        reservationRepository.save(new Reservation(10L, 1L, new GregorianCalendar(2021, 3,1,0,0), new GregorianCalendar(2021, 10,1,0,0), 2L, Reservation.Reason.OTHER.toString()));

        request.addParameter("customer", "1L");
        request.addParameter("startDate", "06/12/2021");
        request.addParameter("startTime", "10:00");
        request.addParameter("endDate", "06/12/2021");
        request.addParameter("endTime", "09:00");
        request.addParameter("courtID", "2");
        request.addParameter("reason", "OTHER");
        request.addParameter("participants", "");

        request.setUserPrincipal(new Principal() {
            @Override
            public String getName() {
                return "velkeib@gmail.com";
            }
        });

        Object handler = handlerMapping.getHandler(request).getHandler();
        ModelAndView modelAndView = handlerAdapter.handle(request, response, handler);
        assertThat("/home").isEqualTo(modelAndView.getViewName());
        assertThat(userRepository.findByUsername("velkeib@gmail.com").get()).isEqualTo(modelAndView.getModel().get("authenticatedUser"));
        assertThat("The start of the reservation has to be earlier than the end!").isEqualTo(modelAndView.getModel().get("error"));
    }
}