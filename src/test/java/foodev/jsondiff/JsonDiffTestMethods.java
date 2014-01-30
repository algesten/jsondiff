package foodev.jsondiff;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

public abstract class JsonDiffTestMethods extends TestCase {

	abstract String diff(String s1, String s2); 

	abstract String apply(String s1, String s2); 
	
    @Test
    public void testAdd() {

        String d = diff("{}", "{a:1}");
        Assert.assertEquals("{\"~\":[{\"+a\":1}]}", d);

        String p = apply("{}", d);
        Assert.assertEquals("{\"a\":1}", p);

    }


    @Test
    public void testAddObject() {

        String d = diff("{}", "{a:{b:1}}");
        Assert.assertEquals("{\"~\":[{\"+a\":{\"b\":1}}]}", d);

        String p = apply("{}", d);
        Assert.assertEquals("{\"a\":{\"b\":1}}", p);

    }


    @Test
    public void testRemove() {

        String d = diff("{a:1}", "{}");
        Assert.assertEquals("{\"~\":[{\"-a\":0}]}", d);

        String p = apply("{a:1}", d);
        Assert.assertEquals("{}", p);

    }


    @Test
    public void testChange() {

        String d = diff("{a:1}", "{a:2}");
        Assert.assertEquals("{\"~\":[{\"a\":2}]}", d);

        String p = apply("{a:1}", d);
        Assert.assertEquals("{\"a\":2}", p);

    }


    @Test
    public void testNestedAdd() {

        String d = diff("{a:1,b:{}}", "{a:1,b:{c:1}}");
        Assert.assertEquals("{\"~b\":[{\"+c\":1}]}", d);

        String p = apply("{a:1,b:{}}", d);
        Assert.assertEquals("{\"a\":1,\"b\":{\"c\":1}}", p);

    }


    @Test
    public void testNestedAddTwo() {

        String d = diff("{a:1,b:{}}", "{a:1,b:{c:1, d:2}}");
        Assert.assertEquals("{\"~b\":[{\"+c\":1},{\"+d\":2}]}", d);

        String p = apply("{a:1,b:{}}", d);
        Assert.assertEquals("{\"a\":1,\"b\":{\"c\":1,\"d\":2}}", p);

    }


    @Test
    public void testNestedRemove() {

        String d = diff("{a:1,b:{c:1}}", "{a:1,b:{}}");
        Assert.assertEquals("{\"~b\":[{\"-c\":0}]}", d);

        String p = apply("{a:1,b:{c:1}}", d);
        Assert.assertEquals("{\"a\":1,\"b\":{}}", p);

    }


    @Test
    public void testNestedChange() {

        String d = diff("{a:1,b:{c:1}}", "{a:1,b:{c:2}}");
        Assert.assertEquals("{\"~b\":[{\"c\":2}]}", d);

        String p = apply("{a:1,b:{c:1}}", d);
        Assert.assertEquals("{\"a\":1,\"b\":{\"c\":2}}", p);

    }


    @Test
    public void testNestedChangeAddBefore() {

        String d = diff("{a:1,b:{d:1}}", "{a:1,b:{c:1, d:2}}");
        Assert.assertEquals("{\"~b\":[{\"+c\":1},{\"d\":2}]}", d);

        String p = apply("{a:1,b:{d:1}}", d);
        Assert.assertEquals("{\"a\":1,\"b\":{\"d\":2,\"c\":1}}", p);

    }


    @Test
    public void testNestedChangeAddAfter() {

        String d = diff("{a:1,b:{d:1}}", "{a:1,b:{d:2, e:3}}");
        Assert.assertEquals("{\"~b\":[{\"d\":2},{\"+e\":3}]}", d);

        String p = apply("{a:1,b:{d:1}}", d);
        Assert.assertEquals("{\"a\":1,\"b\":{\"d\":2,\"e\":3}}", p);

    }


    @Test
    public void testNestedPartialRemove() {

        String d = diff("{a:1,b:{c:1,d:1}}", "{a:1,b:{c:1}}");
        Assert.assertEquals("{\"~b\":[{\"-d\":0}]}", d);

        String p = apply("{a:1,b:{c:1,d:1}}", d);
        Assert.assertEquals("{\"a\":1,\"b\":{\"c\":1}}", p);

    }


    @Test
    public void testNestedRemoveToEmpty() {

        String d = diff("{a:1,b:{c:1,d:1}}", "{a:1,b:{}}");
        Assert.assertEquals("{\"~b\":[{\"-c\":0},{\"-d\":0}]}", d);

        String p = apply("{a:1,b:{c:1,d:1}}", d);
        Assert.assertEquals("{\"a\":1,\"b\":{}}", p);

    }


    @Test
    public void testNestedCompleteRemove() {

        String d = diff("{a:1,b:{c:1,d:1}}", "{a:1}");
        Assert.assertEquals("{\"~\":[{\"-b\":0}]}", d);

        String p = apply("{a:1,b:{c:1,d:1}}", d);
        Assert.assertEquals("{\"a\":1}", p);

    }


    @Test
    public void testArrayAdd() {

        String d = diff("{}", "{a:[1]}");
        Assert.assertEquals("{\"~\":[{\"+a\":[1]}]}", d);

        String p = apply("{}", d);
        Assert.assertEquals("{\"a\":[1]}", p);

    }


    @Test
    public void testArrayAddTwo() {

        String d = diff("{}", "{a:[1,2]}");
        Assert.assertEquals("{\"~\":[{\"+a\":[1,2]}]}", d);

        String p = apply("{}", d);
        Assert.assertEquals("{\"a\":[1,2]}", p);

    }


    @Test
    public void testArrayAddToEmpty() {

        String d = diff("{a: []}", "{a:[1]}");
        Assert.assertEquals("{\"~a\":[{\"+0\":1}]}", d);

        String p = apply("{a: []}", d);
        Assert.assertEquals("{\"a\":[1]}", p);

    }


    @Test
    public void testArrayAddTwoToEmpty() {

        String d = diff("{a: []}", "{a:[1,2]}");
        Assert.assertEquals("{\"~a\":[{\"+0\":1},{\"+1\":2}]}", d);

        String p = apply("{a: []}", d);
        Assert.assertEquals("{\"a\":[1,2]}", p);

    }


    @Test
    public void testArrayAddToExisting() {

        String d = diff("{a: [0]}", "{a:[0,1]}");
        Assert.assertEquals("{\"~a\":[{\"+1\":1}]}", d);

        String p = apply("{a: [0]}", d);
        Assert.assertEquals("{\"a\":[0,1]}", p);

    }


    @Test
    public void testArrayAddTwoToExisting() {

        String d = diff("{a: [3]}", "{a:[1,2,3]}");
        Assert.assertEquals("{\"~a\":[{\"+0\":1},{\"+1\":2}]}", d);

        String p = apply("{a: [3]}", d);
        Assert.assertEquals("{\"a\":[1,2,3]}", p);

    }


    @Test
    public void testArrayAddTwoOtherToExisting() {

        String d = diff("{a: [3,4,1,2]}", "{a:[1,2,5]}");
        Assert.assertEquals("{\"~a\":[{\"-0\":0},{\"-0\":0},{\"+2\":5}]}", d);

        String p = apply("{a: [3,4,1,2]}", d);
        Assert.assertEquals("{\"a\":[1,2,5]}", p);

    }


    @Test
    public void testArrayInsertInExtisting() {

        String d = diff("{a: [0,2,3,4]}", "{a:[0,1,2,3,4]}");
        Assert.assertEquals("{\"~a\":[{\"+1\":1}]}", d);

        String p = apply("{a: [0,2,3,4]}", d);
        Assert.assertEquals("{\"a\":[0,1,2,3,4]}", p);

    }


    @Test
    public void testArrayInsertAfterDeleted() {

        String d = diff("{a: [0,1,2,4]}", "{a:[0,1,3,4]}");
        Assert.assertEquals("{\"~a\":[{\"2\":3}]}", d);

        String p = apply("{a: [0,1,2,4]}", d);
        Assert.assertEquals("{\"a\":[0,1,3,4]}", p);

    }


    @Test
    public void testArrayInsertTwoAfterDeleted() {

        String d = diff("{a: ['a','b','d']}", "{a:['a','c','e','d']}");
        Assert.assertEquals("{\"~a\":[{\"1\":\"c\"},{\"+2\":\"e\"}]}", d);

        String p = apply("{a: ['a','b','d']}", d);
        Assert.assertEquals("{\"a\":[\"a\",\"c\",\"e\",\"d\"]}", p);

    }


    @Test
    public void testArrayRemoveAll() {

        String d = diff("{a: [1,2]}", "{}");
        Assert.assertEquals("{\"~\":[{\"-a\":0}]}", d);

        String p = apply("{a: [1,2]}", d);
        Assert.assertEquals("{}", p);

    }


    @Test
    public void testArrayRemoveToEmpty() {

        String d = diff("{a: [1]}", "{a:[]}");
        Assert.assertEquals("{\"~a\":[{\"-0\":0}]}", d);

        String p = apply("{a: [1]}", d);
        Assert.assertEquals("{\"a\":[]}", p);

    }


    @Test
    public void testArrayRemoveLast() {

        String d = diff("{a: [1,2]}", "{a:[1]}");
        Assert.assertEquals("{\"~a\":[{\"-1\":0}]}", d);

        String p = apply("{a: [1,2]}", d);
        Assert.assertEquals("{\"a\":[1]}", p);

    }


    @Test
    public void testArrayRemoveFirst() {

        String d = diff("{a: [1,2]}", "{a:[2]}");
        Assert.assertEquals("{\"~a\":[{\"-0\":0}]}", d);

        String p = apply("{a: [1,2]}", d);
        Assert.assertEquals("{\"a\":[2]}", p);

    }


    @Test
    public void testArrayRemoveMiddle() {

        String d = diff("{a: [1,2,3]}", "{a:[1,3]}");
        Assert.assertEquals("{\"~a\":[{\"-1\":0}]}", d);

        String p = apply("{a: [1,2,3]}", d);
        Assert.assertEquals("{\"a\":[1,3]}", p);

    }


    @Test
    public void testArrayRemoveMultiple() {

        String d = diff("{a: [1,2,3,4]}", "{a:[1,3]}");
        Assert.assertEquals("{\"~a\":[{\"-1\":0},{\"-2\":0}]}", d);

        String p = apply("{a: [1,2,3,4]}", d);
        Assert.assertEquals("{\"a\":[1,3]}", p);

    }


    @Test
    public void testArrayAddMultiDimensional() {

        String d = diff("{a:[1]}", "{a:[1,[2,3]]}");
        Assert.assertEquals("{\"~a\":[{\"+1\":[2,3]}]}", d);

        String p = apply("{a: [1]}", d);
        Assert.assertEquals("{\"a\":[1,[2,3]]}", p);

    }


    @Test
    public void testArrayRemoveInMulti() {

        String d = diff("{a:[1,[2,3]]}", "{a:[1,[3]]}");
        Assert.assertEquals("{\"a\":{\"~1\":[{\"-0\":0}]}}", d);

        String p = apply("{a:[1,[2,3]]}", d);
        Assert.assertEquals("{\"a\":[1,[3]]}", p);

    }


    @Test
    public void testArrayRemoveLastInMulti() {

        String d = diff("{a:[1,[2,3]]}", "{a:[1,[2]]}");
        Assert.assertEquals("{\"a\":{\"~1\":[{\"-1\":0}]}}", d);

        String p = apply("{a:[1,[2,3]]}", d);
        Assert.assertEquals("{\"a\":[1,[2]]}", p);

    }


    @Test
    public void testArrayInsertInMulti() {

        String d = diff("{a:[1,[2,4]]}", "{a:[1,[2,3,4]]}");
        Assert.assertEquals("{\"a\":{\"~1\":[{\"+1\":3}]}}", d);

        String p = apply("{a:[1,[2,4]]}", d);
        Assert.assertEquals("{\"a\":[1,[2,3,4]]}", p);

    }


    @Test
    public void testAddObjectToArray() {

        String d = diff("{a:[1]}", "{a:[1,{b:2}]}");
        Assert.assertEquals("{\"~a\":[{\"+1\":{\"b\":2}}]}", d);

        String p = apply("{a:[1]}", d);
        Assert.assertEquals("{\"a\":[1,{\"b\":2}]}", p);

    }


    @Test
    public void testMergeObjectInArray() {

        String d = diff("{a:[1,{}]}", "{a:[1,{b:2}]}");
        Assert.assertEquals("{\"a\":{\"~1\":[{\"+b\":2}]}}", d);

        String p = apply("{a:[1,{}]}", d);
        Assert.assertEquals("{\"a\":[1,{\"b\":2}]}", p);

    }


    @Test
    public void testInsertInArrayInObjectInArray() {

        String d = diff("{a:[1,{b:[]}]}", "{a:[1,{b:[2]}]}");

        String p = apply("{a:[1,{b:[]}]}", d);
        Assert.assertEquals("{\"a\":[1,{\"b\":[2]}]}", p);

    }


    @Test
    public void testRemoveFromArrayInObjectInArray() {

        String d = diff("{a:[1,{b:[2,3]}]}", "{a:[1,{b:[]}]}");
        Assert.assertEquals("{\"a\":{\"1\":{\"~b\":[{\"-0\":0},{\"-0\":0}]}}}", d);

        String p = apply("{a:[1,{b:[2,3]}]}", d);
        Assert.assertEquals("{\"a\":[1,{\"b\":[]}]}", p);

    }


    @Test
    public void testRemoveChange() {

        String d = diff("{a: 1, b: 2, c: 3}", "{b:4}");
        Assert.assertEquals("{\"~\":[{\"b\":4},{\"-a\":0},{\"-c\":0}]}", d);

        String p = apply("{a: 1, b: 2, c: 3}", d);
        Assert.assertEquals("{\"b\":4}", p);

    }


    @Test
    public void testArrToNumeric() {

        String d = diff("{a: [1,2]}", "{a: 1}");

        String p = apply("{a: [1,2]}", d);
        Assert.assertEquals("{\"a\":1}", p);
        Assert.assertEquals("{\"~\":[{\"a\":1}]}", d);

    }


    @Test
    public void testMix() {

        String d = diff("{a: [1,2], b: { foo: 'b'}, c: 42}", "{a: 1, b: {foo: 'b', bar: 42}, c: 45}");
        Assert.assertEquals("{\"~b\":[{\"+bar\":42}],\"~\":[{\"a\":1},{\"c\":45}]}", d);

        String p = apply("{a: [1,2], b: { foo: 'b'}, c: 42}", d);
        Assert.assertEquals("{\"a\":1,\"b\":{\"foo\":\"b\",\"bar\":42},\"c\":45}", p);

    }


    @Test
    public void testArrayObjectsChange() {
        String from = "{a:[{b:1}]}";
        String to = "{\"a\":[{\"c\":1}]}";
        String diff = "{\"a\":{\"~0\":[{\"+c\":1},{\"-b\":0}]}}";

        String d = diff(from, to);
        Assert.assertEquals(diff, d);

        String p = apply(from, diff);
        Assert.assertEquals(to, p);

    }


    @Test
    public void testArrayStringsRotateLeft() {
        String from = "{\"a\":[\"s1\",\"s2\",\"s3\"]}";
        String to = "{\"a\":[\"s2\",\"s3\",\"s4\"]}";

        String diff = diff(from, to);
        Assert.assertEquals("{\"~a\":[{\"-0\":0},{\"+2\":\"s4\"}]}", diff);

        String p = apply(from, diff);
        Assert.assertEquals(to, p);

    }


    // Bug #1 thanks to deverton
    @Test
    public void testArrayObjectsRotateLeft() {
        String from = "{\"a\":[{\"b\":1},{\"c\":2},{\"d\":3}]}";
        String to = "{\"a\":[{\"c\":2},{\"d\":3},{\"e\":4}]}";
        String diff = "{\"~a\":[{\"-1\":0},{\"+2\":{\"e\":4}}],\"a\":{\"~0\":[{\"-b\":0},{\"c\":2}],\"~2\":[{\"d\":3}]}}";

        String d = diff(from, to);
        String p = apply(from, d);
        Assert.assertEquals(to, p);
        
        Assert.assertEquals(diff, d);


    }


    // Bug #1, thanks to deverton
    @Test
    public void testArrayObjectsRotateRight() {

        String from = "{\"a\":[{\"c\":2},{\"d\":3},{\"e\":4}]}";
        String to = "{\"a\":[{\"b\":1},{\"c\":2},{\"d\":3}]}";
        String diff = "{\"~a\":[{\"+1\":{\"c\":2}},{\"-3\":0}],\"a\":{\"~0\":[{\"+b\":1},{\"-c\":0}]}}";

        String d = diff(from, to);
        String p = apply(from, d);
        Assert.assertEquals(to, p);
        
        Assert.assertEquals(diff, d);

    }


    @Test
    public void testObjectObjectsRotateLeft() {

        String from = "{\"a1\":{\"c\":2},\"a2\":{\"d\":3},\"a3\":{\"e\":4}}";
        String to = "{\"a1\":{\"b\":1},\"a2\":{\"c\":2},\"a3\":{\"d\":3}}";

        String d = diff(from, to);
        Assert.assertEquals("{\"~a1\":[{\"+b\":1},{\"-c\":0}],\"~a2\":[{\"+c\":2},{\"-d\":0}],\"~a3\":[{\"+d\":3},{\"-e\":0}]}", d);

        String p = apply(from, d);
        Assert.assertEquals(to, p);

    }


    // Bug #2, thanks to deverton
    @Test
    public void testArrayObjectsChangeField() {
        String from = "{\"a\":[{\"c\":2,\"d\":3},{\"c\":2,\"d\":3},{\"c\":2,\"d\":3},{\"c\":2,\"d\":3}]}";
        String to = "{\"a\":[{\"c\":2,\"d\":4},{\"c\":2,\"d\":5},{\"c\":2,\"d\":3},{\"c\":2,\"d\":6}]}";
        String diff = "{\"a\":{\"~0\":[{\"d\":4}],\"~1\":[{\"d\":5}],\"~3\":[{\"d\":6}]}}";

        String d = diff(from, to);
        Assert.assertEquals(diff, d);

        String p = apply(from, diff);
        Assert.assertEquals(to, p);
    }


    // Bug #3, thanks to deverton
    @Test
    public void testArrayObjectsWithNullAndChanges() {

        String from = "{\"a\":[{\"c\":2,\"d\":3},null,{\"c\":2,\"d\":3}]}";
        String to = "{\"a\":[{\"c\":2,\"d\":3},{\"c\":2,\"d\":7},42]}";
        String diff = "{\"~a\":[{\"-1\":0},{\"+2\":42}],\"a\":{\"~2\":[{\"d\":7}]}}";

        String d = diff(from, to);
        Assert.assertEquals(diff, d);

        String p = apply(from, diff);
        Assert.assertEquals(to, p);

    }


    // Issue #5
    @Test
    public void testArrayObjectsRemoveAfterMultipleAdd() {

        String from = "{a:[{c:0},{c:1},{c:2},{c:3},{c:4}]}";
        String to = "{\"a\":[{\"e\":0},{\"c\":0},{\"c\":2},{\"e\":2},{\"c\":3,\"d\":3},{\"e\":3}]}";
        String diff = "{\"~a\":[{\"+1\":{\"c\":0}},{\"-3\":0},{\"+4\":{\"c\":3,\"d\":3}}],\"a\":{\"~0\":[{\"+e\":0},{\"-c\":0}],\"~1\":[{\"c\":2}],\"~3\":[{\"+e\":2},{\"-c\":0}],\"~4\":[{\"+e\":3},{\"-c\":0}]}}";

        String d = diff(from, to);
        Assert.assertEquals(diff, d);

        String p = apply(from, diff);
        Assert.assertEquals(to, p);

    }


    @Test
    public void testArrayObjectsRemoveAfterAdd() {

        String from = "{a:[{c:1},{c:3},{c:4}]}";
        String to = "{\"a\":[{\"e\":2},{\"c\":3,\"d\":3},{\"e\":3}]}";
        String diff = "{\"a\":{\"~0\":[{\"+e\":2},{\"-c\":0}],\"~1\":[{\"+d\":3}],\"~2\":[{\"+e\":3},{\"-c\":0}]}}";

        String d = diff(from, to);
        Assert.assertEquals(diff, d);

        String p = apply(from, diff);
        Assert.assertEquals(to, p);

    }


    // Issue #7, thanks to DrLansing
    @Test
    public void testEndlessLoopInCompareArrays() {

        String from = "{\"offset\":\"PT0S\",\"reference\":\"Today\",\"referenceTimeList\":[{\"name\":\"Yesterday\",\"start\":\"Unknown\"},{\"name\":\"Today\",\"offset\":\"P1D\",\"reference\":\"Yesterday\"}]}";
        String to = "{\"offset\":\"PT0S\",\"reference\":\"Today\",\"referenceTimeList\":[{\"name\":\"Today\",\"start\":\"2010-10-11T17:51:52.204Z\"}]}";

        String d = diff(from, to);

        String p = apply(from, d);
        Assert.assertEquals(to, p);

        Assert.assertEquals(
                "{\"~referenceTimeList\":[{\"-1\":0}],\"referenceTimeList\":{\"~0\":[{\"name\":\"Today\"},{\"start\":\"2010-10-11T17:51:52.204Z\"},{\"-offset\":0},{\"-reference\":0}]}}",
                d);

    }


    // Issue #9, thanks to DrLansing
    @Test
    public void testAdjustArrayMutationBoundariesWithObjectDeletion() {

        String from = "{\"a\":[{\"b\":{\"id\":\"id1\"}},{\"b\":{\"id\":\"id2\"}}]}";
        String to = "{\"a\":[{\"b\":{\"id\":\"id2\"}}]}";

        String d = diff(from, to);
        Assert.assertEquals("{\"~a\":[{\"-1\":0}],\"a\":{\"0\":{\"~b\":[{\"id\":\"id2\"}]}}}", d);

        String p = apply(from, d);
        Assert.assertEquals(to, p);

    }


    // First find while debugging issue #9.
    @Test
    public void testAdjustArrayMutationBoundariesWithObjectDeletionInside() {

        String from = "{\"a\":[{\"b\":{\"id\":\"id1\"}},{\"b\":{ab:{},ac:null},\"id\":\"id2\"}]}";
        String to = "{\"a\":[{\"b\":{\"id\":\"id2\",\"ac\":\"123\"}}]}";

        String d = diff(from, to);

        Assert.assertEquals("{\"~a\":[{\"-1\":0}],\"a\":{\"0\":{\"~b\":[{\"+ac\":\"123\"},{\"id\":\"id2\"}]}}}", d);

        String p = apply(from, d);
        Assert.assertEquals(to, p);

    }


    // Second find while debugging issue #9.
    @Test
    public void testModifyWhileDeletingPreviousElement1() {

        // in this case k comes after i which means the end result
        // is to delete a[0] and modify ~a[1]
        String from = "{\"a\":[{\"id\":1,\"k\":0},{\"id\":2,\"k\":1}]}";
        String to = "{\"a\":[{\"id\":2,\"k\":2}]}";

        String d = diff(from, to);

        Assert.assertEquals("{\"~a\":[{\"-1\":0}],\"a\":{\"~0\":[{\"id\":2},{\"k\":2}]}}", d);

        String p = apply(from, d);
        Assert.assertEquals(to, p);

    }


    // Third find while debugging issue #9.
    @Test
    public void testModifyWhileDeletingPreviousElement2() {

        // in this case b comes before i which means the end result
        // is to delete a[1] and modify ~a[0]
        String from = "{\"a\":[{\"b\":0,\"id\":1},{\"b\":1,\"id\":2}]}";
        String to = "{\"a\":[{\"b\":2,\"id\":2}]}";

        String d = diff(from, to);

        Assert.assertEquals("{\"~a\":[{\"-1\":0}],\"a\":{\"~0\":[{\"b\":2},{\"id\":2}]}}", d);

        String p = apply(from, d);
        Assert.assertEquals(to, p);

    }


    // Issue #9, thanks to DrLansing
    @Test
    public void testIssue9DiffPatch1() {

        String from = "{\"p:timeFrame\":{\"g:id\":\"ID_1bi4uybddb9711i1ih4o3qqwml\",\"g:relatedTime\":[{\"relativePosition\":\"Contains\",\"g:TimeInstant\":{\"g:id\":\"ID_2odwwe6m4fhj1i7uqavgp4mpv\",\"g:identifier\":{\"codeSpace\":\"JP1_02\",\"text\":\"C-day\"},\"g:timePosition\":{\"indeterminatePosition\":\"unknown\"}}},{\"relativePosition\":\"Contains\",\"g:TimeInstant\":{\"g:id\":\"ID_1gu4yx14on411od9yrrwbraia\",\"g:identifier\":{\"codeSpace\":\"JP1_02\",\"text\":\"D-day\"},\"g:relatedTime\":{\"relativePosition\":\"MetBy\",\"g:TimePeriod\":{\"g:id\":\"ID_158yq5cp2z5lm1nugs6byvrjve\",\"g:begin\":{\"x:href\":\"ID_2odwwe6m4fhj1i7uqavgp4mpv\",\"x:title\":\"C-day\"},\"g:end\":{\"nilReason\":\"Unknown\"},\"g:duration\":\"P10D\"}},\"g:timePosition\":{\"indeterminatePosition\":\"unknown\"}}},{\"relativePosition\":\"MetBy\",\"g:TimePeriod\":{\"g:id\":\"ID_190iv1hlow39r1c0gam6p8h02k\",\"g:begin\":{\"x:href\":\"ID_1gu4yx14on411od9yrrwbraia\",\"x:title\":\"D-day\"},\"g:end\":{\"nilReason\":\"Unknown\"},\"g:duration\":\"PT0S\"}}],\"g:beginPosition\":{\"indeterminatePosition\":\"unknown\"},\"g:endPosition\":{\"indeterminatePosition\":\"unknown\"}}}";
        String to = "{\"p:timeFrame\":{\"g:id\":\"ID_1bi4uybddb9711i1ih4o3qqwml\",\"g:relatedTime\":[{\"relativePosition\":\"Contains\",\"g:TimeInstant\":{\"g:id\":\"ID_1gu4yx14on411od9yrrwbraia\",\"g:identifier\":{\"codeSpace\":\"JP1_02\",\"text\":\"D-day\"},\"g:timePosition\":\"2010-10-11T17:51:52.204Z\"}},{\"relativePosition\":\"MetBy\",\"g:TimePeriod\":{\"g:id\":\"ID_190iv1hlow39r1c0gam6p8h02k\",\"g:begin\":{\"x:href\":\"ID_1gu4yx14on411od9yrrwbraia\",\"x:title\":\"D-day\"},\"g:end\":{\"nilReason\":\"Unknown\"},\"g:duration\":\"PT0S\"}}],\"g:beginPosition\":{\"indeterminatePosition\":\"unknown\"},\"g:endPosition\":{\"indeterminatePosition\":\"unknown\"}}}";

        String d = diff(from, to);

        String p = apply(from, d);

        Assert.assertEquals(to, p);

        Assert.assertEquals(
                "{\"p:timeFrame\":{\"~g:relatedTime\":[{\"-1\":0}],\"g:relatedTime\":{\"~0\":[{\"relativePosition\":\"Contains\"}],\"0\":{\"~g:TimeInstant\":[{\"g:id\":\"ID_1gu4yx14on411od9yrrwbraia\"},{\"g:timePosition\":\"2010-10-11T17:51:52.204Z\"},{\"-g:relatedTime\":0}],\"g:TimeInstant\":{\"~g:identifier\":[{\"codeSpace\":\"JP1_02\"},{\"text\":\"D-day\"}]}}}}}",
                d);

    }


    @Test
    public void testChangeArrayToObject() {

        String from = "{\"b\":[1,2]}";
        String to = "{\"b\":{\"id\":\"id2\"}}";

        String d = diff(from, to);

        String p = apply(from, d);
        Assert.assertEquals(to, p);
        Assert.assertEquals("{\"~\":[{\"b\":{\"id\":\"id2\"}}]}", d);

    }


    @Test
    public void testChangeObjectToArray() {

        String from = "{\"b\":{\"id\":\"id2\"}}";
        String to = "{\"b\":[1,2]}";

        String d = diff(from, to);
        
        String p = apply(from, d);
        Assert.assertEquals(to, p);
        Assert.assertEquals("{\"~\":[{\"b\":[1,2]}]}", d);

    }


    @Test
    public void testAdjustArrayMutationNestedArrayToObject() {

        String from = "{\"a\":[{\"b\":[1,2]},{\"b\":[1,2]}]}";
        String to = "{\"a\":[{\"b\":{\"id\":\"id2\"}}]}";

        String d = diff(from, to);

        String p = apply(from, d);
        Assert.assertEquals(to, p);
        Assert.assertEquals("{\"~a\":[{\"-1\":0}],\"a\":{\"~0\":[{\"b\":{\"id\":\"id2\"}}]}}", d);

    }
    
    // Issue #13. Thanks to Daniel Gardner for reporting.
    @Test
    public void testSameEndValueTwoConsequtiveArrayElements() {
            
            String i = "{\"a\":[{\"b\":[1],\"c\":\"x\"}]}";

            String j1 = "{\"a\":[{\"b\":[1],\"c\":\"x\"},{\"b\":[1],\"c\":\"x\"}]}";

        String d = diff(i, j1);

        Assert.assertEquals("{\"~a\":[{\"+1\":{\"b\":[1],\"c\":\"x\"}}]}", d);
        
        String p = apply(i, d);
        
        Assert.assertEquals(j1, p);
        
    }

    
    // #14. Thanks to zhangwei13 for reporting.
        @Test
        public void testArrayObjectMutations() {

                String j1 = "{\"a\":[{\"name\":\"k2\",\"value\":\"k2v2\",\"seq\":1},"
                                + "{\"name\":\"k6\",\"value\":\"k6v1\",\"seq\":1}," + "{\"name\":\"k7\",\"value\":\"k7v1\",\"seq\":1},"
                                + "{\"name\":\"k5\",\"value\":\"k5v1\",\"seq\":1}]}";

                String j2 = "{\"a\":[{\"name\":\"k1\",\"value\":\"k1v1\",\"seq\":1},"
                                + "{\"name\":\"k2\",\"value\":\"k2v1\",\"seq\":1}," + "{\"name\":\"k3\",\"value\":\"k3v1\",\"seq\":1},"
                                + "{\"name\":\"k4\",\"value\":\"k4v1\",\"seq\":1},"
                                + "{\"name\":\"k5\",\"value\":\"k5v1\",\"seq\":1}]}";

                String d = diff(j1, j2);

                Assert.assertEquals("{\"~a\":[{\"+1\":{\"name\":\"k2\",\"value\":\"k2v1\",\"seq\":1}}],\"a\":{\"~0\":[{\"name\":\"k1\"},{\"seq\":1},{\"value\":\"k1v1\"}],\"~1\":[{\"name\":\"k3\"},{\"value\":\"k3v1\"}],\"~2\":[{\"name\":\"k4\"},{\"value\":\"k4v1\"}]}}", d);
                
                String p = apply(j1, d);

                Assert.assertEquals(j2, p);

        }
        
        // issue #12. thanks to nachogmd for test and solution.
        @Test
        public void testArrayChangeToNull() {                
                try {
                 String from,to;
                 from = "{\"externalIds\":[{\"id\":\"4066-b329\"}],\"relation\":[{\"rules\":{\"keys\":[{\"shareHolders\":[{\"percentage\":0.0}]}]}}]}";
                 to = "{\"externalIds\":[{\"id\":\"4066-b329\"}]}";
        
                 diff(from, to);
                 from = "{\"externalIds\":[{\"id\":\"4066-b329\"}],\"catalogueRelationList\":[{\"rules\":{\"keys\":[{\"shareHolders\":[{\"percentage\":0.0}]}]}}]}";
                 to = "{\"externalIds\":[{\"id\":\"4066-b329\"}]}";
                 diff(from, to);
        
                 String diff = "{\"~\":[{\"-catalogueRelationList\":0}]}";
        
                 String d = diff(from, to);
                 Assert.assertEquals(diff, d);
        
                 String p = apply(from, diff);
                 Assert.assertEquals(to, p);
                } catch (NullPointerException npe) {
                        Assert.fail("Caught NPE");
                }
        }

        @Test
        public void testRecoveryTraversal() {
        	String from = "{\r\n" + 
        			"	\"initialStatement\" : {\r\n" + 
        			"		\"shareHolders\" : [{\r\n" + 
        			"				\"cessionary\" : {\"a\":1}\r\n" + 
        			"			}, {\r\n" + 
        			"				\"cessionary\" : {\"a\":2}\r\n" + 
        			"			}\r\n" + 
        			"		],\r\n" + 
        			"		\"territory\" : \"+2WL-FR\"\r\n" + 
        			"	}\r\n" + 
        			"}\r\n" + 
        			"";
        	String to = "{\"initialStatement\":{\"shareHolders\":[{\"cessionary\":{\"a\":1}}],\"territory\":\"+2WL\"}}";
        	String d = diff(from, to);
        	String actual = apply(from, d);
        	Assert.assertEquals(to, actual);
        }
        
        @Test
        public void testRecoveryTraversal2() {
        	String from = "{\r\n" + 
        			"	\"initialStatement\" : {\r\n" + 
        			"		\"shareHolders\" : [{\r\n" + 
        			"				\"cessionary\" : {\"a\":2}\r\n" + 
        			"			}, {\r\n" + 
        			"				\"cessionary\" : {\"a\":1}\r\n" + 
        			"			}\r\n" + 
        			"		],\r\n" + 
        			"		\"territory\" : \"+2WL-FR\"\r\n" + 
        			"	}\r\n" + 
        			"}\r\n" + 
        			"";
        	String to = "{\"initialStatement\":{\"shareHolders\":[{\"cessionary\":{\"a\":1}}],\"territory\":\"+2WL\"}}";
        	String d = diff(from, to);
        	String actual = apply(from, d);
        	Assert.assertEquals(to, actual);
        }
        
        @Test
        public void testOther() {
        	String from = "{\"iswc\":\"\",\"duration\":\"00000000\",\"genre\":{\"id\":\"74\",\"name\":\"M\",\"code\":\"74\"},\"id\":\"0d90f464-d199-4ac6-9935-131662c2d80f\",\"auditInfo\":{\"created\":\"2014-01-29T00:37:20\"},\"workType\":{\"id\":\"MW\",\"name\":\"Obra Musical\",\"code\":\"MW\",\"creationClass\":{\"id\":\"MW\",\"name\":\"Obra musical\"}},\"version\":1,\"title\":{\"id\":\"e22c02e0-aa1b-4c1c-a54c-f17bc73efc5b\",\"name\":\"BRAINWORK\"},\"authors\":[{\"id\":\"d71aca32-1813-4d4e-a8b2-d83084eb6aba\",\"contributor\":{\"id\":\"fb160b7b-4ef0-4cb8-a1f1-f82c1a8e6de6\",\"name\":{\"id\":\"be824898-202f-41b9-86dd-d003b3c71714\",\"lastName\":\"VIGER FREDERIC PETER\",\"formattedName\":\"VIGER FREDERIC PETER\"}},\"role\":{\"id\":\"C\",\"name\":\"Compositor\"}}],\"externalIds\":[{\"id\":\"940be970-ef77-4324-baad-1fe1cbf8a255\",\"namespace\":{\"id\":\"SGAE_WORKID\",\"name\":\"Codigo obra SGAE\",\"key\":\"WORKID\",\"applicableTo\":\"MusicWork\",\"type\":\"COLLECTING_SOCIETY\"},\"value\":\"XXX\",\"defaultId\":\"YES\"}],\"external\":[{\"id\":\"8b200f7a-b83e-4b33-b0d6-cc6d8b8afbd6\",\"namespace\":{\"id\":\"SGAE\",\"name\":\"SGAE\",\"key\":\"SGAE\",\"applicableTo\":\"MusicWork\",\"type\":\"COLLECTING_SOCIETY\"}}]}";
        	String to = "{\"iswc\":\"\",\"duration\":\"00000000\",\"genre\":{\"id\":\"74\",\"name\":\"M\",\"code\":\"74\"},\"id\":\"0d90f464-d199-4ac6-9935-131662c2d80f\",\"auditInfo\":{\"created\":\"2014-01-29T00:37:20\"},\"workType\":{\"id\":\"DRA\",\"name\":\"Obra Musical Dramatica\",\"code\":\"DRA\",\"creationClass\":{\"id\":\"MW\",\"name\":\"Obra musical\"}},\"version\":1,\"title\":{\"id\":\"e22c02e0-aa1b-4c1c-a54c-f17bc73efc5b\",\"name\":\"BRAINWORK\"},\"authors\":[{\"contributor\":{\"id\":\"6f44dda2-0e93-4379-9423-079bf0d141e8\",\"name\":{\"id\":\"240f914f-4dad-430a-b618-b073feb9b6cb\",\"lastName\":\"ACKERMAN WILLIAM\",\"formattedName\":\"ACKERMAN WILLIAM\"}},\"role\":{\"id\":\"A\",\"name\":\"Autor\"}},{\"id\":\"d71aca32-1813-4d4e-a8b2-d83084eb6aba\",\"contributor\":{\"id\":\"fb160b7b-4ef0-4cb8-a1f1-f82c1a8e6de6\",\"name\":{\"id\":\"be824898-202f-41b9-86dd-d003b3c71714\",\"lastName\":\"VIGER FREDERIC PETER\",\"formattedName\":\"VIGER FREDERIC PETER\"}},\"role\":{\"id\":\"C\",\"name\":\"Compositor\"}}],\"externalIds\":[{\"id\":\"940be970-ef77-4324-baad-1fe1cbf8a255\",\"namespace\":{\"id\":\"SGAE_WORKID\",\"name\":\"Codigo obra SGAE\",\"key\":\"WORKID\",\"applicableTo\":\"MusicWork\",\"type\":\"COLLECTING_SOCIETY\"},\"value\":\"XXX\",\"defaultId\":\"YES\"}],\"external\":[{\"id\":\"8b200f7a-b83e-4b33-b0d6-cc6d8b8afbd6\",\"namespace\":{\"id\":\"SGAE\",\"name\":\"SGAE\",\"key\":\"SGAE\",\"applicableTo\":\"MusicWork\",\"type\":\"COLLECTING_SOCIETY\"}}],\"copyrightDate\":\"2014-01-30T12:00:00\"}";
        	String d = diff(from, to);
        	String actual = apply(from, d);
        	Assert.assertEquals(to, actual);
        }
}
