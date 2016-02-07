package at.metalab.payoutsim;

import java.util.List;

import javafx.embed.swing.JFXPanel;

import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.RedissonClient;
import org.redisson.core.MessageListener;
import org.redisson.core.PatternMessageListener;

import at.metalab.payoutsim.Kassomat.ChannelSetup;
import at.metalab.payoutsim.Kassomat.Monies;

import com.mycompany.payoutsimulator.PayoutFrame;

public class PayoutSimMain {

	public static void main(String[] args) throws Exception {
		Config c = new Config();
		c.setCodec(org.redisson.client.codec.StringCodec.INSTANCE);
		c.useSingleServer().setAddress("127.0.0.1:6379");

		// use "redis-cli monitor" in the shell to watch the show

		RedissonClient r = Redisson.create(c);

		// log all messages
		r.<String> getPatternTopic("*").addListener(
				new PatternMessageListener<String>() {
					@Override
					public void onMessage(String pattern, String channel,
							String msg) {
						System.out.println("[redis-topic: '" + channel + "'] " + msg);
					}
				});

		final ChannelSetup validatorSetup = new ChannelSetup();
		validatorSetup.setValueInChannel(1, 500);
		validatorSetup.setValueInChannel(2, 1000);
		validatorSetup.setValueInChannel(3, 2000);
		validatorSetup.setValueInChannel(4, 5000);
		validatorSetup.setValueInChannel(5, 10000);
		validatorSetup.setValueInChannel(6, 20000);
		validatorSetup.setValueInChannel(7, 50000);

		final ChannelSetup hopperSetup = new ChannelSetup();
		hopperSetup.setValueInChannel(1, 10);
		hopperSetup.setValueInChannel(2, 20);
		hopperSetup.setValueInChannel(3, 50);
		hopperSetup.setValueInChannel(4, 100);
		hopperSetup.setValueInChannel(5, 200);

		final Monies validatorMonies = new Monies(validatorSetup);
		validatorMonies.setAmount(1, 3); // 3 x 5 euro
		validatorMonies.setAmount(2, 2); // 2 x 10 euro
		validatorMonies.setAmount(3, 1); // 1 x 20 euro

		final Monies hopperMonies = new Monies(hopperSetup);
		hopperMonies.setAmount(1, 2); // 1 x 0.10 euro
		hopperMonies.setAmount(2, 1); // 2 x 0.20 euro
		hopperMonies.setAmount(3, 5); // 3 x 0.50 euro

		// We need all topics
		final Kassomat kassomat = new Kassomat(validatorSetup, hopperSetup,
				validatorMonies, hopperMonies,
				r.<String> getTopic("hopper-request"),
				r.<String> getTopic("hopper-response"),
				r.<String> getTopic("hopper-event"),
				r.<String> getTopic("validator-request"),
				r.<String> getTopic("validator-response"),
				r.<String> getTopic("validator-event"));

		System.out.println("amount in kassomat: " + kassomat.getReadableTotalAmount());
		
		kassomat.getValidatorEvent().addListener(new MessageListener<String>() {

			@Override
			public void onMessage(String topic, String message) {
				KassomatJson event = KassomatJson.fromJson(message);
				if ("do-payout".equals(event.event)) {
				} else if ("test-payout".equals(event.event)) {
				} else if ("smart-empty".equals(event.event)) {
				}
			}
		});

		kassomat.getHopperEvent().addListener(new MessageListener<String>() {

			@Override
			public void onMessage(String topic, String message) {
				KassomatJson event = KassomatJson.fromJson(message);
				if ("do-payout".equals(event.event)) {
				} else if ("test-payout".equals(event.event)) {
				} else if ("smart-empty".equals(event.event)) {
				}
			}
		});

		kassomat.getValidatorEvent().addListener(new MessageListener<String>() {

			@Override
			public void onMessage(String topic, String message) {
				KassomatJson event = KassomatJson.fromJson(message);
				if ("credit".equals(event.event)) {
					validatorMonies.increase(Integer.parseInt(event.channel));
					System.out.println("amount in kassomat now: " + kassomat.getReadableTotalAmount());
				}
			}
		});

		kassomat.getHopperEvent().addListener(new MessageListener<String>() {

			@Override
			public void onMessage(String topic, String message) {
				KassomatJson event = KassomatJson.fromJson(message);
				if ("credit".equals(event.event)) {
					hopperMonies.increase(hopperSetup.getChannel(event.amount));
					System.out.println("amount in kassomat now: " + kassomat.getReadableTotalAmount());
				}
			}
		});

		// This simulates SMART Payout polling via libevent
		Runnable poller = new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						Thread.sleep(1000);
						kassomat.poll();
					}
				} catch (InterruptedException interruptedException) {
					interruptedException.printStackTrace(System.err);
				}
			}
		};

		Thread pollerThread = new Thread(poller);
		pollerThread.setDaemon(true);
		pollerThread.start();

		// startup the JFX gui
		startupGui(kassomat);
	}

	private static void startupGui(Kassomat kassomat) {
		/* Set the Nimbus look and feel */
		// <editor-fold defaultstate="collapsed"
		// desc=" Look and feel setting code (optional) ">
		/*
		 * If Nimbus (introduced in Java SE 6) is not available, stay with the
		 * default look and feel. For details see
		 * http://download.oracle.com/javase
		 * /tutorial/uiswing/lookandfeel/plaf.html
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
					.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(PayoutFrame.class.getName())
					.log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(PayoutFrame.class.getName())
					.log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(PayoutFrame.class.getName())
					.log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(PayoutFrame.class.getName())
					.log(java.util.logging.Level.SEVERE, null, ex);
		}
		// </editor-fold>
		// </editor-fold>

		JFXPanel fxPanel = new JFXPanel();
		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				PayoutFrame payoutFrame = new PayoutFrame();
				payoutFrame.setKassomat(kassomat);
				payoutFrame.setVisible(true);
			}
		});

	}
}
