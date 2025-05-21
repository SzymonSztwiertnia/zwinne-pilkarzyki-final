package pl.awsb.soccer.auth.api.response;

import lombok.Builder;

@Builder
public record AuthRefreshTokenResponse(
        String accessToken,
        String refreshToken
) {
}
