/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package memalloc;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author IronFox
 */
public class Main {

    public static class Range
    {
        private long min = -1, max = 0,sum=0,samples = 0;
        
        public void include(int value) throws Exception
        {
            if (value < 0)
                throw new Exception("Bad parameter: "+value);
            if (min == -1)
                min = value;
            else
                min = Math.min(min,value);
            max = Math.max(max,value);
            sum += value;
            samples ++;
            if (sum < 0)
                throw new Exception("Bad state: "+sum);
        }
        
        public float getAverage() throws Exception
        {
            if (sum < 0)
                throw new Exception("Bad state: "+sum);
            return samples > 0 ? (float)sum / samples : 0;
        }
        public long getMin()
        {
            return min;
        }
        public long getMax()
        {
            return max;
        }
        public long countSamples()
        {
            return samples;
        }
        
        public static String percent(float x, float max)
        {
            return  ( ((float)((int)(x/max*1000)))/10.0f ) + "%";
        }
        
        public String report(int maxValue) throws Exception
        {
            float avg = getAverage();
            return "avg "+(int)avg+"/"+maxValue+" ("+percent(avg,maxValue)+") in ["+min+"("+percent(min,maxValue)+"),"+max+"("+percent(max,maxValue)+")]"; 
        }
        
        public String report() throws Exception
        {
            float avg = getAverage();
            return "avg "+(int)avg+" in ["+min+","+max+"]"; 
        }
    }
    
    public static class Statistics
    {
        Range   complexity = new Range(),
                free = new Range(),
                largestFree = new Range(),
                internalFragmentation = new Range(),
                externalFragmentation = new Range();
	int	failures = 0;
	
        MemorySystem sys;
        
        public Statistics(MemorySystem sys)
        {
            this.sys = sys;
        }
	
	public static final int extChunkSize = 2048*8;
        
	public void recordFailure()
	{
	    failures++;
	}
	
        public void record() throws Exception
        {
            complexity.include(sys.getStructureComplexity());
            free.include(sys.getFreeBytes());
            internalFragmentation.include(sys.getInternalFragmentationLostBytes());
            externalFragmentation.include(sys.getExternalFragmentationLostBytesFor(extChunkSize));
            largestFree.include(sys.getLargestFreeChunk());
        }
        
        public void report() throws Exception
        {
            System.out.println("stat:"+sys.toString()+":");
            System.out.println("  failures: "+failures);
            System.out.println("  complexity: "+complexity.report());
            System.out.println("  free: "+free.report(sys.getTotalBytes()));
            System.out.println("  largest free: "+largestFree.report(sys.getTotalBytes()));
            System.out.println("  internal frag: "+internalFragmentation.report(sys.getTotalBytes()));
            System.out.println("  external frag("+extChunkSize+"): "+externalFragmentation.report(sys.getTotalBytes()));
        }
        
    }
    
    
    
    
    public static int randomChunkSize(Random rnd)
    {
        double rand = rnd.nextGaussian();
        return (int)(rand*rand *rand*rand* 128) + 1;
    }
    
    
    public static class Test
    {
        MemorySystem sys;
        Statistics stat;
        
        public Test(MemorySystem sys)
        {
            stat = new Statistics(sys);
	    this.sys = sys;
        }
        
        
        public void report()
        {
            System.out.println(sys.toString()+":");
            System.out.println("  complexity: "+sys.getStructureComplexity());
            System.out.println("  free: "+sys.getFreeBytes()+"/"+sys.getTotalBytes()+" ("+((int)((float)sys.getFreeBytes()/sys.getTotalBytes()*100))+"%)");
            System.out.println("  largest free: "+sys.getLargestFreeChunk());
            int internalFrag = sys.getInternalFragmentationLostBytes();
            System.out.println("  internal frag: "+internalFrag+" ("+((int)((float)internalFrag/sys.getTotalBytes()*100))+"%)");
            System.out.println("  external frag(1024): "+sys.getExternalFragmentationLostBytesFor(1024));
        }
    }
    
    public static void report(Test[] tests)
    {
	System.out.println("----------------------------------");
        for (Test t : tests)
            t.report();
	System.out.println("----------------------------------");
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        
	int sizeExponent = 18;
	
        Test[] tests = new Test[]
                            {
                                new Test(new BuddySystem(sizeExponent)),
                                new Test(new FirstFitB(sizeExponent)),
				new Test(new FirstFit(sizeExponent, FirstFit.Strategy.IncreasingSize)),
				new Test(new FirstFit(sizeExponent, FirstFit.Strategy.DecreasingSize)),
				new Test(new FirstFit(sizeExponent, FirstFit.Strategy.IncreasingTimeSinceLastUse))
                            };
        
    //    MemorySystem sys = new BuddySystem();
        
        report(tests);
        
        ArrayList<MemorySystem.Chunk[]> allocatedChunks = new ArrayList<>();
//        ArrayList<MemorySystem.Chunk> allocatedChunks = new ArrayList<>();
        
        final int NumIterations = 10000;
        long sizeSum = 0;
        Random rnd = new Random();
        for (int i = 0; i < NumIterations; i++)
        {
            for (int j = 0; j < allocatedChunks.size(); j++)
            {
                if (rnd.nextFloat() < 0.05f)
                {
                    for (int k = 0; k < tests.length; k++)
                        tests[k].sys.free(allocatedChunks.get(j)[k]);
                    allocatedChunks.remove(j);
                    j--;
                }
            }
            
            int size = randomChunkSize(rnd);
            sizeSum += size;
            MemorySystem.Chunk[] ch = new MemorySystem.Chunk[tests.length];
            for (int k = 0; k < tests.length; k++)
            {
                MemorySystem.Chunk ch0 = tests[k].sys.allocate(size);
                ch[k] = ch0;
                if (ch0 == null)
                {
		    tests[k].stat.recordFailure();
//                    System.out.println("unable to allocate "+size+" bytes");
  //                  tests[k].report();
                }
                tests[k].stat.record();
            }
	    allocatedChunks.add(ch);
        }
        report(tests);
        
        for (int j = 0; j < allocatedChunks.size(); j++)
        {
            for (int k = 0; k < tests.length; k++)
            {
		tests[k].sys.free(allocatedChunks.get(j)[k]);
	    }
        }
        report(tests);
	for (int k = 0; k < tests.length; k++)
	{
	    tests[k].stat.report();
	}
        
        System.out.println("avg chunk size: "+(float)sizeSum / NumIterations);
        
    }
    
}
