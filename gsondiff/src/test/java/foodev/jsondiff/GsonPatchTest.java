package foodev.jsondiff;

public class GsonPatchTest extends JsonPatchTestMethods {

	GsonDiff diff = new GsonDiff();
	
	@Override
	String apply(String s1, String s2) {
		return diff.apply(s1, s2);
	}

}
