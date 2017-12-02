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

class CWSubGraph{
	int id;
	ArrayList<Synset> mainNodes;
	SimpleGraph<Synset, DefaultEdge> sg;
	
	@Override
	public String toString(){
		String glosses = id + "\n";
        for(Synset s : mainNodes){
            glosses += s.getGloss() + "\n";
        }
        return glosses;
	}
}

public class HindiSECNw_2{

	static SimpleGraph<Synset, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
	static Set<Synset> cwsenses = new HashSet<Synset>();
	static Set<Synset> seen = new HashSet<Synset>();
	static ArrayList<ArrayList<Synset>> cwSensesList = new ArrayList<ArrayList<Synset>>();

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
				else if(cwsenses.contains(s)){
					path.add(s);
					Iterator<Synset> it = path.iterator();
					while(it.hasNext()){
						Synset geg = it.next();
						//System.out.println(geg.getGloss());
						g.addVertex(geg);
						//g.addVertex(it.next());
					}
				
					//pairwise add edges between vertices 0--1--2--3
					for(int i=0; i<path.size(); i++){
						Synset s1 = path.get(i);
						if(path.size()>i+1){
							Synset s2 = path.get(i+1);
							g.addEdge(s1, s2);
						}
					}

					//"Add path to cwsenses"
					it = path.iterator();
					while(it.hasNext()){
						Synset ss = it.next();
						if(!cwsenses.contains(ss)){
							cwsenses.add(ss);
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

	private static void Print(Pair<Synset, Double> result) {
		System.out.println(result.synset);
		System.out.println(result.value);
		System.out.println();
	}

	
	static void applyMeasures() throws JHWNLException{
		
		System.out.println("===============APPLYING LOCAL CENTRALITY MEASURES==============\n");
		Pair<Synset, Double> result;
		int len = cwSensesList.size();
		System.out.println(len);
		    
		for(int i=0; i<len; i++)
		{
			Set<Synset> S = new HashSet<Synset>(cwSensesList.get(i));
		        System.out.println("DegreeCentrality: ");
		        result = LocalConnectivityMeasures.DegreeCentrality(g, S, g.vertexSet().size());
		        Print(result);
		        
		        System.out.println("Pagerank: ");
		        result = LocalConnectivityMeasures.EVC_PageRank(g, S);
		        Print(result);
		        
		        System.out.println("HITS: ");
		        result = LocalConnectivityMeasures.EVC_HITS(g, S);
		        Print(result);
		        
		        System.out.println("KPP: ");
		        result = LocalConnectivityMeasures.KPP(g, S);
		        Print(result);
		}
		
		
		System.out.println("\n==============CREATING SUBGRAPHS===================\n");
		List<CWSubGraph> sgList = ErrorSubgraph.create_Subgraph(cwSensesList, g);
    		System.out.println("Number of subgraphs created = " + sgList.size() + "\n");

		System.out.println("===============APPLYING GLOBAL CENTRALITY MEASURES==============\n");
		GlobalMeasures gm;

		double entr, compc, edgeden;
		double maxEntropy = -1, maxComp = -1, maxED= -1;
		CWSubGraph maxEntSG = null, maxCompSG = null, maxEdSG = null;
		for(CWSubGraph e : sgList){
			gm = new GlobalMeasures(e.sg);
			System.out.println("Subgraph #" + e.id);
			System.out.println("Number of nodes in subgraph = " + e.sg.vertexSet().size());

			entr = gm.entropy();
			System.out.println("Entropy: " + entr);
			if(entr > maxEntropy){
				maxEntropy = entr;
				maxEntSG = e;
			}

			edgeden = gm.calculateDensity();
			System.out.println("Edge Density: " + edgeden);
			if(edgeden > maxED){
				maxED = edgeden;
				maxEdSG = e;
			}

			compc = gm.calculateCompactness();
			System.out.println("Compactness: " + compc);
			if(compc > maxComp){
				maxComp = compc;
				maxCompSG = e;
			}
			System.out.println("-----------------------------------------------------------------");
		}

		//Print senses having highest values of the centrlity measures
		System.out.println("\nHighest entropy = " + maxEntropy);
		System.out.println(maxEntSG);
		System.out.println("\nHighest edge density = " + maxED);
		System.out.println(maxEdSG);
		System.out.println("\nHighest compactnes = " + maxComp);
		System.out.println(maxCompSG);
	
	}
	
	static void analyzegraph() throws JHWNLException{
		Set<Synset> graphVertices = g.vertexSet();
		/*System.out.println("Vertices in the candidate words graph are: ");
		Iterator it = graphVertices.iterator();
		while(it.hasNext()){
			System.out.println(it.next());
		}*/
	
		System.out.println("\nNumber of vertices in the graph: " + graphVertices.size());
		
		applyMeasures();
	}

	static void begin(){
		BufferedReader candidateWordsFile;
		String filenameprefix = "cndwordsgp", filename;
		int i=1, n=3;
		 //A list of most popular sense of each erroneous words' candidate words
		ArrayList<Synset> dummy;
		try {
			String inputWord;
			JHWNL.initialize();
			for(;i<=n; i++){
				filename = filenameprefix + i + ".txt";
				candidateWordsFile = new BufferedReader(new InputStreamReader (new FileInputStream (filename), "UTF8"));
				dummy = new ArrayList<Synset>();
				while((inputWord = candidateWordsFile.readLine()) != null){
					System.out.println(inputWord);
					IndexWord indexWord = Dictionary.getInstance().lookupIndexWord(POS.NOUN, inputWord.trim());
					if (indexWord!= null){
					Synset[] senses= indexWord.getSenses();
					
					Synset mpSense = senses[0]; //Obtain the most popular sense of this word
		
					dummy.add(mpSense);
					}
				}
				cwSensesList.add(dummy);
			}

		} catch( FileNotFoundException e){
			System.err.println("Error opening input words file.");
			System.exit(-1);
		} catch (UnsupportedEncodingException e) {
			System.err.println("UTF-8 encoding is not supported.");
			System.exit(-1);
		}catch (IOException e) {
			System.err.println("Error in input/output.");			
			e.printStackTrace();
		}catch (JHWNLException e) {
			System.err.println("Internal Error raised from API.");
			e.printStackTrace();
		}catch(Exception e){
			System.err.println("An error occured");
			e.printStackTrace();
		}


		//------------------CWG CONSTRUCTION STARTS FROM HERE-------------------------------------------------
		try{
			Set<Synset> cwSensesStartSet = null;
			//Set<Synset> cwSensesSecondGroup = null;
			cwsenses.addAll(cwSensesList.get(n-1));

			Iterator<Synset> it = cwsenses.iterator();
			//Add cwsenses to candidate words graph as initial vertices
			while(it.hasNext()){
					Synset geg = it.next();
					//System.out.println(geg.getGloss());
					g.addVertex(geg);
				//g.addVertex(it.next());
			}
	
			//System.out.println("wfgrgg" + cwsenses);
			for(int j=n-1; j>0; j--){
				cwSensesStartSet = new HashSet<Synset>(cwSensesList.get(j-1));
				it = cwSensesStartSet.iterator();
				while(it.hasNext()){
					ArrayList<Synset> path = new ArrayList<Synset>();
					Synset srcSynset = it.next(); //This is one of the cwsenses from which we start applying dfs
					path.add(srcSynset);
					Pointer[] pointers = srcSynset.getPointers();
					dfs(path, pointers);
				}
				it = cwSensesStartSet.iterator();
				while(it.hasNext()){
					Synset geg = it.next();
					//System.out.println(geg.getGloss());
					g.addVertex(geg);
				}
			}
			analyzegraph();
		}catch (JHWNLException e){
			System.err.println("Internal Error raised from API.");
			e.printStackTrace();
		}
	} //end of begin()

	public static void main(String args[]) throws Exception {
		begin();
	}
}
