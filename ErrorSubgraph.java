import in.ac.iitb.cfilt.jhwnl.data.Synset;
import in.ac.iitb.cfilt.jhwnl.JHWNLException;

import java.lang.*;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.*;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class ErrorSubgraph{

	static Multimap<Synset,CWSubGraph> old = ArrayListMultimap.create();
	static Multimap<Synset,CWSubGraph> newmap = ArrayListMultimap.create();
	static Set<Synset> cwSensesStartSet= null;

	public static ArrayList<CWSubGraph> create_Subgraph(ArrayList<ArrayList<Synset>> cwSensesList, SimpleGraph<Synset,DefaultEdge> g)throws JHWNLException{

	int n = cwSensesList.size();
	//insert the last row in cwSenseList as single vertex graphs and store it in multimap as old with 
	cwSensesStartSet = new HashSet<Synset>(cwSensesList.get(n-1));
	Iterator<Synset> it = cwSensesStartSet.iterator();
		while(it.hasNext()){
			Synset key = it.next();
			SimpleGraph<Synset, DefaultEdge> subgraph = new SimpleGraph<>(DefaultEdge.class);
			subgraph.addVertex(key);
			CWSubGraph sgSoFar = new CWSubGraph();
			sgSoFar.mainNodes = new ArrayList<Synset>();
			sgSoFar.mainNodes.add(key);
			sgSoFar.sg = subgraph;
			old.put(key, sgSoFar);
		}
		
		for( int i = n-2;i>=0;i--){
			cwSensesStartSet = new HashSet<Synset>(cwSensesList.get(i));
			Iterator<Synset> it1 = cwSensesStartSet.iterator();
			Set<Synset> keys =  old.keySet();
			while(it1.hasNext()){
				Synset s1 = it1.next();
				
				for(Synset k:keys) {
				   SimpleGraph<Synset, DefaultEdge> tempgraph = new SimpleGraph<>(DefaultEdge.class);
				   MakeSubGraphUtil.senses.clear();
				   MakeSubGraphUtil.seen.clear();
				   tempgraph.addVertex(k);
				   ArrayList<Synset> path = new ArrayList<Synset>();
				   path.add(s1);
				   MakeSubGraphUtil.senses.add(k);
				   SimpleGraph<Synset, DefaultEdge> temp= MakeSubGraphUtil.dfs(path, s1, tempgraph, g);
				   if(temp.vertexSet().size()>1){
					   List<CWSubGraph> graphlist=(List<CWSubGraph>)old.get(k);

					   Iterator<CWSubGraph> p = graphlist.iterator();
					   while( p.hasNext()){
						CWSubGraph gr = p.next();
						SimpleGraph<Synset, DefaultEdge> uniongraph = new SimpleGraph<Synset, DefaultEdge>(DefaultEdge.class);
						CWSubGraph newgraph = new CWSubGraph();
						newgraph.mainNodes = new ArrayList<Synset>(gr.mainNodes);
						Graphs.addGraph(uniongraph,gr.sg);	
						Graphs.addGraph(uniongraph, temp);
						newgraph.sg = uniongraph;
						newgraph.mainNodes.add(s1);
						newmap.put(s1, newgraph);
					        }
 
				           }

				   }
			}
			old = newmap;
			newmap= ArrayListMultimap.create();
			
		}
		int id =1;
		ArrayList<CWSubGraph> SubgraphList = new ArrayList<CWSubGraph>();
		Collection<CWSubGraph> valset =  old.values();
			
		for(CWSubGraph k:valset) {
				   k.id = id;
				   SubgraphList.add(k);
	      			   //System.out.println(k.mainNodes);
				   id++;
		}
			
		
	return SubgraphList;
	}
}

