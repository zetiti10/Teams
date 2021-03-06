package fr.simple.teams.assautRequest;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public class AssautRequestSendData {
	
	private String attaquants = null;
	private String défenceurs = null;
	
	private Player demandeur;
	
	private String accès = "open";
	
	private int tpsPréparation = 50;
	private int tpsAttaque = 120;
	
	//private int mise = 1000;
	
	public static Map<String, AssautRequestSendData> AssautRequestSendData = new HashMap<>();
	
	public AssautRequestSendData(String teamName) {
		this.défenceurs = teamName;
		AssautRequestSendData.put(teamName, this);
	}

	public String getAttaquants() {
		return attaquants;
	}

	public void setAttaquants(String attaquants) {
		this.attaquants = attaquants;
	}

	public String getDéfenceurs() {
		return défenceurs;
	}

	public void setDéfenceurs(String défenceurs) {
		this.défenceurs = défenceurs;
	}

	public Player getDemandeur() {
		return demandeur;
	}

	public void setDemandeur(Player demandeur) {
		this.demandeur = demandeur;
	}

	public String getAccès() {
		return accès;
	}

	public void setAccès(String accès) {
		this.accès = accès;
	}

	public int getTpsPréparation() {
		return tpsPréparation;
	}

	public void setTpsPréparation(int tpsPréparation) {
		this.tpsPréparation = tpsPréparation;
	}

	public int getTpsAttaque() {
		return tpsAttaque;
	}

	public void setTpsAttaque(int tpsAttaque) {
		this.tpsAttaque = tpsAttaque;
	}
}
