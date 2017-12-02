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
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;

public class HindiWSD1{

	static Graph<Synset, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
	static Set<Synset> senses = new HashSet<Synset>();
	static Set<Synset> sensesInitial = new HashSet<Synset>();
	static Set<Synset> seen = new HashSet<Synset>();
        static ArrayList< Set<Synset> > SensesList = new ArrayList<Set<Synset> >();
            

	static void dfs(ArrayList<Synset> path, Pointer[] pointers) throws JHWNLException{
		for(int pc=0; pc<pointers.length; pc++) //For each pointer p in pointers
		{
			Set<Synset> temp = new HashSet<Synset>(path); // temp = set(path)
			Pointer p = pointers[pc];
			Synset s = p.getTargetSynset(); //Synset corresponding to this pointer
			if(s != null){
				temp.add(s);
			
				if(path.contains(s)){
					continue;
				}
				else if(senses.contains(s)){
					path.add(s);
					Iterator<Synset> it = path.iterator();
					while(it.hasNext()){
						g.addVertex(it.next());
					}
				
					//pairwise add edges between vertices 0--1--2--3
					for(int i=0; i<path.size(); i++){
						Synset s1 = path.get(i);
						if(path.size()>i+1){
							Synset s2 = path.get(i+1);
							g.addEdge(s1, s2);
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
					Pointer[] newPtrs = s.getPointers(); //Hyponyms, hypernyms and other relations
					dfs(path, newPtrs);
					path.remove(path.size()-1);
				} //end of else
			}
		} //End of outermost for loop
	} //End of dfs function

	static void analyzegraph(){
		Set<Synset> graphVertices = g.vertexSet();
		System.out.println("Vertices in the wordsense graph are: ");
		Iterator it = graphVertices.iterator();
		while(it.hasNext()){
			System.out.println(it.next());
		}
		System.out.println("\nNumber of vertices in the graph: " + graphVertices.size());
	}

	static void CreateGraph(){
		BufferedReader inputWordsFile = null;
                String FilePath = "mywords.txt";
		try {
			inputWordsFile = new BufferedReader(new InputStreamReader (new FileInputStream (FilePath), "UTF8"));
		} catch( FileNotFoundException e){
			System.err.println("Error opening input words file.");
			System.exit(-1);
		} catch (UnsupportedEncodingException e) {
			System.err.println("UTF-8 encoding is not supported.");
			System.exit(-1);
		}

		String inputLine;
		JHWNL.initialize();

                Set<Synset> dummy;
                int counter = 0;
		try{
			while((inputLine = inputWordsFile.readLine()) != null){
                                dummy = new HashSet<Synset>();
				IndexWordSet IWSet = Dictionary.getInstance().lookupAllIndexWords(inputLine.trim());
				IndexWord[] indexWords = new IndexWord[IWSet.size()];
				indexWords  = IWSet.getIndexWordArray();
				for(int i=0; i<indexWords.length; i++){
					int no_of_senses = indexWords[i].getSenseCount();
					Synset[] synsetArray = indexWords[i].getSenses();

					for(int c=0; c<no_of_senses; c++){
						sensesInitial.add(synsetArray[c]);
						senses.add(synsetArray[c]);
                                                dummy.add(synsetArray[c]);
					}
  				}
                                SensesList.add(dummy);
  			} //End of while

			
			Iterator<Synset> it = sensesInitial.iterator();
			//Add senses to graph as initial vertices
			while(it.hasNext()){
				g.addVertex(it.next());
			}

			//For every sense in sensesInitial, run dfs on it
			it = sensesInitial.iterator();
			while(it.hasNext()){
				ArrayList<Synset> path = new ArrayList<Synset>();
				Synset srcSynset = it.next(); //This is one of the senses from which we start applying dfs
				path.add(srcSynset);
				Pointer[] pointers = srcSynset.getPointers();
				dfs(path, pointers);
			}

		}catch (IOException e) {
			System.err.println("Error in input/output.");			
			e.printStackTrace();
		}catch (JHWNLException e) {
			System.err.println("Internal Error raised from API.");
			e.printStackTrace();
		}
//		analyzegraph();
	}
	public static void main(String args[]) throws Exception {
		
            CreateGraph();
            Pair<Synset, Double> result;
            int len = SensesList.size();
            System.out.println(len);
            
            for(int i=0; i<len; i++)
            {
                System.out.println("DegreeCentrality: ");
                result = LocalConnectivityMeasures.DegreeCentrality(g, SensesList.get(i), g.vertexSet().size());
                Print(result);
                
                System.out.println("Pagerank: ");
                result = LocalConnectivityMeasures.EVC_PageRank(g, SensesList.get(i));
                Print(result);
                
                System.out.println("HITS: ");
                result = LocalConnectivityMeasures.EVC_HITS(g, SensesList.get(i));
                Print(result);
                
                System.out.println("KPP: ");
                result = LocalConnectivityMeasures.KPP(g, SensesList.get(i));
                Print(result);
            }
        }

    private static void Print(Pair<Synset, Double> result) {
        System.out.println(result.synset);
        System.out.println(result.value);
        System.out.println();
    }
}
