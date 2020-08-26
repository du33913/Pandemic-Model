package model;

import java.awt.BorderLayout;



import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.PandemicModelEvent.PMEType;

public class PandemicView extends JPanel implements ActionListener, ChangeListener, PandemicModelObserver {

	private PandemicModel _model;

	private World _world;
	
	private JPanel settings;
	
	private JTextField population;
	private JTextField scale;
	private JTextField step_size;
	
	private JSlider sim_speed;
	private JSlider lockSlider;
	private JSlider asympSlider;
	private JSlider mortSlider;
	private JSlider diseaseSlider;
	
	private JLabel time;
	private JLabel infected;
	private JLabel recovered;
	private JLabel dead;
	private JLabel damage;
	
	private JButton launcher;
	private boolean done;
	
	private JLabel simSpeedValue = new JLabel();
	private JLabel lockValue = new JLabel();
	private JLabel asympValue = new JLabel();
	private JLabel mortValue = new JLabel();
	private JLabel diseaseValue = new JLabel();

	private List<PandemicViewObserver> _observers;

	public PandemicView(PandemicModel model) {
		_observers = new ArrayList<PandemicViewObserver>();

		_model = model;
		_model.addObserver(this);

		_world = new World(model, this);

		setLayout(new BorderLayout());
		add(_world, BorderLayout.CENTER);
		
		settings = new JPanel();
		settings.setLayout(new GridLayout(7, 1));
		
		JPanel start = new JPanel();
		launcher = new JButton("Start");
		launcher.setActionCommand("start");
		launcher.addActionListener(this);
		
		start.add(launcher);
		
		start.add(new JLabel("Time: "));
		time = new JLabel("" +_model.getTime());
		start.add(time);
		start.add(new JLabel("days"));
		
		start.add(new JLabel("Infected: "));
		infected = new JLabel("" + _model.getInfectedCount());
		start.add(infected);
		start.add(new JLabel(" "));
		
		start.add(new JLabel("Recovered: "));
		recovered = new JLabel("" + _model.getRecoveredCount());
		start.add(recovered);
		start.add(new JLabel(" "));
		
		start.add(new JLabel("Dead: "));
		dead = new JLabel("" + _model.getDeadCount());
		start.add(dead);
		start.add(new JLabel(" "));
		
		start.add(new JLabel("Damage: "));
		start.add(new JLabel("$"));
		damage = new JLabel("" + _model.getEconomicDamage());
		start.add(damage);
		start.add(new JLabel("B"));
		
		settings.add(start);
		
		JPanel reset = new JPanel();
		
		population = new JTextField(5);
		reset.add(new JLabel("Population: "));	
		population.setText("" + _model.getPopulation());
		reset.add(population);
		
		scale = new JTextField(5);
		reset.add(new JLabel("Scale: "));	
		scale.setText("" + _model.getScale());
		reset.add(scale);
		
		step_size = new JTextField(5);
		reset.add(new JLabel("Step Size: "));
		step_size.setText("" + _model.getStepSize());
		reset.add(step_size);
		
		JButton reset1 = new JButton("Reset");
		reset1.setActionCommand("reset");
		reset1.addActionListener(this);
		reset.add(reset1);
		
		settings.add(reset);
		
		JPanel step = new JPanel();
		
		step.add(new JLabel("Sim Speed (steps/sec): "));
		sim_speed = new JSlider(1, 1000, 1);
		sim_speed.addChangeListener(this);
		sim_speed.setMajorTickSpacing(1);
		sim_speed.setValue(100);
		simSpeedValue.setText("" + sim_speed.getValue());
		step.add(simSpeedValue);
		step.add(sim_speed);
		
		settings.add(step);
		
		JPanel lockdown = new JPanel();
		
		lockdown.add(new JLabel("Lockdown Factor: "));
		lockSlider = new JSlider(0, 100, 0);
		lockSlider.addChangeListener(this);
		lockSlider.setMajorTickSpacing(1);
		lockSlider.setValue((int) (_model.getLockdownFactor() * 100));
		lockValue.setText(lockSlider.getValue() + "");
		lockdown.add(lockValue);
		lockdown.add(new JLabel("% "));
		lockdown.add(lockSlider);
		
		settings.add(lockdown);
		
		JPanel asymp = new JPanel();
		
		asymp.add(new JLabel("Asymptomacity: "));
		asympSlider = new JSlider(0, 100, 0);
		asympSlider.addChangeListener(this);
		asympSlider.setMajorTickSpacing(1);
		asympSlider.setValue((int) (_model.getAsymptomaticity() * 100));
		asympValue.setText(asympSlider.getValue() + "");
		asymp.add(asympValue);
		asymp.add(new JLabel("% "));
		asymp.add(asympSlider);
		
		settings.add(asymp);
		
		JPanel mortality = new JPanel();
		
		mortality.add(new JLabel("Mortality: "));
		mortSlider = new JSlider(0, 100, 0);
		mortSlider.addChangeListener(this);
		mortSlider.setMajorTickSpacing(1);
		mortSlider.setValue((int) (_model.getMortality() * 100));
		mortValue.setText(mortSlider.getValue() + "");
		mortality.add(mortValue);
		mortality.add(new JLabel("% "));
		mortality.add(mortSlider);
		
		settings.add(mortality);
		
		JPanel disease = new JPanel();
		
		disease.add(new JLabel("Disease Duration: "));
		diseaseSlider = new JSlider(0, 200, 0);
		diseaseSlider.addChangeListener(this);
		diseaseSlider.setMajorTickSpacing(1);
		diseaseSlider.setValue((int) _model.getDiseaseDuration() * 10);
		diseaseValue.setText((double) (diseaseSlider.getValue() / 10) + " ");
		disease.add(diseaseValue);
		disease.add(diseaseSlider);
		
		settings.add(disease);
		
		add(settings, BorderLayout.SOUTH);
	}

	public void infectArea(double x, double y, double width, double height) {
		notifyObservers(new PVEInfectArea(x, y, width, height));
	}
	
	public void addObserver(PandemicViewObserver o) {
		_observers.add(o);
	}

	public void removeObserver(PandemicViewObserver o) {
		_observers.remove(o);
	}

	private void notifyObservers(PandemicViewEvent pve) {
		for (PandemicViewObserver o : _observers) {
			o.handleEvent(pve);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		JButton button = (JButton) e.getSource();
		String key_word = button.getActionCommand();
		Thread runner = new Thread(new Runnable() {
			public void run() {
				while (!done) {
					int _sim_speed = sim_speed.getValue();
					for (int i=0; i<_sim_speed; i++) {
						notifyObservers(new PVEStart());
						try {
							Thread.sleep(1000 / _sim_speed);
						} catch (InterruptedException e) {
						}
						time.setText(String.format("%.2f", _model.getTime()));
						damage.setText(String.format("%.2f", _model.getEconomicDamage()));
					}
				}
			}
		});

		if (key_word.equals("reset")) {
			try {
				int _population = (population.getText().trim().length() != 0) ? Integer.parseInt(population.getText()) : _model.getPopulation();
				double _scale = (scale.getText().trim().length() != 0) ? Double.parseDouble(scale.getText()) : _model.getScale();
				double _step_size = (scale.getText().trim().length() != 0) ? Double.parseDouble(step_size.getText()) : _model.getStepSize();
				
				notifyObservers(new PVEReset(_population, _scale, _step_size));
				
				time.setText(String.format("%.2f", _model.getTime()));
				damage.setText(String.format("%.2f", _model.getEconomicDamage()));
				recovered.setText("" + _model.getRecoveredCount());
				infected.setText("" + _model.getInfectedCount());
				dead.setText("" + _model.getDeadCount());
				
				sim_speed.setValue(100);
				simSpeedValue.setText("" + sim_speed.getValue());
				lockSlider.setValue((int) (_model.getLockdownFactor() * 100));
				lockValue.setText(lockSlider.getValue() + "");
				asympSlider.setValue((int) (_model.getAsymptomaticity() * 100));
				asympValue.setText(asympSlider.getValue() + "");
				mortSlider.setValue((int) (_model.getMortality() * 100));
				mortValue.setText(mortSlider.getValue() + "");
				diseaseSlider.setValue((int) _model.getDiseaseDuration() * 10);
				diseaseValue.setText((double) (diseaseSlider.getValue() / 10) + " ");
			} catch (NumberFormatException x) {
				System.out.println("Error in command: Use only valid numbers!");
			}
		} else if (key_word.equals("start")) {
			done = false;
			runner.start();
			launcher.setText("Stop");
			launcher.setActionCommand("stop");
		} else if (key_word.equals("stop")) {
			done = true;
			try {
				runner.join();
			} catch (InterruptedException e1) {
			}
			launcher.setText("Start");
			launcher.setActionCommand("start");
			notifyObservers(new PVEStop());
		}
	}
	
	@Override
	public void update(PandemicModel model, PandemicModelEvent event) {
		if (event.getType() == PMEType.DEATH) {
			PMEDeath death = (PMEDeath) event;
			dead.setText("" + _model.getDeadCount());
		} else if (event.getType() == PMEType.INFECTION) {
			PMEInfection infection = (PMEInfection) event;
			infected.setText("" + _model.getInfectedCount());
		} else if (event.getType() == PMEType.RECOVERY) {
			PMERecovery recovery = (PMERecovery) event;
			recovered.setText("" + _model.getRecoveredCount());
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		JSlider slider = (JSlider) e.getSource();
		double value = slider.getValue();
		int changer = slider.getValue();
		
		if (slider == lockSlider) {
			_model.setLockdownFactor(value / 100);
			lockValue.setText("" + changer);
		} else if (slider == asympSlider) {
			_model.setAsymptomaticity(value / 100);
			asympValue.setText("" + changer);
		} else if (slider == mortSlider) {
			_model.setMortality(value / 100);
			mortValue.setText("" + changer);
		} else if (slider == diseaseSlider) {
			_model.setDiseaseDuration(value / 10);
			diseaseValue.setText("" + (value / 10));
		} else if (slider == sim_speed) {
			simSpeedValue.setText("" + changer);
		}
		
	}

}
