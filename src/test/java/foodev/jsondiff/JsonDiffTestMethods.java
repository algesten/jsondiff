package foodev.jsondiff;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gwt.junit.client.GWTTestCase;

public class JsonDiffTestMethods extends GWTTestCase {

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
        String p = JsonPatch.apply("{}", d);
        
        assertEquals("{\"a\":1}", p);

    }


    @Test
    public void testAddObject() {

        String d = JsonDiff.diff("{}", "{a:{b:1}}");

        String p = JsonPatch.apply("{}", d);
        assertEquals("{\"a\":{\"b\":1}}", p);

    }


    @Test
    public void testRemove() {

        String d = JsonDiff.diff("{a:1}", "{}");

        String p = JsonPatch.apply("{a:1}", d);
        assertEquals("{}", p);

    }


    @Test
    public void testChange() {

        String d = JsonDiff.diff("{a:1}", "{a:2}");
        
        String p = JsonPatch.apply("{a:1}", d);
        assertEquals("{\"a\":2}", p);

    }


    @Test
    public void testNestedAdd() {

        String d = JsonDiff.diff("{a:1,b:{}}", "{a:1,b:{c:1}}");

        String p = JsonPatch.apply("{a:1,b:{}}", d);
        assertEquals("{\"a\":1,\"b\":{\"c\":1}}", p);

    }


    @Test
    public void testNestedAddTwo() {

        String d = JsonDiff.diff("{a:1,b:{}}", "{a:1,b:{c:1, d:2}}");

        String p = JsonPatch.apply("{a:1,b:{}}", d);
        assertEquals("{\"a\":1,\"b\":{\"c\":1,\"d\":2}}", p);

    }


    @Test
    public void testNestedRemove() {

        String d = JsonDiff.diff("{a:1,b:{c:1}}", "{a:1,b:{}}");

        String p = JsonPatch.apply("{a:1,b:{c:1}}", d);
        assertEquals("{\"a\":1,\"b\":{}}", p);

    }


    @Test
    public void testNestedChange() {

        String d = JsonDiff.diff("{a:1,b:{c:1}}", "{a:1,b:{c:2}}");

        String p = JsonPatch.apply("{a:1,b:{c:1}}", d);
        assertEquals("{\"a\":1,\"b\":{\"c\":2}}", p);

    }


    @Test
    public void testNestedChangeAddBefore() {

        String d = JsonDiff.diff("{a:1,b:{d:1}}", "{a:1,b:{c:1, d:2}}");

        String p = JsonPatch.apply("{a:1,b:{d:1}}", d);
        assertEquals("{\"a\":1,\"b\":{\"d\":2,\"c\":1}}", p);

    }


    @Test
    public void testNestedChangeAddAfter() {

        String d = JsonDiff.diff("{a:1,b:{d:1}}", "{a:1,b:{d:2, e:3}}");

        String p = JsonPatch.apply("{a:1,b:{d:1}}", d);
        assertEquals("{\"a\":1,\"b\":{\"d\":2,\"e\":3}}", p);

    }


    @Test
    public void testNestedPartialRemove() {

        String d = JsonDiff.diff("{a:1,b:{c:1,d:1}}", "{a:1,b:{c:1}}");

        String p = JsonPatch.apply("{a:1,b:{c:1,d:1}}", d);
        assertEquals("{\"a\":1,\"b\":{\"c\":1}}", p);

    }


    @Test
    public void testNestedRemoveToEmpty() {

        String d = JsonDiff.diff("{a:1,b:{c:1,d:1}}", "{a:1,b:{}}");

        String p = JsonPatch.apply("{a:1,b:{c:1,d:1}}", d);
        assertEquals("{\"a\":1,\"b\":{}}", p);

    }


    @Test
    public void testNestedCompleteRemove() {

        String d = JsonDiff.diff("{a:1,b:{c:1,d:1}}", "{a:1}");

        String p = JsonPatch.apply("{a:1,b:{c:1,d:1}}", d);
        assertEquals("{\"a\":1}", p);

    }


    @Test
    public void testArrayAdd() {

        String d = JsonDiff.diff("{}", "{a:[1]}");

        String p = JsonPatch.apply("{}", d);
        assertEquals("{\"a\":[1]}", p);

    }


    @Test
    public void testArrayAddTwo() {

        String d = JsonDiff.diff("{}", "{a:[1,2]}");

        String p = JsonPatch.apply("{}", d);
        assertEquals("{\"a\":[1,2]}", p);

    }


    @Test
    public void testArrayAddToEmpty() {

        String d = JsonDiff.diff("{a: []}", "{a:[1]}");

        String p = JsonPatch.apply("{a: []}", d);
        assertEquals("{\"a\":[1]}", p);

    }


    @Test
    public void testArrayAddTwoToEmpty() {

        String d = JsonDiff.diff("{a: []}", "{a:[1,2]}");

        String p = JsonPatch.apply("{a: []}", d);
        assertEquals("{\"a\":[1,2]}", p);

    }


    @Test
    public void testArrayAddToExisting() {

        String d = JsonDiff.diff("{a: [0]}", "{a:[0,1]}");

        String p = JsonPatch.apply("{a: [0]}", d);
        assertEquals("{\"a\":[0,1]}", p);

    }


    @Test
    public void testArrayAddTwoToExisting() {

        String d = JsonDiff.diff("{a: [3]}", "{a:[1,2,3]}");

        String p = JsonPatch.apply("{a: [3]}", d);
        assertEquals("{\"a\":[1,2,3]}", p);

    }


    @Test
    public void testArrayAddTwoOtherToExisting() {

        String d = JsonDiff.diff("{a: [3,4,1,2]}", "{a:[1,2,5]}");

        String p = JsonPatch.apply("{a: [3,4,1,2]}", d);
        
        assertEquals("{\"a\":[1,2,5]}", p);

    }


    @Test
    public void testArrayInsertInExtisting() {

        String d = JsonDiff.diff("{a: [0,2,3,4]}", "{a:[0,1,2,3,4]}");

        String p = JsonPatch.apply("{a: [0,2,3,4]}", d);
        assertEquals("{\"a\":[0,1,2,3,4]}", p);

    }


    @Test
    public void testArrayInsertAfterDeleted() {

        String d = JsonDiff.diff("{a: [0,1,2,4]}", "{a:[0,1,3,4]}");

        String p = JsonPatch.apply("{a: [0,1,2,4]}", d);
        assertEquals("{\"a\":[0,1,3,4]}", p);

    }


    @Test
    public void testArrayInsertTwoAfterDeleted() {

        String d = JsonDiff.diff("{a: ['a','b','d']}", "{a:['a','c','e','d']}");

        String p = JsonPatch.apply("{a: ['a','b','d']}", d);
        assertEquals("{\"a\":[\"a\",\"c\",\"e\",\"d\"]}", p);

    }


    @Test
    public void testArrayRemoveAll() {

        String d = JsonDiff.diff("{a: [1,2]}", "{}");

        String p = JsonPatch.apply("{a: [1,2]}", d);
        assertEquals("{}", p);

    }


    @Test
    public void testArrayRemoveToEmpty() {

        String d = JsonDiff.diff("{a: [1]}", "{a:[]}");

        String p = JsonPatch.apply("{a: [1]}", d);
        assertEquals("{\"a\":[]}", p);

    }


    @Test
    public void testArrayRemoveLast() {

        String d = JsonDiff.diff("{a: [1,2]}", "{a:[1]}");

        String p = JsonPatch.apply("{a: [1,2]}", d);
        assertEquals("{\"a\":[1]}", p);

    }


    @Test
    public void testArrayRemoveFirst() {

        String d = JsonDiff.diff("{a: [1,2]}", "{a:[2]}");

        String p = JsonPatch.apply("{a: [1,2]}", d);
        assertEquals("{\"a\":[2]}", p);

    }


    @Test
    public void testArrayRemoveMiddle() {

        String d = JsonDiff.diff("{a: [1,2,3]}", "{a:[1,3]}");

        String p = JsonPatch.apply("{a: [1,2,3]}", d);
        assertEquals("{\"a\":[1,3]}", p);

    }


    @Test
    public void testArrayRemoveMultiple() {

        String d = JsonDiff.diff("{a: [1,2,3,4]}", "{a:[1,3]}");

        String p = JsonPatch.apply("{a: [1,2,3,4]}", d);
        assertEquals("{\"a\":[1,3]}", p);

    }


    @Test
    public void testArrayAddMultiDimensional() {

        String d = JsonDiff.diff("{a:[1]}", "{a:[1,[2,3]]}");

        String p = JsonPatch.apply("{a: [1]}", d);
        assertEquals("{\"a\":[1,[2,3]]}", p);

    }


    @Test
    public void testArrayRemoveInMulti() {

        String d = JsonDiff.diff("{a:[1,[2,3]]}", "{a:[1,[3]]}");

        String p = JsonPatch.apply("{a:[1,[2,3]]}", d);
        assertEquals("{\"a\":[1,[3]]}", p);

    }


    @Test
    public void testArrayRemoveLastInMulti() {

        String d = JsonDiff.diff("{a:[1,[2,3]]}", "{a:[1,[2]]}");

        String p = JsonPatch.apply("{a:[1,[2,3]]}", d);
        assertEquals("{\"a\":[1,[2]]}", p);

    }


    @Test
    public void testArrayInsertInMulti() {

        String d = JsonDiff.diff("{a:[1,[2,4]]}", "{a:[1,[2,3,4]]}");

        String p = JsonPatch.apply("{a:[1,[2,4]]}", d);
        assertEquals("{\"a\":[1,[2,3,4]]}", p);

    }


    @Test
    public void testAddObjectToArray() {

        String d = JsonDiff.diff("{a:[1]}", "{a:[1,{b:2}]}");

        String p = JsonPatch.apply("{a:[1]}", d);
        assertEquals("{\"a\":[1,{\"b\":2}]}", p);

    }


    @Test
    public void testMergeObjectInArray() {

        String d = JsonDiff.diff("{a:[1,{}]}", "{a:[1,{b:2}]}");
        
        String p = JsonPatch.apply("{a:[1,{}]}", d);
        assertEquals("{\"a\":[1,{\"b\":2}]}", p);

    }


    @Test
    public void testInsertInArrayInObjectInArray() {

        String d = JsonDiff.diff("{a:[1,{b:[]}]}", "{a:[1,{b:[2]}]}");

        String p = JsonPatch.apply("{a:[1,{b:[]}]}", d);
        assertEquals("{\"a\":[1,{\"b\":[2]}]}", p);

    }


    @Test
    public void testRemoveFromArrayInObjectInArray() {

        String d = JsonDiff.diff("{a:[1,{b:[2,3]}]}", "{a:[1,{b:[]}]}");

        String p = JsonPatch.apply("{a:[1,{b:[2,3]}]}", d);
        assertEquals("{\"a\":[1,{\"b\":[]}]}", p);

    }


    @Test
    public void testRemoveChange() {

        String d = JsonDiff.diff("{a: 1, b: 2, c: 3}", "{b:4}");

        String p = JsonPatch.apply("{a: 1, b: 2, c: 3}", d);
        assertEquals("{\"b\":4}", p);

    }


    @Test
    public void testArrToNumeric() {

        String d = JsonDiff.diff("{a: [1,2]}", "{a: 1}");

        String p = JsonPatch.apply("{a: [1,2]}", d);
        assertEquals("{\"a\":1}", p);

    }


    @Test
    public void testMix() {

        String d = JsonDiff.diff("{a: [1,2], b: { foo: 'b'}, c: 42}", "{a: 1, b: {foo: 'b', bar: 42}, c: 45}");

        String p = JsonPatch.apply("{a: [1,2], b: { foo: 'b'}, c: 42}", d);
        assertEquals("{\"a\":1,\"b\":{\"foo\":\"b\",\"bar\":42},\"c\":45}", p);

    }


    @Test
    public void testArrayObjectsChange() {
        String from = "{a:[{b:1}]}";
        String to = "{\"a\":[{\"c\":1}]}";

        String d = JsonDiff.diff(from, to);

        String p = JsonPatch.apply(from, d);
        assertEquals(to, p);

    }


    @Test
    public void testArrayStringsRotateLeft() {
        String from = "{\"a\":[\"s1\",\"s2\",\"s3\"]}";
        String to = "{\"a\":[\"s2\",\"s3\",\"s4\"]}";

        String diff = JsonDiff.diff(from, to);

        String p = JsonPatch.apply(from, diff);
        assertEquals(to, p);

    }


    // Bug #1 thanks to deverton
    @Test
    public void testArrayObjectsRotateLeft() {
        String from = "{\"a\":[{\"b\":1},{\"c\":2},{\"d\":3}]}";
        String to = "{\"a\":[{\"c\":2},{\"d\":3},{\"e\":4}]}";

        String d = JsonDiff.diff(from, to);

        String p = JsonPatch.apply(from, d);
        assertEquals(to, p);

    }


    // Bug #1, thanks to deverton
    @Test
    public void testArrayObjectsRotateRight() {

        String from = "{\"a\":[{\"c\":2},{\"d\":3},{\"e\":4}]}";
        String to = "{\"a\":[{\"b\":1},{\"c\":2},{\"d\":3}]}";

        String d = JsonDiff.diff(from, to);

        String p = JsonPatch.apply(from, d);
        assertEquals(to, p);
    }


    @Test
    public void testObjectObjectsRotateLeft() {

        String from = "{\"a1\":{\"c\":2},\"a2\":{\"d\":3},\"a3\":{\"e\":4}}";
        String to = "{\"a1\":{\"b\":1},\"a2\":{\"c\":2},\"a3\":{\"d\":3}}";

        String d = JsonDiff.diff(from, to);

        String p = JsonPatch.apply(from, d);
        assertEquals(to, p);

    }


    // Bug #2, thanks to deverton
    @Test
    public void testArrayObjectsChangeField() {
        String from = "{\"a\":[{\"c\":2,\"d\":3},{\"c\":2,\"d\":3},{\"c\":2,\"d\":3},{\"c\":2,\"d\":3}]}";
        String to = "{\"a\":[{\"c\":2,\"d\":4},{\"c\":2,\"d\":5},{\"c\":2,\"d\":3},{\"c\":2,\"d\":6}]}";

        String d = JsonDiff.diff(from, to);

        String p = JsonPatch.apply(from, d);
        assertEquals(to, p);
    }


    // Bug #3, thanks to deverton
    @Test
    public void testArrayObjectsWithNullAndChanges() {

        String from = "{\"a\":[{\"c\":2,\"d\":3},null,{\"c\":2,\"d\":3}]}";
        String to = "{\"a\":[{\"c\":2,\"d\":3},{\"c\":2,\"d\":7},42]}";

        String d = JsonDiff.diff(from, to);

        String p = JsonPatch.apply(from, d);
        assertEquals(to, p);

    }


    // Issue #5
    @Test
    public void testArrayObjectsRemoveAfterMultipleAdd() {

        String from = "{a:[{c:0},{c:1},{c:2},{c:3},{c:4}]}";
        String to = "{\"a\":[{\"e\":0},{\"c\":0},{\"c\":2},{\"e\":2},{\"c\":3,\"d\":3},{\"e\":3}]}";

    	String d = JsonDiff.diff(from, to);

        String p = JsonPatch.apply(from, d);
        assertEquals(" Patch is " + d, to, p);

    }


    @Test
    public void testArrayObjectsRemoveAfterAdd() {

        String from = "{a:[{c:1},{c:3},{c:4}]}";
        String to = "{\"a\":[{\"e\":2},{\"c\":3,\"d\":3},{\"e\":3}]}";

        String d = JsonDiff.diff(from, to);

        String p = JsonPatch.apply(from, d);
        assertEquals(to, p);

    }


    // Issue #7, thanks to DrLansing
    @Test
    public void testEndlessLoopInCompareArrays() {

        String from = "{\"offset\":\"PT0S\",\"reference\":\"Today\",\"referenceTimeList\":[{\"name\":\"Yesterday\",\"start\":\"Unknown\"},{\"name\":\"Today\",\"offset\":\"P1D\",\"reference\":\"Yesterday\"}]}";
        String to = "{\"offset\":\"PT0S\",\"reference\":\"Today\",\"referenceTimeList\":[{\"name\":\"Today\",\"start\":\"2010-10-11T17:51:52.204Z\"}]}";

        String d = JsonDiff.diff(from, to);

        String p = JsonPatch.apply(from, d);
        assertEquals(to, p);

    }


    // Issue #9, thanks to DrLansing
    @Test
    public void testAdjustArrayMutationBoundariesWithObjectDeletion() {

        String from = "{\"a\":[{\"b\":{\"id\":\"id1\"}},{\"b\":{\"id\":\"id2\"}}]}";
        String to = "{\"a\":[{\"b\":{\"id\":\"id2\"}}]}";

        String d = JsonDiff.diff(from, to);

        String p = JsonPatch.apply(from, d);
        assertEquals(to, p);

    }


    // First find while debugging issue #9.
    @Test
    public void testAdjustArrayMutationBoundariesWithObjectDeletionInside() {

        String from = "{\"a\":[{\"b\":{\"id\":\"id1\"}},{\"b\":{ab:{},ac:null},\"id\":\"id2\"}]}";
        String to = "{\"a\":[{\"b\":{\"id\":\"id2\",\"ac\":\"123\"}}]}";

        String d = JsonDiff.diff(from, to);

        String p = JsonPatch.apply(from, d);
        assertEquals(to, p);

    }


    // Second find while debugging issue #9.
    @Test
    public void testModifyWhileDeletingPreviousElement1() {

        // in this case k comes after i which means the end result
        // is to delete a[0] and modify ~a[1]
        String from = "{\"a\":[{\"id\":1,\"k\":0},{\"id\":2,\"k\":1}]}";
        String to = "{\"a\":[{\"id\":2,\"k\":2}]}";

        String d = JsonDiff.diff(from, to);

        String p = JsonPatch.apply(from, d);
        assertEquals(to, p);

    }


    // Third find while debugging issue #9.
    @Test
    public void testModifyWhileDeletingPreviousElement2() {

        // in this case b comes before i which means the end result
        // is to delete a[1] and modify ~a[0]
        String from = "{\"a\":[{\"b\":0,\"id\":1},{\"b\":1,\"id\":2}]}";
        String to = "{\"a\":[{\"b\":2,\"id\":2}]}";

        String d = JsonDiff.diff(from, to);

        String p = JsonPatch.apply(from, d);
        assertEquals(to, p);

    }


    // Issue #9, thanks to DrLansing
    @Test
    public void testIssue9DiffPatch1() {

        String from = "{\"p:timeFrame\":{\"g:id\":\"ID_1bi4uybddb9711i1ih4o3qqwml\",\"g:relatedTime\":[{\"relativePosition\":\"Contains\",\"g:TimeInstant\":{\"g:id\":\"ID_2odwwe6m4fhj1i7uqavgp4mpv\",\"g:identifier\":{\"codeSpace\":\"JP1_02\",\"text\":\"C-day\"},\"g:timePosition\":{\"indeterminatePosition\":\"unknown\"}}},{\"relativePosition\":\"Contains\",\"g:TimeInstant\":{\"g:id\":\"ID_1gu4yx14on411od9yrrwbraia\",\"g:identifier\":{\"codeSpace\":\"JP1_02\",\"text\":\"D-day\"},\"g:relatedTime\":{\"relativePosition\":\"MetBy\",\"g:TimePeriod\":{\"g:id\":\"ID_158yq5cp2z5lm1nugs6byvrjve\",\"g:begin\":{\"x:href\":\"ID_2odwwe6m4fhj1i7uqavgp4mpv\",\"x:title\":\"C-day\"},\"g:end\":{\"nilReason\":\"Unknown\"},\"g:duration\":\"P10D\"}},\"g:timePosition\":{\"indeterminatePosition\":\"unknown\"}}},{\"relativePosition\":\"MetBy\",\"g:TimePeriod\":{\"g:id\":\"ID_190iv1hlow39r1c0gam6p8h02k\",\"g:begin\":{\"x:href\":\"ID_1gu4yx14on411od9yrrwbraia\",\"x:title\":\"D-day\"},\"g:end\":{\"nilReason\":\"Unknown\"},\"g:duration\":\"PT0S\"}}],\"g:beginPosition\":{\"indeterminatePosition\":\"unknown\"},\"g:endPosition\":{\"indeterminatePosition\":\"unknown\"}}}";
        String to = "{\"p:timeFrame\":{\"g:id\":\"ID_1bi4uybddb9711i1ih4o3qqwml\",\"g:relatedTime\":[{\"relativePosition\":\"Contains\",\"g:TimeInstant\":{\"g:id\":\"ID_1gu4yx14on411od9yrrwbraia\",\"g:identifier\":{\"codeSpace\":\"JP1_02\",\"text\":\"D-day\"},\"g:timePosition\":\"2010-10-11T17:51:52.204Z\"}},{\"relativePosition\":\"MetBy\",\"g:TimePeriod\":{\"g:id\":\"ID_190iv1hlow39r1c0gam6p8h02k\",\"g:begin\":{\"x:href\":\"ID_1gu4yx14on411od9yrrwbraia\",\"x:title\":\"D-day\"},\"g:end\":{\"nilReason\":\"Unknown\"},\"g:duration\":\"PT0S\"}}],\"g:beginPosition\":{\"indeterminatePosition\":\"unknown\"},\"g:endPosition\":{\"indeterminatePosition\":\"unknown\"}}}";

        String d = JsonDiff.diff(from, to);
        String p = JsonPatch.apply(from, d);

        assertEquals(to, p);

    }


    @Test
    public void testChangeArrayToObject() {

        String from = "{\"b\":[1,2]}";
        String to = "{\"b\":{\"id\":\"id2\"}}";

        String d = JsonDiff.diff(from, to);

        String p = JsonPatch.apply(from, d);
        assertEquals(to, p);

    }


    @Test
    public void testChangeObjectToArray() {

        String from = "{\"b\":{\"id\":\"id2\"}}";
        String to = "{\"b\":[1,2]}";

        String d = JsonDiff.diff(from, to);

        String p = JsonPatch.apply(from, d);
        assertEquals(to, p);

    }


    @Test
    public void testAdjustArrayMutationNestedArrayToObject() {

        String from = "{\"a\":[{\"b\":[1,2]},{\"b\":[1,2]}]}";
        String to = "{\"a\":[{\"b\":{\"id\":\"id2\"}}]}";

        String d = JsonDiff.diff(from, to);

        String p = JsonPatch.apply(from, d);
        assertEquals(to, p);

    }
    
    // Issue #13. Thanks to Daniel Gardner for reporting.
    @Test
    public void testSameEndValueTwoConsequtiveArrayElements() {
    	
    	String i = "{\"a\":[{\"b\":[1],\"c\":\"x\"}]}";

    	String j1 = "{\"a\":[{\"b\":[1],\"c\":\"x\"},{\"b\":[1],\"c\":\"x\"}]}";

        String d = JsonDiff.diff(i, j1);

        
        String p = JsonPatch.apply(i, d);
        
        assertEquals(j1, p);
        
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

		String d = JsonDiff.diff(j1, j2);

		String p = JsonPatch.apply(j1, d);

		assertEquals(j2, p);

	}
	
	// issue #12. thanks to nachogmd for test and solution.
	@Test
	public void testArrayChangeToNull() {		
		try {
		    String from,to;
		    from = "{\"externalIds\":[{\"id\":\"4066-b329\"}],\"relation\":[{\"rules\":{\"keys\":[{\"shareHolders\":[{\"percentage\":0.0}]}]}}]}";
		    to =   "{\"externalIds\":[{\"id\":\"4066-b329\"}]}";
	
		    JsonDiff.diff(from, to);
		    from = "{\"externalIds\":[{\"id\":\"4066-b329\"}],\"catalogueRelationList\":[{\"rules\":{\"keys\":[{\"shareHolders\":[{\"percentage\":0.0}]}]}}]}";
		    to =   "{\"externalIds\":[{\"id\":\"4066-b329\"}]}";
		    JsonDiff.diff(from, to);
	
		    String diff = "{\"-catalogueRelationList\":0}";
	
		    String d = JsonDiff.diff(from, to);
	
		    String p = JsonPatch.apply(from, diff);
		    assertEquals(to, p);
		} catch (NullPointerException npe) {
			fail("Caught NPE");
		}
	}
	
	@Override
	public String getModuleName() {
		return "foodev.JsonDiff";
	}
}
