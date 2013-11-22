package foodev.jsondiff;

class Root extends Node {

	Root() {
		super(null);
	}

	@Override
	protected Node clone() {
		return this;
	}

	@Override
	int doHash(boolean indexed) {
		return 0;
	}

	@Override
	public String toString() {
		return "root";
	}
}