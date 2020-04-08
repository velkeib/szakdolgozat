package hello;


import hello.entity.Membership;
import hello.entity.User;
import hello.repository.MembershipRepository;
import hello.repository.UserRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.apache.commons.logging.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.MemoryManagerMXBean;
import java.util.Calendar;
import java.util.GregorianCalendar;

@Configuration
@EnableWebSecurity
@EnableScheduling
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/", "/index", "/login").permitAll()
                .antMatchers("/css/**").permitAll()
                .antMatchers("/js/**").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error=true")
                .and()
                .logout()
                .logoutSuccessUrl("/login")
                .and()
                .formLogin()
                .and()
                .httpBasic();
    }

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    protected void configureAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    //inmemoryauthentication
   /* @Autowired
    protected void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .inMemoryAuthentication()
                .withUser("user").password("$2a$04$YDiv9c./ytEGZQopFfExoOgGlJL6/o0er0K.hiGb5TGKHUL8Ebn..").roles("USER");
    }*/

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Scheduled(fixedRate=30000)
    public void work() {
        // task execution logic

        for(User user : userRepository.findAll()){
            if(membershipRepository.getAllMembershipByUserId(user.getId()).size() != 0) {
                //log.info(membershipRepository.getAllMembershipByUserId(user.getId()).get(0).toString());
                GregorianCalendar endOfMembership = membershipRepository.getAllMembershipByUserId(user.getId()).get(0).getEndOfMembership();
                GregorianCalendar currentDate = new GregorianCalendar();

                long differenceInDays = (endOfMembership.getTimeInMillis() - currentDate.getTimeInMillis()) / 1000/60/60/24;

                int year = endOfMembership.get(Calendar.YEAR);
                int month = endOfMembership.get(Calendar.MONTH);
                int nextYear = year;
                int nextMonth = month;


                while(differenceInDays < 30){

                    if(month + 1 > 11){
                        nextYear = year + 1;
                        nextMonth = 0;
                    }else{
                        nextYear = year;
                        nextMonth = month + 1;
                    }

                    Membership membership = new Membership();
                    membership.setUserId(user.getId());
                    membership.setStartOfMembership(new GregorianCalendar(year, month, endOfMembership.get(Calendar.DAY_OF_MONTH)));
                    membership.setEndOfMembership(new GregorianCalendar(nextYear, nextMonth, endOfMembership.get(Calendar.DAY_OF_MONTH)));

                    log.info(year + " - " + month);
                    log.info(nextYear + " - " + nextMonth);

                    membershipRepository.save(membership);

                    endOfMembership = membershipRepository.getAllMembershipByUserId(user.getId()).get(0).getEndOfMembership();
                    differenceInDays = (endOfMembership.getTimeInMillis() - currentDate.getTimeInMillis()) / 1000/60/60/24;

                    year = endOfMembership.get(Calendar.YEAR);
                    month = endOfMembership.get(Calendar.MONTH);

                }
/*
                if(differenceInDays < 20){

                    log.info((differenceInSeconds/60/60/24) + "");

                    for(int i = endOfMembership.get(Calendar.YEAR); i <= currentDate.get(Calendar.YEAR); i++){

                        if(endOfMembership.get(Calendar.YEAR) != currentDate.get(Calendar.YEAR)) {
                            if (i == endOfMembership.get(Calendar.YEAR) && i != currentDate.get(Calendar.YEAR)) {
                                for (int j = endOfMembership.get(Calendar.MONTH); j < 12; j++) {
                                    Membership membership = new Membership();
                                    membership.setUserId(user.getId());
                                    membership.setStartOfMembership(new GregorianCalendar(i, j, endOfMembership.get(Calendar.DAY_OF_MONTH)));
                                    membership.setEndOfMembership(new GregorianCalendar(i, j == 11 ? 0 : j + 1, endOfMembership.get(Calendar.DAY_OF_MONTH)));
                                    log.info(i + "-" + j +" - "+ endOfMembership.get(Calendar.DAY_OF_MONTH));
                                }
                            } else if (i != endOfMembership.get(Calendar.YEAR) && i != currentDate.get(Calendar.YEAR)) {
                                for (int j = 0; j < 12; j++) {
                                    Membership membership = new Membership();
                                    membership.setUserId(user.getId());
                                    membership.setStartOfMembership(new GregorianCalendar(i, j, endOfMembership.get(Calendar.DAY_OF_MONTH)));
                                    membership.setEndOfMembership(new GregorianCalendar(i, j == 11 ? 0 : j + 1, endOfMembership.get(Calendar.DAY_OF_MONTH)));
                                    log.info(i + "-" + j +" - "+ endOfMembership.get(Calendar.DAY_OF_MONTH));

                                }
                            } else if (i != endOfMembership.get(Calendar.YEAR) && i == currentDate.get(Calendar.YEAR)) {
                                for (int j = 0; j <= currentDate.get(Calendar.MONTH); j++) {
                                    Membership membership = new Membership();
                                    membership.setUserId(user.getId());
                                    membership.setStartOfMembership(new GregorianCalendar(i, j, endOfMembership.get(Calendar.DAY_OF_MONTH)));
                                    membership.setEndOfMembership(new GregorianCalendar(i, j == 11 ? 0 : j + 1, endOfMembership.get(Calendar.DAY_OF_MONTH)));
                                    log.info(i + "-" + j +" - "+ endOfMembership.get(Calendar.DAY_OF_MONTH));

                                }
                            }
                        }else{
                            for(int j = endOfMembership.get(Calendar.MONTH); j < currentDate.get(Calendar.MONTH); j++){
                                Membership membership = new Membership();
                                membership.setUserId(user.getId());
                                membership.setStartOfMembership(new GregorianCalendar(i, j, endOfMembership.get(Calendar.DAY_OF_MONTH)));
                                membership.setEndOfMembership(new GregorianCalendar(i, j == 11 ? 0 : j + 1, endOfMembership.get(Calendar.DAY_OF_MONTH)));
                                log.info(i + "-" + j +" - "+ endOfMembership.get(Calendar.DAY_OF_MONTH));

                            }
                        }

                    }

                }
*/

            }
        }

    }


}
