package com.fabio.habiticasync.utils;

import org.json.JSONObject;
import java.util.Set;

/**
 * Utility class for exploring and printing the structure of a JSONObject.
 * Useful for debbuging and discovering the layout of API responses.
 * ----------------------------------
 * Example usage:
 *      JsonExplorer.print(data, "");
 * ----------------------------------
 * Output format:
 *      key -> (Object)
 *        subkey -> value
 * ----------------------------------
 * @author Fabio Peretti Guimarães
 * @version 1.0
 * @since October 2025
 */
public class JsonExplorer {

    /**
     *
     * @param obj The JSONObject to explore,
     * @param prefix A prefix used for identation.
     */
    public static void print(JSONObject obj, String prefix) {
        Set<String> keys = obj.keySet();

        if(keys.isEmpty()) {
            System.out.println(prefix + "(empty object)");
            return;
        }

        for (String key : keys) {
            Object value = obj.get(key);

                // If the value is another JSONObject, recurse deeper
            if (value instanceof JSONObject) {
                System.out.println(prefix + key + " -> (object)");
                print((JSONObject) value, prefix + "  ");
            } else {
                // Otherwise, print the key and its value directly
                System.out.println(prefix + key + " -> " + value);
            }
        }
    }
}
