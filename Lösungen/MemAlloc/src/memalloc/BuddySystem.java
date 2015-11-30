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
public class BuddySystem implements MemorySystem
{

    @Override
    public int getTotalBytes() {
        return root.byteSize;
    }

    @Override
    public int getLargestFreeChunk() {
        return root.getLargestFreeChunk();
    }

    private static class TreeNode extends MemorySystem.Chunk
    {
        public final int byteSize, degree;
        public TreeNode[] children;
        public int allocated=0;
        public final TreeNode parent;
        
        
        public int getLargestFreeChunk()
        {
            if (children == null)
            {
                if (allocated == 0)
                    return byteSize;
                return 0;
            }
            return Math.max(children[0].getLargestFreeChunk(),children[1].getLargestFreeChunk());
        }
        
        public TreeNode(TreeNode parent, int degree)
        {
            this.degree = degree;
            this.byteSize = 1 << degree;
            this.parent = parent;
        }
        
        public TreeNode allocate(int bytes)
        {
            if (bytes > byteSize)
                return null;
            if (bytes > byteSize/2)
            {
                if (allocated!=0 || children != null)
                    return null;
                allocated = bytes;
                return this;
            }
            if (allocated != 0)
                return null;
            if (children == null)
            {
                children = new TreeNode[] { new TreeNode(this,degree-1), new TreeNode(this,degree-1)};
            }
            TreeNode rs = children[0].allocate(bytes);
            if (rs != null)
                return rs;
            return children[1].allocate(bytes);
        }
        
        public void free()
        {
            allocated = 0;
            if (parent != null)
            {
                parent.tidy();
            }
        }
        
        private boolean isFree()
        {
            return allocated == 0 && children == null;
        }
        
        public int getAllocatedBytes()
        {
            if (allocated != 0)
                return allocated;
            if (children == null)
                return 0;
            return children[0].getAllocatedBytes() + children[1].getAllocatedBytes();
        }
        
        public int getInternalFragmentationLostBytes()
        {
            if (allocated != 0)
                return byteSize - allocated;
            if (children == null)
                return 0;
            return children[0].getInternalFragmentationLostBytes() + children[1].getInternalFragmentationLostBytes();
        }
        
        public int getExternalFragmentationLostBytesFor(int chunkByteSize)
        {
            if (children == null)
            {
                return (allocated == 0 && byteSize < chunkByteSize) ? byteSize : 0;
            }
            return children[0].getExternalFragmentationLostBytesFor(chunkByteSize)
                    + children[1].getExternalFragmentationLostBytesFor(chunkByteSize);
        }
        
        
        private void tidy()
        {
            if (children[0].isFree() && children[1].isFree())
            {
                children = null;
                if (parent != null)
                    parent.tidy();
            }
        }
        
        public int getStructureComplexity()
        {
            if (children != null)
                return Math.max(children[0].getStructureComplexity(), children[1].getStructureComplexity()) + 1;
            return 1;
        }

        public boolean isAllocated(TreeNode root)
        {
            if (this == root)
                return true;
            if (parent == null)
                return false;
            if (parent.children == null)
                return false;
            if (parent.children[0] != this && parent.children[1] != this)
                return false;
            return parent.isAllocated(root);
        }
    }
    
    private TreeNode root;

    
    public BuddySystem(int sizeExponent)
    {
	root = new TreeNode(null,sizeExponent);
    }
    
    
    @Override
    public int getStructureComplexity() {
        return root.getStructureComplexity();
    }

    @Override
    public Chunk allocate(int bytes)
    {
        if (bytes <= 0)
            return null;
        return root.allocate(bytes);
    }

    @Override
    public void free(Chunk chunk)
    {
        if (chunk != null)
        {
            TreeNode node = ((TreeNode)chunk);
            assert node.isAllocated(root);
            node.free();
        }
    }

    @Override
    public int getFreeBytes()
    {
        return root.byteSize - root.getAllocatedBytes();
    }

    @Override
    public int getInternalFragmentationLostBytes()
    {
        return root.getInternalFragmentationLostBytes();
    }

    @Override
    public int getExternalFragmentationLostBytesFor(int chunkByteSize)
    {
        return root.getExternalFragmentationLostBytesFor(chunkByteSize);
    }
    
}
