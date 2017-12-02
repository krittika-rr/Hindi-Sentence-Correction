import in.ac.iitb.cfilt.jhwnl.JHWNL;
import in.ac.iitb.cfilt.jhwnl.JHWNLException;
import in.ac.iitb.cfilt.jhwnl.data.IndexWord;
import in.ac.iitb.cfilt.jhwnl.data.IndexWordSet;
import in.ac.iitb.cfilt.jhwnl.data.Pointer;
import in.ac.iitb.cfilt.jhwnl.data.PointerType;
import in.ac.iitb.cfilt.jhwnl.data.Synset;
import in.ac.iitb.cfilt.jhwnl.data.POS;
import in.ac.iitb.cfilt.jhwnl.dictionary.Dictionary;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.*;
import static java.lang.Double.sum;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.DijkstraShortestPath;

public class LocalConnectivityMeasures {
    
    static Pair<Synset, Double> DegreeCentrality(Graph<Synset, DefaultEdge> G, Set<Synset> Senses, int no_of_vertices)
    {
        Pair<Synset, Double> bestSense = new Pair<Synset, Double>();
        Iterator<Synset> it = Senses.iterator();
        Set<DefaultEdge> Edgelist;
        Synset temp;
        int highestDegree = -1;
        
        System.out.println(Senses.size());
        
        while(it.hasNext())
        {
            temp = it.next();
            
            if(degree(G, temp) > highestDegree)
            {
                bestSense.synset = temp;
                highestDegree = degree(G, temp);
            }
        }      
        bestSense.value = ((double)highestDegree)/(no_of_vertices-1);
        return bestSense;
    }
    
    static Pair<Synset, Double> EVC_PageRank(Graph<Synset, DefaultEdge> G, Set<Synset> Senses)
    {
        // Compute Pagerank value
        Pair<Synset, Double> bestSense = new Pair<Synset, Double>((double)-1);
        Map<Synset, Double> M = new HashMap();
        Set<Synset> Vertices = G.vertexSet();
        int no_of_vertices = Vertices.size();
        Iterator<Synset> it = Vertices.iterator();
        double InitialPRValue = 1.00, d = 0.85, newPR, sum = 0, maxi = -1; // Damping factor
        int no_of_loops = 20; //No. of time loop will run to stabilize the pagerank.
        Pointer[] ptr;
        Synset current, target;
        
        while(it.hasNext())
        {
            M.put(it.next(), InitialPRValue);
        }
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(LocalConnectivityMeasures.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        it = Vertices.iterator();
        
        for(int i=0; i<no_of_loops; i++)
        {
            while(it.hasNext())
            {
                sum = 0.0;
                current = it.next();
                ptr = current.getPointers();
                
                for(int j = 0; j<ptr.length; j++)
                {
                    try {
                        target = ptr[j].getTargetSynset();
                        if(target != null && Vertices.contains(target))
                        sum += M.get(target)/degree(G, target);
                    } catch (JHWNLException ex) {
                        Logger.getLogger(LocalConnectivityMeasures.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                newPR = (1-d)/no_of_vertices + d*sum;
                M.put(current, newPR);
            }
        }
        
        // Select the best sense among all senses
        it = Senses.iterator();
        
        while(it.hasNext())
        {
            current = it.next();
            
            if(bestSense.value < M.get(current))
            {
                bestSense.synset = current;
                bestSense.value = M.get(current);
            }
        }
        return bestSense;
    }

    private static Integer degree(Graph<Synset, DefaultEdge> G, Synset target) {
       int res = 0;
        Set<DefaultEdge> Ed = G.edgesOf(target);
        
        Iterator<DefaultEdge> it = Ed.iterator();
        
        while(it.hasNext())
        {
            if(G.containsEdge(it.next()))
                res++;
        }
        return res;
    }
    
    static Pair<Synset, Double> EVC_HITS(Graph<Synset, DefaultEdge> G, Set<Synset> Senses){
    
        Pair<Synset, Double> bestSense = new Pair<Synset, Double>((double)-1);
        Map<Synset, Double> M = new HashMap();
        Set<Synset> Vertices = G.vertexSet();
        int no_of_vertices = Vertices.size();
        Iterator<Synset> it = Vertices.iterator();
        double InitialHValue = 1.00, newH, sum = 0, maxi = -1, norm = 0; // Damping factor
        int no_of_loops = 20; //No. of time loop will run to stabilize the pagerank.
        Pointer[] ptr;
        Synset current, target;
        
        while(it.hasNext())
        {
            M.put(it.next(), InitialHValue); 
        }
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(LocalConnectivityMeasures.class.getName()).log(Level.SEVERE, null, ex);
        }
        it = Vertices.iterator();
        
        for(int i=0; i<no_of_loops; i++)
        {
            norm = 0;
            while(it.hasNext())
            {
                sum = 0;
                current = it.next();
                ptr = current.getPointers();
                
                for(int j = 0; j<ptr.length; j++)
                {
                    try {
                        target = ptr[j].getTargetSynset();
                        if(Vertices.contains(target) && target != null)
                        {
                            sum += M.get(target);
                        }
                    } catch (JHWNLException ex) {
                        Logger.getLogger(LocalConnectivityMeasures.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                norm += (double)(sum*sum);
                M.put(current, sum);
            }
            norm = Math.sqrt(norm);
            it = Vertices.iterator();
            
            while(it.hasNext())
            {
                current = it.next();
                M.put(current, M.get(current)/norm); //Normalising hub/authority value
            }
        }
        
        // Select the best sense among all senses
        it = Senses.iterator();
        
        while(it.hasNext())
        {
            current = it.next();
            
            if(bestSense.value < M.get(current))
            {
                bestSense.synset = current;
                bestSense.value = M.get(current);
            }
        }
        return bestSense; 
    }
    
    static Pair<Synset, Double> KPP(Graph<Synset, DefaultEdge> G, Set<Synset> Senses){
        
        Pair<Synset, Double> bestSense = new Pair<Synset, Double>((double)-1);
        Map<Synset, Double> M = new HashMap<Synset, Double>();
        Set<DefaultEdge> E;
        Synset current, target;
        Double sum;
        Set<Synset> Vertices = G.vertexSet();
        int no_of_vertices = Vertices.size();
        Iterator<Synset> it = Senses.iterator(), it2;
        int cc = 1, dd = 0;
        
        while(it.hasNext())
        {
            current = it.next();
            it2 = Vertices.iterator();
            sum = 0.0;
            dd = 1;
            while(it2.hasNext())
            {
                target = it2.next();
                if(current == target)
                continue;
                
		if(findPath(G, current, target) != 0)
                sum += (double)(1/findPath(G, current, target));
            }
            cc++;
	    if(no_of_vertices > 1)
            M.put(current, sum/(no_of_vertices-1));
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(LocalConnectivityMeasures.class.getName()).log(Level.SEVERE, null, ex);
        }
        it = Senses.iterator();
        
        while(it.hasNext())
        {
            current = it.next();
           
            if(bestSense.value < M.get(current))
            {
                bestSense.synset = current;
                bestSense.value = M.get(current);
            }
        }
        return bestSense; 
    }

    private static int findPath(Graph<Synset, DefaultEdge> G, Synset current, Synset target) {
        
        int res = 0;
        List<DefaultEdge> Ed = DijkstraShortestPath.findPathBetween(G, current, target);
        
        if(Ed == null)
            return Integer.MAX_VALUE;
        int size = Ed.size();
        
        for(int i=0; i<size; i++)
            if(G.containsEdge(Ed.get(i)))
                res++;
        return res;
    }
    
    
    
}
