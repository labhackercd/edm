package net.labhackercd.edemocracia.data.api.client.exception;

import com.liferay.mobile.android.exception.ServerException;

public class AuthorizationException extends ServerException {
    public AuthorizationException(Exception e) {
        super(e);
    }
}