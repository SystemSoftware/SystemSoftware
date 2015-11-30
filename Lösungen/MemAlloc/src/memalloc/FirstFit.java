/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package memalloc;

import java.util.AbstractCollection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.TreeSet;

/**
 *
 * @author IronFox
 */
public class FirstFit implements MemorySystem
{
    public class Chunk extends MemorySystem.Chunk implements Comparable
    {
	public int		allocated = 0;
	public final int	size;
	public final boolean	reverseOrdered;
	
	public Chunk(int size)
	{
	    this.size = size;
	    this.reverseOrdered = strategy == Strategy.DecreasingSize;
	}

	@Override
	public int compareTo(Object o)
	{
	    if (!(o instanceof Chunk))
		return 0;
	    Chunk other = (Chunk)o;
	    if (size < other.size)
		return reverseOrdered ? 1 : -1;
	    if (size > other.size)
		return reverseOrdered ? -1 : 1;
	    return 0;
	}
    }
    
    private final ArrayList<Chunk> sizeOrdered = new ArrayList<>();
    private final ArrayDeque<Chunk> useOrdered = new ArrayDeque<>();
    private final HashSet<Chunk> allAllocated = new HashSet<Chunk>();
    //private ArrayList<Chunk>    freeChunks = new ArrayList<Chunk>();
    
    public final Strategy strategy;
    public final int totalBytes;
    
    public final float splitThreshold = 0.9f;
    
    public FirstFit(int sizeExponent, Strategy s)
    {
	totalBytes = 1<<sizeExponent;
	Chunk c = new Chunk(totalBytes);
	strategy = s;
	if (isUseOrdered())
	    useOrdered.add(c);
	else
	    sizeOrdered.add(c);
    }
    
    void checkSanity() throws Exception
    {
	return;
	/*
	int allocated = getAllocatedBytes();
	int free = getFreeBytes();
	int total = getTotalBytes();
	if (total != allocated + free)
	{
	    throw new Exception(allocated+"+"+free+"!="+total);
	}
	*/
	
    }
    
    public enum Strategy
    {
	IncreasingSize,
	DecreasingSize,
	IncreasingTimeSinceLastUse,
    }
    
    public boolean isUseOrdered()
    {
	return strategy == Strategy.IncreasingTimeSinceLastUse;
    }
    private AbstractCollection<Chunk> getCollection()
    {
	return	isUseOrdered() ? 
		useOrdered
		:
		sizeOrdered;
    }
    
    @Override
    public Chunk allocate(int bytes) throws Exception
    {
	AbstractCollection<Chunk> collection = getCollection();
	
	for (Chunk c: collection)
	{
	    if (bytes <= c.size)
	    {
		checkSanity();
		collection.remove(c);
		
		if (bytes >= c.size * splitThreshold)
		{
		    c.allocated = bytes;
		    allAllocated.add(c);
		    checkSanity();
		    return c;
		}
		Chunk a = new Chunk(bytes);
		a.allocated = bytes;
		allAllocated.add(a);
		Chunk b = new Chunk(c.size - bytes);
		insert(b);
		checkSanity();
		return a;
	    }
	}
	return null;
    }
    
    private void insert(Chunk c) throws Exception
    {
	if (isUseOrdered())
	    useOrdered.addFirst(c);
	else
	{
	    sizeOrdered.add(c);
	    Collections.sort(sizeOrdered);
	}
	if (!getCollection().contains(c))
	    throw new Exception("Internal validation failed");
	if (allAllocated.contains(c))
	    throw new Exception("Internal validation failed");
    }

    @Override
    public void free(MemorySystem.Chunk chunk) throws Exception
    {
	if (chunk == null)
	    return;
	allAllocated.remove((Chunk)chunk);
	insert((Chunk)chunk);
	checkSanity();
    }

    @Override
    public int getFreeBytes()
    {
	int rs = 0;
	for (Chunk c: getCollection())
	{
	    rs += c.size;
	}
	return rs;
    }
    
    private int getAllocatedBytes()
    {
	int rs = 0;
	for (Chunk c: allAllocated)
	{
	    rs += c.size;
	}
	return rs;
    }

    @Override
    public String toString()
    {
	return getClass().getName() + "("+strategy+")";
    }
    
    @Override
    public int getInternalFragmentationLostBytes()
    {
	int rs = 0;
	for (Chunk c: allAllocated)
	{
	    rs += c.size - c.allocated;
	}
	return rs;
    }

    @Override
    public int getExternalFragmentationLostBytesFor(int chunkByteSize)
    {
	int rs = 0;
	for (Chunk c: getCollection())
	{
	    if (c.size < chunkByteSize)
		rs += c.size;
	}
	return rs;
    }

    @Override
    public int getLargestFreeChunk()
    {
	int rs = 0;
	for (Chunk c: getCollection())
	{
	    if (c.size > rs)
		rs = c.size;
	}
	return rs;
    }

    @Override
    public int getTotalBytes()
    {
	return totalBytes;
    }

    @Override
    public int getStructureComplexity()
    {
	return getCollection().size();
    }
    
}
