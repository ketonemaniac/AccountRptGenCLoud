package net.ketone.accrptgen.auth.service;

import com.google.common.collect.ImmutableMap;
import net.ketone.accrptgen.auth.InitialDataLoader;
import net.ketone.accrptgen.auth.model.Role;
import net.ketone.accrptgen.auth.model.User;
import net.ketone.accrptgen.auth.repository.RoleRepository;
import net.ketone.accrptgen.auth.repository.UserRepository;
import net.ketone.accrptgen.store.StorageService;
import net.ketone.accrptgen.util.UserUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static net.ketone.accrptgen.config.Constants.USERS_FILE;
import static net.ketone.accrptgen.config.Constants.USERS_FILE_SEPARATOR;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    @Qualifier("persistentStorage")
    private StorageService storageService;

    private static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());

    private Map<String, Role> roles;

    @PostConstruct
    public void init() {
        roles = ImmutableMap.of(
                "User", save(Role.builder().name("User").build()),
                "Admin", save(Role.builder().name("Admin").build())
        );
    }


    @Override
    public Mono<User> save(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return saveWithEncryptedPassword(user);
    }


    @Override
    public Mono<User> updatePassword(String username, String clearPassword) {
        return Mono.fromCallable(() -> userRepository.findByUsername(username))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "No Logged in User found")))
                .map(user -> {
                    user.setPassword(clearPassword);
                    return user;
                })
                .flatMap(this::save)
                .map(this::ripPassword);
    }

    @Override
    public Mono<User> updateUser(User updatedUser) {
        return Mono.fromCallable(() -> userRepository.findByUsername(updatedUser.getUsername()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("User %s not found", updatedUser.getUsername()))))
                .map(user -> user.toBuilder()
                        .email(updatedUser.getEmail())
                        .roles(updatedUser.getRoles().stream()
                                .map(Role::getName)
                                .map(roles::get)
                                .collect(Collectors.toSet()))
                        .build())
                .flatMap(this::saveWithEncryptedPassword)
                .map(this::ripPassword);

    }

    @Override
    public Mono<User> createUser(User user, boolean isInit) {
        return Mono.fromCallable(() -> userRepository.findByUsername(user.getUsername()))
                .flatMap(existingUser -> Mono.error(new RuntimeException(
                        String.format("user %s already exists", existingUser.getUsername()))))
                .then(saveWithEncryptedPassword(user.toBuilder()
                        .roles(user.getRoles().stream().map(Role::getName).map(roles::get)
                                .collect(Collectors.toSet()))
                        .password(isInit ? user.getPassword() :
                                bCryptPasswordEncoder.encode(user.getPassword()))
                        .build(), isInit)
                );
    }

    @Override
    public Mono<User> deleteUser(String username) {
        return Mono.fromCallable(() -> userRepository.deleteByUsername(username))
                .doOnSuccess(r -> persistUsers());
    }

    @Override
    public Mono<User> findByUsername(String username) {
        return Mono.fromCallable(() -> Optional.ofNullable(userRepository.findByUsername(username))
                .orElse(User.builder().username("Anonymous").build()));
    }

    @Override
    public Flux<User> findAllUsers() {
        return Mono.fromCallable(() -> userRepository.findAll())
                .flatMapMany(users -> Flux.fromStream(users.stream()))
                .map(this::ripPassword);
    }


    private Role save(Role role) {
        return roleRepository.save(role);
    }

    private Mono<User> saveWithEncryptedPassword(User user) {
        return saveWithEncryptedPassword(user, false);
    }

    private Mono<User> saveWithEncryptedPassword(User user, boolean isInit) {
        return Mono.fromCallable(() ->  userRepository.save(user))
                .doOnSuccess(r -> {if(!isInit) persistUsers();});
    }

    private void persistUsers() {
        List<User> users = userRepository.findAll();
        StringBuffer sb = new StringBuffer();
        try {
            users.forEach(user -> {
                sb.append(user.getUsername()).append(USERS_FILE_SEPARATOR)
                        .append(user.getPassword()).append(USERS_FILE_SEPARATOR)
                        .append(user.getEmail()).append(USERS_FILE_SEPARATOR)
                        .append(user.getRoles().stream()
                        .map(Role::getName).collect(Collectors.joining(USERS_FILE_SEPARATOR)))
                        .append(System.lineSeparator());
            });
            storageService.store(sb.toString().getBytes(), USERS_FILE);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error persisting users", e);
        }
    }

    private User ripPassword(User user) {
        user.setPassword(null);
        return user;
    }

}
