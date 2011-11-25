package foodev.jsondiff;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * Patch tool for differences as produced by {@link JsonDiff#diff(String, String)}.
 * <p>
 * The patch is a JSON object where members order is not defined, however when applying the patch order matters when
 * editing arrays. Therefore all deletions are done before additions, and more specifically for array deletions the
 * order is reverse.
 * <p>
 * Syntax:
 * 
 * <pre>
 * <code>
 * {
 *   "key":     "replaced",           // added or replacing key
 *   "~key":    "replaced",           // added or replacing key (~ doesn't matter for primitive data types)
 *   "key":     null,                 // added or replacing key with null.
 *   "~key":    null,                 // added or replacing key with null (~ doesn't matter for null)
 *   "-key":    0                     // key removed (value is ignored)
 *   "key":     { "sub": "replaced" } // whole object "key" replaced
 *   "~key":    { "sub": "merged" }   // key "sub" merged into object "key", rest of object untouched
 *   "key":     [ "replaced" ]        // whole array added/replaced
 *   "~key":    [ "replaced" ]        // whole array added/replaced (~ doesn't matter for whole array)
 *   "key[4]":  { "sub": "replaced" } // object replacing element 4, rest of array untouched
 *   "~key[4]": { "sub": "merged"}    // merging object at element 4, rest of array untouched
 *   "key[+4]": { "sub": "array add"} // object added after 3 becoming the new 4 (current 4 pushed right)
 *   "~key[+4]":{ "sub": "array add"} // ERROR (nonsense)
 *   "-key[4]:  0                     // removing element 4 current 5 becoming new 4 (value is ignored)
 *   "-key[+4]: 0                     // ERROR (nonsense)
 * }
 * </code>
 * </pre>
 * 
 * @author Martin Algesten
 * 
 */
public class JsonPatch {


    private final static JsonNull JNULL = new JsonNull();


    public static String apply(String orig, String patch) throws IllegalArgumentException {

        JsonElement origEl = JsonDiff.JSON.parse(orig);
        JsonElement patchEl = JsonDiff.JSON.parse(patch);

        if (!origEl.isJsonObject()) {
            throw new IllegalArgumentException("Orig can't be parsed to json object");
        }
        if (!patchEl.isJsonObject()) {
            throw new IllegalArgumentException("Patch can't be parsed to json object");
        }

        apply((JsonObject) origEl, (JsonObject) patchEl);

        return origEl.toString();

    }


    public static void apply(JsonObject orig, JsonObject patch) throws IllegalArgumentException {

        TreeSet<Instruction> instructions = new TreeSet<Instruction>();

        for (Entry<String, JsonElement> entry : patch.entrySet()) {
            instructions.add(new Instruction(entry.getKey(), entry.getValue()));
        }

        for (Instruction instr : instructions) {

            JsonElement obj = orig.get(instr.key);
            JsonArray arr = null;
            int lastIndex = -1;
            boolean grew = false;

            // spool to last index
            if (instr.index != null) {

                lastIndex = instr.index.get(instr.index.size() - 1);

                boolean first = true;

                for (int j = 0; j < instr.index.size(); j++) {

                    int idx = instr.index.get(j);

                    if (obj != null && obj.isJsonArray()) {

                        arr = (JsonArray) obj;
                        grew = arrEnsureLength(arr, idx);

                        obj = arr.get(idx);

                    } else if (instr.oper != '-') {

                        JsonArray tmp = new JsonArray();
                        grew = arrEnsureLength(tmp, idx);

                        if (first) {
                            orig.add(instr.key, tmp);
                        } else {
                            arrSet(arr, idx, tmp);
                        }

                        arr = tmp;
                        obj = null;

                    } else {

                        break;

                    }

                    first = false;

                }

            }

            // obj should now point to the element
            // arr should be the last array object

            if (instr.oper == '~') {

                // looking out for objects and arrays since those
                // are the only two being merged.

                if (instr.el.isJsonObject()) {

                    if (obj != null && obj.isJsonObject() && instr.el.isJsonObject()) {
                        apply((JsonObject) obj, (JsonObject) instr.el);
                        continue;
                    }

                    // otherwise we fall back on replacing key below

                }

            } else if (instr.oper == '-') {

                if (arr != null) {

                    arrRemove(arr, lastIndex);

                } else {

                    orig.remove(instr.key);

                }

                // no more to process for -
                continue;

            }

            // initial '~' and '-' has been stripped and dealt with
            if (arr != null) {

                if (instr.arrayInsert && !grew) {

                    arrInsert(arr, lastIndex, instr.el);

                } else {

                    arrSet(arr, lastIndex, instr.el);

                }

            } else {

                // key replacement
                orig.add(instr.key, instr.el);

            }

        }

    }


    private static ArrayList<Integer> parseIndex(String key) {

        if (key.indexOf('[') > 0) {

            ArrayList<Integer> r = new ArrayList<Integer>();

            int from = 0;

            while (key.indexOf('[', from) > 0) {

                // find array index
                int begin = key.indexOf('[', from);
                int end = key.indexOf(']', from);

                if (begin > 0 && end > (begin + 1)) {

                    String indexStr = key.substring(begin + 1, end);
                    if (indexStr.charAt(0) == '+') {
                        indexStr = indexStr.substring(1);
                    }

                    try {
                        int index = Integer.parseInt(indexStr);
                        r.add(index);
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException("Bad patch. Not a valid array index: " + key);
                    }

                    from = end + 1;

                }

            }

            return r;


        } else {

            return null;

        }

    }


    private static void arrInsert(JsonArray arr, int index, JsonElement el) {

        ArrayList<JsonElement> elements = getElements(arr);

        elements.add(index, el);

    }


    private static void arrRemove(JsonArray arr, int index) {

        ArrayList<JsonElement> elements = getElements(arr);

        elements.remove(index);

    }


    private static boolean arrEnsureLength(JsonArray arr, int idx) {

        ArrayList<JsonElement> elements = getElements(arr);

        int len = idx + 1;

        boolean grew = false;

        while (elements.size() < len) {
            elements.add(JNULL);
            grew = true;
        }

        return grew;

    }


    private static void arrSet(JsonArray arr, Integer index, JsonElement el) {

        ArrayList<JsonElement> elements = getElements(arr);

        elements.set(index, el);

    }


    private final static Field JsonArray_elements;


    static {

        try {
            JsonArray_elements = JsonArray.class.getDeclaredField("elements");
            JsonArray_elements.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @SuppressWarnings("unchecked")
    private static ArrayList<JsonElement> getElements(JsonArray arr) {

        try {
            return (ArrayList<JsonElement>) JsonArray_elements.get(arr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    private static int compareArrays(boolean ascending,
            ArrayList<Integer> l1, ArrayList<Integer> l2) {

        if (l1 == null && l2 != null) {
            return 1;
        } else if (l1 != null && l2 == null) {
            return -1;
        } else if (l1 == null && l2 == null) {
            return 0;
        }

        int i = 0;
        while (true) {
            if (i == l1.size()) {
                if (i == l2.size()) {
                    return 0;
                } else {
                    return -1;
                }
            }
            if (i == l2.size()) {
                return 1;
            }
            int d = (ascending ? 1 : -1) * (l1.get(i) - l2.get(i));
            if (d != 0) {
                return d;
            }
        }

    }


    private static class Instruction implements Comparable<Instruction> {

        final String orig;
        final String key;
        final ArrayList<Integer> index;
        final boolean arrayInsert;
        final char oper;
        final JsonElement el;


        public Instruction(String str, JsonElement el) {

            this.orig = str;
            this.el = el;

            switch (str.charAt(0)) {
            case '-':
                oper = '-';
                str = str.substring(1);
                break;
            case '~':
                oper = '~';
                str = str.substring(1);
                break;
            default:
                oper = ' '; // no oper
            }

            index = parseIndex(str);

            boolean pArrayInsert = false;
            if (index != null) {

                int t = str.lastIndexOf("[+");

                if (t == str.lastIndexOf('[')) {
                    pArrayInsert = true;
                }

                str = str.substring(0, str.indexOf('['));

            }
            this.arrayInsert = pArrayInsert;

            this.key = str;

            if (oper == '~' && arrayInsert) {
                throw new IllegalArgumentException("~ at the same time as array insertion (nonsense): " + orig);
            }

            if (oper == '-' && arrayInsert) {
                throw new IllegalArgumentException("- at the same time as array insertion (nonsense): " + orig);
            }


        }


        @Override
        public int compareTo(Instruction o) {

            // 20 sp no oper
            // 2d -
            // 7e ~
            int i = (int) o.oper - (int) oper;

            if (i == 0) {

                i = key.compareTo(o.key);

                if (i == 0) {

                    boolean ascending = oper != '-';
                    i = compareArrays(ascending, index, o.index);

                    if (i == 0) {

                        i = (arrayInsert == o.arrayInsert ? 0 : (arrayInsert ? 1 : -1));

                        if (i == 0) {
                            throw new IllegalArgumentException("Found duplicate instructions: " + orig);
                        }

                    }

                }

            }

            return i;

        }


        @Override
        public String toString() {
            return orig;
        }

    }

}
