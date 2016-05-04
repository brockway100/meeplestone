package meeplestone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * At some point it would be good to make this generic... no time now...
 * 
 * Want to create 100 balanced cards normally distributing trait values
 * and separating cards into 3 groups based on valuation formula.
 * 
 *
 */
public class AdventurerCardGenerator implements Runnable {

	// Gaussian distribution of trait values between 1-9 across 100 cards
	private static final int[] _traitDistribution = {4, 7, 13, 16, 20, 16, 13, 7, 4};
	private static ArrayList<Integer> _traitValues;
	
	
	private ArrayList<AdventurerCard> advCards = new ArrayList<AdventurerCard>();
	private Set<Integer> indexes = new HashSet<Integer>();
	
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 1; i++) {
			AdventurerCardGenerator acd = new AdventurerCardGenerator();
			new Thread(acd).start();
		}
		
		//AdventurerCardGenerator acd = new AdventurerCardGenerator();
		//acd.populateTraits();
		//Collections.sort(acd.advCards);
		//for (AdventurerCard card : acd.advCards)
			//	System.out.println(card);
		//acd.populateTieredTraits(3, 1.5);
		
		//Main.setAdventurers(_advCards);
		//Main.simulateCombat();
	}
	
	static synchronized void init() {
		if (_traitValues == null) {
			_traitValues = new ArrayList<Integer>();
			// Create distribution
			for (int i = 0; i < _traitDistribution.length; i++)
				while (_traitDistribution[i]-- > 0)
					_traitValues.add(i+1);
		}
	}
	
	public AdventurerCardGenerator() {
		init();
		
		// Create Adventurer Cards
		for (int i = 0; i < 100; i++)
			advCards.add(new AdventurerCard("", 0, 0, 0, 0, 0, 0, 0, 0));
	}
	
	private void populateTieredTraits(int nrLevels, double d) {
		boolean balanced = false;
		int border = advCards.size() / nrLevels;
		while (!balanced) {
			populateTraits();
			Collections.sort(advCards);
			balanced = true;
			for (int i = border; i < advCards.size() - 1; i += border) {
				double val1 = advCards.get(i).getValue();
				double val2 = advCards.get(i - 1).getValue();
				if ( (advCards.get(i).getValue() - advCards.get(i - 1).getValue()) < d) {
					balanced = false;
					break;
				}
			}
		}
	}
	
	private void populateTraits() {
		for (Traits trait : Traits.values())
			populateTrait(trait);
	}
	
	private void populateTrait(Traits trait) {
		indexes.clear();
		for (int i : _traitValues) {
			int index = ThreadLocalRandom.current().nextInt(100);
			while (indexes.contains(index))
				index = ThreadLocalRandom.current().nextInt(100);
			indexes.add(index);
			advCards.get(index).setTrait(trait, i);
		}
	}

	@Override
	public void run() {
		populateTieredTraits(3, 1.0);
		for (AdventurerCard card : advCards)
			System.out.println(card.getValue() + ", " + card);
		
		Main.setAdventurers(advCards);
		try {
			Main.simulateMonsterCombat();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
