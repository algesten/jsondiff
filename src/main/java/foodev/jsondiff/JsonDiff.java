package foodev.jsondiff;

import java.util.ArrayList;
import java.util.Collection;
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

        Collection<Leaf> mutations = buildMutationList(diff, fromLeaves, toLeaves);

        // adjust after mutation list is built since it alters hash
        adjustArrayIndexes(mutations, fromArrs, toArrs);

        JzonObject patch = JsonWrapperFactory.createJsonObject(from);

        for (Leaf leaf : mutations) {
            if (leaf.oper != null) {
                leaf.apply(patch);
            }
        }

        return patch;

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


    // goes through the IncavaEntry and build the corresponding list of LEAF
    // the set will remove any add + delete leaving just the add instruction.
    private static Collection<Leaf> buildMutationList(List<IncavaEntry> diff, ArrayList<Leaf> fromLeaves,
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

                        // mark also deletion leaf to ease debugging.
                        del.oper = Oper.SET;

                    }

                }

            }

        }

        return mutations.values();

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

                int adjustment = findArrayMutationAdjustment(fromLeaves, ent.getDeletedStart(), ent.getDeletedEnd());

                if (adjustment > 0) {
                    ent = new IncavaEntry(ent.getDeletedStart() - adjustment, ent.getDeletedEnd() - adjustment,
                            ent.getAddedStart(), ent.getAddedEnd());

                    diff.set(i, ent);
                }

            }

            if (ent.getAddedStart() > 0 && ent.getAddedEnd() > 0) {

                int adjustment = findArrayMutationAdjustment(toLeaves, ent.getAddedStart(), ent.getAddedEnd());

                if (adjustment > 0) {
                    ent = new IncavaEntry(ent.getDeletedStart(), ent.getDeletedEnd(),
                            ent.getAddedStart() - adjustment, ent.getAddedEnd() - adjustment);

                    diff.set(i, ent);
                }

            }

        }

    }


    // attempts to move array addition/deletion boundaries to include array elements beforehand
    // this is only possible for array of objects.
    // attempt to change this situation:
    // [a0] {o} [a1] {o} {o} [a3] {o} {o} [a4]
    //                    x    x   x      
    // to this:
    // [a0] {o} [a1] {o} {o} [a3] {o} {o} [a4]
    //            x   x   x      
    private static int findArrayMutationAdjustment(ArrayList<Leaf> leaves, int start, int end) {

        // no action if nothing to delete.
        // [a0] {o} [a1]
        //       x   x
        if (end == start) {
            return -1;
        }

        int firstArrayElementAt = -1;

        // search for first any array element in range
        // only if we find one are we going to 
        // attempt a move.
        for (int i = start + 1; i <= end; i++) {

            Leaf leaf = leaves.get(i);

            if (leaf.parent instanceof ArrNode) {
                firstArrayElementAt = i;
                break;
            }

        }

        // no array element found in range
        if (firstArrayElementAt < 0) {
            return -1;
        }

        int adjustment = 1;
        while (true) {

            // impossible move
            if (start - adjustment < 0) {
                return -1;
            }

            Leaf first = leaves.get(start - adjustment);
            Leaf last = leaves.get(end - adjustment + 1);

            // we can only move if the *values* hash to exactly the same (including non-indexed parent)
            if (first.hashCode() != last.hashCode()) {
                // fail
                return -1;
            }

            if (first.parent instanceof ArrNode) {
                // success;
                return adjustment;
            }

            // try one more
            adjustment++;

        }

    }


    private static void adjustArrayIndexes(Collection<Leaf> mutations,
            HashMap<Integer, ArrNode> fromArrs, HashMap<Integer, ArrNode> toArrs) {

        HashMap<Integer, ArrNode> todo = new HashMap<Integer, ArrNode>();

        for (Leaf l : mutations) {

            if (l.parent instanceof ArrNode && (l.oper == Oper.DELETE || l.oper == Oper.INSERT)) {

                if (l.oper == Oper.DELETE) {
                    ((ArrNode) l.parent).delta = true;
                } else if (l.oper == Oper.INSERT) {
                    ((ArrNode) l.parent).delta = true;
                }

                // synthetic node just to get index 0 which is added into todo
                // to check that whole array from first index.
                ArrNode startNode = new ArrNode(l.parent.parent, l.parent.el, 0);

                todo.put(startNode.doHash(true), startNode);

            }

        }

        // todo array contains the index 0 hash for all arrays that need to be adjusted.
        for (ArrNode cur : todo.values()) {

            int insert = 0;
            int delete = 0;

            ArrNode fr;
            ArrNode to;

            while (true) {

                fr = fromArrs.get(cur.doHash(true));
                to = toArrs.get(cur.doHash(true));

                // both null means we've reached end of both arrays
                if (fr == null && to == null) {
                    break;
                }

                // changed adjustments
                if (fr != null) {
                    fr.prevDeletes = delete;
                }
                if (to != null) {
                    to.prevInserts = insert;
                }

                // increase delete/insert counts
                if (fr != null && fr.delta) {
                    delete++;
                }
                if (to != null && to.delta) {
                    insert++;
                }

                // changed adjustments
                if (fr != null) {
                    fr.prevInserts = insert;
                }
                if (to != null) {
                    to.prevDeletes = delete;
                }

                cur.index++;

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

                    ArrNode arrNode = (ArrNode) path.pop();

                    arrNode.toPathEl(key, oper, arrNode == parent);

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
            if (val.isJsonArray()) {
                // for arr and obj we must hash in a type qualifier
                // since otherwise changes between these kinds of
                // nodes will be considered equal 
                i = i * 31 + ArrNode.class.hashCode();
            } else if (val.isJsonObject()) {
                i = i * 31 + ObjNode.class.hashCode();
            } else {
                i = i * 31 + (val.isJsonPrimitive() || val.isJsonNull() ? val.hashCode() : 0);
            }
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

            StringBuilder bld = new StringBuilder();

            if (parent != null && parent instanceof ArrNode) {
                parent.toPathEl(bld);
            }
            bld.append("LEAF<");
            if (oper != null) {
                bld.append(oper);
                bld.append("_");
            }
            bld.append(val);
            bld.append("_");
            bld.append(hashCode());
            bld.append(">");
            bld.append("\n");

            return bld.toString();

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
            i = i * 31 + ObjNode.class.hashCode();
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

        // used for adjusting indexes
        boolean delta;
        int prevInserts;
        int prevDeletes;


        ArrNode(Node parent, JzonElement el, int index) {
            super(parent, el);
            this.index = index;
        }


        @Override
        void toPathEl(StringBuilder bld) {
            bld.append("[");
            bld.append(index);
            bld.append(",");
            bld.append(prevDeletes);
            bld.append(",");
            bld.append(prevInserts);
            bld.append("]");
        }


        void toPathEl(StringBuilder bld, Oper oper, boolean last) {

            bld.append("[");

            int idx = index;

            switch (oper) {
            case DELETE:
                // if not last, this is a path specification in
                // which case the index is correct already.
                if (last) {
                    // each arr delete must be adjusted with
                    // previous inserts because of instruction
                    // order
                    idx += prevInserts;
                }
                break;
            case INSERT:
                // each arr insert must be adjusted with
                // previous deletes since they are missing
                // from the to-leafs.
                idx += prevDeletes;
                break;
            case SET:
                // each set must be adjusted with previous
                // deletes since they are missing from
                // the to-leafs
                idx += prevDeletes;
                break;
            }

            if (!last && (oper == Oper.INSERT || oper == Oper.SET)) {
                // if this is not the last element, we are doing
                // a path specification. if that for INSERT/SET we
                // are looking at the to-leaf, in which we must
                // remove previous inserts since they happen later
                // in the instruction order.
                idx -= prevInserts;
            }

            bld.append(idx);

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
