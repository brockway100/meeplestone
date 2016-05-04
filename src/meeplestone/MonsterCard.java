package meeplestone;

import static meeplestone.Traits.*;

import java.util.HashMap;
import java.util.Map;


public class MonsterCard implements Comparable<MonsterCard> {

	private String name;
	private Map<Traits, Integer> traitMap = new HashMap<Traits, Integer>();
	
	public MonsterCard() {
	}
	
	public MonsterCard(String name) {
		setName(name);
	}
	
	public MonsterCard(String name, int minions, int health, int defense, int vps) { 
		setName(name);
		setTrait(MINIONS, minions);
		setTrait(HEALTH, health);
		setTrait(DEFENSE, defense);
		setTrait(VPS, vps);
	}
	
	public int getTrait(Traits trait) {
		return traitMap.containsKey(trait) ? traitMap.get(trait) : -1;
	}
	
	public void setTrait(Traits trait, int value) {
		traitMap.put(trait,  value);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name + ", " + getTrait(MINIONS) + ", " +
							 getTrait(HEALTH)  + ", " +
							 getTrait(DEFENSE) + ", " +
							 getTrait(VPS);
	}
	
	@Override
	public boolean equals(Object o) {
		boolean result = false;

		if (o instanceof MonsterCard)
			result = getValue() == ((MonsterCard)o).getValue();
			
		return result;
	}

	public double getValue() {
		return (1.5 * getTrait(DEFENSE)) + (1.3 * getTrait(MINIONS)) + (1.2 * getTrait(HEALTH));
	}

	@Override
	public int compareTo(MonsterCard c) {
		int result = 0;
		
		double thisValue = getValue();
		double oValue = c.getValue();
		
		if ( (thisValue - oValue) < 0 )
			result = -1;
		else if ( (thisValue - oValue) > 0)
			result = 1;
		
		return result;
	}

}
