import in.ac.iitb.cfilt.jhwnl.data.Synset;

import java.lang.*;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.*;

public class GlobalMeasures{
	
	double pathsum = 0;
	ArrayList<Synset> verticesList;
	ArrayList<Double> pe = new ArrayList<Double>();
	Graph<Synset, DefaultEdge> g ;

	//Constructor
	public GlobalMeasures(SimpleGraph<Synset, DefaultEdge> g){
		this.g = g;
		verticesList = new ArrayList<Synset>(g.vertexSet());

		@SuppressWarnings("deprecation")
		FloydWarshallShortestPaths<Synset, DefaultEdge> paths = new FloydWarshallShortestPaths<Synset, DefaultEdge>(g);
		if(verticesList.size() > 0){
			for(int i=0;i<verticesList.size()-1;i++){
				pe.add((double)g.degreeOf(verticesList.get(i)));
				for ( int  j =i+1;j<verticesList.size();j++){
					double temp = paths.shortestDistance(verticesList.get(i), verticesList.get(j));
					if( Double.isInfinite(temp)) pathsum += verticesList.size();
					else pathsum+=temp;
				}
			}

			pe.add((double)g.degreeOf(verticesList.get(verticesList.size()-1)));
		}
	}

	public double calculateDensity(){
		if(g.edgeSet().size() == 0.0)
			return 0.0;
		else
    		return 2.0*(double)g.edgeSet().size()/ ((double)g.vertexSet().size()*((double)g.vertexSet().size()-1));
    }
    	     
    public double calculateCompactness(){
		int vertices = g.vertexSet().size();
		if(g.edgeSet().size() == 0.0)
			return 0.0;
		else
			return (double)((vertices*vertices*(vertices-1))-pathsum*2)/(double)(vertices*(vertices-1)*(vertices-1));
    }
    
    public double entropy()
    {
    	int n = g.vertexSet().size();
    	double s = (double)g.edgeSet().size()*2.0;
		if(s == 0.0)
			return 0.0;
    	double ent = 0;
    	for( int i=0;i<n;i++)
    	{
			pe.set(i, pe.get(i)/s);
    		if(pe.get(i)>0) ent += (pe.get(i) * Math.log(pe.get(i)))/Math.log(2.0);
    	}
		
    	return (ent*(-1) * Math.log(2.0))/Math.log(n); //Divided by log(|V|) to get entropy value in the [0,1] range.
    }
}
