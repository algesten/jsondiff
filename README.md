Structural JSON diff/patch tool
===============================

The patches are json themselves where the object memeber keys follows
a certain syntax.

Example:

    var orig = {
      a: 1,
      b: ["an", "array"],
      c: { "an": "object" }
    };
 
    var patch = {
      a: 42,                      // replace a
      "b[+1]": "example",         // insert into array b
      "~c": { "for": "example" }  // merge into object c (It's a TILDE)
    };

    var changed = patch(orig, patch);

would yield a changed object as:

    {
      a: 42,
      b: ["an", "example", "array"],
      c: {"an": "object", "for": "example" }
    }

### Syntax ###

* `~` (tilde) is an object merge. I.e. `{a:1}` merged with `{b:2}` would yield `{a:1,b:2}`
* `-` (hyphen-minus) is a deletion.
* `[n]` is an array index i.e. `a[4]` means element 4 in array named `a`.
* `[+n]` means an array insertion. i.e `a[+4]` means insert an item at position 4.

### Further definitions ###

    "key":     "replaced",           // added or replacing key
    "~key":    "replaced",           // added or replacing key (~ doesn't matter for primitive data types)
    "key":     null,                 // added or replacing key with null.
    "~key":    null,                 // added or replacing key with null (~ doesn't matter for null)
    "-key":    0                     // key removed (value is ignored)
    "key":     { "sub": "replaced" } // whole object "key" replaced
    "~key":    { "sub": "merged" }   // key "sub" merged into object "key", rest of object untouched
    "key":     [ "replaced" ]        // whole array added/replaced
    "~key":    [ "replaced" ]        // whole array added/replaced (~ doesn't matter for whole array)
    "key[4]":  { "sub": "replaced" } // object replacing element 4, rest of array untouched
    "~key[4]": { "sub": "merged"}    // merging object at element 4, rest of array untouched
    "key[+4]": { "sub": "array add"} // object added after 3 becoming the new 4 (current 4 pushed right)
    "~key[+4]":{ "sub": "array add"} // object added after 3 becoming the new 4 (current 4 pushed right)
    "-key[4]:  0                     // removing element 4 current 5 becoming new 4 (value is ignored)

### Instruction order ###

For arrays the instruction order matter. Array insert and deletions will affect the index offset for each other and 
subsequent instructions. Regardless of instruction order in the json object passed as diff, the entries are sorted as:

1. Merge (`~a[n]`)
2. Set (`a[n]`)
3. Insert (`a[+n]`)
4. Delete (`-`)

Example:

    var orig = {
      a = [0, 1, 2, { me: "too" }];    
    };

    var patch = {
      "a[2]": 42,
      "~a[3]": { foo: "bar" },
      "a[+1]": "example",
      "-a[2]: null,
    };

This would yield:

1. Merge at 3: `[0, 1, 2, { me: "too", foo: "bar" }]`
2. Set at 2: `[0, 1, 42, { me: "too", foo: "bar" }]`
3. Insert at 1: `[0, "example", 1, 42, { me: "too", foo: "bar" }]`
4. Delete at 2: `[0, "example", 42, { me: "too", foo: "bar" }]`

### GSON vs Jackson ###

The package works default using GSON. However Jackson is also supported. The API has two main methods (both `JsonDiff` 
and `JsonPatch` follows this pattern):

    public static String diff(String from, String to)
    public static Object diff(Object from, Object to)

The first method works off strings and returns a string. Internally GSON is used, but that is of no concern to the user.
The second method can either work off two GSON `JsonObject` and return the corresponding, or alternatively two Jackson
`ObjectNode` and return that kind of object. The latter style would require the user to include the Jackson jar on
the classpath.
