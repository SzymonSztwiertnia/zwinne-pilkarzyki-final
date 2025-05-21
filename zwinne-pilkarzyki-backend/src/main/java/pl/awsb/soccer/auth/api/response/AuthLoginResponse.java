package pl.awsb.soccer.auth.api.response;

import lombok.Builder;

@Builder
public record AuthLoginResponse(
        String accessToken,
        String refreshToken
) {
}
