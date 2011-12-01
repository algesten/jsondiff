package foodev.jsondiff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.jackson.node.ObjectNode;

import com.google.gson.JsonObject;

import foodev.jsondiff.incava.IncavaDiff;
import foodev.jsondiff.incava.IncavaEntry;
import foodev.jsondiff.jsonwrap.JsonWrapperException;
import foodev.jsondiff.jsonwrap.JsonWrapperFactory;
import foodev.jsondiff.jsonwrap.JzonArray;
import foodev.jsondiff.jsonwrap.JzonElement;
import foodev.jsondiff.jsonwrap.JzonObject;


/**
 * Util for comparing two json-objects and create a new object with a set of instructions to transform the first to the
 * second. The output of this util can be fed into {@link JsonPatch#apply(JzonObject, JzonObject)}.
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
 * Instruction order is merge, set, insert, delete. This is important when altering arrays, since insertions will affect
 * the array index of subsequent delete instructions.
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

    // by providing null as hint we default to GSON.
    private static Object hint = null;


    // For testing
    static void setHint(Object hint) {

        JsonDiff.hint = hint;

    }


    /**
     * Runs a diff on the two given JSON objects given as string to produce another JSON object with instructions of how
     * to transform the first argument to the second. Both from/to are expected to be objects {}.
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
    public static String diff(String from, String to) throws IllegalArgumentException, JsonWrapperException {

        JzonElement fromEl = JsonWrapperFactory.parse(from, JsonDiff.hint);
        JzonElement toEl = JsonWrapperFactory.parse(to, JsonDiff.hint);

        return diff(fromEl, toEl).toString();

    }


    /**
     * Runs a diff using underlying JSON parser implementations. Accepts two GSON {@link JsonObject} or (if jar is
     * provided) a Jackson style {@link ObjectNode}. The returned type is the same as the received.
     * 
     * @param from
     *            Object to transform from. One of {@link JsonObject} or {@link ObjectNode} (if jar available).
     * @param to
     *            Object to transform to. One of {@link JsonObject} or {@link ObjectNode} (if jar available).
     * @return Object containing the instructions. The type will be the same as that passed in constructor.
     * @throws IllegalArgumentException
     *             if the given arguments are not accepted.
     */
    public static Object diff(Object from, Object to) throws IllegalArgumentException {

        JzonElement fromEl = JsonWrapperFactory.wrap(from);
        JzonElement toEl = JsonWrapperFactory.wrap(to);

        JzonObject diff = diff(fromEl, toEl);

        return diff.unwrap();
    }


    private static JzonObject diff(JzonElement fromEl, JzonElement toEl) {

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

        adjustArrayMutationBoundaries(diff, fromLeaves, toLeaves);

        ArrayList<Leaf> mutations = buildMutationList(diff, fromLeaves, toLeaves);

        adjustArrayIndexes(mutations, fromArrs, toArrs);

        JzonObject patch = JsonWrapperFactory.createJsonObject(from);

        for (Leaf leaf : mutations) {
            leaf.apply(patch);
        }

        return patch;

    }


    // goes through the IncavaEntry and build the corresponding list of LEAF
    // the set will remove any add + delete leaving just the add instruction.
    private static ArrayList<Leaf> buildMutationList(List<IncavaEntry> diff, ArrayList<Leaf> fromLeaves,
            ArrayList<Leaf> toLeaves) {

        LinkedHashMap<Integer, Leaf> mutations = new LinkedHashMap<Integer, Leaf>();

        for (IncavaEntry ent : diff) {

            if (ent.getAddedStart() >= 0 && ent.getAddedEnd() >= 0) {

                // additions first since deletions may hash to same position,
                // and will in that case not be added.
                for (int i = ent.getAddedStart(), n = ent.getAddedEnd(); i <= n; i++) {

                    Leaf add = toLeaves.get(i);

                    // if any parent has been added already,
                    // this node is irrelevant
                    if (anyParent(mutations, add.parent)) {
                        continue;
                    }

                    // right now we assume it's an insertion, but going through
                    // deletions we may change this to SET.
                    add.oper = Oper.INSERT;

                    // get an indexed hash of the parent node since
                    // we are trying to match up indexed positions
                    // in add/remove
                    int hash = add.parent.doHash(true);

                    mutations.put(hash, add);

                }
            }

        }

        for (IncavaEntry ent : diff) {

            if (ent.getDeletedStart() >= 0 && ent.getDeletedEnd() >= 0) {

                // go through deletions and add only those that don't have a corresponding addition.
                for (int i = ent.getDeletedStart(), n = ent.getDeletedEnd(); i <= n; i++) {

                    Leaf del = fromLeaves.get(i);

                    // indexed hash of parent
                    int hash = del.parent.doHash(true);

                    // the hash will be matching if an element
                    // is both deleted/added
                    Leaf add = mutations.get(hash);

                    if (add == null) {

                        // if any parent has been added/removed already,
                        // this node is irrelevant
                        if (anyParent(mutations, del.parent)) {
                            continue;
                        }

                        del.oper = Oper.DELETE;
                        mutations.put(hash, del);

                    } else {

                        // both added and deleted, change to SET.
                        add.oper = Oper.SET;

                    }

                }

            }

        }

        return new ArrayList<Leaf>(mutations.values());

    }


    private static boolean anyParent(LinkedHashMap<Integer, Leaf> mutations, Node node) {

        if (node == null) {
            return false;
        }

        if (mutations.containsKey(node.doHash(true))) {
            return true;
        }

        return anyParent(mutations, node.parent);

    }


    private static void findLeaves(Node parent, JzonElement el, List<Leaf> leaves, HashMap<Integer, ArrNode> arrs) {

        leaves.add(new Leaf(parent, el));

        if (el.isJsonObject()) {

            Set<Entry<String, JzonElement>> memb = new TreeSet<Entry<String, JzonElement>>(OBJECT_KEY_COMPARATOR);
            memb.addAll(((JzonObject) el).entrySet());

            for (Entry<String, JzonElement> e : memb) {

                ObjNode newParent = new ObjNode(parent, el, e.getKey());
                findLeaves(newParent, e.getValue(), leaves, arrs);

            }

        } else if (el.isJsonArray()) {

            JzonArray arr = (JzonArray) el;

            for (int i = 0, n = arr.size(); i < n; i++) {

                ArrNode newParent = new ArrNode(parent, el, i);

                // this array saves a reference to all arrnodes
                // which is used to adjust arr node indexes.
                arrs.put(newParent.doHash(true), newParent);

                findLeaves(newParent, arr.get(i), leaves, arrs);

            }

        }


    }


    // the diff algorithm may sometimes make a strange diff for arrays of objects.
    // from: {a:[{b:2},{c:3}]}
    // to: {a:[{c:3}]]
    // ends up with deleting
    // ~a[0]: {-b:0}
    // -a[1]: 0 // WRONG
    // this is because the intermediate array nodes are thought of as equal
    // and the first for a0 is considered "same" in both from/to which means
    // the patch is not deleting the correct one. Same problem goes for additions. Issue #2
    private static void adjustArrayMutationBoundaries(List<IncavaEntry> diff,
            ArrayList<Leaf> fromLeaves, ArrayList<Leaf> toLeaves) {

        for (int i = 0, n = diff.size(); i < n; i++) {

            IncavaEntry ent = diff.get(i);

            if (ent.getDeletedStart() > 0 && ent.getDeletedEnd() > 0) {

                Leaf first = fromLeaves.get(ent.getDeletedStart());
                Leaf beforeFirst = fromLeaves.get(ent.getDeletedStart() - 1);
                Leaf last = fromLeaves.get(ent.getDeletedEnd());

                boolean beforeIsSameArr = last.parent instanceof ArrNode && beforeFirst.parent.el == last.parent.el;

                if (beforeFirst.parent instanceof ArrNode
                        && first.parent instanceof ObjNode
                        && beforeIsSameArr) {

                    ent = new IncavaEntry(ent.getDeletedStart() - 1, ent.getDeletedEnd() - 1,
                            ent.getAddedStart(), ent.getAddedEnd());

                    diff.set(i, ent);

                }

            }

            if (ent.getAddedStart() > 0 && ent.getAddedEnd() > 0) {

                Leaf first = toLeaves.get(ent.getAddedStart());
                Leaf beforeFirst = toLeaves.get(ent.getAddedStart() - 1);
                Leaf last = toLeaves.get(ent.getAddedEnd());

                boolean beforeIsSameArr = last.parent instanceof ArrNode && beforeFirst.parent.el == last.parent.el;

                if (beforeFirst.parent instanceof ArrNode
                        && first.parent instanceof ObjNode
                        && beforeIsSameArr) {

                    ent = new IncavaEntry(ent.getDeletedStart(), ent.getDeletedEnd(),
                            ent.getAddedStart() - 1, ent.getAddedEnd() - 1);

                    diff.set(i, ent);

                }

            }

        }

    }


    private static void adjustArrayIndexes(ArrayList<Leaf> mutations,
            HashMap<Integer, ArrNode> fromArrs, HashMap<Integer, ArrNode> toArrs) {

        for (Leaf l : mutations) {

            if (l.parent instanceof ArrNode) {

                ArrNode cur = (ArrNode) l.parent;

                if (l.oper == Oper.INSERT) {

                    // used to make hash codes.
                    ArrNode clone = new ArrNode(cur.parent, cur.el, cur.index);

                    ArrNode adjust;

                    while ((adjust = fromArrs.get(clone.doHash(true))) != null) {
                        adjust.index++;
                        clone.index++;
                    }

                } else if (l.oper == Oper.DELETE) {

                    // used to make hash codes.
                    ArrNode clone = new ArrNode(cur.parent, cur.el, cur.index);

                    ArrNode adjust;

                    while ((adjust = toArrs.get(clone.doHash(true))) != null) {
                        adjust.index++;
                        clone.index++;
                    }

                }

            }

        }

    }


    private static enum Oper {
        INSERT,
        DELETE,
        SET
    }


    private static class Leaf implements Comparable<Leaf> {

        final Node parent;
        final JzonElement val;
        Integer hash;
        Oper oper;


        Leaf(Node parent, JzonElement val) {
            this.parent = parent;
            this.val = val;
        }


        void apply(JzonObject patch) {

            // collect all parent nodes to this leaf.
            LinkedList<Node> path = new LinkedList<Node>();
            Node n = parent;
            while (n != null) {
                path.add(n);
                n = n.parent;
            }
            // reverse to get root first
            Collections.reverse(path);

            // remove root
            path.pop();

            StringBuilder key = new StringBuilder();

            JzonObject cur = patch;

            while (!path.isEmpty()) {

                // clear builder
                key.delete(0, key.length());

                // ought to be an ObjNode
                ObjNode on = (ObjNode) path.pop();

                on.toPathEl(key);

                // add on any array specifications
                while (!path.isEmpty() && path.peek() instanceof ArrNode) {
                    path.pop().toPathEl(key);
                }

                if (path.isEmpty()) {

                    switch (oper) {
                    case DELETE:
                        key.insert(0, "-");
                        cur.addProperty(key.toString(), 0);
                        break;
                    case INSERT:
                        int index = key.lastIndexOf("[");
                        if (index >= 0) {
                            key.insert(index + 1, "+");
                        }
                    case SET:
                        cur.add(key.toString(), val);
                        break;
                    }

                } else {

                    // continue object traversal

                    // if we're about to traverse, it's impossible that
                    // the entire node has been added, in which case it must
                    // be an object merge.

                    key.insert(0, "~");

                    String keyStr = key.toString();

                    if (cur.has(keyStr)) {

                        cur = (JzonObject) cur.get(keyStr);

                    } else {

                        JzonObject next = JsonWrapperFactory.createJsonObject(patch);
                        cur.add(keyStr, next);
                        cur = next;

                    }

                }

            }

        }


        @Override
        public int hashCode() {
            if (hash != null) {
                return hash;
            }
            int i = parent.hashCode();
            i = i * 31 + (val.isJsonPrimitive() || val.isJsonNull() ? val.hashCode() : 0);
            hash = i;
            return hash;
        }


        @Override
        public boolean equals(Object obj) {
            return hashCode() == ((Leaf) obj).hashCode();
        }


        @Override
        public int compareTo(Leaf o) {
            return hashCode() - o.hashCode();
        }


        @Override
        public String toString() {
            return "LEAF<" + oper + "_" + val + "_" + hashCode() + ">\n";
        }
    }


    private static abstract class Node {

        final Node parent;
        final JzonElement el;
        int hash = -1;


        Node(Node parent, JzonElement el) {
            this.el = el;
            this.parent = parent;
        }


        abstract void toPathEl(StringBuilder bld);


        @Override
        public int hashCode() {
            if (hash >= 0) {
                return hash;
            }
            hash = doHash(false);
            return hash;
        }


        protected abstract int doHash(boolean indexed);

    }


    private static class ObjNode extends Node {

        final String key;


        ObjNode(Node parent, JzonElement el, String key) {
            super(parent, el);
            this.key = key;
        }


        @Override
        void toPathEl(StringBuilder bld) {
            bld.append(key);
        }


        @Override
        protected int doHash(boolean indexed) {
            int i = parent.doHash(indexed);
            i = i * 31 + key.hashCode();
            return i;
        }


        @Override
        public String toString() {
            return key;
        }

    }


    private static class ArrNode extends Node {

        int index;


        ArrNode(Node parent, JzonElement el, int index) {
            super(parent, el);
            this.index = index;
        }


        @Override
        void toPathEl(StringBuilder bld) {
            bld.append("[");
            bld.append(index);
            bld.append("]");
        }


        @Override
        protected int doHash(boolean indexed) {
            int i = parent.doHash(indexed);
            i = i * 31 + ArrNode.class.hashCode();
            if (indexed) {
                i = i * 31 + index;
            }
            return i;
        }


        @Override
        public String toString() {
            return "" + index;
        }

    }


    private static class Root extends Node {


        Root() {
            super(null, null);
        }


        @Override
        void toPathEl(StringBuilder bld) {
        }


        @Override
        protected int doHash(boolean indexed) {
            return 0;
        }


        @Override
        public String toString() {
            return "root";
        }

    }


    private static Comparator<Entry<String, JzonElement>> OBJECT_KEY_COMPARATOR = new Comparator<Entry<String, JzonElement>>() {

        @Override
        public int compare(Entry<String, JzonElement> o1, Entry<String, JzonElement> o2) {

            return o1.getKey().compareTo(o2.getKey());

        }
    };


}
