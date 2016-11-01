package at.metalab.payoutsim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Random;
import java.util.Set;

import org.redisson.core.RTopic;

public class Kassomat {

	private final RTopic<String> hopperRequest;

	private final RTopic<String> hopperResponse;

	private final RTopic<String> hopperEvent;

	private final RTopic<String> validatorRequest;

	private final RTopic<String> validatorResponse;

	private final RTopic<String> validatorEvent;

	private final ChannelSetup validatorSetup;

	private final ChannelSetup hopperSetup;

	private final Monies validatorMonies;

	private final Monies hopperMonies;

	public static class ChannelSetup {

		private Map<Integer, Integer> valueByChannel = new HashMap<Integer, Integer>();

		private Map<Integer, Integer> channelByValue = new HashMap<Integer, Integer>();

		private Map<Integer, Boolean> inhibits = new HashMap<Integer, Boolean>();

		public void setInhibited(int channel, boolean inhibited) {
			synchronized (inhibits) {
				inhibits.put(channel, inhibited);
			}
		}

		public boolean isInhibited(int channel) {
			synchronized (inhibits) {
				return inhibits.get(channel);
			}
		}

		public void setValueInChannel(int channel, int value) {
			valueByChannel.put(channel, value);
			channelByValue.put(value, channel);
		}

		public int getValue(int channel) {
			return valueByChannel.get(channel);
		}

		public int getChannel(int value) {
			return channelByValue.get(value);
		}

		public Set<Integer> getChannels() {
			return valueByChannel.keySet();
		}

		public List<Integer> getChannelsByDescendingValue() {
			List<Map.Entry<Integer, Integer>> sortedEntries = new LinkedList<Map.Entry<Integer, Integer>>(
					channelByValue.entrySet());
			Collections.sort(sortedEntries, BY_VALUE_DESC);

			List<Integer> channels = new ArrayList<Integer>();
			for (Map.Entry<Integer, Integer> e : sortedEntries) {
				channels.add(e.getValue());
			}

			return channels;
		}

		private static Comparator<Map.Entry<Integer, Integer>> BY_VALUE_DESC = new Comparator<Map.Entry<Integer, Integer>>() {
			@Override
			public int compare(Entry<Integer, Integer> o1,
					Entry<Integer, Integer> o2) {
				return o2.getValue().compareTo(o1.getValue()); // o2 compare o1!
																// descending!
			}
		};
	}

	private static class Observable2 extends Observable {
		@Override
		public synchronized void setChanged() {
			super.setChanged();
		}
	}

	public static class Monies {

		private Map<Integer, Integer> amountByChannel = new HashMap<Integer, Integer>();

		private ChannelSetup channelSetup;

		private Observable2 moneyChange = new Observable2();

		public Monies(ChannelSetup channelSetup) {
			this.channelSetup = channelSetup;
			for (Integer channel : channelSetup.getChannels()) {
				amountByChannel.put(channel, 0);
			}
		}

		public void setAmount(int channel, int amount) {
			synchronized (amountByChannel) {
				amountByChannel.put(channel, amount);
				
				moneyChange.setChanged();
				moneyChange.notifyObservers(this);
			}
		}

		public void increase(int channel) {
			synchronized (amountByChannel) {
				int currentAmount = amountByChannel.get(channel);
				amountByChannel.put(channel, currentAmount + 1);

				moneyChange.setChanged();
				moneyChange.notifyObservers(this);
			}
		}

		public void decrease(int channel) {
			synchronized (amountByChannel) {
				int currentAmount = amountByChannel.get(channel);
				if (currentAmount <= 0) {
					throw new IllegalArgumentException("channel " + channel
							+ " already at or below zero");
				}

				amountByChannel.put(channel, currentAmount - 1);

				moneyChange.setChanged();
				moneyChange.notifyObservers(this);
			}
		}

		public int getAmount(int channel) {
			return amountByChannel.get(channel);
		}

		public Observable getMoneyChange() {
			return moneyChange;
		}

		public int getTotalAmount() {
			synchronized (amountByChannel) {
				int totalAmount = 0;
				for (Map.Entry<Integer, Integer> entry : amountByChannel
						.entrySet()) {
					totalAmount += channelSetup.getValue(entry.getKey())
							* entry.getValue();
				}
				return totalAmount;
			}
		}

		public String getReadableTotalAmount() {
			return Utils.amountReadable(getTotalAmount());
		}

		@Override
		public String toString() {
			return super.toString() + "(" + getReadableTotalAmount()
					+ ", totalAmount=" + getTotalAmount() + ", "
					+ amountByChannel + ")";
		}

		public ChannelSetup getChannelSetup() {
			return channelSetup;
		}
	}

	public Kassomat(ChannelSetup nvSetup, ChannelSetup hSetup, Monies nvMonies,
			Monies hMonies, RTopic<String> hopperRequest,
			RTopic<String> hopperResponse, RTopic<String> hopperEvent,
			RTopic<String> validatorRequest, RTopic<String> validatorResponse,
			RTopic<String> validatorEvent) {
		this.validatorSetup = nvSetup;
		this.hopperSetup = hSetup;
		this.validatorMonies = nvMonies;
		this.hopperMonies = hMonies;
		this.hopperEvent = hopperEvent;
		this.hopperRequest = hopperRequest;
		this.hopperResponse = hopperResponse;
		this.validatorEvent = validatorEvent;
		this.validatorRequest = validatorRequest;
		this.validatorResponse = validatorResponse;
	}

	public ChannelSetup getValidatorSetup() {
		return validatorSetup;
	}

	public ChannelSetup getHopperSetup() {
		return hopperSetup;
	}

	public RTopic<String> getHopperRequest() {
		return hopperRequest;
	}

	public RTopic<String> getValidatorRequest() {
		return validatorRequest;
	}

	public RTopic<String> getHopperResponse() {
		return hopperResponse;
	}

	public RTopic<String> getHopperEvent() {
		return hopperEvent;
	}

	public RTopic<String> getValidatorResponse() {
		return validatorResponse;
	}

	public RTopic<String> getValidatorEvent() {
		return validatorEvent;
	}

	public Monies getHopperMonies() {
		return hopperMonies;
	}

	public Monies getValidatorMonies() {
		return validatorMonies;
	}

	public void pubValidatorEvent(KassomatJson kassomatJson) {
		getValidatorEvent().publishAsync(kassomatJson.toJson());
	}

	public void pubValidatorResponse(KassomatJson kassomatJson) {
		getValidatorResponse().publishAsync(kassomatJson.toJson());
	}

	public void pubHopperEvent(KassomatJson kassomatJson) {
		getHopperEvent().publishAsync(kassomatJson.toJson());
	}

	public void pubHopperResponse(KassomatJson kassomatJson) {
		getHopperResponse().publishAsync(kassomatJson.toJson());
	}

	public int getTotalAmount() {
		return hopperMonies.getTotalAmount() + validatorMonies.getTotalAmount();
	}

	public String getReadableTotalAmount() {
		return String.format("%s (notes: %s, coins: %s)", Utils
				.amountReadable(getTotalAmount()), getValidatorMonies()
				.getReadableTotalAmount(), getHopperMonies()
				.getReadableTotalAmount());
	}

	private final List<Runnable> steps = Collections
			.synchronizedList(new ArrayList<Runnable>());

	private final List<Runnable> stepsOnce = Collections
			.synchronizedList(new ArrayList<Runnable>());

	public synchronized void poll() {
		synchronized (stepsOnce) {
			Iterator<Runnable> i = stepsOnce.iterator();

			while (i.hasNext()) {
				Runnable step = i.next();
				i.remove();

				step.run();
				synchronized (step) {
					step.notify();
				}

			}
		}

		synchronized (steps) {
			Iterator<Runnable> i = steps.iterator();

			while (i.hasNext()) {
				Runnable step = i.next();
				step.run();
			}
		}
	}

	public void waitFor(Runnable runnable) throws InterruptedException {
		Thread t = new Thread(runnable, "simulation-runner");
		t.start();
		t.join();
	}

	public void runOnce(Runnable step) throws InterruptedException {
		registerOnce(step);
		synchronized (step) {
			step.wait();
		}
	}

	public void runFor(int minMillis, int maxMillis, Runnable step)
			throws InterruptedException {
		try {
			register(step);
			step.run(); // guarantees execution at least once (regardless of
						// poll interval)
			waitRnd(minMillis, maxMillis);
		} finally {
			unregister(step);
		}
	}

	private final static Random RND = new Random();

	public void waitRnd(int minMillis, int maxMillis)
			throws InterruptedException {
		int min = Math.min(minMillis, maxMillis);
		int max = Math.max(minMillis, maxMillis);

		int p = max - min;
		int r;
		if (p > 0) {
			r = RND.nextInt(p);
		} else {
			r = 0;
		}

		Thread.sleep(min + r);
	}

	private void registerOnce(Runnable step) {
		synchronized (stepsOnce) {
			stepsOnce.add(step);
		}
	}

	private void register(Runnable step) {
		synchronized (steps) {
			steps.add(step);
		}
	}

	private void unregister(Runnable step) {
		synchronized (steps) {
			steps.remove(step);
		}
	}
}
