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

package net.labhackercd.nhegatu.data.api.client;

/** A hook allowing clients to customize {@linkplain EDMServiceImpl client} exceptions. */
public interface ErrorHandler {

    /**
     * Return a custom exception to be thrown for an {@link Exception}.
     * It is recommended that you pass the supplied {@code error} as the cause to any new exceptions.
     * <p>
     * Example usage:
     * <pre>
     * <code>
     * class MyErrorHandler implements ErrorHandler {
     *     {@literal @}Override public Throwable handleError(Exception error) {
     *        if (error instanceof ServiceError) {
     *            String message = t.getMessage().toLowerCase().trim();
     *            if (message.matches(".*((no *such|no *)\\w+ *exists).*")) {
     *                return new NotFoundException(error);
     *           }
     *        }
     *        return error;
     *     }
     * }
     * </code>
     * </pre>
     *
     * @param error the original {@link Exception}
     * @return Throwable an exception which will be thrown by the client. Must not be null.
     */
    Throwable handleError(Exception error);

    /** An {@link ErrorHandler} which returns the original error. */
    ErrorHandler DEFAULT = new ErrorHandler() {
        @Override
        public Throwable handleError(Exception error) {
            return error;
        }
    };
}