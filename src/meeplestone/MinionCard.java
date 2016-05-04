package meeplestone;

import static meeplestone.Traits.*;

import java.util.HashMap;
import java.util.Map;


public class MinionCard implements Comparable<MinionCard> {

	private String name;
	private Map<Traits, Integer> traitMap = new HashMap<Traits, Integer>();
	
	public MinionCard() {
	}
	
	public MinionCard(String name) {
		setName(name);
	}
	
	public MinionCard(String name, int health, int defense, int vps) { 
		setName(name);
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
		return name + ", " + getTrait(HEALTH)  + ", " +
							 getTrait(DEFENSE) + ", " +
							 getTrait(VPS);
	}
	
	@Override
	public boolean equals(Object o) {
		boolean result = false;

		if (o instanceof MinionCard)
			result = getValue() == ((MinionCard)o).getValue();
			
		return result;
	}

	public double getValue() {
		return (1.6 * getTrait(DEFENSE)) + (1.4 * getTrait(HEALTH));
	}

	@Override
	public int compareTo(MinionCard c) {
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
