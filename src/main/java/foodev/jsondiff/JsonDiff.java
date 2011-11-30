package foodev.jsondiff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import foodev.jsondiff.jsonwrap.JzonArray;
import foodev.jsondiff.jsonwrap.JzonElement;
import foodev.jsondiff.jsonwrap.JzonObject;
import foodev.jsondiff.jsonwrap.JsonWrapperFactory;

import foodev.jsondiff.incava.IncavaDiff;
import foodev.jsondiff.incava.IncavaEntry;


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


    public static String diff(String from, String to) {

        JzonElement fromEl = JsonWrapperFactory.parse(from, JsonDiff.hint);
        JzonElement toEl = JsonWrapperFactory.parse(to, JsonDiff.hint);

        return diff(fromEl, toEl).toString();

    }


    public static Object diff(Object from, Object to) {

        JzonElement fromEl = JsonWrapperFactory.wrap(from);
        JzonElement toEl = JsonWrapperFactory.wrap(to);

        JzonObject diff = diff(fromEl, toEl);

        return diff.unwrap();
    }


    public static JzonObject diff(JzonElement fromEl, JzonElement toEl) {

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

        // quick lookups to check whether a key/index has been added or deleted
        // these hash codes are "index position aware" which means there's no risk
        // of confusing position independent array positions.
        HashSet<Integer> deletions = new HashSet<Integer>();
        HashSet<Integer> additions = new HashSet<Integer>();

        for (IncavaEntry d : diff) {
            for (int i = d.getDeletedStart(), n = d.getDeletedEnd(); i <= n; i++) {
                deletions.add(fromLeaves.get(i).parent.doHash(true));
            }
            for (int i = d.getAddedStart(), n = d.getAddedEnd(); i <= n; i++) {
                additions.add(toLeaves.get(i).parent.doHash(true));
            }
        }

        adjustArrayMutationBoundaries(diff, fromLeaves, toLeaves);
        adjustArrayDeletion(diff, fromLeaves, toArrs, additions);
        adjustArrayInsertion(diff, toLeaves, fromArrs, deletions);

        // rebuild since adjustArrayInsertion/DeletionIndexes alters indexed hash.
        deletions.clear();
        additions.clear();
        for (IncavaEntry d : diff) {
            for (int i = d.getDeletedStart(), n = d.getDeletedEnd(); i <= n; i++) {
                deletions.add(fromLeaves.get(i).parent.doHash(true));
            }
            for (int i = d.getAddedStart(), n = d.getAddedEnd(); i <= n; i++) {
                additions.add(toLeaves.get(i).parent.doHash(true));
            }
        }

        JzonObject patch = JsonWrapperFactory.createJsonObject(from);

        buildPatch(patch, diff, fromLeaves, toLeaves, additions, deletions);

        return patch;

    }


    private static void buildPatch(JzonObject patch, List<IncavaEntry> diff,
            ArrayList<Leaf> from, ArrayList<Leaf> to, HashSet<Integer> additions, HashSet<Integer> deletions) {

        // holds added instructions to check for double additions (where a deep addition is
        // superfluous since a parent has been added). This also holds hash codes where
        // index is used.
        HashSet<Integer> added = new HashSet<Integer>();

        for (IncavaEntry d : diff) {

            if (d.getDeletedEnd() >= 0) {

                int i = d.getDeletedStart();

                Leaf prev = from.get(i);

                // if something is in added, it's a change, we don't
                // do delete + add for change, just add
                if (!selfOrAncestor(additions, prev.parent, true)) {

                    // not array, just remove
                    addInstruction(patch, prev, true, false);

                }

                for (i = i + 1; i <= d.getDeletedEnd(); i++) {

                    Leaf cur = from.get(i);

                    if (cur.hasAncestor(prev.parent)) {

                        // ignore since the whole parent is deleted/changed.

                    } else if (!selfOrAncestor(additions, cur.parent, true)) {

                        // add remove instruction
                        addInstruction(patch, cur, true, false);

                        prev = cur;

                    }

                }

            }

            if (d.getAddedEnd() >= 0) {

                int i = d.getAddedStart();

                Leaf prev = to.get(i);
                if (!selfOrAncestor(added, prev.parent, true)) {
                    addInstruction(patch, prev, selfOrAncestor(deletions, prev.parent, false), true);
                    added.add(prev.parent.doHash(true));
                }

                for (i = i + 1; i <= d.getAddedEnd(); i++) {

                    Leaf cur = to.get(i);

                    if (cur.hasAncestor(prev.parent)) {
                        // ignore since the whole parent has been added.
                    } else if (!selfOrAncestor(added, cur.parent, true)) {
                        addInstruction(patch, cur, selfOrAncestor(deletions, cur.parent, false), true);
                        added.add(cur.parent.doHash(true));
                        prev = cur;
                    }

                }

            }

        }

    }


    private static boolean selfOrAncestor(HashSet<Integer> set, Node node, boolean adjusted) {

        if (node == null) {
            return false;
        }

        if (set.contains(node.doHash(true))) {
            return true;
        }

        return selfOrAncestor(set, node.parent, adjusted);

    }


    private static void addInstruction(JzonObject patch, Leaf leaf, boolean isInDeleted, boolean isInAdded) {

        ArrayList<Node> path = leaf.toPath();
        JzonObject cur = patch;
        StringBuilder keyBld = new StringBuilder();

        int last = path.size() - 1;

        for (int i = 0; i < last; i++) {

            buildKey(keyBld, path, i);
            keyBld.insert(0, '~');

            String key = keyBld.toString();

            JzonObject tmp;
            if (cur.has(key)) {
                tmp = (JzonObject) cur.get(key);
            } else {
                tmp = JsonWrapperFactory.createJsonObject(patch);
                cur.add(key, tmp);
            }

            cur = tmp;

        }

        buildKey(keyBld, path, last);

        if (isInAdded) {

            if (!isInDeleted) {

                ObjNode o = (ObjNode) path.get(last);

                if (o.subindex != null) {

                    // not in deleted and has an array specifier,
                    // it's an inserted array member
                    int p = keyBld.lastIndexOf("[");
                    keyBld.insert(p + 1, '+');

                }

            }

            cur.add(keyBld.toString(), leaf.val);


        } else if (isInDeleted) {

            keyBld.insert(0, '-');
            cur.addProperty(keyBld.toString(), 0);

        }

    }


    private static void buildKey(StringBuilder key, ArrayList<Node> path, int i) {

        key.delete(0, key.length());

        ObjNode o = (ObjNode) path.get(i);

        key.append(o.key);

        ArrNode a = o.subindex;

        while (a != null) {
            key.append('[');
            key.append(a.index);
            key.append(']');
            a = a.subindex;
        }

    }


    private static void findLeaves(Node parent, JzonElement el, List<Leaf> leaves, HashMap<Integer, ArrNode> arrs) {

        leaves.add(new Leaf(parent, el));

        if (el.isJsonObject()) {

            Set<Entry<String, JzonElement>> memb = new TreeSet<Entry<String, JzonElement>>(ENTRY_COMPARATOR);
            memb.addAll(((JzonObject) el).entrySet());

            for (Entry<String, JzonElement> e : memb) {

                ObjNode newParent = new ObjNode(parent, e.getKey());
                findLeaves(newParent, e.getValue(), leaves, arrs);

            }

        } else if (el.isJsonArray()) {

            JzonArray arr = (JzonArray) el;

            for (int i = 0, n = arr.size(); i < n; i++) {

                ArrNode newParent = new ArrNode(parent, el, i);

                if (arrs != null) {
                    arrs.put(newParent.doHash(true), newParent);
                }

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

                if (beforeFirst.parent instanceof ArrNode && first.parent instanceof ObjNode &&
                        last.parent instanceof ArrNode
                        && ((ArrNode) beforeFirst.parent).el == ((ArrNode) last.parent).el) {

                    ent = new IncavaEntry(ent.getDeletedStart() - 1, ent.getDeletedEnd() - 1,
                            ent.getAddedStart(), ent.getAddedEnd());

                    diff.set(i, ent);

                }

            }

            if (ent.getAddedStart() > 0 && ent.getAddedEnd() > 0) {

                Leaf first = toLeaves.get(ent.getAddedStart());
                Leaf beforeFirst = toLeaves.get(ent.getAddedStart() - 1);
                Leaf last = toLeaves.get(ent.getAddedEnd());

                if (beforeFirst.parent instanceof ArrNode && first.parent instanceof ObjNode &&
                        last.parent instanceof ArrNode
                        && ((ArrNode) beforeFirst.parent).el == ((ArrNode) last.parent).el) {

                    ent = new IncavaEntry(ent.getDeletedStart(), ent.getDeletedEnd(),
                            ent.getAddedStart() - 1, ent.getAddedEnd() - 1);

                    diff.set(i, ent);

                }

            }

        }

    }


    // when deleting elements in an array, the indexes in subsequent cells differ between from/to
    // this causes problems when mutations happens after the deleted index. Issue #3.
    // this method adjusts the toLeaves to ensure the array indexes are the same as that of
    // the fromArray regardless of deletions.
    private static void adjustArrayDeletion(List<IncavaEntry> diff, ArrayList<Leaf> fromLeaves,
            HashMap<Integer, ArrNode> toArrs, HashSet<Integer> additions) {


        for (IncavaEntry ent : diff) {

            if (ent.getDeletedStart() >= 0 && ent.getDeletedEnd() >= 0) {

                for (int cur = ent.getDeletedStart(), n = ent.getDeletedEnd(); cur <= n; cur++) {

                    Leaf leaf = fromLeaves.get(cur);

                    if (leaf.parent instanceof ArrNode) {

                        ArrNode first = (ArrNode) leaf.parent;

                        // don't adjust delete + add = set entries
                        if (additions.contains(first.doHash(true))) {
                            continue;
                        }

                        // clone is used just to create a hash code for looking
                        // up the real node from toArrs.
                        ArrNode clone = new ArrNode(first.parent, first.el, 0);

                        int i = first.index;

                        while (true) {

                            clone.index = i++;

                            ArrNode adjust = toArrs.get(clone.doHash(true));

                            if (adjust == null) {
                                break;
                            }

                            // adjust forward one
                            adjust.index++;

                        }

                    }

                }

            }
        }

    }


    // when inserting elements in an array, the indexes in subsequent cells differ between from/to
    // this causes problems when mutations happens after the inserted index. Related to issue #3.
    // this method adjusts the fromLeaves to ensure the array indexes are adjusted for every insertion.
    private static void adjustArrayInsertion(List<IncavaEntry> diff, ArrayList<Leaf> toLeaves,
            HashMap<Integer, ArrNode> fromArrs, HashSet<Integer> deletions) {


        for (IncavaEntry ent : diff) {

            if (ent.getAddedStart() >= 0 && ent.getAddedEnd() >= 0) {

                for (int cur = ent.getAddedStart(), n = ent.getAddedEnd(); cur <= n; cur++) {

                    Leaf leaf = toLeaves.get(cur);

                    if (leaf.parent instanceof ArrNode) {

                        ArrNode first = (ArrNode) leaf.parent;

                        // don't adjust delete + add = set entries
                        if (deletions.contains(first.doHash(true))) {
                            continue;
                        }

                        // clone is used just to create a hash code for looking
                        // up the real node from toArrs.
                        ArrNode clone = new ArrNode(first.parent, first.el, 0);

                        int i = first.index;

                        while (true) {

                            clone.index = i++;

                            ArrNode adjust = fromArrs.get(clone.doHash(true));

                            if (adjust == null) {
                                break;
                            }

                            // adjust forward one
                            adjust.index++;

                        }

                    }

                }

            }
        }

    }


    private static Comparator<Entry<String, JzonElement>> ENTRY_COMPARATOR = new Comparator<Entry<String, JzonElement>>() {

        @Override
        public int compare(Entry<String, JzonElement> o1, Entry<String, JzonElement> o2) {

            return o1.getKey().compareTo(o2.getKey());

        }
    };


    private static class Leaf implements Comparable<Leaf> {

        final Node parent;
        final JzonElement val;
        Integer hash;
        ArrayList<Node> path;


        Leaf(Node parent, JzonElement val) {
            this.parent = parent;
            this.val = val;
        }


        public ArrayList<Node> toPath() {
            if (path != null) {
                return path;
            }

            ArrayList<Node> tmp = new ArrayList<Node>();
            Node cur = parent;

            tmp.add(cur);
            while (!(cur.parent instanceof Root)) {
                cur = cur.parent;
                tmp.add(cur);
            }
            Collections.reverse(tmp);

            for (int i = 0; i < tmp.size(); i++) {

                ObjNode o = (ObjNode) tmp.get(i);

                int j = i + 1;
                if (j < tmp.size() && tmp.get(j) instanceof ArrNode) {
                    o.subindex = (ArrNode) tmp.get(j);
                    ArrNode a = o.subindex;
                    tmp.remove(j);
                    while (j < tmp.size() && tmp.get(j) instanceof ArrNode) {
                        a.subindex = (ArrNode) tmp.get(j);
                        a = a.subindex;
                        tmp.remove(j);
                    }
                }

            }

            return tmp;

        }


        public boolean hasAncestor(Node anc) {
            return parent == anc || parent.hasAncestor(anc);
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
            return "LEAF<" + val + "#" + hashCode() + ">\n";
        }

    }


    private static abstract class Node {

        final Node parent;
        int hash = -1;


        Node(Node parent) {
            this.parent = parent;
        }


        public boolean hasAncestor(Node anc) {
            return parent == anc || parent.hasAncestor(anc);
        }


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

        ArrNode subindex;


        ObjNode(Node parent, String key) {
            super(parent);
            this.key = key;
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

        final JzonElement el;
        int index;

        ArrNode subindex;


        ArrNode(Node parent, JzonElement el, int index) {
            super(parent);
            this.el = el;
            this.index = index;
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
            super(null);
        }


        @Override
        protected int doHash(boolean indexed) {
            return 0;
        }


        @Override
        public boolean hasAncestor(Node anc) {
            return false;
        }


        @Override
        public String toString() {
            return "root";
        }

    }

}
