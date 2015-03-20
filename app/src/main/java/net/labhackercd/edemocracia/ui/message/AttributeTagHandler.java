package net.labhackercd.edemocracia.ui.message;

import android.text.Editable;
import android.text.Html;

import org.xml.sax.XMLReader;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A {@link Html.TagHandler} that can extract tag arguments through black magic.
 *
 * I know black magic makes ponies sad, but the only alternative to this is to copy-paste-change
 * the whole Html.java file just to make the attributes accessible to TagHandlers. And, as you
 * may already know by now, I have no time for that. So yeah, I'll just leave a note here:
 *
 * FIXME Make ponies happy **
 *
 * [**] Oh, there's also a reason beyond the ponies happiness: reflection is slow and can break anytime.
 */
public abstract class AttributeTagHandler implements Html.TagHandler {
    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        Map<String, String> attrs = getAttributes(xmlReader);
        if (attrs == null) attrs = Collections.emptyMap();
        handleTag(opening, tag, attrs, output);
    }

    protected abstract void handleTag(boolean opening, String tag, Map<String, String> attrs, Editable output);

    private Map<String, String> getAttributes(final XMLReader xmlReader) {
        try {
            Field elementField = xmlReader.getClass().getDeclaredField("theNewElement");
            elementField.setAccessible(true);
            Object element = elementField.get(xmlReader);

            if (element == null)
                return null;

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
            return null;
        }
    }
}
