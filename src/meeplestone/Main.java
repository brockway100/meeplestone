package meeplestone;

import static meeplestone.Traits.*;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 * 
 * Thoughts on combat
 * Each individual Adventurer would deal between 0-10 damage against a monster as it's
 * more difficult to track and add damage in a group battle.
 * 
 * Statistics to capture. For each defense level, over a large number of group battles
 * 	Max group damage, 
 * 
 *
 */
public class Main {

	/* List of all Adventurer Cards */
	private static ArrayList<AdventurerCard> _advCards = new ArrayList<AdventurerCard>();
	/* List of Adventurers in a specific battle */
	private static ArrayList<AdventurerCard> _adventurers = new ArrayList<AdventurerCard>();
	private static ArrayList<MonsterCard> _monsterCards = new ArrayList<MonsterCard>();
	/* List of all Minion Cards */
	private static ArrayList<MinionCard> _minionCards = new ArrayList<MinionCard>();
	/* List of Minions in a specific battle */
	private static ArrayList<MinionCard> _minions = new ArrayList<MinionCard>();
	private static Set<Integer> _indexes = new HashSet<Integer>();
	private static Random _random = new Random();
	private static int _maxGroupDamage, _minGroupDamage, _maxAdvDamage, _minAdvDamage, _minionFights, _minionWins;
	
	public static void main(String[] args) throws Exception {
		loadAdventurerCards();
		loadMonsterCards();
		loadMinionCards();
		simulateMonsterCombat();
	}
	
	static void setAdventurers(ArrayList<AdventurerCard> adventurers) {
		_advCards.clear();
		_advCards.addAll(adventurers);
	}
	
	static void simulateCombat() {
		long damage;
		
		// Want loop over Monster with a specific defense a number of times
		for (int monsterDefense = 1; monsterDefense < 10; monsterDefense++) {
			damage = 0;
			_maxGroupDamage = _minGroupDamage = _maxAdvDamage = _minAdvDamage = 0;
			for (int nmbrCombats = 0; nmbrCombats < 1000000; nmbrCombats++) {
				generateIndexes();
				int adventurerPos = 0;
				int battleDamage = 0;
				for (int i : _indexes) {
					battleDamage += generateDamage(_advCards.get(i), adventurerPos++ < 2 ? true : false, monsterDefense);
				}
				damage += battleDamage;
				_maxGroupDamage = Math.max(_maxGroupDamage, battleDamage);
				_minGroupDamage = Math.min(_minGroupDamage, battleDamage);
			}
			System.out.println("Average damage dealt for monster with defense " + monsterDefense + " is " + damage/1000000);
			System.out.println("Max Group Damage: " + _maxGroupDamage + " Min Group Damage: " + _minGroupDamage + " Max Adv Damage: " + _maxAdvDamage + " Min Adv Damage: " + _minAdvDamage);
		}
	}
	
	static void simulateMonsterCombat() throws Exception {
		if (_monsterCards.size() == 0)
			loadMonsterCards();
		long damage;

		// Want loop over Monster with a specific defense a number of times
		for (MonsterCard monsterCard : _monsterCards) {
			System.out.println("Simulating combat against: " + monsterCard.getName());
			damage = 0;
			int nmbrCombats = 0, advWins = 0;
			_maxGroupDamage = _minGroupDamage = _maxAdvDamage = _minAdvDamage = _minionFights = _minionWins = 0;
			while (nmbrCombats++ < 1000000) {
				setMinions(monsterCard);
				setAdventurers();
				minionFights();
				int battleDamage = 0;
				for (int i = 0; i < _adventurers.size(); i++) {
					battleDamage += generateDamage(_adventurers.get(i), i < 2, monsterCard.getTrait(DEFENSE));
				}
				damage += battleDamage;
				int totalHealth = monsterCard.getTrait(HEALTH);
				for (MinionCard mc : _minions)
					totalHealth += mc.getTrait(HEALTH);
				if (battleDamage >= totalHealth) advWins++;
				_maxGroupDamage = Math.max(_maxGroupDamage, battleDamage);
				_minGroupDamage = Math.min(_minGroupDamage, battleDamage);
			}
			float winPercent = ((float)advWins/(float)(nmbrCombats-1)) * 100;
			float minionWinPercent = ((float)_minionWins)/(float)_minionFights * 100;
			System.out.println("Adventurers won " + minionWinPercent + "% of minion fights (" + _minionWins + " out of " + _minionFights + ")");
			System.out.println("Average damage dealt against his defense of " + monsterCard.getTrait(DEFENSE) + " was " + damage/1000000);
			System.out.println("Adventurers won " + winPercent + "% of battles ("+ advWins + " out of " + (nmbrCombats-1) + ")");
			System.out.println("Max Group Damage: " + _maxGroupDamage + " Min Group Damage: " + _minGroupDamage + " Max Adv Damage: " + _maxAdvDamage + " Min Adv Damage: " + _minAdvDamage);
		}
	}
	
	private static void minionFights() {
		// Match up the strongest attack -vs- the strongest defense
		ArrayList<AdventurerCard> sorted = new ArrayList<AdventurerCard>(_adventurers);
		Collections.sort(sorted, new Comparator<AdventurerCard>() {
			public int compare(AdventurerCard a1, AdventurerCard a2) {
				int a1Attack = (a1 == _adventurers.get(0) || a1 == _adventurers.get(1)) ? a1.getTrait(MELEE) : a1.getTrait(RANGED);
				int a2Attack = (a2 == _adventurers.get(0) || a2 == _adventurers.get(1)) ? a2.getTrait(MELEE) : a2.getTrait(RANGED);
				return a2Attack - a1Attack;
			}});
		
		// Now we might
		int i = 0;
		ArrayList<MinionCard> toRemove = new ArrayList<MinionCard>();
		for (MinionCard minion : _minions) {
			// 80% chance the fight will happen... various reasons may cause the
			// controller of the Adventurer to avoid a fight
			if (_random.nextDouble() >= .2) {
				_minionFights++;
				int damage = generateDamage(sorted.get(i), _adventurers.indexOf(sorted.get(i)) < 2, minion.getTrait(DEFENSE));
				if (damage >= minion.getTrait(HEALTH)) {
					toRemove.add(minion);
					_minionWins++;
				}
			}
			i++;
		}
		_minions.removeAll(toRemove);
	}
		
	/**
	 * Returns a random selection of Minions equal to the MINIONS trait of the
	 * passed in monster. The returned ArrayList is sorted in descending order of
	 * the minion's defense.
	 * @param monster
	 * @return
	 */
	private static void setMinions(MonsterCard monster) {
		_minions.clear();
		
		for (int i = 0; i < monster.getTrait(MINIONS); i++)
			_minions.add(_minionCards.get(_random.nextInt(_minionCards.size())));
		
		if (_minions.size() > 1) {
			Collections.sort(_minions, new Comparator<MinionCard>() {
				public int compare(MinionCard m1, MinionCard m2) {
					return m2.getTrait(DEFENSE) - m1.getTrait(DEFENSE);
				}});
		}
	}
	
	private static void loadAdventurerCards() throws Exception {
		Reader in = new FileReader("C:\\eclipse\\workspaces\\Meeplestone\\Meeplestone\\data\\adventurers.csv");
		Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in);
		for (CSVRecord record : records)
			_advCards.add(new AdventurerCard(record.get("Name"),
													 new Integer(record.get("Health")),
													 new Integer(record.get("Strength")),
													 new Integer(record.get("Melee")),
													 new Integer(record.get("Ranged")),
													 new Integer(record.get("Defense")),
													 new Integer(record.get("VPs")),
													 new Integer(record.get("Loyalty")),
													 new Integer(record.get("Luck"))));
	}
	
	private static void loadMonsterCards() throws Exception {
		Reader in = new FileReader("C:\\eclipse\\workspaces\\Meeplestone\\Meeplestone\\data\\monsters.csv");
		Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in);
		for (CSVRecord record : records)
			_monsterCards.add(new MonsterCard(record.get("Name"),
													new Integer(record.get("Minions")),
													new Integer(record.get("Health")),
													new Integer(record.get("Defense")),
													new Integer(record.get("VPs"))));
	}
	
	private static void loadMinionCards() throws Exception {
		Reader in = new FileReader("C:\\eclipse\\workspaces\\Meeplestone\\Meeplestone\\data\\minions.csv");
		Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in);
		for (CSVRecord record : records)
			_minionCards.add(new MinionCard(record.get("Name"),
													new Integer(record.get("Health")),
													new Integer(record.get("Defense")),
													new Integer(record.get("VPs"))));
	}
	
	/**
	 * 
	 */
	private static void setAdventurers() {
		_adventurers.clear();
		while (_adventurers.size() != 5)
			_adventurers.add(_advCards.get(_random.nextInt(_advCards.size())));
		
		// First two in list will Melee attack, others Ranged
		// Make sure we swap one, if necessary, to simulate what would likely happen
		int meleeToSwap = (_adventurers.get(0).getTrait(RANGED) - _adventurers.get(0).getTrait(MELEE)) > (_adventurers.get(1).getTrait(RANGED) - _adventurers.get(1).getTrait(MELEE)) ? 0 : 1;
		int rangedToSwap = (_adventurers.get(2).getTrait(MELEE) - _adventurers.get(2).getTrait(RANGED)) > (_adventurers.get(3).getTrait(MELEE) - _adventurers.get(3).getTrait(RANGED)) ? 2 : 3; 
		rangedToSwap = (_adventurers.get(rangedToSwap).getTrait(MELEE) - _adventurers.get(rangedToSwap).getTrait(RANGED)) > (_adventurers.get(4).getTrait(MELEE) - _adventurers.get(4).getTrait(RANGED)) ? rangedToSwap : 4;
		// Swap only if there would be a net gain to attack
		if ( (_adventurers.get(meleeToSwap).getTrait(RANGED) - _adventurers.get(meleeToSwap).getTrait(MELEE)) + (_adventurers.get(rangedToSwap).getTrait(MELEE) - _adventurers.get(rangedToSwap).getTrait(RANGED)) > 0) {
			AdventurerCard c = _adventurers.get(meleeToSwap);
			_adventurers.set(meleeToSwap,  _adventurers.get(rangedToSwap));
			_adventurers.set(rangedToSwap, c);
		}
	}
	
	private static void generateIndexes() {
		_indexes.clear();
		while (_indexes.size() != 5) {
			_indexes.add(_random.nextInt(_advCards.size()));
		}
	}
	
	private static int generateDamage(AdventurerCard advCard, boolean melee, int monsterDefense) {
		int result = 0;
		int diceToRoll = (melee ? advCard.getTrait(Traits.MELEE) : advCard.getTrait(Traits.RANGED)) - monsterDefense;
		for (int i = 0; i < diceToRoll; i++)
			result += getDamageWithCrit(advCard.getTrait(Traits.STRENGTH), melee);
		
		_maxAdvDamage = Math.max(result, _maxAdvDamage);
		_minAdvDamage = Math.min(result, _minAdvDamage);
		
		return result;
	}
	
	private static int getDamage(int strength) {
		int result = 0;
		int dieRoll = _random.nextInt(6) + 1;
		
		switch (strength) {
			// Yellow combat dice, strength between 1-3
			case 1: case 2: case 3:
				switch (dieRoll) {
					case 2: case 3:
						result = 1;
						break;
					case 4: case 5:
						result = 2;
						break;
					case 6: result = 3;
						break;
				}
				break;
			// Orange combat dice, strength between 4-6
			case 4: case 5: case 6:
				switch (dieRoll) {
					case 2: case 3: 
						result = 2;
						break;
					case 4: case 5:
						result = 3;
						break;
					case 6: result = 4;
						break;
				}
				break;
			// Red combat dice, strength between 7-9
			case 7: case 8: case 9:
				switch (dieRoll) {
					case 2: case 3: 
						result = 3;
						break;
					case 4: case 5:
						result = 4;
						break;
					case 6: result = 5;
						break;
			}
			break;
		}
		
		//System.out.println("Strength: " + strength + " die roll: " + dieRoll + " damage: " + result);
		return result;
	}
	
	private static int getDamageWithCrit(int strength, boolean melee) {
		int result = 0;
		int dieRoll = _random.nextInt(6) + 1;
		
		switch (strength) {
			// Yellow combat dice, strength between 1-3
			case 1: case 2: case 3:
				switch (dieRoll) {
				case 1: case 6:
					// 1 and 6 are misses
					break;
				case 2: case 5:
					// 2 and 5 are melee hits
					if (melee) result = 1;
					break;
				case 3: case 4:
					// 3 and 4 are melee hits
					if (!melee) result = 1;
					break;
				}
				break;
			// Orange combat dice, strength between 4-6
			case 4: case 5: case 6:
				switch (dieRoll) {
					case 1:
						// 1 is a miss
						break;
					case 2: case 5:
						// 2 and 5 are melee hits
						if (melee) result = 1;
						break;
					case 3: case 4:
						// 3 and 4 are ranged hits
						if (!melee) result = 1;
						break;
					case 6: 
						// 6 is a critical hit
						result = 1 + getDamageWithCrit(strength, melee);
						break;
				}
				break;
			// Red combat dice, strength between 7-9
			case 7: case 8: case 9:
				switch (dieRoll) {
					case 1: case 6:
						// 1 and 6 are both crits
						result = 1 + getDamageWithCrit(strength, melee);
						break;
					case 2: case 5:
						// 2 and 5 are melee hits
						if (melee) result = 1;
						break;
					case 3: case 4:
						// 3 and 4 are ranged hits
						if (!melee) result = 1;
						break;
			}
			break;
		}
		
		return result;
	}
	
}
