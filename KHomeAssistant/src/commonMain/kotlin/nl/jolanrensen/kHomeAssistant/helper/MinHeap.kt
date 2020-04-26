package nl.jolanrensen.kHomeAssistant.helper

import com.soywiz.kmem.toIntFloor

/**
 * Algorithms by Tom Peters
 */

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalStdlibApi::class)
class MinHeap<E : Comparable<E>> : MutableCollection<E> {

    var heap: Array<E?>
    override var size: Int

    constructor(initSize: Int) {
        heap = arrayOfNulls<Comparable<E>>(initSize) as Array<E?>
        size = 0
    }

    constructor(otherHeap: MinHeap<E>) {
        heap = otherHeap.heap.copyOf()
        size = otherHeap.size
    }

    private fun parent(i: Int) = ((i - 1) / 2.0).toIntFloor()

    /** Swap 2 elements in the heap. */
    fun swap(a: Int, b: Int) {
        val temp = heap[a]
        heap[a] = heap[b]
        heap[b] = temp
    }

    /**
     * Heapify the entire minHeap.
     */
    fun heapify() {
        var end = 1
        while (end < size) {
            siftUp(0, end)
            end++
        }
    }

    /**
     * Sift a node up to its correct place
     * @param start sift up to max this index
     * @param end the node to sift up
     */
    fun siftUp(start: Int, end: Int) {
        var child = end
        while (child > start) {
            val parent = parent(child)
            if (heap[parent]!! > heap[child]!!) {
                swap(parent, child)
                child = parent
            } else return
        }
    }

    /** Get the element currently at the root without removing it. */
    fun peekMin(): E? = heap[0]

    /**
     * Remove and get the element currently at the root while fixing the heap.
     * @return the element formerly at the root
     * @throws NullPointerException if heap.isEmpty()
     * */
    fun extractMin(): E {
        val min = peekMin()!!
        heap[0] = heap[size - 1]
        size--
        heapify()
        return min
    }

    /** Insert an element in the heap. */
    fun insert(element: E) {
        // check if heap is big enough and possibly extend it
        if (size == heap.size)
            heap = heap.copyOf(newSize = size + 1)

        // insert
        heap[size] = element
        size++

        // and fix heap
        heapify()
    }


    fun asSortedArray(): Array<E> {
        val copy = copyOf()
        return Array<Comparable<E>>(copy.size) { copy.extractMin() } as Array<E>
    }

    override fun add(element: E): Boolean {
        insert(element)
        return true
    }

    override fun addAll(elements: Collection<E>): Boolean {
        elements.forEach { insert(it) }
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

    override fun iterator() = object : MutableIterator<E> {
        var i = 0

        override fun hasNext() = i < size - 1

        override fun next(): E = heap[++i]!!

        override fun remove() {
            heap[i] = null
            size--
            heapify()
        }

    }

    override fun toString() = "MinHeap: " + heap.map { it.toString() }.toString()

    fun copyOf() = MinHeap(this)
}


fun <E : Comparable<E>> minHeapOf(elements: Collection<E>) =
    MinHeap<E>(elements.size).also { it += elements }

fun <E : Comparable<E>> minHeapOf(vararg elements: E) =
    MinHeap<E>(elements.size).also { it += elements }