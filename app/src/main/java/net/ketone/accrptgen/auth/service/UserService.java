package net.ketone.accrptgen.auth.service;


import net.ketone.accrptgen.auth.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<User> save(User user);

    Mono<User> updatePassword(String username, String clearPassword);

    Mono<User> updateUser(User user);

    Mono<User> createUser(User user, boolean isInit);

    Mono<User> deleteUser(String username);

    Mono<User> findByUsername(String username);

    Flux<User> findAllUsers();

}
