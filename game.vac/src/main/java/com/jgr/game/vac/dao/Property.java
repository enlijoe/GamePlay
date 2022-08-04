package com.jgr.game.vac.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name"}))
public class Property {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	String name;
	String value;
	
	protected Property() {}
	
	public Property(String name, String value) {
		this.name = name;
		this.value = value;
	}

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Long getId() {
		return id;
	}
	
	
}
