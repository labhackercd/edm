package br.leg.camara.labhacker.edemocracia.liferay.auth;

import com.google.common.base.CharMatcher;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.format.Time;

import br.leg.camara.labhacker.edemocracia.liferay.HttpHelper;
import br.leg.camara.labhacker.edemocracia.liferay.Session;


class Token {
    // 25 minutes, in seconds
    private static final int DEFAULT_TOKEN_DURATION = 25 * 60;

    private Time expiration;
    private String token;

    public Token() {
        init(null, getExpirationFor(-1));
    }

    public Token(String token) {
        init(token, DEFAULT_TOKEN_DURATION);
    }

    public Token(String token, int durationInSeconds) {
        init(token, getExpirationFor(durationInSeconds));
    }

    public Token(String token, Time expiration) {
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


/**
 * Provides authentication tokens for Sessions.
 */
public class AuthTokenProvider {

    private static Map<Session, Token> tokens = new WeakHashMap<>();

    public static String getAuthenticationToken(Session session) throws Exception {
        Token token = tokens.get(session);

        if (!isTokenUsable(token)) {
            token = fetchUsableToken(session);

            if (!isTokenUsable(token)) {
                token = null;
            }
        }

        if (token != null) {
            tokens.put(session, token);
        }

        return token.getToken();
    }

    private static Token fetchUsableToken(Session session) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) session.getPortalURL().openConnection();

        // Prepare the request just as the session would do
        session.prepareRequest(connection);

        // Process the response just like the session would do
        session.processResponse(connection);

        // Read the response body
        String body = HttpHelper.readBody(connection);

        return extractToken(body);
    }

    private static Token extractToken(String content) {
        Pattern pattern = Pattern.compile("authToken\\s*=\\s*(\"[^\"]+\"|'[^']+')");
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String token = CharMatcher.anyOf("\"'").trimFrom(matcher.group(1));
            return new Token(token);
        } else {
            return null;
        }
    }

    private static boolean isTokenUsable(Token token) {
        return token != null && !token.toString().isEmpty() && !token.isExpired();
    }
}


