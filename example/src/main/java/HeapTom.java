public class HeapTom<E extends Comparable<E>> {

    E[] heap;
    int currentSize;

    public HeapTom(int initSize) {
        heap = (E[]) new Comparable[initSize];
        currentSize = 0;
    }

    private int left(int i) {
        return i * 2 + 1;
    }

    private int right(int i) {
        return i * 2 + 2;
    }

    /**
     * Swap 2 elements in the heap
     */
    private void swap(int a, int b) {
        E temp = heap[a];
        heap[a] = heap[b];
        heap[b] = temp;
    }

    /**
     * Heapify from node i, where i is index in heap (min heap)
     *
     * @param i start from this index in the heap
     */
    public void heapify(int i) {
        int smallest = i; // current smallest index init as root
        int left = left(i);
        int right = right(i);
        // check if one of the childs is smaller
        if (left < currentSize && heap[left].compareTo(heap[smallest]) < 0) {
            smallest = left;
        }
        if (right < currentSize && heap[right].compareTo(heap[smallest]) < 0) {
            smallest = right;
        }
        // swap if not smallest in the root already
        if (smallest != i) {
            swap(smallest, i);
            //recurse on that sub tree
            heapify(smallest);
        }
    }

    public E peekMin() {
        return heap[0];
    }

    public E extractMin() {
        E min = peekMin();
        heap[0] = heap[currentSize - 1];
        currentSize--;
        heapify(0);
        return min;
    }

    public void insert(E e) {
        // check if heap is big enough and possibly extend it
        if (currentSize == heap.length) {
            E[] biggerHeap = (E[]) new Comparable[currentSize + 1];
            System.arraycopy(heap, 0, biggerHeap, 0, currentSize);
            heap = biggerHeap;
        }
        // insert
        heap[currentSize] = e;
        currentSize++;
        // and fix heap
        heapify(0);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < currentSize; i++) {
            s.append(heap[i].toString());
            s.append(", ");
        }
        s.append("]");
        return s.toString();
    }

}