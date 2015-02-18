package net.labhackercd.edemocracia.data.api.exception;

import com.liferay.mobile.android.exception.ServerException;

public class NotFoundException extends ServerException {
    public NotFoundException(Exception e) {
        super(e);
    }
}
