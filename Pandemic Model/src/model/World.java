package model;

import java.awt.AlphaComposite;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import model.PandemicModelEvent.PMEType;

import javax.swing.BorderFactory;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class World extends JPanel implements PandemicModelObserver, MouseListener, MouseMotionListener {

	private PandemicModel _model;
	private PandemicView _view;
	private long _last_repaint;
	private boolean _repaint_needed;

	private int _area_start_x;
	private int _area_start_y;
	private Rectangle _infection_area;
	
	private double big;
	private float alpha;
	private long delay;
	
	public World(PandemicModel model, PandemicView view) {
		_model = model;
		_view = view;
		setPreferredSize(new Dimension(650,650));

		_model.addObserver(this);
		_last_repaint = System.currentTimeMillis();
		_repaint_needed = false;

		_infection_area = null;
		addMouseListener(this);
		addMouseMotionListener(this);
		
		// Start repaint thread.

		new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (_repaint_needed) {
						synchronized (World.this) {
							_repaint_needed = false;
						}
						long ms_since_last = System.currentTimeMillis()-_last_repaint;
						delay = (ms_since_last < 25) ? 25 - ms_since_last : 0;
						try {
							Thread.sleep(delay);
						} catch (InterruptedException e) {
						}	
						SwingUtilities.invokeLater(new Runnable () {
							public void run() {
								repaint();
								_last_repaint = System.currentTimeMillis();
							}
						});
					} else {
						synchronized(World.this) {
							try {
								if (!_repaint_needed) {
									World.this.wait();
								}
							} catch (InterruptedException e) {
							}
						}
					}
				}
			}
		}).start();
	}

	// Overriding paintComponent from JPanel to paint ourselves
	// the way we want.
	
	public void deathPaint(Person deadMan) {
		Graphics g = this.getGraphics();
		Graphics2D g2d = (Graphics2D) g.create();

		alpha = 1f;
		big = 4.0;

		int a = (int) (deadMan.getX() * getWidth());
		int b = (int) (deadMan.getY() * getHeight());

		new Thread(new Runnable() {
			public void run() {
				while (alpha > 0) {
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
					g2d.setColor(Color.BLACK);
					int circle_width = (int) ((big/_model.getScale()) * getWidth());
					int circle_height = (int) ((big/_model.getScale()) * getHeight());
					g2d.fillOval(a-(circle_width/2), b-(circle_height/2), circle_width, circle_height);
					big -= 0.2;
					alpha -= 0.001f;
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
	}

	@Override
	public void paintComponent(Graphics g) {
		// Super class paintComponent will take care of 
		// painting the background.
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g.create();

		AlphaComposite halo_composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
		Composite original_composite = g2d.getComposite();

		synchronized (_model) {
			for (Person p : _model.getPeople()) {
				int x = (int) (p.getX() * getWidth());
				int y = (int) (p.getY() * getHeight());


				if (p.isSymptomatic()) {
					int halo_width = (int) (p.getContagiousRange() * getWidth());
					int halo_height = (int) (p.getContagiousRange() * getHeight());
					g2d.setComposite(halo_composite);
					g2d.setColor(Color.RED);
					g2d.fillOval(x-(halo_width/2), y-(halo_height/2), halo_width, halo_height);
				}

				int pwidth = (int) (p.getSize() * getWidth());
				int pheight = (int) (p.getSize() * getHeight());
				g2d.setComposite(original_composite);

				if (p.isImmune()) {
					g2d.setColor(Color.GREEN);				
				} else if (p.isSymptomatic()) {
					g2d.setColor(Color.RED);
				} else {
					g2d.setColor(Color.BLUE);
				}
				g2d.fillOval(x-(pwidth/2), y-(pheight/2), pwidth, pheight);
			}
			
			
		}

		
		
		if (_infection_area != null) {
			AlphaComposite infection_area_composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.33f);
			g2d.setComposite(infection_area_composite);
			g2d.setColor(Color.RED);
			g2d.fillRect((int) _infection_area.getX(), (int) _infection_area.getY(), 
					(int) _infection_area.getWidth(), (int) _infection_area.getHeight());
		}
	}

	@Override
	public void update(PandemicModel model, PandemicModelEvent event) {
		synchronized (this) {
			_repaint_needed = true;
			if (event.getType() == PMEType.DEATH) {
				PMEDeath death = (PMEDeath) event;
				deathPaint(death.getPerson());
			}
			this.notify();
		}
	}
	
	// MouseListerer and MouseMotionListener
	// methods below.
	// These start / update infection area.
	
	private void update_infection_area(int x, int y) {
		if (_infection_area == null) {
			return;
		}
		int min_x = (x < _area_start_x) ? x : _area_start_x;
		int max_x = (x < _area_start_x) ? _area_start_x : x;
		int min_y = (y < _area_start_y) ? y : _area_start_y;
		int max_y = (y < _area_start_y) ? _area_start_y : y;
		
		_infection_area = new Rectangle(min_x, min_y, (max_x-min_x), (max_y-min_y));
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		_area_start_x = e.getX();
		_area_start_y = e.getY();
		_infection_area = new Rectangle(e.getX(), e.getY(), 0, 0);
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (_infection_area != null) {
			update_infection_area(e.getX(), e.getY());
			double x = ((double) _infection_area.getX())/((double) getWidth());
			double y = ((double) _infection_area.getY())/((double) getHeight());
			double w = ((double) _infection_area.getWidth())/((double) getWidth());
			double h = ((double) _infection_area.getHeight())/((double) getHeight());

			_infection_area = null;
			repaint();
			
			_view.infectArea(x, y, w, h);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (_infection_area != null) {
			update_infection_area(e.getX(), e.getY());
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

}
