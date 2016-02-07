package at.metalab.payoutsim;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
public class KassomatJson {
	public String cmd;
	public String event;
	public String msgId;
	public String correlId;
	public Integer requested;
	public Integer dispensed;
	public Integer amount;
	public String error;
	public String result;
	public String cc;
	public String channel;

	private final static ObjectMapper om = new ObjectMapper();

	public String toJson() {
		try {
			return om.writeValueAsString(this);
		} catch (Exception exception) {
			throw new RuntimeException("problem while marshalling json",
					exception);
		}
	}

	public static KassomatJson fromJson(String json) {
		try {
			return om.readValue(json, KassomatJson.class);
		} catch (Exception exception) {
			throw new RuntimeException("problem while unmarshalling json");
		}
	}
}