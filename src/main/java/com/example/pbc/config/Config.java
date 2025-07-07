package com.example.pbc.config;

import com.example.pbc.logs.Logging;
import com.example.pbc.security.JwtFilter;
import com.example.pbc.security.JwtUtil;
import com.example.pbc.security.PasswordEncoder;
import com.example.pbc.service.AuthService;
import com.example.pbc.service.ScoreService;
import com.example.pbc.service.TransferService;
import com.example.pbc.work_databased.AuthRepository;
import com.example.pbc.work_databased.DatabaseManager;
import com.example.pbc.service.Service;
import com.example.pbc.work_databased.ScoreRepository;
import com.example.pbc.work_databased.TransferRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class Config {

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/pbc");
        dataSource.setUsername("PBC");
        dataSource.setPassword("pbc1");
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public DatabaseManager databaseManager(JdbcTemplate jdbcTemplate) {
        return new DatabaseManager(jdbcTemplate);
    }

    @Bean
    public Service ServiceRunner(DatabaseManager dataManager) {
        return new Service(dataManager);
    }
    @Bean
    public StartupRunner startupRunner(Service service) {
        return new StartupRunner(service);
    }
    @Bean
    public Logging logging() {
        return new Logging();
    }

    // aвторизация и регистрация
    @Bean
    public AuthRepository authRepository(JdbcTemplate jdbcTemplate) {
        return new AuthRepository(jdbcTemplate);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder();
    }

    @Bean
    public AuthService authService(AuthRepository authRepository, PasswordEncoder passwordEncoder) {
        return new AuthService(authRepository, passwordEncoder);
    }

    //взаимодействие со счетами пользователя
    @Bean
    public ScoreRepository scoreRepository(JdbcTemplate jdbcTemplate) {
        return new ScoreRepository(jdbcTemplate);
    }
    @Bean
    public ScoreService scoreService(ScoreRepository scoreRepository) {
        return new ScoreService(scoreRepository);
    }
    //переводы
    @Bean
    public TransferRepository transferRepository(JdbcTemplate jdbcTemplate, ScoreRepository scoreRepository) {
        return new TransferRepository(jdbcTemplate,scoreRepository);
    }
    @Bean
    public TransferService transferService(TransferRepository transferRepository, ScoreService scoreService) {
        return new TransferService(transferRepository,scoreService);
    }



    @Bean
    public JwtUtil jwtUtil(){
        return new JwtUtil();
    }
    @Bean
    public JwtFilter jwtFilter(JwtUtil jwtUtil){
        return new JwtFilter(jwtUtil);
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        /*http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login.html", "/register.html","/score.html", "/transfer.html").permitAll()
                        .requestMatchers("/auth/**", "/score/**","/transfer/**").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable());*/

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // Доступ без токена
                        .requestMatchers("/login.html", "/register.html").permitAll()

                        // Разрешаем вход через /auth/login и /auth/register
                        .requestMatchers("/auth/login", "/auth/register").permitAll()

                        // ВСЁ остальное в /auth/** — только по токену
                        .requestMatchers("/auth/**").authenticated()

                        // Все прочие запросы требуют аутентификации
                        .anyRequest().authenticated());


        return http.build();
    }
}
