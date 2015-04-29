package net.labhackercd.nhegatu.data.api.model.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Small utility to ease the loading of JSONObject and JSONArrays of JSONObjects we
 * retrieve from the remote services.
 *
 * Usage:
 *
 *      class Album {
 *          private long albumId;
 *          private String title;
 *          private String artistName;
 *          private int stars;
 *
 *          // ... class code ...
 *
 *          public static final JSONReader<Album> JSON_READER = new JSONReader<Album> {
 *              @Override
 *              public static Album fromJSON(JSONObject json, JSONReader<Album> reader) {
 *              }
 *          };
 *      }
 *
 *      [...]
 *
 *          JSONArray albums = getSomeAlbumsFromSomeWhere();
 *
 *          // It could be something like this:
 *          // [{"albumId": 29223, "title": "Anjunabeats,Volume 11", "artistName": "Above & Beyond", stars: 5}, ...]
 *
 *          Album album = Album.JSON_READER.fromJSON(albums.getJSONObject(0));
 *
 *          // And the best part:
 *          List<Album> albums = Album.JSON_READER.fromJSON(albums, Album.JSON_READER);
 *
 *
 * I don't know about you, but I find this pretty handy...
 *
 * @param <T>
 */
public abstract class JSONReader<T> {
    public abstract T fromJSON(JSONObject json) throws JSONException;

    public List<T> fromJSON(JSONArray json) throws JSONException {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            list.add(fromJSON(json.getJSONObject(i)));
        }
        return list;
    }
}
