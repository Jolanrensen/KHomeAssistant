package nl.jolanrensen.kHomeAssistant.helper

/**
 * An implementation of a Priority Queue using a minheap.
 * Insertions and deletions can be done in `O(log n)`, searching in `O(n)` and peeking in `O(1)` time.
 * @param E the [Comparable]<E> type of the items in the heap.
 *
 * Algorithms by Tom Peters.
 */
class PriorityQueue<E : Comparable<E>> : MutableCollection<E> {

    /** The dataset, unsorted except for the root, which is at heap[0].] */
    private var heap: Array<E?>

    /** The number of elements in the MinHeap. */
    override var size: Int

    /**
     * Constructs a [PriorityQueue].
     * @param initSize the initial size of the Array backing the data of the heap.
     * */
    @Suppress("UNCHECKED_CAST")
    constructor(initSize: Int) {
        heap = arrayOfNulls<Comparable<*>>(initSize) as Array<E?>
        size = 0
    }

    /**
     * Constructs a [PriorityQueue] based on another [PriorityQueue].
     * @param otherQueue the other MinHeap to copy
     */
    constructor(otherQueue: PriorityQueue<E>) {
        heap = otherQueue.heap.copyOf()
        size = otherQueue.size
    }

    /** Returns the index of the parent of the node at index i.
     * @param i the index of the node of which the index of the parent should be returned */
    private fun parent(i: Int) = ((i - 1) / 2.0).toInt()

    /** Swap 2 elements in the heap. */
    private fun swap(a: Int, b: Int) {
        val temp = heap[a]
        heap[a] = heap[b]
        heap[b] = temp
    }

    /** Heapify the entire MinHeap such that the root is correct. */
    private fun heapify() {
        var end = 1
        while (end < size) {
            siftUp(0, end)
            end++
        }
    }

    /**
     * Sift a node up to its correct place.
     * @param start sift up to max this index
     * @param end the node to sift up
     */
    private fun siftUp(start: Int, end: Int) {
        var child = end
        while (child > start) {
            val parent = parent(child)
            if (heap[parent]!! > heap[child]!!) {
                swap(parent, child)
                child = parent
            } else return
        }
    }


    /** Get the element currently at the root without removing it.
     * @throws NullPointerException if [isEmpty] is true
     * @see next
     * */
    fun peek(): E = heap[0]!!

    /**
     * Remove and get the element currently at the root.
     * @return the element formerly at the root
     * @throws NullPointerException if [isEmpty] is true
     * @see extractNext
     * */
    fun poll(): E {
        val min = peek()
        heap[0] = heap[size - 1]
        size--
        heapify()
        return min
    }

    /** Get the element currently at the root without removing it.
     * @throws NullPointerException if [isEmpty] is true
     * @see peek
     * */
    val next: E get() = peek()

    /**
     * Remove and get the element currently at the root.
     * @return the element formerly at the root
     * @throws NullPointerException if [isEmpty] is true
     * @see poll
     */
    fun extractNext(): E = poll()

    /**
     * Insert an element in the queue.
     * @param element the element to be inserted into the queue
     * */
    fun push(element: E) {
        // check if heap is big enough and possibly extend it
        if (size == heap.size)
            heap = heap.copyOf(newSize = size + 1)

        // insert
        heap[size] = element
        size++

        // and fix heap
        heapify()
    }

    /**
     * @see push
     */
    override fun add(element: E): Boolean {
        push(element)
        return true
    }

    /**
     * @see push
     */
    override fun addAll(elements: Collection<E>): Boolean {
        elements.forEach { push(it) }
        return true
    }

    override fun contains(element: E) = element in heap

    override fun containsAll(elements: Collection<E>) = elements.all { it in heap }

    override fun isEmpty(): Boolean = size == 0

    override fun clear() {
        size = 0
        heap.fill(null)
    }

    override fun remove(element: E): Boolean {
        val index = heap.indexOf(element)
        if (index < 0) return false
        heap[index] = null
        size--
        heapify()
        return true
    }

    override fun removeAll(elements: Collection<E>) = elements.any { remove(it) }

    override fun retainAll(elements: Collection<E>) = removeAll(heap.filter { it !in elements })

    /** This iterator loops over the sorted queue. */
    override fun iterator() = object : MutableIterator<E> {
        private val copy = copyOf()
        private var lastItem: E? = null

        override fun hasNext() = copy.isNotEmpty()

        override fun next(): E = copy.poll().also { lastItem = it }

        override fun remove() {
            remove(lastItem)
        }
    }

    override fun toString() = "MinHeap: ${map { it.toString() }}"

    /** Returns a copy of this [PriorityQueue].
     * @return the copy of this [PriorityQueue]
     * */
    fun copyOf() = PriorityQueue(this)
}

/**
 * Helper function to create a [PriorityQueue] from all the elements in [elements].
 * @param E the [Comparable] type of the elements in the queue
 * @param elements the elements to be initially added to the queue
 * @return a new [PriorityQueue] containing the elements provided
 */
fun <E : Comparable<E>> priorityQueueOf(elements: Collection<E>) =
    PriorityQueue<E>(elements.size).also { it += elements }

/**
 * Helper function to create a [PriorityQueue] from all the elements in [elements].
 * @param E the [Comparable] type of the elements in the queue
 * @param elements the elements to be initially added to the queue
 * @return a new [PriorityQueue] containing the elements provided
 */
fun <E : Comparable<E>> priorityQueueOf(vararg elements: E) =
    PriorityQueue<E>(elements.size).also { it += elements }