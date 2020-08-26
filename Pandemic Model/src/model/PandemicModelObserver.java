package model;

public interface PandemicModelObserver {
	
	void update(PandemicModel model, PandemicModelEvent event);

}
