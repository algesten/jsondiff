package foodev.jsondiff;

import foodev.jsondiff.jsonwrap.JzonElement;

/**
 * Interface that allows filtering patch instructions.
 * 
 * @since 2.0.0
 * @version @PROJECT_VERSION@
 */
public interface Visitor {

	/**
	 * Should a patch instruction be created for an element like <code>to</code> if its destiny is an element like <code>to</code>?
	 * 
	 * @param from
	 *            - from element
	 * @param to
	 *            - to element
	 * @return if the instruction should be created
	 */
	boolean shouldCreatePatch(JzonElement from, JzonElement to);
}
