/**
 * 
 */
package pl.edu.mimuw.models;

import java.util.HashMap;
import java.util.List;

import javax.ws.rs.client.WebTarget;

import org.json.JSONObject;
import org.pfsw.odem.INamespace;

/**
 * Model reprezentujący pojedynczą przestrzeń nazw w projekcie, a jednocześnie wierzchołek w grafowej bazie danych.
 * 
 * @author Paweł Nowosad
 *
 */
public class ProjectNamespace extends ProjectObject {
	
	/**
	 * Odpowiada za zainicjalizowanie i przetworzenie danych o przestrzeni nazw oraz zapisanie jej w grafowej bazie danych.
	 * 
	 * @param rootTarget	główny adres do REST API grafowej bazy danych
	 * @param namespace		obiekt, którym zostanie zainicjalizowany model
	 * @param externalData	dodatkowe dane, które zostaną przypisane do wierzchołka
	 */
	public ProjectNamespace(WebTarget rootTarget, INamespace namespace, List<JSONObject> externalData) {
		super(rootTarget, namespace, externalData);
		
		createNode(new HashMap<String, String>());
		addLabel("namespace");
	}

}
