package foodev.jsondiff;

import org.junit.Test;

import junit.framework.TestCase;

public class GsonVisitorTest extends TestCase {

	GsonDiff diff = new GsonDiff();

	@Test
	public void test() {
		GsonVisitor visitor = new GsonVisitor();
		String from = "{\"relation\":{\"id\":1,\"name\":4}}";
		String to = "{\"relation\":{\"id\":1}}";
		String s = diff.diff(from, to);
		assertTrue(!s.equals("{}"));
		
		diff.setVisitor(visitor);
		s = diff.diff(from, to);

		assertTrue(s.equals("{}"));
	}

	@Test
	public void test2() {
		GsonVisitor visitor = new GsonVisitor();
		String from = "{\"a\":{\"relation\":{\"id\":1,\"name\":4}}}";
		String to = "{\"a\":{\"relation\":{\"id\":1}}}";
		String s = diff.diff(from, to);
		assertTrue(!s.equals("{}"));
		
		diff.setVisitor(visitor);
		s = diff.diff(from, to);
		assertTrue(s.equals("{}"));
	}

}
