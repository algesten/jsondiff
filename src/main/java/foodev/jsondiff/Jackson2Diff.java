package foodev.jsondiff;

import foodev.jsondiff.jsonwrap.jackson2.Jackson2Wrapper;

public class Jackson2Diff extends JsonDiff {

	public Jackson2Diff() {
		super(new Jackson2Wrapper());
	}

}
