package pl.awsb.soccer.security;

import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.awsb.soccer.user.domain.DbUser;

import java.util.Collection;
import java.util.List;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CustomUserDetails implements UserDetails {
    @Getter
    private final DbUser user;
    String username;
    String password;
    List<GrantedAuthority> authorities;

    public CustomUserDetails(DbUser user) {
        this.user = user;
        this.username = user.getEmail();
        this.password = user.getPassword();
        this.authorities = List.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}