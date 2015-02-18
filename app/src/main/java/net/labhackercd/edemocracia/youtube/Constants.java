package net.labhackercd.edemocracia.youtube;

import com.google.android.gms.common.Scopes;
import com.google.api.services.youtube.YouTubeScopes;

public class Constants {
    public static final int MAX_KEYWORD_LENGTH = 30;
    public static final String DEFAULT_KEYWORD = "e-democracia app";

    // A playlist ID is a string that begins with PL. You must replace this string with the correct
    // playlist ID for the app to work
    public static final String UPLOAD_PLAYLIST = "PLdXJWWDmjGDJEJDArbcTKGEu41JvWLyTP";
    public static final String APP_NAME = "edemocracia";

    public static final String REQUEST_AUTHORIZATION_INTENT = "net.labhackercd.edemocracia.RequestAuth";
    public static final String REQUEST_AUTHORIZATION_INTENT_PARAM = "net.labhackercd.edemocracia.RequestAuth.param";

    public static final String[] AUTH_SCOPES = {Scopes.PROFILE, YouTubeScopes.YOUTUBE};
}
