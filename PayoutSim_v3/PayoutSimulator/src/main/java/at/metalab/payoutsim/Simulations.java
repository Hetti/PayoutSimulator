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
								kassomat.pubHopperEvent(JsonFactory
										.dispensing(finalDispensed));
							}
						});
						kassomat.getHopperMonies().decrease(
								kassomat.getHopperMonies().getChannelSetup().getChannel(coin));
					}

					kassomat.runOnce(new Runnable() {
						@Override
						public void run() {
							kassomat.pubHopperEvent(JsonFactory.cashboxPaid(
									0, "EUR"));
						}
					});

					final int finalDispensed = dispensed; // -.-
					kassomat.runOnce(new Runnable() {
						@Override
						public void run() {
							kassomat.pubHopperEvent(JsonFactory
									.dispensed(finalDispensed));
						}
					});
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

					if(kassomat.getValidatorSetup().isInhibited(channel)) {
						// this type of banknote is not accepted in the moment,
						// simulate rejection.
						
						kassomat.runFor(1000, 3000, new Runnable() {
							@Override
							public void run() {
								kassomat.pubValidatorEvent(JsonFactory.rejecting());
							}
						});

						kassomat.runOnce(new Runnable() {
							
							@Override
							public void run() {
								kassomat.pubValidatorEvent(JsonFactory
										.rejected());
							}
						});
					} else {
						// all fine, this type of banknote is not inhibited now,
						// we will simulate acceptance.
						
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
					}
					
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
							kassomat.pubHopperEvent(JsonFactory.coinCredit(amount,
									"EUR"));
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
