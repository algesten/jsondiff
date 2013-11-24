package foodev.jsondiff;

import foodev.jsondiff.jsonwrap.JzonElement;

public interface Visitor {

	boolean shouldCreatePatch(JzonElement from, JzonElement to);
}
