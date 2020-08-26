package model;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PandemicModel {
	// Default values for various model properties
	
	public static final double ASYM_PERC = 0.5;			// Chance infected person is asymptomatic
	public static final double DISEASE_DURATION = 10.0; // Duration of disease in simulated days
	public static final double MORTALITY = 0.05;		// Mortality rate of symptomatic people.
	public static final double LOCKDOWN_FACTOR = 0.0;   // Initial lockdown factor
	
	private List<Person> _people;
	private List<Person> _dead_people;
	private List<PandemicModelObserver> _observers;

	// These are set at creation / reset and are not changeable without a reset
	
	private int _init_population;
	private double _scale;
	private double _step_size;

	// Read / write properties
	
	private double _asym_perc;
	private double _disease_duration;
	private double _mortality;
	private double _lockdown_factor;
	
	// Read only properties
	
	private double _time;
	private int _dead_count;
	private int _infected_count;
	private int _recovered_count;
	private double _economic_damage;
	
	
	public PandemicModel(double scale, int population, double step_size) {		
		_observers = new ArrayList<PandemicModelObserver>();
		reset(scale, population, step_size);
	}

	public void reset(double scale, int population, double step_size) {
		if (scale <= 0.0) {
			throw new IllegalArgumentException("Simulation scale must be positive");
		}
		if (population <= 0) {
			throw new IllegalArgumentException("Simulation population must be positive");
		}
		if (step_size <= 0.0) {
			throw new IllegalArgumentException("Simulation step size must be positive");
		}
		
		_init_population = population;
		_scale = scale;
		_step_size = step_size;
		
		_asym_perc = ASYM_PERC;
		_disease_duration = DISEASE_DURATION;
		_mortality = MORTALITY;
		_lockdown_factor = LOCKDOWN_FACTOR;

		_time = 0.0;
		_people = new ArrayList<Person>();
		for (int i=0; i<population; i++) {
			_people.add(Person.makeRandom(this));
		}
		_dead_people = new ArrayList<Person>();

		_economic_damage = 0.0;
		_dead_count = 0;
		_infected_count = 0;
		_recovered_count = 0;
		
		notifyObservers(PandemicModelEvent.RESET_EVENT);
	}

	public double getScale() {
		return _scale;
	}
	
	public double getStepSize() {
		return _step_size;
	}
	
	public int getPopulation() {
		return _init_population;
	}
	
	public double getTime() {
		return _time;
	}
	
	public double getEconomicDamage() {
		return _economic_damage;
	}
	
	public int getDeadCount() {
		return _dead_count;
	}
	
	public int getInfectedCount() {
		return _infected_count;
	}

	public int getRecoveredCount() {
		return _recovered_count;
	}

	public double getLockdownFactor() {
		return _lockdown_factor;
	}
	
	public void setLockdownFactor(double value) {
		_lockdown_factor = (value < 0) ? 0 : (value > 1.0) ? 1.0 : value;
	}
	
	public double getDiseaseDuration() {
		return _disease_duration;
	}
	
	public void setDiseaseDuration(double sim_days) {
		if (sim_days < 0) {
			sim_days = 0;
		}
		_disease_duration = sim_days;
	}
	
	public double getAsymptomaticity() {
		return _asym_perc;
	}
	
	public void setAsymptomaticity(double asym_perc) {
		_asym_perc = (asym_perc < 0) ? 0.0 : (asym_perc > 1.0) ? 1.0 : asym_perc;
	}

	public double getMortality() {
		return _mortality;
	}

	public void setMortality(double mortality) {
		_mortality = (mortality < 0) ? 0.0 : (mortality > 1.0) ? 1.0 : mortality;
	}
	
	public List<Person> getPeople() {
		return Collections.unmodifiableList(_people);
	}
	
	public List<Person> getPeople(Rectangle2D.Double area) {
		List<Person> people_in_area = new ArrayList<Person>();
		
		for (Person p : _people) {
			if (area.contains(p.getX(), p.getY())) {
				people_in_area.add(p);
			}
		}
		return people_in_area;
	}

	public synchronized void advance() {
		_time += _step_size;
		_economic_damage += (20540.0 / 365.0) * _step_size * 0.5 * _lockdown_factor;
		
		int previously_dead = _dead_people.size();
		
		for (Person p : _people) {
			p.advance(_step_size);
		}
		
		for (int i=0; i<_people.size(); i++) {
			Person a = _people.get(i);
			for (int j=i+1; j<_people.size(); j++) {
				Person b = _people.get(j);
				
				if (a.isContagious()) {
					if (!b.isImmune()) {
						if (b.distanceTo(a) < a.getContagiousRange()) {
							b.infect();
						}
					}
				} else if (b.isContagious()) {
					if (!a.isImmune()) {
						if (a.distanceTo(b) < b.getContagiousRange()) {
							a.infect();
						}
					}
				}
			}
		}

		for (int i=previously_dead; i<_dead_people.size(); i++) {
			_people.remove(_dead_people.get(i));
		}
		notifyObservers(PandemicModelEvent.ADVANED_EVENT);
	}

	public void recordDeath(Person person) {
		_dead_people.add(person);
		_dead_count++;
		notifyObservers(new PMEDeath(person, _time));
	}
	
	public void recordInfection(Person person) {
		_infected_count++;
		notifyObservers(new PMEInfection(person, _time));
	}
	
	public void recordRecovery(Person person) {
		_recovered_count++;
		notifyObservers(new PMERecovery(person, _time));
	}

	public void addObserver(PandemicModelObserver o) {
		_observers.add(o);
	}

	public void removeObserver(PandemicModelObserver o) {
		_observers.remove(o);
	}
	
	private void notifyObservers(PandemicModelEvent e) {
		for (PandemicModelObserver o : _observers) {
			o.update(this, e);
		}
	}

}
