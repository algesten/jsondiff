package foodev.jsondiff;

import junit.framework.TestCase;

import org.junit.Test;

public abstract class JsonPatchTestMethods extends TestCase {

	abstract String apply(String s1, String s2); 

    @Test
    public void testPrimAdd() {

        String n = apply("{}", "{\"~\":[{a:1}]}");
        assertEquals("{\"a\":1}", n);

    }


    @Test
    public void testPrimMerge() {

        String n = apply("{}", "{\"~\":[{\"a\":1}]}");
        assertEquals("{\"a\":1}", n);

    }


    @Test
    public void testPrimRemove() {

        String n = apply("{a:1}", "{\"-a\":0}");
        assertEquals("{}", n);

    }


    @Test
    public void testPrimChange() {

        String n = apply("{a:1}", "{a:2}");
        assertEquals("{\"a\":2}", n);

    }


    @Test
    public void testPrimChangeMerge() {

        String n = apply("{a:1}", "{\"~\":[{\"a\":2}]}");
        assertEquals("{\"a\":2}", n);

    }


    @Test
    public void testNullAdd() {

        String n = apply("{}", "{a:null}");
        assertEquals("{\"a\":null}", n);

    }


    @Test
    public void testNullMerge() {

        String n = apply("{}", "{\"~\":[{\"a\":null}]}");
        assertEquals("{\"a\":null}", n);

    }


    @Test
    public void testNullRemove() {

        String n = apply("{a:null}", "{\"-a\":0}");
        assertEquals("{}", n);

    }


    @Test
    public void testObjAdd() {

        String n = apply("{a:1}", "{\"~\":[{a:{}}]}");
        assertEquals("{\"a\":{}}", n);

    }


    @Test
    public void testObjRemove() {

        String n = apply("{a:{}}", "{\"-a\":{}}");
        assertEquals("{}", n);

    }


    @Test
    public void testObjMerge() {

        String n = apply("{a:{b:1}}", "{\"~a\":[{c:2}]}");
        assertEquals("{\"a\":{\"b\":1,\"c\":2}}", n);

    }


    @Test
    public void testObjMergeToPrim() {

        String n = apply("{a:1}", "{\"~a\":[{b:1}]}");
        assertEquals("{\"a\":{\"b\":1}}", n);

    }


    @Test
    public void testObjMergetToNull() {

        String n = apply("{a:null}", "{\"~a\":[{b:1}]}");
        assertEquals("{\"a\":{\"b\":1}}", n);

    }


    @Test
    public void testObjMergetToArr() {

        String n = apply("{a:[1]}", "{\"~a\":[{\"b\":1}]}");
        assertEquals("{\"a\":{\"b\":1}}", n);

    }


    @Test
    public void testArrayAddBad() {

        try {
        	apply("{}", "{\"~a[bad]\": 2}");
        	fail();
        } catch(IllegalArgumentException e) {}
    }


    @Test
    public void testArrayAddFull() {

        String n = apply("{}", "{\"~a\":[{\"+0\":0},{\"+1\":1}]}");
        assertEquals("{\"a\":[0,1]}", n);

    }


    @Test
    public void testArrayMergeFull() {

        String n = apply("{}", "{\"~\":[{\"+a\":[0,1]}]}");
        assertEquals("{\"a\":[0,1]}", n);

    }


    @Test
    public void testArrayAddToEmpty() {

        String n = apply("{a:[]}", "{\"~a\": [{\"+0\":1}]}");
        assertEquals("{\"a\":[1]}", n);

    }


    @Test
    public void testArrayAddLast() {

        String n = apply("{a:[0]}", "{\"~a\":[{\"+1\":1}]}");
        assertEquals("{\"a\":[0,1]}", n);

    }


    @Test
    public void testArrayAddFirst() {

        String n = apply("{a:[0]}", "{\"~a\":[{\"+0\":1}]}");
        assertEquals("{\"a\":[1,0]}", n);

    }


    @Test
    public void testArrayInsertMiddle() {

        String n = apply("{a:[0,1]}", "{\"~a\":[{\"+1\":2}]}");
        assertEquals("{\"a\":[0,2,1]}", n);

    }


    @Test
    public void testArrRemoveToEmpty() {
        String n = apply("{a:[0]}", "{\"~a\":[{\"-0\":null}]}");
        assertEquals("{\"a\":[]}", n);
    }


    @Test
    public void testArrRemoveFirst() {
        String n = apply("{a:[0,1]}", "{\"~a\":[{\"-0\":null}]}");
        assertEquals("{\"a\":[1]}", n);
    }


    @Test
    public void testArrRemoveLast() {
        String n = apply("{a:[0,1]}", "{\"~a\":[{\"-1\":null}]}");
        assertEquals("{\"a\":[0]}", n);
    }


    @Test
    public void testArrRemoveMiddle() {
        String n = apply("{a:[0,1,2]}", "{\"~a\":[{\"-1\":null}]}");
        assertEquals("{\"a\":[0,2]}", n);
    }


    @Test
    public void testArrRemoveInsertMiddle() {
    	try {
    		apply("{a:[0,1,2]}", "{\"~a[+1]\":null}");
    		fail();
    	} catch(IllegalArgumentException e) {}
    }


    @Test
    public void testAddRemoveOrderMatters() {
        String n = apply("{a:[0,1,2]}", "{\"~a\":[{\"-0\":null},{\"-0\":null},{\"+1\":3}]}");
        assertEquals("{\"a\":[2,3]}", n);
    }


    @Test
    public void testAddRemoveOrderMatters2() {
        String n = apply("{a:[{b:0},{b:1},{b:2},{b:3}]}",
                "{\"~a\":[{\"+3\":{d:2}},{\"-0\":0},{\"-0\":0}],\"a\":{\"~2\":[{c:2}]}}");
        assertEquals("{\"a\":[{\"b\":2,\"c\":2},{\"d\":2},{\"b\":3}]}", n);
    }


    @Test
    public void testAddRemoveOrderMatters3() {
        String n = apply("{a:[{b:0},{b:1},{b:2},{b:3}]}",
                "{\"~a\":[{\"+2\":{d:2}},{\"-1\":0}],\"a\":{\"~2\":[{c:2}]}}");
        assertEquals("{\"a\":[{\"b\":0},{\"d\":2},{\"b\":2,\"c\":2},{\"b\":3}]}", n);
    }


    @Test
    public void testArrObjMerge() {
        String n = apply("{a:[0,{b:1},3]}", "{\"a\": {\"~1\":[{c:2}]}}");
        assertEquals("{\"a\":[0,{\"b\":1,\"c\":2},3]}", n);
    }


    @Test
    public void testArrObjMergeInsert() {
    	try {
            apply("{a:[0,{b:1},3]}", "{\"~a[+1]\":{c:2}}");
    		fail();
    	} catch(IllegalArgumentException e) {}

    }


    // test for Issue #7, this will hang if not fixed.
    // Thanks to DrLansing for finding the problem.
    @Test
    public void testCompareArraysIndirect() {

        // must not hang
        apply("{a:[[[0]]]}", "{\"a[0][0]\":2,\"a[0][0][0]\":2}");

    }


    @Test
    public void testBadDeleteAfterReplace() {

        String from = "{a:[1,2]}";
        String patch = "{a:3, \"~a\": [{\"-3\": 0}]}";

    	try {
            apply(from, patch);
    		fail();
    	} catch(IllegalArgumentException e) {}

    }
}
