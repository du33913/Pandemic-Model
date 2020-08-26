package model;

public class PandemicController implements PandemicViewObserver {

	private PandemicModel _model;
	private PandemicView _view;
	
	public PandemicController(PandemicModel model, PandemicView view) {
		_model = model;
		_view = view;
		
		_view.addObserver(this);		
	}
	
	public void handleEvent(PandemicViewEvent pve) {
		switch (pve.getType()) {
		case INFECT_AREA:
			PVEInfectArea area_infect = (PVEInfectArea) pve;
			for (Person p : _model.getPeople(area_infect.getArea())) {
				p.infect();
			}
			break;
		case START:
			_model.advance();
			break;
		case STOP:
			break;
		case RESET:
			PVEReset reset = (PVEReset) pve;
			_model.reset(reset.getScale(), reset.getPopulation(), reset.getStepSize());
			break;
		}
	}
}
