package at.metalab.payoutsim;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class LevelJson {

	public int value;

	public int level;

	public String cc;

}
