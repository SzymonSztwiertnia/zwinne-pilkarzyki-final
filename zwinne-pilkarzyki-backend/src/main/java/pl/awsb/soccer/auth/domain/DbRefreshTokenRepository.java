package pl.awsb.soccer.auth.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface DbRefreshTokenRepository extends JpaRepository<DbRefreshToken, Long> {

    DbRefreshToken findByToken(String token);

    void deleteByToken(String token);

    @Transactional
    void deleteAllByUserId(Long userId);
}
