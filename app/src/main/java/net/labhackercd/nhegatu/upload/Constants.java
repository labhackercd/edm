package net.labhackercd.nhegatu.upload;

import com.google.android.gms.common.Scopes;
import com.google.api.services.youtube.YouTubeScopes;

public class Constants {
    public static final String REQUEST_AUTHORIZATION_INTENT = "net.labhackercd.edemocracia.RequestAuth";
    public static final String REQUEST_AUTHORIZATION_INTENT_PARAM = "net.labhackercd.edemocracia.RequestAuth.param";

    public static final String[] AUTH_SCOPES = {Scopes.PROFILE, YouTubeScopes.YOUTUBE};
}
