package at.metalab.payoutsim;

import java.math.BigDecimal;

public class Utils {

	public static String amountReadable(int amount) {
		BigDecimal bd = new BigDecimal(amount).movePointLeft(2).setScale(2);
		return String.format("€ %s", bd.toPlainString());
	}
}
