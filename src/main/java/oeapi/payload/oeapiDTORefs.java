package oeapi.payload;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Keeps the two halves of an expandable relation in step.
 *
 * <p>Every expandable relation is declared twice on a DTO: the id form
 * ({@code parentId}, {@code childrenIds}, {@code coordinatorIds}, {@code programIds}), which
 * is what the wire format carries, and the object form, which {@code ?expand=...} promotes
 * into its place. Both have a setter, ModelMapper calls them in an order nobody controls, and
 * each one has to fill in its counterpart WITHOUT destroying a better value the other may
 * already have written.</p>
 *
 * <p>That is the bug these helpers exist to prevent. An id setter that unconditionally
 * replaces the object with a reference stub reduces {@code ?expand=...} to a bare id whenever
 * it happens to run last. Building a stub is right on input - the client sent an id and
 * nothing else - and wrong on a read, where the mapper has already supplied the real object.
 * The id itself is what tells the two cases apart: if the object in hand is the object for
 * that id, there is nothing to build.</p>
 *
 * <p>The same slip appeared three times in this codebase written as {@code !=} on Strings,
 * which compares references and is therefore always true. Hence one helper rather than a
 * fourth hand-written guard.</p>
 */
final class oeapiDTORefs {

    private oeapiDTORefs() {
    }

    /**
     * The reference to keep for a to-one relation: the one already held when it is the object
     * for this id, a fresh stub otherwise, null when the id is null.
     *
     * @param current the reference currently on the DTO, may be null
     * @param id      the id just set
     * @param idOf    reads the id of a reference
     * @param stubOf  builds an id-only reference
     */
    static <E> E keepOrStub(E current, String id, Function<E, String> idOf, Function<String, E> stubOf) {

        if (id == null) {
            return null;
        }

        if (current != null && id.equals(idOf.apply(current))) {
            return current;
        }

        return stubOf.apply(id);
    }

    /**
     * The to-many form. The list already held is kept only when it is the objects for exactly
     * these ids, in this order; otherwise every element becomes a stub. Reusing part of the
     * list is deliberately not attempted: the ids are the input, so a list that does not match
     * them wholesale has nothing to contribute.
     */
    static <E> List<E> keepOrStub(List<E> current, List<String> ids, Function<E, String> idOf, Function<String, E> stubOf) {

        if (ids == null) {
            return null;
        }

        if (matches(current, ids, idOf)) {
            return current;
        }

        List<E> stubs = new ArrayList<>(ids.size());

        for (String id : ids) {
            stubs.add(id == null ? null : stubOf.apply(id));
        }

        return stubs;
    }

    /**
     * The ids of a list of references, for an object setter to keep its id counterpart in
     * sync. Null in, null out, so that an absent relation stays absent rather than becoming
     * an empty array.
     */
    static <E> List<String> idsOf(List<E> items, Function<E, String> idOf) {

        if (items == null) {
            return null;
        }

        List<String> ids = new ArrayList<>(items.size());

        for (E item : items) {
            ids.add(item == null ? null : idOf.apply(item));
        }

        return ids;
    }

    private static <E> boolean matches(List<E> current, List<String> ids, Function<E, String> idOf) {

        if (current == null || current.size() != ids.size()) {
            return false;
        }

        for (int i = 0; i < ids.size(); i++) {
            E item = current.get(i);
            String id = ids.get(i);

            if (item == null || id == null || !id.equals(idOf.apply(item))) {
                return false;
            }
        }

        return true;
    }
}
