package com.jgr.game.vac.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.lang.NonNull;

@Entity
public class Property {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(unique = true)
	@NonNull
	private String name;
	private String initalValue;
	private String typeClassName;
	
	protected Property() {}
	
	public Property(String name, String initalValue, String typeClassName) {
		this.name = name;
		this.initalValue = initalValue;
		this.typeClassName = typeClassName; 
	}
	
	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getInitalValue() {
		return initalValue;
	}

	public void setInitalValue(String initalValue) {
		this.initalValue = initalValue;
	}

	public String getTypeClassName() {
		return typeClassName;
	}
	
}
