package at.metalab.payoutsim.tests;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import at.metalab.payoutsim.Kassomat.ChannelSetup;
import at.metalab.payoutsim.Kassomat.Monies;
import at.metalab.payoutsim.Utils;
import at.metalab.payoutsim.Utils.PayoutResult;

public class TestPayout {

	private static Monies monies = null;
	private static ChannelSetup channelSetup = null;

	@BeforeClass
	public static void setup() {
		final ChannelSetup hopperSetup = new ChannelSetup();
		hopperSetup.setValueInChannel(1, 10);
		hopperSetup.setValueInChannel(2, 20);
		hopperSetup.setValueInChannel(3, 50);
		hopperSetup.setValueInChannel(4, 100);
		hopperSetup.setValueInChannel(5, 200);

		final Monies hopperMonies = new Monies(hopperSetup);
		hopperMonies.setAmount(1, 2); // 2 x 0.10 euro
		hopperMonies.setAmount(2, 1); // 1 x 0.20 euro
		hopperMonies.setAmount(3, 3); // 3 x 0.50 euro
		hopperMonies.setAmount(4, 3); // 3 x 1 euro
		hopperMonies.setAmount(5, 2); // 2 x 2 euro

		channelSetup = hopperSetup;
		monies = hopperMonies;
	}

	@Test
	public void testTotalAmount() {
		{
			Monies m = new Monies(channelSetup);
			m.setAmount(1, 2);
			Assert.assertEquals(20, m.getTotalAmount());
		}

		{
			Monies m = new Monies(channelSetup);
			m.setAmount(2, 3);
			Assert.assertEquals(60, m.getTotalAmount());
		}

		{
			Monies m = new Monies(channelSetup);
			m.setAmount(3, 2);
			Assert.assertEquals(100, m.getTotalAmount());
		}

		{
			Monies m = new Monies(channelSetup);
			m.setAmount(4, 5);
			Assert.assertEquals(500, m.getTotalAmount());
		}

		{
			Monies m = new Monies(channelSetup);
			m.setAmount(4, 2);
			m.setAmount(5, 8);
			Assert.assertEquals(1800, m.getTotalAmount());
		}
	}

	@Test
	public void testNotEnoughValue() {
		Monies m = new Monies(channelSetup);
		m.setAmount(4, 2);
		m.setAmount(5, 8);

		Assert.assertEquals(1800, m.getTotalAmount());
		Assert.assertEquals(Utils.PayoutResult.ERR_NOT_ENOUGH_MONEY,
				Utils.testPayout(9999, m));
	}

	@Test
	public void testPayout() {
		Monies m = new Monies(channelSetup);
		m.setAmount(4, 2);
		m.setAmount(5, 1);

		Assert.assertEquals(PayoutResult.ERR_CANT_PAY_EXACT_AMOUNT, Utils.testPayout(50, m));
		Assert.assertEquals(PayoutResult.OK, Utils.testPayout(100, m));
		Assert.assertEquals(PayoutResult.OK, Utils.testPayout(200, m));
		Assert.assertEquals(PayoutResult.OK, Utils.testPayout(300, m));
		Assert.assertEquals(PayoutResult.OK, Utils.testPayout(400, m));
		Assert.assertEquals(PayoutResult.ERR_NOT_ENOUGH_MONEY, Utils.testPayout(401, m));
	}
}
