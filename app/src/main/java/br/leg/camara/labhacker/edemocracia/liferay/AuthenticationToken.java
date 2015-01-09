package br.leg.camara.labhacker.edemocracia.liferay;

import android.text.format.Time;

public class AuthenticationToken {
    // 25 minutes, in seconds
    private static final int DEFAULT_TOKEN_DURATION = 25 * 60;

    private Time expiration;
    private String token;

    public AuthenticationToken() {
        init(null, getExpirationFor(-1));
    }

    public AuthenticationToken(String token) {
        init(token, DEFAULT_TOKEN_DURATION);
    }

    public AuthenticationToken(String token, int durationInSeconds) {
        init(token, getExpirationFor(durationInSeconds));
    }

    public AuthenticationToken(String token, Time expiration) {
        init(token, expiration);
    }

    public String getToken() {
        return token;
    }

    public String toString() {
        return getToken();
    }

    public boolean isExpired() {
        return getExpiration().before(getExpirationFor(0));
    }

    public Time getExpiration() {
        return expiration;
    }

    private void init(String token, int duration) {
        Time expiration = new Time();
        expiration.setToNow();
        expiration.second += duration;
        expiration.normalize(true);
        init(token, expiration);
    }

    private void init(String token, Time expiration) {
        this.token = token;
        this.expiration = expiration;
    }

    private Time getExpirationFor(int durationInSeconds) {
        Time time = new Time();
        time.setToNow();
        time.second += durationInSeconds;
        return time;
    }
}
