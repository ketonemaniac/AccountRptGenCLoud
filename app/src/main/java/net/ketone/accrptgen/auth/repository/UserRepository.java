package net.ketone.accrptgen.auth.repository;

import net.ketone.accrptgen.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    User deleteByUsername(String username);
}
