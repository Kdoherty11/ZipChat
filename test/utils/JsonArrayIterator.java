package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class JsonArrayIterator implements Iterable<JSONObject> {

    private JSONArray arr;

    public JsonArrayIterator(JSONArray arr) {
        this.arr = arr;
    }

    @Override
    public Iterator<JSONObject> iterator() {
        return new JsonIterator(arr);
    }

    class JsonIterator implements Iterator<JSONObject> {

        private JSONArray arr;

        private int index = 0;
        private int len;

        public JsonIterator(JSONArray arr) {
            this.arr = arr;
            this.len = arr.length();
        }

        @Override
        public boolean hasNext() {
            return index < len;
        }

        @Override
        public JSONObject next() {
            try {
                return arr.getJSONObject(index++);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

}

