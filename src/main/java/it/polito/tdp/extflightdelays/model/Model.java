package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {
	private Graph<Airport,DefaultWeightedEdge> grafo;
	private ExtFlightDelaysDAO dao;
	private Map<Integer,Airport> idMap;
	
	public Model() {
		this.dao = new ExtFlightDelaysDAO();
		this.idMap = new HashMap<Integer,Airport>();
		dao.loadAllAirports(idMap);
	}
	
	public void creaGrafo(int x) {
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class); //NON ORIENTATO, quindi considerare entrambe direzioni della rotta
		
		//aggiungo vertici
		Graphs.addAllVertices(this.grafo, dao.getVertici(x, idMap));
		
		//aggiungo archi
		for(Rotta r : dao.getRotte(idMap)) {
			if(this.grafo.containsVertex(r.getA1()) && this.grafo.containsVertex(r.getA2())) {
				DefaultWeightedEdge edge = this.grafo.getEdge(r.getA1(), r.getA2());
				if(edge == null) { //se NON esiste ancora l'arco all'interno del grafo, lo aggiungo.
					Graphs.addEdgeWithVertices(this.grafo, r.getA1(), r.getA2(), r.getnVoli());
				}else {   // ALTRIMENTI SE L'ARCO ESISTE GIA' ---> AGGIORNO IL PESO DELL'ARCO  (CI POSSONO ESSERE PIU' ROTTE TRA DUE VERTICI)
					double pesoVecchio=this.grafo.getEdgeWeight(edge);
					double pesoNuovo = pesoVecchio + r.getnVoli();
					this.grafo.setEdgeWeight(edge, pesoNuovo);
				}
			}
		}
		System.out.println("vertici: "+this.grafo.vertexSet().size());
		System.out.println("archi: "+this.grafo.edgeSet().size());
	}
	
	public List<Airport> getVertici(){
		List<Airport> vertici = new ArrayList<>(this.grafo.vertexSet());
		return vertici;
	}
	
	public List<Airport> getPercorso(Airport a1, Airport a2){
		
		List<Airport> percorso = new ArrayList<>();
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it = new BreadthFirstIterator<Airport, DefaultWeightedEdge>(this.grafo,a1);
		Boolean trovato = false;
		
		//VISITO IL GRAFO
		while(it.hasNext()) {
			Airport visitato = it.next();
			if(visitato.equals(a2))
				trovato = true;
		}
		
		//OTTENGO IL PERCORSO (ricordando che si parte dalla fine per arrivare in testa)
		// 	quindi aggiungo alla lista SEMPRE IN TESTA
		if(trovato==true) { //PRIMA DOBBIAMO VERIFICARE CHE a2 SIA COLLEGATO AD a1!
		percorso.add(a2); 
		Airport step = it.getParent(a2); // RISALGO AL PADRE DI a2(destinaz.) e vado avanti fino a quando non arrivo alla sorgente !!
		while(!step.equals(a1)) { // finchè non arrivo ad a1
			percorso.add(0, step); //aggiungo in TESTA!!
			step = it.getParent(step);
		}
		percorso.add(0, a1);
		return percorso;
		} else
			return null;
	}
	
	
	
	
}
