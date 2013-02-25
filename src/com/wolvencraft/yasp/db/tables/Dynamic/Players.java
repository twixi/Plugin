package com.wolvencraft.yasp.db.tables.Dynamic;

public enum Players implements _DynamicTable {
	
	TableName("players"),
	
	PlayerId("player_id"),
	Name("name"),
	Online("online"),
	ExperiencePercent("exp_perc"),
	ExperienceTotal("exp_total"),
	ExperienceLevel("level"),
	FoodLevel("food_level"),
	HealthLevel("health"),
	FirstLogin("first_login"),
	Logins("logins");
	
	Players(String columnName) {
		this.columnName = columnName;
	}
	
	private String columnName;
	
	@Override
	public String toString() { return columnName; }

}
