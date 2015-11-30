/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package memalloc;

/**
 *
 * @author IronFox
 */
public interface MemorySystem
{
    public static class Chunk
    {}
    
    /**
     * Allocates the requested amount of bytes and returns a chunk reference to
     * it.
     * @param bytes Number of bytes requested. If zero or negative the method
     * should return null.
     * @return New chunk that represents the allocated data block, or null, if
     * allocation failed.
     */
    public Chunk allocate(int bytes) throws Exception;
    /**
     * Frees the allocated chunk and allows its memory section to be used for
     * subsequent allocations.
     * @param chunk 
     */
    public void free(Chunk chunk) throws Exception;
    
    /**
     * Retrieves the amount of allocatable bytes left
     * @return Number of bytes not currently allocated 
     */
    public int getFreeBytes();
    /**
     * Gets the amount of bytes lost due to internal fragmentation
     * @return Number of bytes not used, but also unavailable for allocation
     */
    public int getInternalFragmentationLostBytes();
    /**
     * Retrieves the amount of bytes lost for the allocation for a 
     * given request size
     * @param chunkByteSize Size of a single chunk for which the amount of
     * lost bytes should be calculated
     * @return  Number of bytes that can currently not be allocated if
     * the specified amount of memory were passed into allocate()
     */
    public int getExternalFragmentationLostBytesFor(int chunkByteSize);
    /**
     * Retrieves the size in bytes of the largest allocatable memory section.
     * @return Largest number of bytes that allocate() can currently handle
     */
    public int getLargestFreeChunk();
    
    public int getTotalBytes();
    public int getStructureComplexity();
}
