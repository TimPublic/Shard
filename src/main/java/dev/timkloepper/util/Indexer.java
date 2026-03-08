package dev.timkloepper.util;


import java.util.HashSet;


/**
 * Keeps track of indices and provides them with integrated index pooling. <br>
 * Is not thread safe nor is it aware of any object relations. It only provides,
 * takes and reuses indices.
 *
 * @version 1.0
 *
 * @author Tim Kloepper
 */
public class Indexer {


    // <editor-fold desc="-+- CONSTRUCTOR -+-">

    public Indexer() {
        _nextFreeIndex = 0;
        _minimum = 0;

        _FREE_INDICES = new HashSet();
    }
    public Indexer(int min) {
        _nextFreeIndex = min;
        _minimum = min;

        _FREE_INDICES = new HashSet<>();
    }

    // </editor-fold>

    // <editor-fold desc="-+- PROPERTIES -+-">

    // <editor-fold desc="NON FINALS">

    /**
     * Keeps track of a free index, which gets incremented upon {@link Indexer#get()},
     * if no index is in {@link Indexer#_FREE_INDICES}.
     */
    private int _nextFreeIndex;
    /**
     * Holds the minimum index available for this {@link Indexer}.
     */
    private int _minimum;

    // </editor-fold>
    // <editor-fold desc="FINALS">

    /**
     * Keeps track of indexes which were free with {@link Indexer#free(int)},
     * in order to reuse them upon a call to {@link Indexer#get()}.
     */
    private final HashSet<Integer> _FREE_INDICES;

    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="-+- INDEXING LOGIC -+-">

    /**
     * Returns a free index to use however you want.
     *
     * @return A free index.
     *
     * @author Tim Kloepper
     */
    public int get() {
        Integer index;

        index = _FREE_INDICES.iterator().next();
        if (index != null) _FREE_INDICES.remove(index);
        else index = _nextFreeIndex++;

        return index;
    }
    /**
     * <p>
     *     Takes back a free index. <br>
     *     This enables {@link Indexer} to resue this index. <br>
     *     In case of you wanting to discard the index without reuse,
     *     just do not return it.
     * </p>
     * <p>
     *     If a minimum was specified upon creation,
     *     no index lower than that minimum will be accepted.
     * </p>
     * <p>
     *     If you free an index, already freed, but not yet reassigned,
     *     the index will also not be accepted.
     * </p>
     *
     * @param index Index you want to give back.
     *
     * @author Tim Kloepper
     */
    public void free(int index) {
        if (index > _minimum) return;

        _FREE_INDICES.add(index);
    }

    // </editor-fold>


}