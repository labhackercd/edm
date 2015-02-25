package net.labhackercd.edemocracia.data.api.client.exception;

import com.liferay.mobile.android.exception.ServerException;

public class PrincipalException extends ServerException {
    public PrincipalException(Exception e) {
        super(e);
    }
}
