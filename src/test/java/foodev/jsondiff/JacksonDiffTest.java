package foodev.jsondiff;

public class JacksonDiffTest extends JsonDiffTestMethods {

	JsonDiff diff = new JacksonDiff();
	

	@Override
	String diff(String s1, String s2) {
		return diff.diff(s1, s2);
	}

	@Override
	String apply(String s1, String s2) {
		return diff.apply(s1, s2);
	}

}
