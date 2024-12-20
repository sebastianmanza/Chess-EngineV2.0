public interface ListADT {
   
    /**
     * Add an item to the end of the list.
     * @param val the item to add
     */
    public void append(T val);

    /**
     * Add an item to the beginning of the list
     * @param val
     */
    public void addFront(T val);

    /**
     * Remove and return the first instance of an item in the list.
     * @param val the item to remove.
     * @return the item removed or null if item was not located
     */
    public T remove(T val);
    
    /**
     * Check if the list contains an item.
     * @param val the item to check for
     * @return true if it does, else false.
     */
    public boolean hasItem(T val);

    /**
     * Get the size of the list.
     * @return an integer representing the number of elements.
     */
    public int size();

    /**
     * Get an iterator for the list.
     * @return an iterator.
     */
    public Iterator listIterator();
}