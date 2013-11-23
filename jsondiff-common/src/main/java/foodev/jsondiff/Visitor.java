package foodev.jsondiff;

public interface Visitor<E> {

	boolean shouldCreatePatch(E from, E to);
}
