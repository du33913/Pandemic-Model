package model;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Pandemic {
	public static void main(String[] args) {

		/* Create top level window. */

		JFrame main_frame = new JFrame();
		main_frame.setTitle("Example");
		main_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		/* Create panel for content. Uses BorderLayout. */
		JPanel top_panel = new JPanel();
		top_panel.setLayout(new BorderLayout());
		main_frame.setContentPane(top_panel);

		PandemicModel model = new PandemicModel(100.0, 100, 0.01);
		PandemicView view = new PandemicView(model);
		PandemicController controller = new PandemicController(model, view);

		top_panel.add(view, BorderLayout.CENTER);

		/* Pack main frame and make visible. */

		main_frame.pack();
		main_frame.setVisible(true);


	}
}

