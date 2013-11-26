package foodev.jsondiff;

import com.google.gson.JsonElement;

public class GsonVisitor implements Visitor<JsonElement> {

	@Override
	public boolean shouldCreatePatch(JsonElement from, JsonElement to) {
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
