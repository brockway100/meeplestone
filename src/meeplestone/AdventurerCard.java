package meeplestone;

import java.util.HashMap;
import java.util.Map;

import static meeplestone.Traits.*;


public class AdventurerCard implements Comparable<AdventurerCard> {

	private String name;
	private Map<Traits, Integer> traitMap = new HashMap<Traits, Integer>();
	
	public AdventurerCard() {
	}
	
	public AdventurerCard(String name) {
		setName(name);
	}
	
	public AdventurerCard(String name, int health, int strength, int melee, int ranged, int defense, int vps, int loyalty, int luck) throws IllegalArgumentException { 
		setName(name);
		setTrait(HEALTH, health);
		setTrait(STRENGTH, strength);
		setTrait(MELEE, melee);
		setTrait(RANGED, ranged);
		setTrait(DEFENSE, defense);
		setTrait(VPS, vps);
		setTrait(LOYALTY, loyalty);
		setTrait(LUCK, luck);
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
		return name + ", " + getTrait(HEALTH) + ", " +
							 getTrait(STRENGTH) + ", " +
							 getTrait(MELEE)    + ", " +
							 getTrait(RANGED)   + ", " +
							 getTrait(DEFENSE)  + ", " +
							 getTrait(VPS)      + ", " +
							 getTrait(LOYALTY)  + ", " +
							 getTrait(LUCK);
	}
	
	@Override
	public boolean equals(Object o) {
		boolean result = false;

		if (o instanceof AdventurerCard)
			result = getValue() == ((AdventurerCard)o).getValue();
			
		return result;
	}

	public double getValue() {
		return (1.25 * getTrait(LOYALTY)) + (1.2 * getTrait(MELEE)) + (1.18 * getTrait(RANGED)) + (1.15 * getTrait(STRENGTH)) + (1.11 * getTrait(HEALTH)) + (1.11 * getTrait(DEFENSE));
	}

	@Override
	public int compareTo(AdventurerCard c) {
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
