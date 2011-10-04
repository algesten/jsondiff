package jsondiff;

import org.junit.Assert;
import org.junit.Test;


public class JsonDiffTest {

    @Test
    public void testAdd() {

        String d = JsonDiff.diff("{}", "{a:1}");
        Assert.assertEquals("{\"a\":1}", d);

    }


    @Test
    public void testRemove() {

        String d = JsonDiff.diff("{a:1}", "{}");
        Assert.assertEquals("{\"-a\":0}", d);

    }


    @Test
    public void testChange() {

        String d = JsonDiff.diff("{a:1}", "{a:2}");
        Assert.assertEquals("{\"a\":2}", d);

    }


    @Test
    public void testNestedAdd() {

        String d = JsonDiff.diff("{a:1,b:{}}", "{a:1,b:{c:1}}");
        Assert.assertEquals("{\"+b\":{\"c\":1}}", d);

    }


    @Test
    public void testNestedAddTwo() {

        String d = JsonDiff.diff("{a:1,b:{}}", "{a:1,b:{c:1, d:2}}");
        Assert.assertEquals("{\"+b\":{\"c\":1,\"d\":2}}", d);

    }


    @Test
    public void testNestedRemove() {

        String d = JsonDiff.diff("{a:1,b:{c:1}}", "{a:1,b:{}}");
        Assert.assertEquals("{\"+b\":{\"-c\":0}}", d);

    }


    @Test
    public void testNestedChange() {

        String d = JsonDiff.diff("{a:1,b:{c:1}}", "{a:1,b:{c:2}}");
        Assert.assertEquals("{\"+b\":{\"c\":2}}", d);

    }


    @Test
    public void testNestedChangeAddBefore() {

        String d = JsonDiff.diff("{a:1,b:{d:1}}", "{a:1,b:{c:1, d:2}}");
        Assert.assertEquals("{\"+b\":{\"c\":1,\"d\":2}}", d);

    }


    @Test
    public void testNestedChangeAddAfter() {

        String d = JsonDiff.diff("{a:1,b:{d:1}}", "{a:1,b:{d:2, e:3}}");
        Assert.assertEquals("{\"+b\":{\"d\":2,\"e\":3}}", d);

    }


    @Test
    public void testNestedPartialRemove() {

        String d = JsonDiff.diff("{a:1,b:{c:1,d:1}}", "{a:1,b:{c:1}}");
        Assert.assertEquals("{\"+b\":{\"-d\":0}}", d);

    }


    @Test
    public void testNestedRemoveToEmpty() {

        String d = JsonDiff.diff("{a:1,b:{c:1,d:1}}", "{a:1,b:{}}");
        Assert.assertEquals("{\"+b\":{\"-c\":0,\"-d\":0}}", d);

    }


    @Test
    public void testNestedCompleteRemove() {

        String d = JsonDiff.diff("{a:1,b:{c:1,d:1}}", "{a:1}");
        Assert.assertEquals("{\"-b\":0}", d);

    }


    @Test
    public void testArrayAdd() {

        String d = JsonDiff.diff("{}", "{a:[1]}");
        Assert.assertEquals("{\"a\":[1]}", d);

    }


    @Test
    public void testArrayAddTwo() {

        String d = JsonDiff.diff("{}", "{a:[1,2]}");
        Assert.assertEquals("{\"a\":[1,2]}", d);

    }


    @Test
    public void testArrayAddToEmpty() {

        String d = JsonDiff.diff("{a: []}", "{a:[1]}");
        Assert.assertEquals("{\"a+[0]\":1}", d);

    }


    @Test
    public void testArrayAddTwoToEmpty() {

        String d = JsonDiff.diff("{a: []}", "{a:[1,2]}");
        Assert.assertEquals("{\"a+[0]\":1,\"a+[1]\":2}", d);

    }


    @Test
    public void testArrayAddToExisting() {

        String d = JsonDiff.diff("{a: [0]}", "{a:[0,1]}");
        Assert.assertEquals("{\"a+[1]\":1}", d);

    }


    @Test
    public void testArrayAddTwoToExisting() {

        String d = JsonDiff.diff("{a: [3]}", "{a:[1,2,3]}");
        Assert.assertEquals("{\"a+[0]\":1,\"a+[1]\":2}", d);

    }


    @Test
    public void testArrayAddTwoOtherToExisting() {

        String d = JsonDiff.diff("{a: [3,4,1,2]}", "{a:[1,2,5]}");
        Assert.assertEquals("{\"-a[0]\":0,\"-a[1]\":0,\"a+[2]\":5}", d);

    }


    @Test
    public void testArrayInsertInExtisting() {

        String d = JsonDiff.diff("{a: [0,2,3,4]}", "{a:[0,1,2,3,4]}");
        Assert.assertEquals("{\"a+[1]\":1}", d);

    }


    @Test
    public void testArrayInsertAfterDeleted() {

        String d = JsonDiff.diff("{a: [0,1,2,4]}", "{a:[0,1,3,4]}");
        Assert.assertEquals("{\"a[2]\":3}", d);

    }


    @Test
    public void testArrayInsertTwoAfterDeleted() {

        String d = JsonDiff.diff("{a: ['a','b','d']}", "{a:['a','c','e','d']}");
        Assert.assertEquals("{\"a[1]\":\"c\",\"a+[2]\":\"e\"}", d);

    }


    @Test
    public void testArrayRemoveAll() {

        String d = JsonDiff.diff("{a: [1,2]}", "{}");
        Assert.assertEquals("{\"-a\":0}", d);

    }


    @Test
    public void testArrayRemoveToEmpty() {

        String d = JsonDiff.diff("{a: [1]}", "{a:[]}");
        Assert.assertEquals("{\"-a[0]\":0}", d);

    }


    @Test
    public void testArrayRemoveLast() {

        String d = JsonDiff.diff("{a: [1,2]}", "{a:[1]}");
        Assert.assertEquals("{\"-a[1]\":0}", d);

    }


    @Test
    public void testArrayRemoveFirst() {

        String d = JsonDiff.diff("{a: [1,2]}", "{a:[2]}");
        Assert.assertEquals("{\"-a[0]\":0}", d);

    }


    @Test
    public void testArrayRemoveMiddle() {

        String d = JsonDiff.diff("{a: [1,2,3]}", "{a:[1,3]}");
        Assert.assertEquals("{\"-a[1]\":0}", d);

    }


    @Test
    public void testArrayRemoveMultiple() {

        String d = JsonDiff.diff("{a: [1,2,3,4]}", "{a:[1,3]}");
        Assert.assertEquals("{\"-a[1]\":0,\"-a[3]\":0}", d);

    }


    @Test
    public void testArrayAddMultiDimensional() {

        String d = JsonDiff.diff("{a:[1]}", "{a:[1,[2,3]]}");
        Assert.assertEquals("{\"a+[1]\":[2,3]}", d);

    }


    @Test
    public void testArrayRemoveInMulti() {

        String d = JsonDiff.diff("{a:[1,[2,3]]}", "{a:[1,[3]]}");
        Assert.assertEquals("{\"-a[1][0]\":0}", d);

    }


    @Test
    public void testArrayRemoveLastInMulti() {

        String d = JsonDiff.diff("{a:[1,[2,3]]}", "{a:[1,[2]]}");
        Assert.assertEquals("{\"-a[1][1]\":0}", d);

    }


    @Test
    public void testArrayInsertInMulti() {

        String d = JsonDiff.diff("{a:[1,[2,4]]}", "{a:[1,[2,3,4]]}");
        Assert.assertEquals("{\"a[1]+[1]\":3}", d);

    }


    @Test
    public void testAddObjectToArray() {

        String d = JsonDiff.diff("{a:[1]}", "{a:[1,{b:2}]}");
        Assert.assertEquals("{\"a+[1]\":{\"b\":2}}", d);

    }


    @Test
    public void testMergeObjectInArray() {

        String d = JsonDiff.diff("{a:[1,{}]}", "{a:[1,{b:2}]}");
        Assert.assertEquals("{\"+a[1]\":{\"b\":2}}", d);

    }


    @Test
    public void testInsertInArrayInObjectInArray() {

        String d = JsonDiff.diff("{a:[1,{b:[]}]}", "{a:[1,{b:[2]}]}");
        Assert.assertEquals("{\"+a[1]\":{\"b+[0]\":2}}", d);

    }


    @Test
    public void testRemoveFromArrayInObjectInArray() {

        String d = JsonDiff.diff("{a:[1,{b:[2,3]}]}", "{a:[1,{b:[]}]}");
        Assert.assertEquals("{\"+a[1]\":{\"-b[0]\":0,\"-b[1]\":0}}", d);

    }


    @Test
    public void testRemoveChange() {

        String d = JsonDiff.diff("{a: 1, b: 2, c: 3}", "{b:4}");
        Assert.assertEquals("{\"-a\":0,\"-c\":0,\"b\":4}", d);

    }


    @Test
    public void testMix() {

        String d = JsonDiff.diff("{a: [1,2], b: { foo: 'b'}, c: 42}", "{a: 1, b: {foo: 'b', bar: 42}, c: 45}");
        Assert.assertEquals("{\"a\":1,\"+b\":{\"bar\":42},\"c\":45}", d);

    }

}
