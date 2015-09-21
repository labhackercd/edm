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

package net.labhackercd.nhegatu.account;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.labhackercd.nhegatu.data.api.model.User;

import java.lang.reflect.Type;

/** Little utility to write typed objects into user data. */
class GsonUserData {
    static <T> String toUserData(T value) {
        return gson.toJson(Entry.create(value));
    }

    static <T> T fromUserData(String data) throws JsonParseException {
        JsonObject json = gson.fromJson(data, JsonObject.class);
        Entry<T> entry = Entry.<T>fromJson(gson, json);
        return entry.value;
    }

    static String createKey(Object key) {
        return GsonUserData.class.getCanonicalName().concat("{").concat(gson.toJson(key)).concat("}");
    }

    private static class Entry<T> {
        public final T value;
        public final Class<?> classOfValue;

        private Entry(T value, Class<?> classOfValue) {
            this.value = value;
            this.classOfValue = classOfValue;
        }

        public static <T> Entry<T> create(T value) {
            return new Entry<>(value, value.getClass());
        }

        public static <T> Entry<T> fromJson(Gson gson, JsonObject entry) throws JsonParseException {
            Type type = new TypeToken<Class<T>>() {}.getType();
            Class<T> classOfValue = gson.fromJson(entry.get("classOfValue"), type);
            T value = gson.fromJson(entry.get("value"), classOfValue);
            return Entry.create(value);
        }
    }

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Class.class, new ClassTypeAdapter(User.class.getClassLoader()))
            .create();

    private static class ClassTypeAdapter implements JsonSerializer<Class>, JsonDeserializer<Class> {
        private final ClassLoader classLoader;

        private ClassTypeAdapter(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        public JsonElement serialize(Class src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getCanonicalName());
        }

        @Override
        public Class deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            String className = json.getAsString();
            try {
                return classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                String message = String.format(
                        "Serialized class `%s' could not be found at runtime.", className);
                throw new JsonParseException(message, e);
            }
        }
    }
}
