import java.util.Scanner;

public class PayoutSim{

	public static void main (String [] args) {
		
		System.out.println("PayoutSim initialised");
		
		String[] eventsValidator = {"{\"event\":\"fraud attempt\",\"dispensed\":%d}",
			"{\"event\":\"read\",\"amount\":%d,\"channel\":%d}", 
			"{\"event\":\"credit\",\"amount\":%d,\"channel\":%d}",
			"{\"event\":\"incomplete payout\",\"dispensed\":%d,\"requested\":%d,\"cc\":\"%s\"}",
			"{\"event\":\"incomplete float\",\"dispensed\":%d,\"requested\":%d,\"cc\":\"%s\"}",
			"{\"event\":\"reading\"}",
			"{\"event\":\"reading\"}",
			"{\"event\":\"emptying\"}",
			"{\"event\":\"rejecting\"}",
			"{\"event\":\"rejected\"}",
			"{\"event\":\"stacking\"}",
			"{\"event\":\"stored\"}",
			"{\"event\":\"stacked\"}",
			"{\"event\":\"safe jam\"}",
			"{\"event\":\"unsafe jam\"}",
			"{\"event\":\"disabled\"}",
			"{\"event\":\"stacker full\"}",
			"{\"event\":\"cashbox removed\"}",
			"{\"event\":\"cashbox replaced\"}",
			"{\"event\":\"cleared from front\"}",
			"{\"event\":\"cleared into cashbox\"}",
			"{\"event\":\"calibration fail\",\"error\":\"no error\"}",
			"{\"event\":\"calibration fail\",\"error\":\"sensor flap\"}",
			"{\"event\":\"calibration fail\",\"error\":\"sensor exit\"}",
			"{\"event\":\"calibration fail\",\"error\":\"sensor coil 1\"}",
			"{\"event\":\"calibration fail\",\"error\":\"sensor coil 2\"}",
			"{\"event\":\"calibration fail\",\"error\":\"not initialized\"}",
			"{\"event\":\"calibration fail\",\"error\":\"checksum error\"}",
			"{\"event\":\"recalibrating\"}"
		};

		String[] eventsHopper = {"{\"event\":\"read\",\"channel\":%d}",
			"{\"event\":\"dispensing\",\"amount\":%d}",
			"{\"event\":\"dispensed\",\"amount\":%d}",
			"{\"event\":\"cashbox paid\",\"amount\":%d,\"cc\":\"%s\"}",
			"{\"event\":\"smart emptying\",\"amount\":%d,\"cc\":\"%s\"}",
			"{\"event\":\"smart emptied\",\"amount\":%d,\"cc\":\"%s\"}",
			"{\"event\":\"credit\",\"channel\":%d,\"cc\":\"%s\"}",
			"{\"event\":\"coin credit\",\"amount\":%d,\"cc\":\"%s\"}",
			"{\"event\":\"incomplete payout\",\"dispensed\":%d,\"requested\":%d,\"cc\":\"%s\"}",
			"{\"event\":\"incomplete float\",\"dispensed\":%d,\"requested\":%d,\"cc\":\"%s\"}",
			"{\"event\":\"jammed\"}",
			"{\"event\":\"empty\"}",
			"{\"event\":\"emptying\"}",
			"{\"event\":\"reading\"}",
			"{\"event\":\"disabled\"}",
			"{\"event\":\"calibration fail\",\"error\":\"no error\"}",
			"{\"event\":\"calibration fail\",\"error\":\"sensor flap\"}",
			"{\"event\":\"calibration fail\",\"error\":\"sensor exit\"}",
			"{\"event\":\"calibration fail\",\"error\":\"sensor coil 1\"}",
			"{\"event\":\"calibration fail\",\"error\":\"sensor coil 2\"}",
			"{\"event\":\"calibration fail\",\"error\":\"not initialized\"}",
			"{\"event\":\"calibration fail\",\"error\":\"checksum error\"}",
			"{\"event\":\"recalibrating\"}"
		};

		int amount = 0;
		int channelid = 2;
		int arrayID = 2;
		int hardware = 0;
		String out = "test";
		
		 // create a scanner so we can read the command-line input
		Scanner scanner = new Scanner(System.in);
		
		// prompt for Hopper or Validator
		System.out.print("Enter 1 for Validator Event or 2 for Hopper Event \n");

		// set hardware
		hardware = scanner.nextInt();

		//  prompt for wished Event
		System.out.print("Enter Event you wish\n2 is event credit\n");

		// get wished Event as arrayID
		arrayID = scanner.nextInt();

		// prompt for wished amount
		System.out.print("Enter amount in cent:\n");

		// set amount
		amount = scanner.nextInt();
		
		if(arrayID == 0){
			System.out.printf(eventsValidator[arrayID]+"\n",amount);
		}
		else if(arrayID == 1 || arrayID == 2){
			System.out.printf(eventsValidator[arrayID]+"\n",amount,channelid);
		}
		else if(arrayID == 3 || arrayID == 4){
			System.out.printf(eventsValidator[arrayID]+"\n",amount,channelid, out);
		}
		else{
			System.out.printf(eventsValidator[arrayID]+"\n");
		}

		
		String output = "{"+eventsValidator[0]+",\"amount\":"+amount+",\"channel\":"+channelid+"}";
		
	//	System.out.printf(eventsValidator[arrayID]+"\n",amount,channelid);
		
		
		
		
	}

}
