package ben.kn.algorithms.google;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * found on github: https://github.com/benkn/algorithms/blob/master/src/main/java/ben/kn/algorithms/google/CoinDenomination.java
 * </pre>
 * 
 * Problem: You have coins with different denominations and you have limited
 * number of each denominations (some maybe zero), how will you determine if you
 * can supply the given change.
 * 
 * Solution: Go through the coins in a descending array, and get the most of
 * each coin to fill the balance. Recursively seek to fill the updated balance
 * with the lesser denominations.
 * 
 * @author Ben (bknear@gmail.com)
 */
public class CoinDenomination {

	private Coin[] coins;

	public CoinDenomination(Coin[] coins) {
		this.coins = coins;
	}

	/**
	 * Get the list of coins acceptable for forming the given amount
	 * 
	 * @param amount
	 *            int of amount to find
	 * @return List of coins, or an empty list if none can form it
	 */
	public List<Coin> getChange(int amount) {
		List<Coin> change = new ArrayList<Coin>();
		getChange(amount, 0, change);
		return change;
	}

	/**
	 * Get the change for creating the given amount with the given list of Coins
	 * 
	 * @param balance
	 *            int of the amount to find
	 * @param coinIndex
	 * @param change
	 *            List of Coins
	 * @return boolean for if the change has been found
	 */
	private boolean getChange(int balance, int coinIndex, List<Coin> change) {
		// check that the request index isn't larger than the array of Coins
		if (coinIndex >= coins.length) {
			return false;
		}

		// get the Coin for the current index
		Coin coin = coins[coinIndex];

		// corner case -.- fix by m68k, if there are no coins available for the current
		// denomination we will try the next one.
		// and check for the corner case that the coin is more worth than the change
		// requested.
		if (coin.count <= 0 || coin.value > balance) {
			if (coinIndex + 1 < coins.length) {
				// at least one more denomination is available
				return getChange(balance, coinIndex + 1, change);
			} else {
				// thats bad, we have no other denominations left, return false to abort
				return false;
			}
		} else {
			// get either the total amount of this Coin can go into the balance,
			// or
			// the total number of this Coin available, whichever is least.
			int count = Math.min(coin.count, balance / coin.value);

			// loop from this point
			for (int i = count; i > 0; i--) {
				// update the balance for the total remaining after the amount
				// used
				// with this coin is taken
				int newBalance = balance - i * coin.value;

				// if the new balance is 0, then we have found enough
				if (newBalance == 0) {
					change.add(new Coin(coin.value, i));
					return true;
				}

				// if we haven't, then get the change using the new balance and
				// the
				// next Coin denomination
				if (getChange(newBalance, coinIndex + 1, change)) {
					// if getting the lesser value returns true, then add the
					// number
					// of this coin to the change.
					if (i > 0) {
						change.add(new Coin(coin.value, i));
					}
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		int[] values = { 200, 100, 50, 20, 10 };
		int[] counts = {};

		counts = new int[] { 1, 3, 0, 0, 0 }; // 1 x 2e, 2 x 1e

		Coin[] coins = new Coin[values.length];
		for (int i = 0; i < values.length; i++) {
			coins[i] = new Coin(values[i], counts[i]);
		}
		CoinDenomination cd = new CoinDenomination(coins);
		int amount = 200;
		System.out.println(amount + ": " + cd.getChange(amount));
	}

	public static class Coin {

		private int value;
		private int count;

		public Coin(int value, int count) {
			this.value = value;
			this.count = count;
		}

		@Override
		public String toString() {
			return "(" + value + "x" + count + ")";
		}
		
		public int getCount() {
			return count;
		}
		
		public int getValue() {
			return value;
		}
	}
}