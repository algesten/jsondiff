package foodev.jsondiff;

import com.google.gson.JsonElement;

import foodev.jsondiff.jsonwrap.JzonElement;

public class GsonVisitor implements Visitor {

	@Override
	public boolean shouldCreatePatch(JzonElement fromNative, JzonElement toNative) {
		JsonElement from = (JsonElement) fromNative.unwrap();
		JsonElement to = (JsonElement) toNative.unwrap();
		if (from.isJsonObject() && to.isJsonObject()) {
			JsonElement fromId = from.getAsJsonObject().get("id");
			JsonElement toId = to.getAsJsonObject().get("id");
			if (fromId != null && toId != null && fromId.equals(toId)) {
				return false;
			}
		}
		return true;
	}

}
