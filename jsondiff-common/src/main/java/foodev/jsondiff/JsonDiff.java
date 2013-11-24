package foodev.jsondiff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import foodev.jsondiff.incava.IncavaDiff;
import foodev.jsondiff.incava.IncavaEntry;
import foodev.jsondiff.jsonwrap.JzonArray;
import foodev.jsondiff.jsonwrap.JzonElement;
import foodev.jsondiff.jsonwrap.JzonObject;
import foodev.jsondiff.jsonwrap.Wrapper;

/**
 * Util for comparing two json-objects and create a new object with a set of instructions to transform the first to the second. The output of this util can be fed into
 * {@link JsonPatch#apply(JzonObject, JzonObject)}.
 * 
 * <p>
 * Syntax for instructions:
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
 *   "key[+4]": { "sub": "array add"} // object inserted after 3 becoming the new 4 (current 4 pushed right)
 *   "~key[+4]":{ "sub": "array add"} // object inserted after 3 becoming the new 4 (current 4 pushed right)
 *   "-key[4]:  0                     // removing element 4 current 5 becoming new 4 (value is ignored)
 * }
 * </code>
 * </pre>
 * 
 * <p>
 * Instruction order is merge, set, insert, delete. This is important when altering arrays, since insertions will affect the array index of subsequent delete instructions.
 * </p>
 * 
 * <p>
 * When diffing, the object is expanded to a structure like this: <code><pre>Example: {a:[{b:1,c:2},{d:3}]}
 * </pre></code> Becomes a list of:
 * <ol>
 * <li>Leaf: obj
 * <li>Leaf: array 0
 * <li>Leaf: obj
 * <li>Leaf: b: 1
 * <li>Leaf: c: 2
 * <li>Leaf: array 1
 * <li>Leaf: obj
 * <li>Leaf: d: 3
 * </ol>
 * 
 * @author Martin Algesten
 * 
 */
public class JsonDiff {
	
	static final String MOD = "~";

	static class Instruction {
		Oper oper;
		int index;
		String key;

		boolean isIndexed() {
			return index > -1;
		}
	}

	static final Logger LOG = Logger.getLogger(JsonDiff.class.getName());

	protected final Wrapper factory;

	final static Comparator<Entry<String, JzonElement>> INSTRUCTIONS_COMPARATOR = new Comparator<Entry<String, JzonElement>>() {

		@Override
		public int compare(Entry<String, JzonElement> o1, Entry<String, JzonElement> o2) {
			if (o1.getKey().startsWith(MOD) && !o2.getKey().startsWith(MOD)) {
				return 1;
			} else if (!o1.getKey().startsWith(MOD) && o2.getKey().startsWith(MOD)) {
				return -1;
			}
			return o1.getKey().compareTo(o2.getKey());
		}
	};

	final static Comparator<Entry<String, JzonElement>> OBJECT_KEY_COMPARATOR = new Comparator<Entry<String, JzonElement>>() {

		@Override
		public int compare(Entry<String, JzonElement> o1, Entry<String, JzonElement> o2) {
			return o1.getKey().compareTo(o2.getKey());

		}
	};

	private Visitor visitor;

	JsonDiff(Wrapper factory) {
		this.factory = factory;
	}

	boolean accept(Leaf leaf, JzonArray instructions, JzonObject childPatch) {
		JzonObject object = (JzonObject) factory.parse(leaf.val.toString());
		JzonObject patch = factory.createJsonObject();
		patch.add(MOD , instructions);
		if (!childPatch.entrySet().isEmpty()) {
			patch.entrySet().addAll((Collection) childPatch.entrySet());
		}
		apply(object, patch);
		return visitor.shouldCreatePatch(leaf.val, object);
	}
	
	void apply(JzonElement origEl, JzonElement patchEl) throws IllegalArgumentException {

		JzonObject patch = (JzonObject) patchEl;
		Set<Entry<String, JzonElement>> memb = new TreeSet<Entry<String, JzonElement>>(INSTRUCTIONS_COMPARATOR);
		memb.addAll(patch.entrySet());
		for (Entry<String, JzonElement> entry : memb) {
			String key = entry.getKey();
			JzonElement value = entry.getValue();
			if (key.startsWith(MOD)) {
				JzonElement partialInstructions = entry.getValue();
				if (!partialInstructions.isJsonArray()) {
					throw new IllegalArgumentException();
				}
				JzonArray array = (JzonArray) partialInstructions;
				JzonElement applyTo;
				if (key.equals(MOD)) {
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
						throw new IllegalArgumentException();
					}
					Entry<String, JzonElement> childentry = ((JzonObject) partial).entrySet().iterator().next();
					String childKey = childentry.getKey();
					Instruction instruction = create(childKey);
					boolean newAppliance = false;
					if (instruction.isIndexed() && !applyTo.isJsonArray()) {
						applyTo = factory.createJsonArray();
						newAppliance = true;
					} else if (!instruction.isIndexed() && !applyTo.isJsonObject()) {
						applyTo = factory.createJsonObject();
						newAppliance = true;
					}
					if (newAppliance) {
						if (origEl.isJsonArray()) {
							int index = Integer.parseInt(key);
							((JzonArray) origEl).insert(index, applyTo);
						} else {
							((JzonObject) origEl).add(key.substring(1), applyTo);
						}
					}
					applyPartial(applyTo, instruction, childentry.getValue());
				}
			} else {
				Instruction instruction = create(key);
				if (instruction.oper == Oper.INSERT || instruction.oper == Oper.DELETE) {
					applyPartial(origEl, instruction, value);
				} else if (instruction.isIndexed()) {
					if (!origEl.isJsonArray()) {
						throw new IllegalArgumentException();
					}
					if (value.isJsonPrimitive()) {
						((JzonArray) origEl).set(instruction.index, value);
					} else {
						if (((JzonArray) origEl).size() <= instruction.index) {
							throw new IllegalArgumentException("Wrong index " + instruction.index + " for " + origEl);
						}
						JzonElement childEl = ((JzonArray) origEl).get(instruction.index);
						apply(childEl, value);
					}
				} else if (origEl.isJsonObject()) {
					if (value.isJsonPrimitive() || value.isJsonNull()) {
						((JzonObject) origEl).add(key, value);
					} else {
						JzonElement childEl = ((JzonObject) origEl).get(key);
						apply(childEl, value);
					}
				} else {
					throw new IllegalArgumentException();
				}
			}
		}

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
	public void apply(Object orig, Object patch) {

		JzonElement origEl = factory.wrap(orig);
		JzonElement patchEl = factory.wrap(patch);

		apply(origEl, patchEl);

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
	public String apply(String orig, String patch) throws IllegalArgumentException {

		// by providing null as hint we default to GSON.
		JzonElement origEl = factory.parse(orig);
		JzonElement patchEl = factory.parse(patch);

		apply(origEl, patchEl);

		return origEl.toString();

	}

	void applyPartial(JzonElement applyTo, Instruction instruction, JzonElement value) {
		if (instruction.oper == Oper.DELETE) {
			if (instruction.isIndexed()) {
				if (((JzonArray) applyTo).size() <= instruction.index) {
					throw new IllegalArgumentException("Wrong index " + instruction.index + " for " + applyTo);
				}
				((JzonArray) applyTo).remove(instruction.index);
			} else {
				((JzonObject) applyTo).remove(instruction.key);
			}
		} else if (instruction.oper == Oper.INSERT) {
			if (instruction.isIndexed()) {
				if (((JzonArray) applyTo).size() < instruction.index) {
					throw new IllegalArgumentException("Wrong index " + instruction.index + " for " + applyTo);
				}
				((JzonArray) applyTo).insert(instruction.index, value);
			} else {
				((JzonObject) applyTo).add(instruction.key, value);
			}
		} else if (applyTo.isJsonArray()) {
			if (((JzonArray) applyTo).size() <= instruction.index) {
				throw new IllegalArgumentException("Wrong index " + instruction.index + " for " + applyTo);
			}
			((JzonArray) applyTo).set(instruction.index, value);
		} else {
			((JzonObject) applyTo).add(instruction.key, value);
		}
	}

	void checkIndex(JzonElement applyTo, int index) {
		if (((JzonArray) applyTo).size() < index) {
			throw new IllegalArgumentException();
		}
	}

	Instruction create(String childKey) {
		Instruction instruction = new Instruction();
		if (childKey.startsWith("-")) {
			instruction.key = childKey.substring(1);
			instruction.index = isIndexed(instruction.key);
			instruction.oper = Oper.DELETE;
		} else if (childKey.startsWith("+")) {
			instruction.key = childKey.substring(1);
			instruction.index = isIndexed(instruction.key);
			instruction.oper = Oper.INSERT;
		} else {
			instruction.key = childKey;
			instruction.index = isIndexed(instruction.key);
			instruction.oper = Oper.SET;
		}
		return instruction;
	}

	JzonObject diff(JzonElement fromEl, JzonElement toEl) {

		if (!fromEl.isJsonObject()) {
			throw new IllegalArgumentException("From is not a json object");
		}
		if (!toEl.isJsonObject()) {
			throw new IllegalArgumentException("To is not a json object");
		}

		JzonObject from = (JzonObject) fromEl;
		JzonObject to = (JzonObject) toEl;

		Root fromRoot = new Root();
		Root toRoot = new Root();

		ArrayList<Leaf> fromLeaves = new ArrayList<Leaf>();
		ArrayList<Leaf> toLeaves = new ArrayList<Leaf>();

		HashMap<Integer, ArrNode> fromArrs = new HashMap<Integer, ArrNode>();
		HashMap<Integer, ArrNode> toArrs = new HashMap<Integer, ArrNode>();

		findLeaves(fromRoot, from, fromLeaves, fromArrs);
		findLeaves(toRoot, to, toLeaves, toArrs);

		IncavaDiff<Leaf> idiff = new IncavaDiff<Leaf>(fromLeaves, toLeaves);

		List<IncavaEntry> diff = idiff.diff();
		int delta = 0;
		// be careful with direct use of indexOf: need instance equality, not equals!
		for (IncavaEntry incavaEntry : diff) {
			int deletes = Math.max(0, incavaEntry.getDeletedEnd() - incavaEntry.getDeletedStart() + 1);
			int insertionIndex = (incavaEntry.getDeletedStart() > 0) ? incavaEntry.getDeletedStart() + delta - 1 : 0;
			Leaf fromLeaf = (fromLeaves.size() > insertionIndex) ? fromLeaves.get(insertionIndex) : fromLeaves.get(fromLeaves.size() - 1);
			for (int i = incavaEntry.getDeletedStart(); i < incavaEntry.getDeletedEnd() + 1; i++) {
				// ensure not orphan
				fromLeaf.recover(fromLeaves);
				// proceed to delete
				Leaf toLeaf = fromLeaves.get(i + delta);
				fromLeaf.delete(toLeaf, null);
				fromLeaf = toLeaf;
			}
			fromLeaf = (fromLeaves.size() > insertionIndex) ? fromLeaves.get(insertionIndex) : fromLeaves.get(fromLeaves.size() - 1);
			int index = insertionIndex;
			while (fromLeaf.oper == Oper.DELETE && index > 0) {
				// find a NOT deleted node for set / insertion - parent traversal will be done later
				index--;
				fromLeaf = fromLeaves.get(index);
			}
			for (int i = incavaEntry.getAddedStart(); i < incavaEntry.getAddedEnd() + 1; i++) {
				// ensure not orphan
				fromLeaf.recover(fromLeaves);

				Leaf toLeaf = toLeaves.get(i);
				if (deletes > 0) {
					deletes--;
					Leaf deleted = fromLeaves.get(incavaEntry.getDeletedStart() + delta + (i - incavaEntry.getAddedStart()));
					deleted.recover(fromLeaves);
					if (!fromLeaf.cancelDelete(deleted, toLeaf)) {
						// couldn't cancel delete (different obj key): INSERT
						fromLeaf.insert(toLeaf, null);
						fromLeaves.add(insertionIndex + 1, toLeaf);
						fromLeaf = toLeaf;
						delta++;
					} else {
						// cancel delete: pure SET
						fromLeaf = deleted;
					}
				} else {
					// regular INSERT
					fromLeaf.insert(toLeaf, null);
					fromLeaves.add(insertionIndex + 1, toLeaf);
					fromLeaf = toLeaf;
					delta++;
				}
				insertionIndex++;
			}
		}
		// recover all pending orphans: this could be easily optimized
		for (Leaf fromLeaf : fromLeaves) {
			if (fromLeaf.isOrphan()) {
				fromLeaf.recover(fromLeaves);
			}
		}
		// create patch
		JzonObject patch = fromLeaves.iterator().next().patch();
		// prints the new structure
		// fromLeaves.iterator().next().print();
		return patch;

	}

	/**
	 * Runs a diff using underlying JSON parser implementations. Accepts two GSON {@link JsonObject} or (if jar is provided) a Jackson style {@link ObjectNode}. The returned type
	 * is the same as the received.
	 * 
	 * @param from
	 *            Object to transform from. One of {@link JsonObject} or {@link ObjectNode} (if jar available).
	 * @param to
	 *            Object to transform to. One of {@link JsonObject} or {@link ObjectNode} (if jar available).
	 * @return Object containing the instructions. The type will be the same as that passed in constructor.
	 * @throws IllegalArgumentException
	 *             if the given arguments are not accepted.
	 */
	public Object diff(Object from, Object to) throws IllegalArgumentException {

		JzonElement fromEl = factory.wrap(from);
		JzonElement toEl = factory.wrap(to);

		JzonObject diff = diff(fromEl, toEl);

		return diff.unwrap();
	}
	
	/**
	 * Runs a diff on the two given JSON objects given as string to produce another JSON object with instructions of how to transform the first argument to the second. Both from/to
	 * are expected to be objects {}.
	 * 
	 * @param from
	 *            The origin to transform
	 * @param to
	 *            The desired result
	 * @return The set of instructions to go from -> to as a JSON object {}.
	 * @throws IllegalArgumentException
	 *             if the given arguments are not accepted.
	 * @throws JsonWrapperException
	 *             if the strings can't be parsed as JSON.
	 */
	public String diff(String from, String to) throws IllegalArgumentException {

		JzonElement fromEl = factory.parse(from);
		JzonElement toEl = factory.parse(to);

		return diff(fromEl, toEl).toString();

	}

	Leaf findLeaves(Node parent, JzonElement el, List<Leaf> leaves, HashMap<Integer, ArrNode> arrs) {

		// create leaf for this part
		Leaf leaf = new Leaf(parent, el);
		leaf.factory = factory;
		if (visitor != null) {
			leaf.visitor = this;
		}
		leaves.add(leaf);
		parent.leaf = leaf;

		if (el.isJsonObject()) {

			Set<Entry<String, JzonElement>> memb = new TreeSet<Entry<String, JzonElement>>(OBJECT_KEY_COMPARATOR);
			memb.addAll(((JzonObject) el).entrySet());
			for (Entry<String, JzonElement> e : memb) {

				ObjNode newParent = new ObjNode(parent, e.getKey());
				Leaf child = findLeaves(newParent, e.getValue(), leaves, arrs);
				leaf.children.add(child);
			}

		} else if (el.isJsonArray()) {

			JzonArray arr = (JzonArray) el;
			for (int i = 0, n = arr.size(); i < n; i++) {

				ArrNode newParent = new ArrNode(parent, i);

				// this array saves a reference to all arrnodes
				// which is used to adjust arr node indexes.
				arrs.put(newParent.doHash(true), newParent);

				Leaf child = findLeaves(newParent, arr.get(i), leaves, arrs);
				leaf.children.add(child);
			}

		}
		leaf.init();
		return leaf;
	}
	public Visitor getVisitor() {
		return visitor;
	}

	int isIndexed(String childKey) {
		try {
			return Integer.parseInt(childKey);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public void setVisitor(Visitor visitor) {
		this.visitor = visitor;
	}

}
