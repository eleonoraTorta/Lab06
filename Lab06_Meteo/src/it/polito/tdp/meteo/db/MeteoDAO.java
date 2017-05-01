package it.polito.tdp.meteo.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import it.polito.tdp.meteo.bean.Rilevamento;

public class MeteoDAO {

	public List<Rilevamento> getAllRilevamenti() {

		final String sql = "SELECT Localita, Data, Umidita FROM situazione ORDER BY data ASC";

		List<Rilevamento> rilevamenti = new ArrayList<Rilevamento>();

		try {
			Connection conn = DBConnect.getInstance().getConnection();
			PreparedStatement st = conn.prepareStatement(sql);

			ResultSet rs = st.executeQuery();

			while (rs.next()) {

				Rilevamento r = new Rilevamento(rs.getString("Localita"), rs.getDate("Data"), rs.getInt("Umidita"));
				rilevamenti.add(r);
			}

			conn.close();
			return rilevamenti;

		} catch (SQLException e) {

			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public List<Rilevamento> getAllRilevamentiLocalitaMese(int mese, String localita) {
		
		String sql = "SELECT localita, data, umidita "+
				"FROM situazione "+
				"WHERE localita=? AND MONTH(data) =?";
		
		List<Rilevamento> rilevamenti = new ArrayList<Rilevamento>();
		
		try {
			Connection conn = DBConnect.getInstance().getConnection();      
			PreparedStatement st = conn.prepareStatement(sql);
			st.setString(1,localita);
			st.setInt(2,mese);
			
			ResultSet rs = st.executeQuery();
			
			while(rs.next()){	
				Rilevamento r = new Rilevamento(rs.getString("Localita"), rs.getDate("Data"), rs.getInt("Umidita"));
				rilevamenti.add(r);
			}
		
			conn.close();
			return rilevamenti;
			

		} catch (SQLException e) {
			// e.printStackTrace();
			throw new RuntimeException("Errore Db");
		}

	}

	public Double getAvgRilevamentiLocalitaMese(int mese, String localita) {
		List<Rilevamento> rilevamenti = this.getAllRilevamentiLocalitaMese(mese, localita);

		double tot=0.0;
		for(Rilevamento r : rilevamenti){
			tot+= r.getUmidita(); }
		int numRilevamenti = rilevamenti.size();
		return tot/numRilevamenti;
	}
	
	
	
	public List<String> getTutteCitta(){
		String sql = "SELECT DISTINCT localita "+
					"FROM situazione "+
					"GROUP BY localita";
		
		List<String> citta = new ArrayList <String>();
		
		try {
			Connection conn = DBConnect.getInstance().getConnection();
			PreparedStatement st = conn.prepareStatement(sql);

			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				citta.add(rs.getString("Localita"));
			}

			conn.close();
			return citta;

		} catch (SQLException e) {

			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
