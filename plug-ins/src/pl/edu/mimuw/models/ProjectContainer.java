/**
 * 
 */
package pl.edu.mimuw.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.WebTarget;

import org.json.JSONObject;
import org.pfsw.odem.IContainer;

/**
 * Model reprezentujący pojedynczy kontener w projekcie, a jednocześnie wierzchołek w grafowej bazie danych.
 * 
 * @author Paweł Nowosad
 *
 */
public class ProjectContainer extends ProjectObject {
	
	/**
	 * Odpowiada za zainicjalizowanie i przetworzenie danych o kontenerze oraz zapisanie go w grafowej bazie danych.
	 * 
	 * @param rootTarget	główny adres do REST API grafowej bazy danych
	 * @param container		obiekt, którym zostanie zainicjalizowany model
	 * @param externalData	dodatkowe dane, które zostaną przypisane do wierzchołka
	 */
	public ProjectContainer(WebTarget rootTarget, IContainer container, List<JSONObject> externalData) {
		super(rootTarget, container, externalData);
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("classification", container.getClassification().toString());
		createNode(properties);
		
		List<String> labels = new ArrayList<String>();
		labels.add("container");
		labels.add(container.getClassification().toString());
		this.addLabels(labels);
	}

}
