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
import at.metalab.payoutsim.Utils.PayoutResult;

import com.mycompany.payoutsimulator.PayoutFrame;

public class PayoutSimMain {

	public static void main(String[] args) throws Exception {
		Config c = new Config();
		c.setCodec(org.redisson.client.codec.StringCodec.INSTANCE);
		c.useSingleServer().setAddress("127.0.0.1:6379");

		// use "redis-cli monitor" in the shell to watch the show

		RedissonClient r = Redisson.create(c);

		// log all messages
		Redisson.create(c).<String> getPatternTopic("*").addListener(
				new PatternMessageListener<String>() {
					@Override
					public void onMessage(String pattern, String channel,
							String msg) {
						System.out.println("[redis-topic: '" + channel + "'] "
								+ msg);
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

		// inhibit all channels
		validatorSetup.setInhibited(1, true);
		validatorSetup.setInhibited(2, true);
		validatorSetup.setInhibited(3, true);
		validatorSetup.setInhibited(4, true);
		validatorSetup.setInhibited(5, true);
		validatorSetup.setInhibited(6, true);
		validatorSetup.setInhibited(7, true);
		
		final ChannelSetup hopperSetup = new ChannelSetup();
		hopperSetup.setValueInChannel(1, 10);
		hopperSetup.setValueInChannel(2, 20);
		hopperSetup.setValueInChannel(3, 50);
		hopperSetup.setValueInChannel(4, 100);
		hopperSetup.setValueInChannel(5, 200);
		
		final Monies validatorMonies = new Monies(validatorSetup);
		/*
		validatorMonies.setAmount(1, 3); // 3 x 5 euro
		validatorMonies.setAmount(2, 2); // 2 x 10 euro
		validatorMonies.setAmount(3, 1); // 1 x 20 euro
		validatorMonies.setAmount(4, 1); // 1 x 50 euro
		*/

		final Monies hopperMonies = new Monies(hopperSetup);
		/*
		hopperMonies.setAmount(1, 10); // 10 x 10 cent
		hopperMonies.setAmount(2, 5); // 5 x 20 cent
		hopperMonies.setAmount(3, 4); // 4 x 50 cent
		hopperMonies.setAmount(4, 5); // 5 x 1 euro
		hopperMonies.setAmount(5, 3); // 3 x 2 euro
		 */
		
		// We need all topics
		final Kassomat kassomat = new Kassomat(validatorSetup, hopperSetup,
				validatorMonies, hopperMonies,
				r.<String> getTopic("hopper-request"),
				r.<String> getTopic("hopper-response"),
				r.<String> getTopic("hopper-event"),
				r.<String> getTopic("validator-request"),
				r.<String> getTopic("validator-response"),
				r.<String> getTopic("validator-event"));

		System.out.println("amount in kassomat: "
				+ kassomat.getReadableTotalAmount());

		kassomat.getValidatorRequest().addListener(
				new MessageListener<String>() {

					private void setInhibit(int channel, String channels) {
						if(channels.contains(String.valueOf(channel))) {
							kassomat.getValidatorSetup().setInhibited(channel, true);
						} else {
							kassomat.getValidatorSetup().setInhibited(channel, false);
						}
					}
					
					@Override
					public void onMessage(String topic, String message) {
						KassomatJson cmd = KassomatJson.fromJson(message);
						KassomatJson response = JsonFactory.response(cmd);

						switch (cmd.cmd) {
						case "disable-channels":
						case "enable-channels":
							synchronized (kassomat.getValidatorSetup()) {
								String channels = cmd.channels;
								setInhibit(1, channels);
								setInhibit(2, channels);
								setInhibit(3, channels);
								setInhibit(4, channels);
								setInhibit(5, channels);
								setInhibit(6, channels);
								setInhibit(7, channels);
							};
							
							response.result="ok";
							break;
						case "do-payout":
							break;
						case "test-payout":
							break;
						case "smart-empty":
							break;
						case "disable":
							break;
						case "enable":
							break;
						default:
							response.error = "unknown command";
						}

						kassomat.pubValidatorResponse(response);
					}
				});

		kassomat.getHopperRequest().addListener(new MessageListener<String>() {

			@Override
			public void onMessage(String topic, String message) {
				KassomatJson cmd = KassomatJson.fromJson(message);
				KassomatJson response = JsonFactory.response(cmd);

				switch (cmd.cmd) {
				case "do-payout": {
					PayoutResult payoutResult = Utils.testPayout(cmd.amount,
							hopperMonies);

					switch (payoutResult) {
					case OK:
						response.result = "ok";
						kassomat.pubHopperResponse(response);

						List<Integer> coins = Utils.generatePayout(cmd.amount, hopperMonies);
						new Thread() {
							public void run() {
								try {
									Simulations.dispenseCoins(kassomat, coins);
								}
								catch(InterruptedException interruptedException) {
									interruptedException.printStackTrace();
								}
							};
						}.start();
						
						break;
					case ERR_CANT_PAY_EXACT_AMOUNT:
						response.error = "can't pay exact amount";
						kassomat.pubHopperResponse(response);
						break;
					case ERR_NOT_ENOUGH_MONEY:
						response.error = "not enough value in smart payout";
						kassomat.pubHopperResponse(response);
						break;
					default:
						response.error = "unknown";
						kassomat.pubHopperResponse(response);
					}

					break;
				}
				case "test-payout": {
					PayoutResult payoutResult = Utils.testPayout(cmd.amount,
							hopperMonies);

					switch (payoutResult) {
					case OK:
						response.result = "ok";
						kassomat.pubHopperResponse(response);
						break;
					case ERR_CANT_PAY_EXACT_AMOUNT:
						response.error = "can't pay exact amount";
						kassomat.pubHopperResponse(response);
						break;
					case ERR_NOT_ENOUGH_MONEY:
						response.error = "not enough value in smart payout";
						kassomat.pubHopperResponse(response);
						break;
					default:
						response.error = "unknown";
						kassomat.pubHopperResponse(response);
					}

					break;
				}
				case "get-all-levels": {
					response.result = "ok";

					LevelJson levels[] = new LevelJson [kassomat.getHopperMonies().getChannelSetup().getChannels().size()];
					
					int l = 0;
					for(int channel : kassomat.getHopperMonies().getChannelSetup().getChannels()) {
						LevelJson levelJson = new LevelJson();
						levelJson.cc = "EUR";
						levelJson.level = kassomat.getHopperMonies().getAmount(channel);
						levelJson.value = kassomat.getHopperMonies().getChannelSetup().getValue(channel);
						
						levels[l] = levelJson;
						l++;
					}
					
					response.levels = levels;
						
					kassomat.pubHopperResponse(response);
					break;
					
				}
				case "set-denomination-level": {
					response.result = "ok";
					kassomat.getHopperMonies().setAmount(
							kassomat.getHopperMonies().getChannelSetup().getChannel(cmd.amount),
							cmd.level);

					kassomat.pubHopperResponse(response);
					break;
				}
				case "smart-empty":
					response.result = "ok";
					kassomat.pubHopperResponse(response);
					break;
					
				case "enable":
					break;
					
				default:
					System.out.println("unknown command: '" + cmd.cmd + "'");
					response.error = "unknown command";
				}

			}
		});

		kassomat.getValidatorEvent().addListener(new MessageListener<String>() {

			@Override
			public void onMessage(String topic, String message) {
				KassomatJson event = KassomatJson.fromJson(message);
				
				switch (event.event) {
				case "credit":
					validatorMonies.increase(Integer.parseInt(event.channel));
					System.out.println("amount in kassomat now: "
							+ kassomat.getReadableTotalAmount());
					break;
				default:
					break;
				}
			}
		});

		kassomat.getHopperEvent().addListener(new MessageListener<String>() {

			@Override
			public void onMessage(String topic, String message) {
				KassomatJson event = KassomatJson.fromJson(message);
				if ("coin credit".equals(event.event)) {
					hopperMonies.increase(hopperSetup.getChannel(event.amount));
					System.out.println("amount in kassomat now: "
							+ kassomat.getReadableTotalAmount());
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
