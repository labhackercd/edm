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

package net.labhackercd.nhegatu.data;

import java.util.LinkedHashMap;

/**
 * Poor man's in-memory cache.
 */
public class LHMCache extends Cache {
    private LinkedHashMap<Object, Object> cache = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    protected <T> T get(Object key) {
        try {
            return (T) cache.get(key);
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    protected <T> void put(Object key, T value) {
        cache.put(key, value);
    }
}
