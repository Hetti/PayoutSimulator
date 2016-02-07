package at.metalab.payoutsim;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import at.metalab.payoutsim.Kassomat.Monies;
import ben.kn.algorithms.google.CoinDenomination;
import ben.kn.algorithms.google.CoinDenomination.Coin;

public class Utils {

	public static enum PayoutResult {
		ERR_NOT_ENOUGH_MONEY, ERR_CANT_PAY_EXACT_AMOUNT, OK
	}

	public static String amountReadable(int amount) {
		BigDecimal bd = new BigDecimal(amount).movePointLeft(2).setScale(2);
		return String.format("€ %s", bd.toPlainString());
	}

	public static List<Integer> generatePayout(int amount, Monies monies) {
		int numOfChannels = monies.getChannelSetup().getChannels().size();

		int[] counts = new int[numOfChannels];
		int[] values = new int[numOfChannels];

		int j = 0;
		for (Integer channel : monies.getChannelSetup()
				.getChannelsByDescendingValue()) {
			values[j] = monies.getChannelSetup().getValue(channel);
			counts[j] = monies.getAmount(channel);

			j++;
		}

		// stuff from github

		Coin[] coins = new Coin[values.length];
		for (int i = 0; i < values.length; i++) {
			coins[i] = new Coin(values[i], counts[i]);
		}
		CoinDenomination cd = new CoinDenomination(coins);
		List<Coin> change = cd.getChange(amount);

		// return the coins as a list of values (not as a list of value x
		// amount)
		List<Integer> changeValues = new ArrayList<Integer>();
		for (Coin coin : change) {
			for (int i = 0; i < coin.getCount(); i++) {
				changeValues.add(coin.getValue());
			}
		}

		return changeValues;
	}

	public static PayoutResult testPayout(int amount, Monies monies) {
		if (amount > monies.getTotalAmount()) {
			// easy case :D
			return PayoutResult.ERR_NOT_ENOUGH_MONEY;
		}

		if (generatePayout(amount, monies).isEmpty()) {
			return PayoutResult.ERR_CANT_PAY_EXACT_AMOUNT;
		} else {
			return PayoutResult.OK;
		}
	}

}
