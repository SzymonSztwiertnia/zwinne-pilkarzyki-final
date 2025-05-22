package pl.awsb.soccer.user.api.response;

import lombok.Builder;


@Builder
public record User (
        Long id,
        String email,
        String name,
        String lastName
) {
}
