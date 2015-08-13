package foodev.jsondiff;

public class Jackson2PatchTest extends JsonPatchTestMethods {

	JsonDiff diff = new Jackson2Diff();
	
	@Override
	String apply(String s1, String s2) {
		return diff.apply(s1, s2);
	}

}
