package net.labhackercd.nhegatu.account;

import android.accounts.Account;

import android.accounts.AccountManager;
import android.content.Context;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.labhackercd.nhegatu.data.Cache;
import net.labhackercd.nhegatu.data.api.model.User;
import timber.log.Timber;

import java.lang.reflect.Type;

/** Caches stuff into an {@link Account}'s *user data* section. */
public class UserDataCache extends Cache {
    private final Account account;
    private final AccountManager manager;

    public UserDataCache(Context context, Account account) {
        this.manager = AccountManager.get(context);
        this.account = account;
    }

    @Override
    protected <T> T get(Object key) {
        String json = manager.getUserData(account, genKey(key));
        return json == null ? null : this.<T>loadValue(json);
    }

    @Override
    protected <T> void put(Object key, T value) {
        manager.setUserData(account, genKey(key), genValue(value));
    }

    public static UserDataCache with(Context context, Account account) {
        return new UserDataCache(context.getApplicationContext(), account);
    }

    /** XXX WARNING FIXME: This sucks. I have no idea what I'm doing here. Seriously, don't read this. */

    private static String genKey(Object key) {
        return UserDataCache.class.getCanonicalName().concat("{").concat(gson.toJson(key)).concat("}");
    }

    private <T> String genValue(T value) {
        return gson.toJson(Entry.create(value));
    }

    private <T> T loadValue(String json) {
        try {
            JsonObject entry = gson.fromJson(json, JsonObject.class);

            Class<T> clazz = gson.fromJson(entry.get("classOfValue"), new TypeToken<Class<T>>() {}.getType());

            if (clazz == null)
                return null;

            return gson.fromJson(entry.get("value"), clazz);
        } catch (Throwable t) {
            Timber.e(t, "Failed to load cached value.");
            return null;
        }
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
            try {
                return classLoader.loadClass(json.getAsString());
            } catch (ClassNotFoundException e) {
                throw new JsonParseException("Failed to load class.", e);
            }
        }
    }
}
