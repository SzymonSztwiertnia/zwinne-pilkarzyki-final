package pl.awsb.soccer.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DbUserRepository extends JpaRepository<DbUser, Long> {
    Optional<DbUser> findByEmail(String email);

    List<DbUser> getDbUserById(Long id);
}
