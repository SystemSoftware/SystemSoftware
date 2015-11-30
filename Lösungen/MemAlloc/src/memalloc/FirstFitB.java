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
public class FirstFitB implements MemorySystem
{
    public static class Chunk extends MemorySystem.Chunk
    {
        public int byteSize = 0;
        Chunk   next,prev;
        boolean freeChunk = true;
        
        public Chunk(int byteSize, Chunk prev, Chunk next)
        {
            this.prev = prev;
            this.next = next;
            this.byteSize = byteSize;
        }
        
        
        public Chunk walkSplit(int size, Chunk pivot)
        {
            if (this == pivot)
                return null;
            if (!freeChunk || size > byteSize)
            {
                return next.walkSplit(size, pivot);
            }
            if (size == byteSize)
            {
                freeChunk = false;
                return this;
            }
            Chunk allocated = new Chunk(size,prev,this);
            allocated.freeChunk = false;
            prev.next = allocated;
            prev = allocated;
            byteSize -= size;
            return allocated;
        }
        
        public void free(Chunk pivot)
        {
            freeChunk = true;
            if (prev.freeChunk && prev != pivot)
            {
                byteSize += prev.byteSize;
                prev.prev.next = this;
                prev = prev.prev;
            }
            
            if (next.freeChunk && next != pivot)
            {
                byteSize += next.byteSize;
                next.next.prev = this;
                next = next.next;
            }
        }
        
        
    }
    
    Chunk   pivot = new Chunk(0,null,null);
    
    
    public FirstFitB(int sizeExponent)
    {
        Chunk remainder = new Chunk(1<<sizeExponent,pivot,pivot);
        pivot.next = remainder;
        pivot.prev = remainder;
    }
    
    @Override
    public Chunk allocate(int bytes)
    {
        return pivot.next.walkSplit(bytes,pivot);
    }

    @Override
    public void free(MemorySystem.Chunk chunk)
    {
	if (chunk != null)
            ((Chunk)chunk).free(pivot);
    }

    @Override
    public int getFreeBytes() {
        Chunk ch = pivot.next;
        int rs = 0;
        while (ch != pivot)
        {
            if (ch.freeChunk)
                rs += ch.byteSize;
            ch = ch.next;
        }
        return rs;
    }

    @Override
    public int getTotalBytes()
    {
        return 1<<18;
    }

    @Override
    public int getInternalFragmentationLostBytes()
    {
        return 0;
    }

    @Override
    public int getExternalFragmentationLostBytesFor(int chunkByteSize)
    {
        Chunk ch = pivot.next;
        int rs = 0;
        while (ch != pivot)
        {
            if (ch.freeChunk && ch.byteSize < chunkByteSize)
                rs += ch.byteSize;
            ch = ch.next;
        }
        return rs;
        
    }

    @Override
    public int getLargestFreeChunk()
    {
        Chunk ch = pivot.next;
        int rs = 0;
        while (ch != pivot)
        {
            if (ch.freeChunk)
                rs = Math.max(rs,ch.byteSize);
            ch = ch.next;
        }
        return rs;
    }

    @Override
    public int getStructureComplexity()
    {
        Chunk ch = pivot.next;
        int rs = 0;
        while (ch != pivot)
        {
            rs++;
            ch = ch.next;
        }
        return rs;
    }
    
}
