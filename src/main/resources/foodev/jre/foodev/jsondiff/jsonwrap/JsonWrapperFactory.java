package foodev.jsondiff.jsonwrap;

import foodev.jsondiff.jsonwrap.gwt.GWTWrapper;

/**
 * Internal wrapper helper crafter for GWT support only. JRE emulation layer will subsitute.
 * 
 * @author Martin Algesten
 * 
 */
public class JsonWrapperFactory {

	private final static Wrapper gwtWrapper = new GWTWrapper();

	public static JzonElement parse(String json, Object hint) {
		return selectWrapper(hint).parse(json);
	}

	public static JzonObject createJsonObject(Object hint) {
		return selectWrapper(hint).createJsonObject();
	}

	public static JzonArray createJsonArray(Object hint) {
		return selectWrapper(hint).createJsonArray();
	}

	public static JzonElement wrap(Object obj) {
		if (obj instanceof JzonElement) {
			return (JzonElement) obj;
		} else {
			return selectWrapper(obj).wrap(obj);
		}
	}

	private static Wrapper selectWrapper(Object hint) {
		return gwtWrapper;
	}

}
