package it.polito.tdp.meteo;

import java.util.*;

import it.polito.tdp.meteo.bean.Citta;
import it.polito.tdp.meteo.bean.Rilevamento;
import it.polito.tdp.meteo.bean.SimpleCity;
import it.polito.tdp.meteo.db.MeteoDAO;

public class Model {

	private final static int COSTO_CAMBIO = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;
	private List<String> cittaString;
	private List <Citta> citta;
	private MeteoDAO m = null ;
	private double punteggioMiglioreSoluzione;
	private ArrayList <SimpleCity> best = null;


	public Model() {
		m = new MeteoDAO();
		cittaString = new ArrayList <String>();
		cittaString = m.getTutteCitta();
		
		citta = new ArrayList <Citta>();
		for(String s : cittaString){
			Citta c = new Citta(s);
			citta.add(c);
		}
	}

	public String getUmiditaMedia(int mese) {
		if(mese <1 || mese >12){
			return "ERRORE: Mese deve essere compreso nel range 1-12\n";
		}
		String result = "";
		for( String s : cittaString){
			result += s + " " + m.getAvgRilevamentiLocalitaMese(mese,s ) + "\n";
			
		}
		return result;
	}

	public void setCitta(int mese){
		for (Citta c : citta){
			c.setCounter(0);
			c.setRilevamenti(m.getAllRilevamentiLocalitaMese(mese, c.getNome()));
		}
	}
	
	//Lancia la ricorsiva
	public String trovaSequenza(int mese) {
		if(mese <1 || mese >12){
			return "ERRORE: Mese deve essere compreso nel range 1-12\n";
		}
	
		punteggioMiglioreSoluzione = Double.MAX_VALUE;
		best= null;
		
		this.setCitta(mese);
	
		ArrayList <SimpleCity> parziale = new ArrayList <SimpleCity>();
	
		System.out.println("START");
		recursive(0, parziale);
		System.out.println("FINISH");
		
		if(best != null){	
			String result= "Soluzione migliore per il mese "+ mese + " :\n" ;
			result += this.punteggioSoluzione(best) + "\n";
			int numero =1;
			for(int i =0 ; i<best.size(); i++){
				result += "Giorno " + (numero++) + ": " + best.get(i).getNome() + ";\n"; 
			}
			return result;
		}
		return "Nessuna soluzione trovata";
	}
	
	private void recursive( int step, List<SimpleCity> parziale){
		
		if(step >= NUMERO_GIORNI_TOTALI){
				double score = this.punteggioSoluzione(parziale);
				if(score < punteggioMiglioreSoluzione) {   
					punteggioMiglioreSoluzione = score;
					best = new ArrayList <SimpleCity> (parziale);	
				}
				return;
		}
		
		for(Citta c : citta){	
			SimpleCity s = new SimpleCity(c.getNome(),c.getRilevamenti().get(step).getUmidita());
			parziale.add(s);
			c.increaseCounter();
			if(controllaParziale(parziale)){
				recursive ( step+1,parziale);
			}
			parziale.remove(s);
			c.decreaseCounter();
		}
	}
		


	private Double punteggioSoluzione(List<SimpleCity> soluzioneCandidata){
		
		// Controllo se la lista non sia nulla o vuota
		if(soluzioneCandidata == null || soluzioneCandidata.size() == 0){
			return Double.MAX_VALUE;     // ritorna il piu` grande valore finito di tipo Double
		}
		
		//Controllo che la soluzione contenga tutte le citta
		for( Citta c : citta ){
			if(! soluzioneCandidata.contains(new SimpleCity(c.getNome()))){
				return Double.MAX_VALUE;
			}
		}
		
		//Calcolo il costo : costo giornaliero (umidita) + costo spostamento tra due citta diverse (100)
		SimpleCity precedente = soluzioneCandidata.get(0);
		double score = 0.0;
		
		for(SimpleCity s : soluzioneCandidata){
			if(!precedente.equals(s) ){
				score = score + COSTO_CAMBIO;
			}
			score = score + s.getCosto();
			precedente = s;
		}
		return score;
	}

	private boolean controllaParziale(List<SimpleCity> parziale) {
		//Se e` nulla non e` valida
		if (parziale == null){
			return false;
		}
		//Se la soluzione parziale e` vuota e` valida
		if(parziale.size() == 0){
			return true;
		}

		//Controllo sui vincoli del numero di giorni massimo in ciascua citta
		for(Citta c: citta){
			if(c.getCounter() > NUMERO_GIORNI_CITTA_MAX){
				return false;
			}
		}
		
		//Controllo sul vincolo del numero minimo di giorni consecutivi
		SimpleCity precedente  = parziale.get(0);
		int counter =0;
		for(SimpleCity s : parziale){
			if(!precedente.equals(s)){
				if( counter < NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN){
					return false;
				}
				counter = 1;
				precedente = s;
			}else{
				counter ++;
			}
		}
		return true;
	
	}
}
