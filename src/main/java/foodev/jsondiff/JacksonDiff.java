package foodev.jsondiff;

import foodev.jsondiff.jsonwrap.jackson.JacksonWrapper;

public class JacksonDiff extends JsonDiff {

	public JacksonDiff() {
		super(new JacksonWrapper());
	}

}
