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

/** An API endpoint. */
public interface Endpoint {

    /** Create an endpoint with the provided {@code url}. */
    public static Endpoint createFixed(final String url) {
        if (url == null)
            throw new IllegalStateException("url == null");
        return new Endpoint() {
            @Override public String url() {
                return url;
            }
        };
    }

    /**
     * The base URL.
     * <p>
     * Consumers will call this method every time they need to create a request
     * allowing values to change over time.
     */
    public String url();
}
