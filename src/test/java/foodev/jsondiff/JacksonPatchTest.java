package foodev.jsondiff;

public class JacksonPatchTest extends JsonPatchTestMethods {

	JsonDiff diff = new JacksonDiff();
	
	@Override
	String apply(String s1, String s2) {
		return diff.apply(s1, s2);
	}

}
