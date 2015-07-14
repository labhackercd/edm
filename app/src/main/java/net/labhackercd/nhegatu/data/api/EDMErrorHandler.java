/*
 * This file is part of Nhegatu, the e-Demoracia Client for Android.
 *
 * Nhegatu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nhegatu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nhegatu.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.labhackercd.nhegatu.data.api;

import net.labhackercd.nhegatu.data.api.client.exception.AuthorizationException;

import java.io.IOException;

/**
 * Yeah, this is kinda hard to explain...
 *
 * But hey, don't worry! It should be gone by the next commit.
 */
public class EDMErrorHandler implements ErrorHandler {
    @Override
    public Throwable handleError(ServiceError error) {
        return error.getCause();
    }

    public static Throwable getCause(Throwable t) {
        return t instanceof ServiceError ? t.getCause() : t;
    }

    public static boolean isAuthorizationError(Throwable t) {
        while (t != null) {
            if (t instanceof AuthorizationException)
                return true;
            t = t.getCause();
        }
        return false;
    }

    public static boolean isNetworkError(Throwable t) {
        while (t != null) {
            if (t instanceof IOException)
                return true;
            t = t.getCause();
        }
        return false;
    }
}
