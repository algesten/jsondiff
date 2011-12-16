package foodev.jsondiff;

import java.util.Arrays;
import java.util.Collection;

import org.codehaus.jackson.node.NullNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.gson.JsonObject;

@RunWith(value = Parameterized.class)
public class JsonDiffTest {


        public JsonDiffTest(Object hint) {
    
            JsonDiff.setHint(hint);
    
        }
    
    
        @Parameters
        public static Collection<Object[]> hints() {
    
            Object[][] data = new Object[][] { { new JsonObject() }, { NullNode.getInstance() } };
            return Arrays.asList(data);
    
        }


    @Before
    public void noSetup() {

        JsonPatch.setHint(null);

    }


    @After
    public void noTearDown() {

        JsonPatch.setHint(null);

    }


    @Test
    public void testAdd() {

        String d = JsonDiff.diff("{}", "{a:1}");
        Assert.assertEquals("{\"a\":1}", d);

        String p = JsonPatch.apply("{}", d);
        Assert.assertEquals("{\"a\":1}", p);

    }


    @Test
    public void testAddObject() {

        String d = JsonDiff.diff("{}", "{a:{b:1}}");
        Assert.assertEquals("{\"a\":{\"b\":1}}", d);

        String p = JsonPatch.apply("{}", d);
        Assert.assertEquals("{\"a\":{\"b\":1}}", p);

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
        Assert.assertEquals("{\"a[+4]\":5,\"-a[0]\":0,\"-a[1]\":0}", d);

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
        Assert.assertEquals("{\"b\":4,\"-a\":0,\"-c\":0}", d);

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
        String diff = "{\"~a[0]\":{\"c\":1,\"-b\":0}}";

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
        Assert.assertEquals("{\"a[+3]\":\"s4\",\"-a[0]\":0}", diff);

        String p = JsonPatch.apply(from, diff);
        Assert.assertEquals(to, p);

    }


    // Bug #1 thanks to deverton
    @Test
    public void testArrayObjectsRotateLeft() {
        String from = "{\"a\":[{\"b\":1},{\"c\":2},{\"d\":3}]}";
        String to = "{\"a\":[{\"c\":2},{\"d\":3},{\"e\":4}]}";
        String diff = "{\"a[+3]\":{\"e\":4},\"-a[0]\":0}";

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
        String diff = "{\"a[+0]\":{\"b\":1},\"-a[3]\":0}";

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
        Assert.assertEquals("{\"~a1\":{\"b\":1,\"-c\":0},\"~a2\":{\"c\":2,\"-d\":0},\"~a3\":{\"d\":3,\"-e\":0}}", d);

        String p = JsonPatch.apply(from, d);
        Assert.assertEquals(to, p);

    }


    // Bug #2, thanks to deverton
    @Test
    public void testArrayObjectsChangeField() {
        String from = "{\"a\":[{\"c\":2,\"d\":3},{\"c\":2,\"d\":3},{\"c\":2,\"d\":3},{\"c\":2,\"d\":3}]}";
        String to = "{\"a\":[{\"c\":2,\"d\":4},{\"c\":2,\"d\":5},{\"c\":2,\"d\":3},{\"c\":2,\"d\":6}]}";
        String diff = "{\"~a[0]\":{\"d\":4},\"~a[1]\":{\"d\":5},\"~a[3]\":{\"d\":6}}";

        String d = JsonDiff.diff(from, to);
        Assert.assertEquals(diff, d);

        String p = JsonPatch.apply(from, diff);
        Assert.assertEquals(to, p);
    }


    // Bug #3, thanks to deverton
    @Test
    public void testArrayObjectsWithNullAndChanges() {

        String from = "{\"a\":[{\"c\":2,\"d\":3},null,{\"c\":2,\"d\":3}]}";
        String to = "{\"a\":[{\"c\":2,\"d\":3},{\"c\":2,\"d\":7},42]}";
        String diff = "{\"~a[2]\":{\"d\":7},\"a[+3]\":42,\"-a[1]\":0}";

        String d = JsonDiff.diff(from, to);
        Assert.assertEquals(diff, d);

        String p = JsonPatch.apply(from, diff);
        Assert.assertEquals(to, p);

    }


    // Issue #5
    @Test
    public void testArrayObjectsRemoveAfterMultpleAdd() {

        String from = "{a:[{c:0},{c:1},{c:2},{c:3},{c:4}]}";
        String to = "{\"a\":[{\"e\":0},{\"c\":0},{\"c\":2},{\"e\":2},{\"c\":3,\"d\":3},{\"e\":3}]}";
        String diff = "{\"a[+0]\":{\"e\":0},\"a[+4]\":{\"e\":2},\"~a[3]\":{\"d\":3},\"~a[4]\":{\"e\":3,\"-c\":0},\"-a[2]\":0}";

        String d = JsonDiff.diff(from, to);
        Assert.assertEquals(diff, d);

        String p = JsonPatch.apply(from, diff);
        Assert.assertEquals(to, p);

    }


    // Issue #7, thanks to DrLansing
    @Test
    public void testEndlessLoopInCompareArrays() {

        String from = "{\"offset\":\"PT0S\",\"reference\":\"Today\",\"referenceTimeList\":[{\"name\":\"Yesterday\",\"start\":\"Unknown\"},{\"name\":\"Today\",\"offset\":\"P1D\",\"reference\":\"Yesterday\"}]}";
        String to = "{\"offset\":\"PT0S\",\"reference\":\"Today\",\"referenceTimeList\":[{\"name\":\"Today\",\"start\":\"2010-10-11T17:51:52.204Z\"}]}";

        String d = JsonDiff.diff(from, to);

        Assert.assertEquals(
                "{\"~referenceTimeList[1]\":{\"start\":\"2010-10-11T17:51:52.204Z\",\"-offset\":0,\"-reference\":0},\"-referenceTimeList[0]\":0}",
                d);

        String p = JsonPatch.apply(from, d);
        Assert.assertEquals(to, p);

    }


    // Issue #9, thanks to DrLansing
    @Test
    public void testAdjustArrayMutationBoundariesWithObjectDeletion() {

        String from = "{\"a\":[{\"b\":{\"id\":\"id1\"}},{\"b\":{\"id\":\"id2\"}}]}";
        String to = "{\"a\":[{\"b\":{\"id\":\"id2\"}}]}";

        String d = JsonDiff.diff(from, to);
        Assert.assertEquals("{\"-a[0]\":0}", d);

        String p = JsonPatch.apply(from, d);
        Assert.assertEquals(to, p);

    }


    // First find while debugging issue #9.
    @Test
    public void testAdjustArrayMutationBoundariesWithObjectDeletionInside() {

        String from = "{\"a\":[{\"b\":{\"id\":\"id1\"}},{\"b\":{ab:{},ac:null},\"id\":\"id2\"}]}";
        String to = "{\"a\":[{\"b\":{\"id\":\"id2\",\"ac\":\"123\"}}]}";

        String d = JsonDiff.diff(from, to);

        Assert.assertEquals("{\"~a[0]\":{\"~b\":{\"ac\":\"123\",\"id\":\"id2\"}},\"-a[1]\":0}", d);

        String p = JsonPatch.apply(from, d);
        Assert.assertEquals(to, p);

    }


    // Second find while debugging issue #9.
    @Test
    public void testModifyWhileDeletingPreviousElement1() {

        // in this case k comes after i which means the end result
        // is to delete a[0] and modify ~a[1]
        String from = "{\"a\":[{\"id\":1,\"k\":0},{\"id\":2,\"k\":1}]}";
        String to = "{\"a\":[{\"id\":2,\"k\":2}]}";

        String d = JsonDiff.diff(from, to);

        Assert.assertEquals("", d);

    }


    // Third find while debugging issue #9.
    @Test
    public void testModifyWhileDeletingPreviousElement2() {

        // in this case b comes before i which means the end result
        // is to delete a[1] and modify ~a[0]
        String from = "{\"a\":[{\"id\":1,\"b\":0},{\"id\":2,\"b\":1}]}";
        String to = "{\"a\":[{\"id\":2,\"b\":2}]}";

        String d = JsonDiff.diff(from, to);

        Assert.assertEquals("", d);

    }


    // Issue #9, thanks to DrLansing
    @Test
    public void testIssue9DiffPatch1() {

        String from = "{\"p:timeFrame\":{\"g:id\":\"ID_1bi4uybddb9711i1ih4o3qqwml\",\"g:relatedTime\":[{\"relativePosition\":\"Contains\",\"g:TimeInstant\":{\"g:id\":\"ID_2odwwe6m4fhj1i7uqavgp4mpv\",\"g:identifier\":{\"codeSpace\":\"JP1_02\",\"text\":\"C-day\"},\"g:timePosition\":{\"indeterminatePosition\":\"unknown\"}}},{\"relativePosition\":\"Contains\",\"g:TimeInstant\":{\"g:id\":\"ID_1gu4yx14on411od9yrrwbraia\",\"g:identifier\":{\"codeSpace\":\"JP1_02\",\"text\":\"D-day\"},\"g:relatedTime\":{\"relativePosition\":\"MetBy\",\"g:TimePeriod\":{\"g:id\":\"ID_158yq5cp2z5lm1nugs6byvrjve\",\"g:begin\":{\"x:href\":\"ID_2odwwe6m4fhj1i7uqavgp4mpv\",\"x:title\":\"C-day\"},\"g:end\":{\"nilReason\":\"Unknown\"},\"g:duration\":\"P10D\"}},\"g:timePosition\":{\"indeterminatePosition\":\"unknown\"}}},{\"relativePosition\":\"MetBy\",\"g:TimePeriod\":{\"g:id\":\"ID_190iv1hlow39r1c0gam6p8h02k\",\"g:begin\":{\"x:href\":\"ID_1gu4yx14on411od9yrrwbraia\",\"x:title\":\"D-day\"},\"g:end\":{\"nilReason\":\"Unknown\"},\"g:duration\":\"PT0S\"}}],\"g:beginPosition\":{\"indeterminatePosition\":\"unknown\"},\"g:endPosition\":{\"indeterminatePosition\":\"unknown\"}}}";
        String to = "{\"p:timeFrame\":{\"g:id\":\"ID_1bi4uybddb9711i1ih4o3qqwml\",\"g:relatedTime\":[{\"relativePosition\":\"Contains\",\"g:TimeInstant\":{\"g:id\":\"ID_1gu4yx14on411od9yrrwbraia\",\"g:identifier\":{\"codeSpace\":\"JP1_02\",\"text\":\"D-day\"},\"g:timePosition\":\"2010-10-11T17:51:52.204Z\"}},{\"relativePosition\":\"MetBy\",\"g:TimePeriod\":{\"g:id\":\"ID_190iv1hlow39r1c0gam6p8h02k\",\"g:begin\":{\"x:href\":\"ID_1gu4yx14on411od9yrrwbraia\",\"x:title\":\"D-day\"},\"g:end\":{\"nilReason\":\"Unknown\"},\"g:duration\":\"PT0S\"}}],\"g:beginPosition\":{\"indeterminatePosition\":\"unknown\"},\"g:endPosition\":{\"indeterminatePosition\":\"unknown\"}}}";

        String d = JsonDiff.diff(from, to);

        String p = JsonPatch.apply(from, d);

        System.out.println(from);
        System.out.println(to);
        System.out.println(d);

        Assert.assertEquals(to, p);

    }


    @Test
    public void testChangeArrayToObject() {

        String from = "{\"b\":[1,2]}";
        String to = "{\"b\":{\"id\":\"id2\"}}";

        String d = JsonDiff.diff(from, to);
        Assert.assertEquals("{\"b\":{\"id\":\"id2\"}}", d);

        String p = JsonPatch.apply(from, d);
        Assert.assertEquals(to, p);

    }


    @Test
    public void testChangeObjectToArray() {

        String from = "{\"b\":{\"id\":\"id2\"}}";
        String to = "{\"b\":[1,2]}";

        String d = JsonDiff.diff(from, to);
        Assert.assertEquals("{\"b\":[1,2]}", d);

        String p = JsonPatch.apply(from, d);
        Assert.assertEquals(to, p);

    }


    @Test
    public void testAdjustArrayMutationNestedArrayToObject() {

        String from = "{\"a\":[{\"b\":[1,2]},{\"b\":[1,2]}]}";
        String to = "{\"a\":[{\"b\":{\"id\":\"id2\"}}]}";

        String d = JsonDiff.diff(from, to);
        Assert.assertEquals("{\"~a[0]\":{\"b\":{\"id\":\"id2\"}},\"-a[1]\":0}", d);

        String p = JsonPatch.apply(from, d);
        Assert.assertEquals(to, p);

    }

}
