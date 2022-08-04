package com.jgr.game.vac.entity;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.springframework.lang.NonNull;

@Entity
public class GameSetup {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Column(unique = true)
	@NonNull
	String name;
	String beanName;
	
	Date lastRun;
	Date created;
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "game")
	@Cascade(CascadeType.ALL)
	List<GameProps> properties;
	
	protected GameSetup() {}
	
	public GameSetup(String name, String beanName) {
		this.name = name;
		this.beanName = beanName;
		created = new Date(System.currentTimeMillis());
	}
	
	public void addProperty(GameProps prop) {
		if(properties == null) {
			properties = new ArrayList<>();
		}
		properties.add(prop);
	}

	public Date getLastRun() {
		return lastRun;
	}

	public void setLastRun(Date lastRun) {
		this.lastRun = lastRun;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Date getCreated() {
		return created;
	}

	public List<GameProps> getProperties() {
		return properties;
	}

	public String getBeanName() {
		return beanName;
	}
	
	
}
