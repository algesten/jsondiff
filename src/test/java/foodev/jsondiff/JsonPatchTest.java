package foodev.jsondiff;

import org.junit.Assert;
import org.junit.Test;


public class JsonPatchTest {

    @Test
    public void testPrimAdd() {

        String n = JsonPatch.apply("{}", "{a:1}");
        Assert.assertEquals("{\"a\":1}", n);

    }


    @Test
    public void testPrimMerge() {

        String n = JsonPatch.apply("{}", "{~a:1}");
        Assert.assertEquals("{\"a\":1}", n);

    }


    @Test
    public void testPrimRemove() {

        String n = JsonPatch.apply("{a:1}", "{-a:0}");
        Assert.assertEquals("{}", n);

    }


    @Test
    public void testPrimChange() {

        String n = JsonPatch.apply("{a:1}", "{a:2}");
        Assert.assertEquals("{\"a\":2}", n);

    }


    @Test
    public void testPrimChangeMerge() {

        String n = JsonPatch.apply("{a:1}", "{~a:2}");
        Assert.assertEquals("{\"a\":2}", n);

    }


    @Test
    public void testNullAdd() {

        String n = JsonPatch.apply("{}", "{a:null}");
        Assert.assertEquals("{\"a\":null}", n);

    }


    @Test
    public void testNullMerge() {

        String n = JsonPatch.apply("{}", "{~a:null}");
        Assert.assertEquals("{\"a\":null}", n);

    }


    @Test
    public void testNullRemove() {

        String n = JsonPatch.apply("{a:null}", "{-a:0}");
        Assert.assertEquals("{}", n);

    }


    @Test
    public void testObjAdd() {

        String n = JsonPatch.apply("{a:1}", "{a:{}}");
        Assert.assertEquals("{\"a\":{}}", n);

    }


    @Test
    public void testObjRemove() {

        String n = JsonPatch.apply("{a:{}}", "{-a:{}}");
        Assert.assertEquals("{}", n);

    }


    @Test
    public void testObjMerge() {

        String n = JsonPatch.apply("{a:{b:1}}", "{~a:{c:2}}");
        Assert.assertEquals("{\"a\":{\"b\":1,\"c\":2}}", n);

    }


    @Test
    public void testObjMergeToPrim() {

        String n = JsonPatch.apply("{a:1}", "{~a:{b:1}}");
        Assert.assertEquals("{\"a\":{\"b\":1}}", n);

    }


    @Test
    public void testObjMergetToNull() {

        String n = JsonPatch.apply("{a:null}", "{~a:{b:1}}");
        Assert.assertEquals("{\"a\":{\"b\":1}}", n);

    }


    @Test
    public void testObjMergetToArr() {

        String n = JsonPatch.apply("{a:[1]}", "{~a:{b:1}}");
        Assert.assertEquals("{\"a\":{\"b\":1}}", n);

    }


    @Test
    public void testArrayAddBad() {

        try {
            JsonPatch.apply("{}", "{\"a[bad]\": 2}");
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }

    }


    @Test
    public void testArrayAddFull() {

        String n = JsonPatch.apply("{}", "{a:[0,1]}");
        Assert.assertEquals("{\"a\":[0,1]}", n);

    }


    @Test
    public void testArrayMergeFull() {

        String n = JsonPatch.apply("{}", "{~a:[0,1]}");
        Assert.assertEquals("{\"a\":[0,1]}", n);

    }


    @Test
    public void testArrayAddToEmpty() {

        String n = JsonPatch.apply("{a:[]}", "{\"a[+0]\":1}");
        Assert.assertEquals("{\"a\":[1]}", n);

    }


    @Test
    public void testArrayAddLast() {

        String n = JsonPatch.apply("{a:[0]}", "{\"a[+1]\":1}");
        Assert.assertEquals("{\"a\":[0,1]}", n);

    }


    @Test
    public void testArrayAddFirst() {

        String n = JsonPatch.apply("{a:[0]}", "{\"a[+0]\":1}");
        Assert.assertEquals("{\"a\":[1,0]}", n);

    }


    @Test
    public void testArrayInsertMiddle() {

        String n = JsonPatch.apply("{a:[0,1]}", "{\"a[+1]\":2}");
        Assert.assertEquals("{\"a\":[0,2,1]}", n);

    }


    @Test
    public void testArrRemoveToEmpty() {
        String n = JsonPatch.apply("{a:[0]}", "{\"-a[0]\":null}");
        Assert.assertEquals("{\"a\":[]}", n);
    }


    @Test
    public void testArrRemoveFirst() {
        String n = JsonPatch.apply("{a:[0,1]}", "{\"-a[0]\":null}");
        Assert.assertEquals("{\"a\":[1]}", n);
    }


    @Test
    public void testArrRemoveLast() {
        String n = JsonPatch.apply("{a:[0,1]}", "{\"-a[1]\":null}");
        Assert.assertEquals("{\"a\":[0]}", n);
    }


    @Test
    public void testArrRemoveMiddle() {
        String n = JsonPatch.apply("{a:[0,1,2]}", "{\"-a[1]\":null}");
        Assert.assertEquals("{\"a\":[0,2]}", n);
    }


    @Test
    public void testArrRemoveInsertMiddle() {
        try {
            JsonPatch.apply("{a:[0,1,2]}", "{\"-a[+1]\":null}");
            Assert.fail();
        } catch (IllegalArgumentException ie) {
        }
    }


    @Test
    public void testAddRemoveOrderMatters() {
        String n = JsonPatch.apply("{a:[0,1,2]}", "{\"-a[0]\":null,\"-a[1]\":null,\"a[+1]\":3}");
        Assert.assertEquals("{\"a\":[2,3]}", n);
    }


    @Test
    public void testAddRemoveOrderMatters2() {
        String n = JsonPatch.apply("{a:[{b:0},{b:1},{b:2},{b:3}]}",
                "{\"a[+1]\":{d:2},\"-a[0]\":0,\"-a[1]\":0,\"~a[0]\":{c:2}}");
        Assert.assertEquals("{\"a\":[{\"b\":2,\"c\":2},{\"d\":2},{\"b\":3}]}", n);
    }


    @Test
    public void testAddRemoveOrderMatters3() {
        String n = JsonPatch.apply("{a:[{b:0},{b:1},{b:2},{b:3}]}",
                "{\"a[+1]\":{d:2},\"-a[1]\":0,\"~a[2]\":{c:2}}");
        Assert.assertEquals("{\"a\":[{\"b\":0},{\"d\":2},{\"b\":2,\"c\":2},{\"b\":3}]}", n);
    }


    @Test
    public void testArrObjMerge() {
        String n = JsonPatch.apply("{a:[0,{b:1},3]}", "{\"~a[1]\":{c:2}}");
        Assert.assertEquals("{\"a\":[0,{\"b\":1,\"c\":2},3]}", n);
    }


    @Test
    public void testArrObjMergeInsert() {
        try {
            JsonPatch.apply("{a:[0,{b:1},3]}", "{\"~a[+1]\":{c:2}}");
            Assert.fail();
        } catch (IllegalArgumentException ie) {
        }
    }

}
