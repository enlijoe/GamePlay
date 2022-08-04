package com.jgr.game.vac.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
public class GameProps {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@ManyToOne
	@JoinColumn(name="gameId", nullable = false)
	private GameSetup game;

	@ManyToOne
	@JoinColumn(name="propId", nullable = false)
	private Property property;

	@NotNull
	private String value;

	@Column(name = "gameId", insertable = false, updatable = false)
	private Long gameId;
	
	@Column(name="propId", insertable = false, updatable = false)
	private Long propId;
	
	
	protected GameProps() {}
	
	public GameProps(GameSetup game, Property property, String value) {
		this.game = game;
		this.property = property;
		this.value = value;
	}

	public GameSetup getGame() {
		return game;
	}

	public Property getProperty() {
		return property;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public long getId() {
		return id;
	}
}
