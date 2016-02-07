package at.metalab.payoutsim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.metalab.payoutsim.Kassomat.Monies;

public class Simulations {

	public static void dispenseCoins(final Kassomat kassomat,
			List<Integer> coins) throws InterruptedException {
		Runnable runnable = new Runnable() {

			public void run() {
				try {
					int dispensed = 0;
					List<Integer> shuffeledCoins = new ArrayList<Integer>();
					shuffeledCoins.addAll(coins);
					Collections.shuffle(shuffeledCoins);

					for (Integer coin : coins) {
						dispensed += coin;
						final int finalDispensed = dispensed; // -.-
						kassomat.runFor(500, 2000, new Runnable() {
							@Override
							public void run() {
								kassomat.pubValidatorEvent(JsonFactory
										.dispensing(finalDispensed));
							}
						});
					}

					kassomat.runOnce(new Runnable() {
						@Override
						public void run() {
							kassomat.pubValidatorEvent(JsonFactory.cashboxPaid(
									0, "EUR"));
						}
					});

					final int finalDispensed = dispensed; // -.-
					kassomat.runOnce(new Runnable() {
						@Override
						public void run() {
							kassomat.pubValidatorEvent(JsonFactory
									.dispensed(finalDispensed));
						}
					});

					// not totally accurate i guess, but we reduce the amount of
					// money
					// in the kassomat here after the dispense cycle has
					// completed.
					Monies m = kassomat.getHopperMonies();

					for (Integer coin : shuffeledCoins) {
						m.decrease(m.getChannelSetup().getChannel(coin));
					}
				} catch (InterruptedException interruptedException) {
					interruptedException.printStackTrace(System.err);
				}
			}
		};

		kassomat.waitFor(runnable);
	}

	public static void rejectNote(final Kassomat kassomat)
			throws InterruptedException {
		Runnable runnable = new Runnable() {

			public void run() {
				try {
					kassomat.runFor(1000, 3000, new Runnable() {
						@Override
						public void run() {
							kassomat.pubValidatorEvent(JsonFactory.reading());
						}
					});

					kassomat.runFor(1000, 1500, new Runnable() {
						@Override
						public void run() {
							kassomat.pubValidatorEvent(JsonFactory.rejecting());
						}
					});

					kassomat.runOnce(new Runnable() {
						@Override
						public void run() {
							kassomat.pubValidatorEvent(JsonFactory.rejected());
						}
					});
				} catch (InterruptedException interruptedException) {
					interruptedException.printStackTrace(System.err);
				}
			}
		};

		kassomat.waitFor(runnable);
	}

	public static void insertNote(final Kassomat kassomat, final int amount)
			throws InterruptedException {
		Runnable runnable = new Runnable() {

			public void run() {
				try {
					final int channel = kassomat.getValidatorSetup()
							.getChannel(amount);

					kassomat.runFor(1000, 3000, new Runnable() {
						@Override
						public void run() {
							kassomat.pubValidatorEvent(JsonFactory.reading());
						}
					});

					kassomat.runOnce(new Runnable() {
						@Override
						public void run() {
							kassomat.pubValidatorEvent(JsonFactory
									.read(channel));
						}
					});

					kassomat.runFor(1000, 3000, new Runnable() {
						@Override
						public void run() {
							kassomat.pubValidatorEvent(JsonFactory.stacking());
						}
					});

					kassomat.runOnce(new Runnable() {
						@Override
						public void run() {
							kassomat.pubValidatorEvent(JsonFactory.credit(
									amount, channel));
						}
					});

					kassomat.runFor(1000, 1500, new Runnable() {
						@Override
						public void run() {
							kassomat.pubValidatorEvent(JsonFactory.stacking());
						}
					});

					kassomat.runOnce(new Runnable() {
						@Override
						public void run() {
							kassomat.pubValidatorEvent(JsonFactory.stacked());
						}
					});
				} catch (InterruptedException interruptedException) {
					interruptedException.printStackTrace(System.err);
				}
			}
		};

		kassomat.waitFor(runnable);
	}

	public static void insertCoin(final Kassomat kassomat, final int amount)
			throws InterruptedException {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					kassomat.runOnce(new Runnable() {
						@Override
						public void run() {
							int channel = kassomat.getHopperSetup().getChannel(
									amount);
							kassomat.pubHopperEvent(JsonFactory.credit(amount,
									channel));
						}
					});
				} catch (InterruptedException interruptedException) {
					interruptedException.printStackTrace(System.err);
				}
			};
		};

		kassomat.waitFor(runnable);
	}

}
