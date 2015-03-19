package net.labhackercd.edemocracia.ui.message;

import android.text.Editable;
import android.text.Html;

import org.xml.sax.XMLReader;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * A {@link Html.TagHandler} that can extract tag arguments through black magic.
 *
 * It's not used right now, but it will be pretty handy when we start to support more bbcodes.
 */
public abstract class AttributeTagHandler implements Html.TagHandler {
    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        handleTag(opening, tag, opening ? getAttributes(xmlReader) : Collections.emptyMap(), output);
    }

    protected abstract void handleTag(boolean opening, String tag, Map<String, String> attrs, Editable output);

    private Map<String, String> getAttributes(final XMLReader xmlReader) {
        try {
            Field elementField = xmlReader.getClass().getDeclaredField("theNewElement");
            elementField.setAccessible(true);
            Object element = elementField.get(xmlReader);

            if (element == null)
                return Collections.emptyMap();

            Field attsField = element.getClass().getDeclaredField("theAtts");
            attsField.setAccessible(true);
            Object atts = attsField.get(element);
            Field dataField = atts.getClass().getDeclaredField("data");
            dataField.setAccessible(true);
            String[] data = (String[]) dataField.get(atts);
            Field lengthField = atts.getClass().getDeclaredField("length");
            lengthField.setAccessible(true);

            int len = (Integer) lengthField.get(atts);

            Map<String, String> attributes = new LinkedHashMap<>();

            /**
             * MSH: Look for supported attributes and add to hash map.
             * This is as tight as things can get :)
             * The data index is "just" where the keys and values are stored.
             */
            for(int i = 0; i < len; i++)
                attributes.put(data[i * 5 + 1], data[i * 5 + 4]);

            return attributes;
        } catch (Exception e) {
            Timber.e(e, "Error while fetching attributes.");
            return Collections.emptyMap();
        }
    }
}
