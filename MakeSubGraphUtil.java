import in.ac.iitb.cfilt.jhwnl.data.Synset;
import in.ac.iitb.cfilt.jhwnl.JHWNLException;

import java.lang.*;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.*;

public class MakeSubGraphUtil{
	public static Set<Synset> senses = new HashSet<Synset>();
	public static Set<Synset> seen = new HashSet<Synset>();

	public static SimpleGraph<Synset, DefaultEdge> dfs(ArrayList<Synset> path,Synset current , SimpleGraph<Synset,DefaultEdge> subgraph, SimpleGraph<Synset,DefaultEdge> g) throws JHWNLException{
      	List<Synset> neighbours = Graphs.neighborListOf(g, current);
        Iterator<Synset> it = neighbours.iterator();
        while(it.hasNext()){
          Synset s = it.next();
          if(s != null){
            if(path.contains(s)){
                continue;
            }
            else if(senses.contains(s)){
                path.add(s);
                Iterator<Synset> it_1= path.iterator();
                while(it_1.hasNext()){
                    subgraph.addVertex(it_1.next());
                }
           
                //pairwise add edges between vertices 0--1--2--3
                for(int i=0; i<path.size(); i++){
                    Synset s1 = path.get(i);
                    if(path.size()>i+1){
                        Synset s2 = path.get(i+1);
                        subgraph.addEdge(s1, s2);
                    }
                }

                //"Add path to senses"
                it = path.iterator();
                while(it.hasNext()){
                    Synset ss = it.next();
                    if(!senses.contains(ss)){
                        senses.add(ss);
                    }
                }
            } //End of first else if
       
            else if(seen.contains(s)){
                continue;
            } //End of second else if
            else{
                seen.add(s);
                path.add(s); //Added on KP's bidding
                //Pointer[] newPtrs = s.getedges(); //Hyponyms, hypernyms and other relations
                dfs(path, s, subgraph, g);
                path.remove(path.size()-1);
            } //end of else
        }
        }
		return subgraph;
	} //End of dfs function
}
