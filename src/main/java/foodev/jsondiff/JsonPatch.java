package foodev.jsondiff;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.codehaus.jackson.node.ObjectNode;

import com.google.gson.JsonObject;

import foodev.jsondiff.jsonwrap.JsonWrapperException;
import foodev.jsondiff.jsonwrap.JsonWrapperFactory;
import foodev.jsondiff.jsonwrap.JzonArray;
import foodev.jsondiff.jsonwrap.JzonElement;
import foodev.jsondiff.jsonwrap.JzonObject;

/**
 * Patch tool for differences as produced by {@link JsonDiff#diff(String, String)}.
 * <p>
 * The patch is a JSON object where members order is not defined, however when applying the patch order matters when editing arrays. Therefore all deletions are done before
 * additions, and more specifically for array deletions the order is reverse.
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
 * <p>
 * Instruction order is merge, set, insert, delete. This is important when altering arrays, since insertions will affect the array index of subsequent delete instructions.
 * </p>
 * 
 * @author Martin Algesten
 * 
 */
public class JsonPatch {

	// by providing null as hint we default to GSON.
	private static Object hint = null;

	// For testing
	static void setHint(Object hint) {

		JsonPatch.hint = hint;

	}

	/**
	 * Modifies the given original JSON object using the instructions provided and returns the result. Each argument is expected to be a JSON object {}.
	 * 
	 * @param orig
	 *            The original JSON object to modify.
	 * @param patch
	 *            The set of instructions to use.
	 * @return The modified JSON object.
	 * @throws IllegalArgumentException
	 *             if the given arguments are not accepted.
	 * @throws JsonWrapperException
	 *             if the strings can't be parsed as JSON.
	 */
	public static String apply(String orig, String patch) throws IllegalArgumentException {

		// by providing null as hint we default to GSON.
		JzonElement origEl = JsonWrapperFactory.parse(orig, JsonPatch.hint);
		JzonElement patchEl = JsonWrapperFactory.parse(patch, JsonPatch.hint);

		apply(origEl, patchEl);

		return origEl.toString();

	}

	/**
	 * Patches the first argument with the second. Accepts two GSON {@link JsonObject} or (if jar is provided) a Jackson style {@link ObjectNode}.
	 * 
	 * @param orig
	 *            Object to patch. One of {@link JsonObject} or {@link ObjectNode} (if jar available).
	 * @param patch
	 *            Object holding patch instructions. One of {@link JsonObject} or {@link ObjectNode} (if jar available).
	 * @throws IllegalArgumentException
	 *             if the given arguments are not accepted.
	 */
	public static void apply(Object orig, Object patch) {

		JzonElement origEl = JsonWrapperFactory.wrap(orig);
		JzonElement patchEl = JsonWrapperFactory.wrap(patch);

		apply(origEl, patchEl);
		System.out.println(origEl);

	}

	static Comparator<Entry<String, JzonElement>> OBJECT_KEY_COMPARATOR = new Comparator<Entry<String, JzonElement>>() {

		@Override
		public int compare(Entry<String, JzonElement> o1, Entry<String, JzonElement> o2) {
			if (o1.getKey().startsWith("~") && !o2.getKey().startsWith("~")) {
				return 1;
			} else if (!o1.getKey().startsWith("~") && o2.getKey().startsWith("~")) {
				return -1;
			}
			return o1.getKey().compareTo(o2.getKey());
		}
	};

	private static void apply(JzonElement origEl, JzonElement patchEl) throws IllegalArgumentException {

		JzonObject patch = (JzonObject) patchEl;
		Set<Entry<String, JzonElement>> memb = new TreeSet<Entry<String, JzonElement>>(OBJECT_KEY_COMPARATOR);
		memb.addAll(patch.entrySet());
		for (Entry<String, JzonElement> entry : memb) {
			String key = entry.getKey();
			JzonElement value = entry.getValue();
			if (key.startsWith("~")) {
				JzonElement partialInstructions = entry.getValue();
				if (!partialInstructions.isJsonArray()) {
					throw new IllegalStateException();
				}
				JzonArray array = (JzonArray) partialInstructions;
				JzonElement applyTo;
				if (key.equals("~")) {
					applyTo = origEl;
				} else if (origEl.isJsonArray()) {
					int index = Integer.parseInt(key.substring(1));
					applyTo = ((JzonArray) origEl).get(index);
				} else {
					applyTo = ((JzonObject) origEl).get(key.substring(1));
				}
				for (int i = 0; i < array.size(); i++) {
					JzonElement partial = array.get(i);
					if (!partial.isJsonObject()) {
						throw new IllegalStateException();
					}
					Entry<String, JzonElement> childentry = ((JzonObject) partial).entrySet().iterator().next();
					applyPartial(applyTo, childentry.getKey(), childentry.getValue());
				}
			} else if (key.startsWith("-") || key.startsWith("+")) {
				applyPartial(origEl, key, value);
			} else if (origEl.isJsonArray()) {
				int index = Integer.parseInt(key);
				if (value.isJsonPrimitive()) {
					((JzonArray) origEl).set(index, value);
				} else {
					JzonElement childEl = ((JzonArray) origEl).get(index);
					apply(childEl, value);
				}
			} else if (origEl.isJsonObject()) {
				if (value.isJsonPrimitive()) {
					((JzonObject) origEl).add(key, value);
				} else {
					JzonElement childEl = ((JzonObject) origEl).get(key);
					apply(childEl, value);
				}
			}
		}

	}

	static void applyPartial(JzonElement applyTo, String childKey, JzonElement value) {
		if (childKey.startsWith("-")) {
			if (applyTo.isJsonArray()) {
				int index = Integer.parseInt(childKey.substring(1));
				((JzonArray) applyTo).remove(index);
			} else {
				((JzonObject) applyTo).remove(childKey.substring(1));
			}
		} else if (childKey.startsWith("+")) {
			if (applyTo.isJsonArray()) {
				int index = Integer.parseInt(childKey.substring(1));
				((JzonArray) applyTo).insert(index, value);
			} else {
				((JzonObject) applyTo).add(childKey.substring(1), value);
			}
		} else if (applyTo.isJsonArray()) {
			int index = Integer.parseInt(childKey);
			((JzonArray) applyTo).set(index, value);
		} else {
			((JzonObject) applyTo).add(childKey, value);
		}
	}

}
