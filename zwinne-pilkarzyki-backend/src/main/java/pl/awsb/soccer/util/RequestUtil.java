package pl.awsb.soccer.util;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtil {
    public static String getClientIP(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");

        return header == null ? request.getRemoteAddr() : header.split(",")[0];
    }
}
