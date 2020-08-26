package model;

abstract public class PandemicModelEvent {

	public enum PMEType {ADVANCED, RESET, DEATH, INFECTION, RECOVERY};

	// Premade singletons for simple events.
	
	public static final PandemicModelEvent ADVANED_EVENT = new PMEAdvanced();
	public static final PandemicModelEvent RESET_EVENT = new PMEReset();
	
	private PMEType _type;
	
	protected PandemicModelEvent(PMEType type) {
		_type = type;
	}
	
	public PMEType getType() {
		return _type;
	}
}

class PMEAdvanced extends PandemicModelEvent {
	protected PMEAdvanced() {
		super(PMEType.ADVANCED);
	}
}

class PMEReset extends PandemicModelEvent {
	protected PMEReset() {
		super(PMEType.RESET);
	}
}

abstract class PMEPersonalEvent extends PandemicModelEvent {
	private Person _person;
	private double _time;
	
	protected PMEPersonalEvent(PMEType type, Person p, double time) {
		super(type);
		_person = p;
		_time = time;
	}
	
	public Person getPerson() {
		return _person;
	}
	
	public double getTime() {
		return _time;
	}
}

class PMEDeath extends PMEPersonalEvent {
	protected PMEDeath(Person p, double time) {
		super(PMEType.DEATH, p, time);
	}
}

class PMEInfection extends PMEPersonalEvent {
	protected PMEInfection(Person p, double time) {
		super(PMEType.INFECTION, p, time);
	}
}

class PMERecovery extends PMEPersonalEvent {
	protected PMERecovery(Person p, double time) {
		super(PMEType.RECOVERY, p, time);
	}
}