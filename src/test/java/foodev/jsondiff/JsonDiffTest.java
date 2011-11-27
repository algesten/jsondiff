package foodev.jsondiff;

import org.junit.Assert;
import org.junit.Test;


public class JsonDiffTest {


    @Test
    public void testAdd() {

        String d = JsonDiff.diff("{}", "{a:1}");
        Assert.assertEquals("{\"a\":1}", d);

        String p = JsonPatch.apply("{}", d);
        Assert.assertEquals("{\"a\":1}", p);

    }


    @Test
    public void testRemove() {

        String d = JsonDiff.diff("{a:1}", "{}");
        Assert.assertEquals("{\"-a\":0}", d);

        String p = JsonPatch.apply("{a:1}", d);
        Assert.assertEquals("{}", p);

    }


    @Test
    public void testChange() {

        String d = JsonDiff.diff("{a:1}", "{a:2}");
        Assert.assertEquals("{\"a\":2}", d);

        String p = JsonPatch.apply("{a:1}", d);
        Assert.assertEquals("{\"a\":2}", p);

    }


    @Test
    public void testNestedAdd() {

        String d = JsonDiff.diff("{a:1,b:{}}", "{a:1,b:{c:1}}");
        Assert.assertEquals("{\"~b\":{\"c\":1}}", d);

        String p = JsonPatch.apply("{a:1,b:{}}", d);
        Assert.assertEquals("{\"a\":1,\"b\":{\"c\":1}}", p);

    }


    @Test
    public void testNestedAddTwo() {

        String d = JsonDiff.diff("{a:1,b:{}}", "{a:1,b:{c:1, d:2}}");
        Assert.assertEquals("{\"~b\":{\"c\":1,\"d\":2}}", d);

        String p = JsonPatch.apply("{a:1,b:{}}", d);
        Assert.assertEquals("{\"a\":1,\"b\":{\"c\":1,\"d\":2}}", p);

    }


    @Test
    public void testNestedRemove() {

        String d = JsonDiff.diff("{a:1,b:{c:1}}", "{a:1,b:{}}");
        Assert.assertEquals("{\"~b\":{\"-c\":0}}", d);

        String p = JsonPatch.apply("{a:1,b:{c:1}}", d);
        Assert.assertEquals("{\"a\":1,\"b\":{}}", p);

    }


    @Test
    public void testNestedChange() {

        String d = JsonDiff.diff("{a:1,b:{c:1}}", "{a:1,b:{c:2}}");
        Assert.assertEquals("{\"~b\":{\"c\":2}}", d);

        String p = JsonPatch.apply("{a:1,b:{c:1}}", d);
        Assert.assertEquals("{\"a\":1,\"b\":{\"c\":2}}", p);

    }


    @Test
    public void testNestedChangeAddBefore() {

        String d = JsonDiff.diff("{a:1,b:{d:1}}", "{a:1,b:{c:1, d:2}}");
        Assert.assertEquals("{\"~b\":{\"c\":1,\"d\":2}}", d);

        String p = JsonPatch.apply("{a:1,b:{d:1}}", d);
        Assert.assertEquals("{\"a\":1,\"b\":{\"d\":2,\"c\":1}}", p);

    }


    @Test
    public void testNestedChangeAddAfter() {

        String d = JsonDiff.diff("{a:1,b:{d:1}}", "{a:1,b:{d:2, e:3}}");
        Assert.assertEquals("{\"~b\":{\"d\":2,\"e\":3}}", d);

        String p = JsonPatch.apply("{a:1,b:{d:1}}", d);
        Assert.assertEquals("{\"a\":1,\"b\":{\"d\":2,\"e\":3}}", p);

    }


    @Test
    public void testNestedPartialRemove() {

        String d = JsonDiff.diff("{a:1,b:{c:1,d:1}}", "{a:1,b:{c:1}}");
        Assert.assertEquals("{\"~b\":{\"-d\":0}}", d);

        String p = JsonPatch.apply("{a:1,b:{c:1,d:1}}", d);
        Assert.assertEquals("{\"a\":1,\"b\":{\"c\":1}}", p);

    }


    @Test
    public void testNestedRemoveToEmpty() {

        String d = JsonDiff.diff("{a:1,b:{c:1,d:1}}", "{a:1,b:{}}");
        Assert.assertEquals("{\"~b\":{\"-c\":0,\"-d\":0}}", d);

        String p = JsonPatch.apply("{a:1,b:{c:1,d:1}}", d);
        Assert.assertEquals("{\"a\":1,\"b\":{}}", p);

    }


    @Test
    public void testNestedCompleteRemove() {

        String d = JsonDiff.diff("{a:1,b:{c:1,d:1}}", "{a:1}");
        Assert.assertEquals("{\"-b\":0}", d);

        String p = JsonPatch.apply("{a:1,b:{c:1,d:1}}", d);
        Assert.assertEquals("{\"a\":1}", p);

    }


    @Test
    public void testArrayAdd() {

        String d = JsonDiff.diff("{}", "{a:[1]}");
        Assert.assertEquals("{\"a\":[1]}", d);

        String p = JsonPatch.apply("{}", d);
        Assert.assertEquals("{\"a\":[1]}", p);

    }


    @Test
    public void testArrayAddTwo() {

        String d = JsonDiff.diff("{}", "{a:[1,2]}");
        Assert.assertEquals("{\"a\":[1,2]}", d);

        String p = JsonPatch.apply("{}", d);
        Assert.assertEquals("{\"a\":[1,2]}", p);

    }


    @Test
    public void testArrayAddToEmpty() {

        String d = JsonDiff.diff("{a: []}", "{a:[1]}");
        Assert.assertEquals("{\"a[+0]\":1}", d);

        String p = JsonPatch.apply("{a: []}", d);
        Assert.assertEquals("{\"a\":[1]}", p);

    }


    @Test
    public void testArrayAddTwoToEmpty() {

        String d = JsonDiff.diff("{a: []}", "{a:[1,2]}");
        Assert.assertEquals("{\"a[+0]\":1,\"a[+1]\":2}", d);

        String p = JsonPatch.apply("{a: []}", d);
        Assert.assertEquals("{\"a\":[1,2]}", p);

    }


    @Test
    public void testArrayAddToExisting() {

        String d = JsonDiff.diff("{a: [0]}", "{a:[0,1]}");
        Assert.assertEquals("{\"a[+1]\":1}", d);

        String p = JsonPatch.apply("{a: [0]}", d);
        Assert.assertEquals("{\"a\":[0,1]}", p);

    }


    @Test
    public void testArrayAddTwoToExisting() {

        String d = JsonDiff.diff("{a: [3]}", "{a:[1,2,3]}");
        Assert.assertEquals("{\"a[+0]\":1,\"a[+1]\":2}", d);

        String p = JsonPatch.apply("{a: [3]}", d);
        Assert.assertEquals("{\"a\":[1,2,3]}", p);

    }


    @Test
    public void testArrayAddTwoOtherToExisting() {

        String d = JsonDiff.diff("{a: [3,4,1,2]}", "{a:[1,2,5]}");
        Assert.assertEquals("{\"-a[0]\":0,\"-a[1]\":0,\"a[+2]\":5}", d);

        String p = JsonPatch.apply("{a: [3,4,1,2]}", d);
        Assert.assertEquals("{\"a\":[1,2,5]}", p);

    }


    @Test
    public void testArrayInsertInExtisting() {

        String d = JsonDiff.diff("{a: [0,2,3,4]}", "{a:[0,1,2,3,4]}");
        Assert.assertEquals("{\"a[+1]\":1}", d);

        String p = JsonPatch.apply("{a: [0,2,3,4]}", d);
        Assert.assertEquals("{\"a\":[0,1,2,3,4]}", p);

    }


    @Test
    public void testArrayInsertAfterDeleted() {

        String d = JsonDiff.diff("{a: [0,1,2,4]}", "{a:[0,1,3,4]}");
        Assert.assertEquals("{\"a[2]\":3}", d);

        String p = JsonPatch.apply("{a: [0,1,2,4]}", d);
        Assert.assertEquals("{\"a\":[0,1,3,4]}", p);

    }


    @Test
    public void testArrayInsertTwoAfterDeleted() {

        String d = JsonDiff.diff("{a: ['a','b','d']}", "{a:['a','c','e','d']}");
        Assert.assertEquals("{\"a[1]\":\"c\",\"a[+2]\":\"e\"}", d);

        String p = JsonPatch.apply("{a: ['a','b','d']}", d);
        Assert.assertEquals("{\"a\":[\"a\",\"c\",\"e\",\"d\"]}", p);

    }


    @Test
    public void testArrayRemoveAll() {

        String d = JsonDiff.diff("{a: [1,2]}", "{}");
        Assert.assertEquals("{\"-a\":0}", d);

        String p = JsonPatch.apply("{a: [1,2]}", d);
        Assert.assertEquals("{}", p);

    }


    @Test
    public void testArrayRemoveToEmpty() {

        String d = JsonDiff.diff("{a: [1]}", "{a:[]}");
        Assert.assertEquals("{\"-a[0]\":0}", d);

        String p = JsonPatch.apply("{a: [1]}", d);
        Assert.assertEquals("{\"a\":[]}", p);

    }


    @Test
    public void testArrayRemoveLast() {

        String d = JsonDiff.diff("{a: [1,2]}", "{a:[1]}");
        Assert.assertEquals("{\"-a[1]\":0}", d);

        String p = JsonPatch.apply("{a: [1,2]}", d);
        Assert.assertEquals("{\"a\":[1]}", p);

    }


    @Test
    public void testArrayRemoveFirst() {

        String d = JsonDiff.diff("{a: [1,2]}", "{a:[2]}");
        Assert.assertEquals("{\"-a[0]\":0}", d);

        String p = JsonPatch.apply("{a: [1,2]}", d);
        Assert.assertEquals("{\"a\":[2]}", p);

    }


    @Test
    public void testArrayRemoveMiddle() {

        String d = JsonDiff.diff("{a: [1,2,3]}", "{a:[1,3]}");
        Assert.assertEquals("{\"-a[1]\":0}", d);

        String p = JsonPatch.apply("{a: [1,2,3]}", d);
        Assert.assertEquals("{\"a\":[1,3]}", p);

    }


    @Test
    public void testArrayRemoveMultiple() {

        String d = JsonDiff.diff("{a: [1,2,3,4]}", "{a:[1,3]}");
        Assert.assertEquals("{\"-a[1]\":0,\"-a[3]\":0}", d);

        String p = JsonPatch.apply("{a: [1,2,3,4]}", d);
        Assert.assertEquals("{\"a\":[1,3]}", p);

    }


    @Test
    public void testArrayAddMultiDimensional() {

        String d = JsonDiff.diff("{a:[1]}", "{a:[1,[2,3]]}");
        Assert.assertEquals("{\"a[+1]\":[2,3]}", d);

        String p = JsonPatch.apply("{a: [1]}", d);
        Assert.assertEquals("{\"a\":[1,[2,3]]}", p);

    }


    @Test
    public void testArrayRemoveInMulti() {

        String d = JsonDiff.diff("{a:[1,[2,3]]}", "{a:[1,[3]]}");
        Assert.assertEquals("{\"-a[1][0]\":0}", d);

        String p = JsonPatch.apply("{a:[1,[2,3]]}", d);
        Assert.assertEquals("{\"a\":[1,[3]]}", p);

    }


    @Test
    public void testArrayRemoveLastInMulti() {

        String d = JsonDiff.diff("{a:[1,[2,3]]}", "{a:[1,[2]]}");
        Assert.assertEquals("{\"-a[1][1]\":0}", d);

        String p = JsonPatch.apply("{a:[1,[2,3]]}", d);
        Assert.assertEquals("{\"a\":[1,[2]]}", p);

    }


    @Test
    public void testArrayInsertInMulti() {

        String d = JsonDiff.diff("{a:[1,[2,4]]}", "{a:[1,[2,3,4]]}");
        Assert.assertEquals("{\"a[1][+1]\":3}", d);

        String p = JsonPatch.apply("{a:[1,[2,4]]}", d);
        Assert.assertEquals("{\"a\":[1,[2,3,4]]}", p);

    }


    @Test
    public void testAddObjectToArray() {

        String d = JsonDiff.diff("{a:[1]}", "{a:[1,{b:2}]}");
        Assert.assertEquals("{\"a[+1]\":{\"b\":2}}", d);

        String p = JsonPatch.apply("{a:[1]}", d);
        Assert.assertEquals("{\"a\":[1,{\"b\":2}]}", p);

    }


    @Test
    public void testMergeObjectInArray() {

        String d = JsonDiff.diff("{a:[1,{}]}", "{a:[1,{b:2}]}");
        Assert.assertEquals("{\"~a[1]\":{\"b\":2}}", d);

        String p = JsonPatch.apply("{a:[1,{}]}", d);
        Assert.assertEquals("{\"a\":[1,{\"b\":2}]}", p);

    }


    @Test
    public void testInsertInArrayInObjectInArray() {

        String d = JsonDiff.diff("{a:[1,{b:[]}]}", "{a:[1,{b:[2]}]}");

        String p = JsonPatch.apply("{a:[1,{b:[]}]}", d);
        Assert.assertEquals("{\"a\":[1,{\"b\":[2]}]}", p);

    }


    @Test
    public void testRemoveFromArrayInObjectInArray() {

        String d = JsonDiff.diff("{a:[1,{b:[2,3]}]}", "{a:[1,{b:[]}]}");
        Assert.assertEquals("{\"~a[1]\":{\"-b[0]\":0,\"-b[1]\":0}}", d);

        String p = JsonPatch.apply("{a:[1,{b:[2,3]}]}", d);
        Assert.assertEquals("{\"a\":[1,{\"b\":[]}]}", p);

    }


    @Test
    public void testRemoveChange() {

        String d = JsonDiff.diff("{a: 1, b: 2, c: 3}", "{b:4}");
        Assert.assertEquals("{\"-a\":0,\"-c\":0,\"b\":4}", d);

        String p = JsonPatch.apply("{a: 1, b: 2, c: 3}", d);
        Assert.assertEquals("{\"b\":4}", p);

    }


    @Test
    public void testArrToNumeric() {

        String d = JsonDiff.diff("{a: [1,2]}", "{a: 1}");
        Assert.assertEquals("{\"a\":1}", d);

        String p = JsonPatch.apply("{a: [1,2]}", d);
        Assert.assertEquals("{\"a\":1}", p);

    }


    @Test
    public void testMix() {

        String d = JsonDiff.diff("{a: [1,2], b: { foo: 'b'}, c: 42}", "{a: 1, b: {foo: 'b', bar: 42}, c: 45}");
        Assert.assertEquals("{\"a\":1,\"~b\":{\"bar\":42},\"c\":45}", d);

        String p = JsonPatch.apply("{a: [1,2], b: { foo: 'b'}, c: 42}", d);
        Assert.assertEquals("{\"a\":1,\"b\":{\"foo\":\"b\",\"bar\":42},\"c\":45}", p);

    }


    @Test
    public void testArrayObjectsChange() {
        String from = "{a:[{b:1}]}";
        String to = "{\"a\":[{\"c\":1}]}";
        String diff = "{\"~a[0]\":{\"-b\":0,\"c\":1}}";

        String d = JsonDiff.diff(from, to);
        Assert.assertEquals(diff, d);

        String p = JsonPatch.apply(from, diff);
        Assert.assertEquals(to, p);

    }


    @Test
    public void testArrayStringsRotateLeft() {
        String from = "{\"a\":[\"s1\",\"s2\",\"s3\"]}";
        String to = "{\"a\":[\"s2\",\"s3\",\"s4\"]}";

        String diff = JsonDiff.diff(from, to);
        Assert.assertEquals("{\"-a[0]\":0,\"a[+2]\":\"s4\"}", diff);

        String p = JsonPatch.apply(from, diff);
        Assert.assertEquals(to, p);

    }


    // Bug #1 thanks to deverton
    @Test
    public void testArrayObjectsRotateLeft() {
        String from = "{\"a\":[{\"b\":1},{\"c\":2},{\"d\":3}]}";
        String to = "{\"a\":[{\"c\":2},{\"d\":3},{\"e\":4}]}";
        String diff = "{\"-a[0]\":0,\"a[+2]\":{\"e\":4}}";

        String d = JsonDiff.diff(from, to);
        Assert.assertEquals(diff, d);

        String p = JsonPatch.apply(from, diff);
        Assert.assertEquals(to, p);

    }


    // Bug #1, thanks to deverton
    @Test
    public void testArrayObjectsRotateRight() {

        String from = "{\"a\":[{\"c\":2},{\"d\":3},{\"e\":4}]}";
        String to = "{\"a\":[{\"b\":1},{\"c\":2},{\"d\":3}]}";
        String diff = "{\"a[+0]\":{\"b\":1},\"-a[2]\":0}";

        String d = JsonDiff.diff(from, to);
        Assert.assertEquals(diff, d);

        String p = JsonPatch.apply(from, diff);
        Assert.assertEquals(to, p);
    }


    @Test
    public void testObjectObjectsRotateLeft() {

        String from = "{\"a1\":{\"c\":2},\"a2\":{\"d\":3},\"a3\":{\"e\":4}}";
        String to = "{\"a1\":{\"b\":1},\"a2\":{\"c\":2},\"a3\":{\"d\":3}}";

        String d = JsonDiff.diff(from, to);
        Assert.assertEquals("{\"~a1\":{\"-c\":0,\"b\":1},\"~a2\":{\"-d\":0,\"c\":2},\"~a3\":{\"-e\":0,\"d\":3}}", d);

        String p = JsonPatch.apply(from, d);
        Assert.assertEquals(to, p);

    }

}
