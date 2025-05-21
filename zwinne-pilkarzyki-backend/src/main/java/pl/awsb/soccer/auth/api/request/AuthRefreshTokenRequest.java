package pl.awsb.soccer.auth.api.request;

public record AuthRefreshTokenRequest(
        String email,
        String refreshToken
) {
}
