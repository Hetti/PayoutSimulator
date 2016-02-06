package at.metalab.payoutsim;

public class Simulations {

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
