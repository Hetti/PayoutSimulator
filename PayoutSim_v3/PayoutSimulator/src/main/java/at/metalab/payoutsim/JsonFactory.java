package at.metalab.payoutsim;


/**
 * Factory class for creating JSON objects.
 * @author m68k
 *
 */

public class JsonFactory {

	private static KassomatJson event(String event) {
		KassomatJson e = new KassomatJson();
		e.event = event;

		return e;
	}

	public static KassomatJson read(int channel) {
		KassomatJson e = event("read");
		e.channel = String.valueOf(channel);
		return e;
	}

	public static KassomatJson reading() {
		return event("reading");
	}

	public static KassomatJson dispensing(int amount) {
		KassomatJson e = event("dispensing");
		e.amount = amount;
		return e;
	}

	public static KassomatJson dispensed(int amount) {
		KassomatJson e = event("dispensed");
		e.amount = amount;
		return e;
	}

	public static KassomatJson jammed() {
		return event("jammed");
	}

	public static KassomatJson coinCredit(int amount, String cc) {
		KassomatJson e = event("coin credit");
		e.amount = amount;
		e.cc = cc;
		return e;
	}

	public static KassomatJson empty() {
		return event("empty");
	}

	public static KassomatJson emptying() {
		return event("emptying");
	}

	public static KassomatJson smartEmptying(int amount, String cc) {
		KassomatJson e = event("smart emptying");
		e.amount = amount;
		e.cc = cc;
		return e;
	}

	public static KassomatJson smartEmptied(int amount, String cc) {
		KassomatJson e = event("smart emptied");
		e.amount = amount;
		e.cc = cc;
		return e;
	}

	public static KassomatJson credit(int amount, int channel) {
		KassomatJson e = event("credit");
		e.channel = String.valueOf(channel);
		e.amount = amount;
		return e;
	}

	public static KassomatJson disabled() {
		return event("disabled");
	}

	public static KassomatJson calibrationFail(String error) {
		KassomatJson e = event("calibration fail");
		e.error = error;
		return e;
	}

	public static KassomatJson recalibrating() {
		return event("recalibrating");
	}

	public static KassomatJson cashboxPaid(int amount, String cc) {
		KassomatJson e = event("cashbox paid");
		e.amount = amount;
		e.cc = cc;
		return e;
	}

	public static KassomatJson incompletePayout(int dispensed, int requested,
			String cc) {
		KassomatJson e = event("incomplete payout");
		e.dispensed = dispensed;
		e.requested = requested;
		e.cc = cc;
		return e;
	}

	public static KassomatJson incompleteFloat(int dispensed, int requested,
			String cc) {
		KassomatJson e = event("incomplete float");
		e.dispensed = dispensed;
		e.requested = requested;
		e.cc = cc;
		return e;
	}

	public static KassomatJson rejecting() {
		return event("rejecting");
	}

	public static KassomatJson rejected() {
		return event("rejected");
	}

	public static KassomatJson stacking() {
		return event("stacking");
	}

	public static KassomatJson stored() {
		return event("stored");
	}

	public static KassomatJson stacked() {
		return event("stacked");
	}

	public static KassomatJson safeJam() {
		return event("safe jam");
	}

	public static KassomatJson unsafeJam() {
		return event("unsafe jam");
	}

	public static KassomatJson stackerFull() {
		return event("stacker full");
	}

	public static KassomatJson cashboxRemoved() {
		return event("cashbox removed");
	}

	public static KassomatJson cashboxReplaced() {
		return event("cashbox replaced");
	}

	public static KassomatJson clearedFromFront() {
		return event("cleared from front");
	}

	public static KassomatJson clearedIntoCashbox() {
		return event("cleared into cashbox");
	}

	public static KassomatJson fraudAttempt(int dispensed) {
		KassomatJson e = event("fraud attempt");
		e.dispensed = dispensed;
		return e;

	}

}
