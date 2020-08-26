package model;

import model.Direction.Reflection;

public class Person {
	public enum DiseaseState {UNINFECTED, ASYMPTOMATIC, SYMPTOMATIC, POSTINFECTED, DEAD};
	
	private static final double MAX_SPEED = 0.1;
	
	private double _x;
	private double _y;
	private Direction _direction;
	private double _speed;
	private DiseaseState _state;
	private PandemicModel _model;
	private double _infect_time;
	
	public Person(double x, double y, double direction, double speed, PandemicModel model) {
		_x = x;
		_y = y;
		_direction = new Direction(direction);
		_speed = speed;
		_model = model;
		_state = DiseaseState.UNINFECTED;
		_infect_time = -1.0;
	}
	
	public double getX() {
		return _x;
	}
	
	public double getY() {
		return _y;
	}
	
	public double getSize() {
		return 1.0/_model.getScale();
	}
	
	public double getContagiousRange() {
		if (!isContagious()) {
			return 0.0;
		}
		
		return 5.0/_model.getScale();
	}
	
	public void infect() {
		if (_state == DiseaseState.UNINFECTED) {
			_infect_time = _model.getTime();
			if (Math.random() < _model.getAsymptomaticity()) {
				_state = DiseaseState.ASYMPTOMATIC;
			} else {
				_state = DiseaseState.SYMPTOMATIC;
			}
			_model.recordInfection(this);
		}
	}
		
	public void advance(double elapsed) {
		if (_state == DiseaseState.DEAD) {
			return;
		}
		
		if (isContagious()) {
			if (_infect_time + _model.getDiseaseDuration() < _model.getTime()) {
				if (_state == DiseaseState.SYMPTOMATIC) {
					if (Math.random() < _model.getMortality()) {
						_state = DiseaseState.DEAD;
						_model.recordDeath(this);
						return;
					}
				}
				_state = DiseaseState.POSTINFECTED;
				_model.recordRecovery(this);
			}
		}
		
		_x += elapsed * _speed * (1.0 - _model.getLockdownFactor()) * Math.cos(_direction.getRadians());
		_y -= elapsed * _speed * (1.0 - _model.getLockdownFactor()) * Math.sin(_direction.getRadians());
		
		if (_x >= 1.0) {
			_x = 1.0;
			_direction.reflect(Reflection.RIGHT);
		} else if (_x <= 0.0) {
			_x = 0.0;
			_direction.reflect(Reflection.LEFT);
		}
		
		if (_y >= 1.0) {
			_y = 1.0;
			_direction.reflect(Reflection.BOTTOM);
		} else if (_y <= 0.0) {
			_y = 0.0;
			_direction.reflect(Reflection.TOP);
		}
	}
	
	public boolean isContagious() {
		return _state == DiseaseState.ASYMPTOMATIC || _state == DiseaseState.SYMPTOMATIC;
	}

	public boolean isImmune() {
		return _state == DiseaseState.POSTINFECTED;
	}
	
	public boolean isSymptomatic() {
		return _state == DiseaseState.SYMPTOMATIC;
	}

	public double distanceTo(Person other) {
		double dx = _x - other.getX();
		double dy = _y - other.getY();
		return (Math.sqrt((dx*dx)+(dy*dy)));
	}

	/* Helper method for making a random person. */
	
	public static Person makeRandom(PandemicModel model) {
		return new Person(Math.random(), Math.random(), Math.PI*2.0*Math.random(), MAX_SPEED*Math.random(), model);		
	}

}
