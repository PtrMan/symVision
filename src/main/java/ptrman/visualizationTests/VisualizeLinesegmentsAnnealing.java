/**
 * Copyright 2019 The SymVision authors
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.visualizationTests;

import boofcv.gui.image.ImagePanel;
import ptrman.bpsolver.Solver2b;
import ptrman.levels.retina.RetinaPrimitive;
import ptrman.levels.retina.helper.ProcessConnector;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.stream.Stream;

/** visualize line-segments of endosceleton
 *
 */
public class VisualizeLinesegmentsAnnealing {
	int episodeLen = 100; // how many frames do we visualize one step?

	final static int RETINA_WIDTH = 128;
	final static int RETINA_HEIGHT = 128;
	int chosenImage = 0; // chosen image
	int animationFrameNumber = 0;
	public Solver2b solution = Solver2b.graph();
	public VisualizationDrawer outputRenderer = new VisualizationDrawer(); // used for drawing
	int time = 0;
	ImagePanel inView = new ImagePanel();
	ImagePanel outView = new ImagePanel();
	private BufferedImage output;
	private BufferedImage input;
	public VisualizeLinesegmentsAnnealing() {
		JFrame w = new JFrame();
		w.setLayout(new GridLayout(2, 1));
		w.getContentPane().add(inView);
		w.getContentPane().add(outView);
		w.setSize(600, 600);
		w.doLayout();
		w.setVisible(true);
		javax.swing.Timer t = new Timer(1000 / 30, (a) -> {
			input();
			update();
			time++;
		});
		t.start();
	}

	public static void main(String[] passedArgs) {
		new VisualizeLinesegmentsAnnealing();
	}

	public void input() {
		if (input == null || input.getWidth() != RETINA_WIDTH || input.getHeight() != RETINA_HEIGHT)
			input = new BufferedImage(RETINA_WIDTH, RETINA_HEIGHT, BufferedImage.TYPE_INT_ARGB);

		if (time % episodeLen != 0) {
			return; //no need to change
		}

		chosenImage = new Random().nextInt(6);

		Graphics2D g2 = input.createGraphics();

		g2.setColor(Color.BLACK);

		g2.fillRect(0, 0, input.getWidth(), input.getHeight());

		g2.setColor(Color.WHITE);


		switch (chosenImage) {
			case 0:  // draw polygon
				g2.setColor(new Color(1.0f, 0.0f, 0.0f));

				Polygon poly = new Polygon();

				poly.addPoint(10, 10);
				poly.addPoint(70, 10);
				poly.addPoint(40, 50);

				g2.fillPolygon(poly);
				break;
		}
		if (chosenImage == 1) { // draw "A"
			int endpointADeltaX = (int) (Math.cos(animationFrameNumber * 0.1) * 10);
			int endpointADeltaY = (int) (Math.sin(animationFrameNumber * 0.1) * 10);

			g2.setStroke(new BasicStroke(12));
			g2.drawLine(10 + endpointADeltaX, 80 + endpointADeltaY, 40, 10);
			g2.drawLine(90 + endpointADeltaX, 80 + endpointADeltaY, 40, 10);
			g2.drawLine(30, 40, 70, 40);
		} else if (chosenImage == 2) {
			// draw star
			g2.setFont(new Font("TimesRoman", Font.PLAIN, 230));
			g2.drawString("*", 20, 170);
		} else if (chosenImage == 3) {
			// text
			g2.setFont(new Font("TimesRoman", Font.PLAIN, 90));
			g2.drawString("/en-", 2, 100);
		} else if (chosenImage == 4) {
			// draw big boxes
			g2.fillRect(10, 10, 70, 20);
			g2.fillRect(10, 50, 70, 20);
		} else if (chosenImage == 5) { // chinese symbol
			// text
			g2.setFont(new Font("TimesRoman", Font.PLAIN, 90));
			g2.drawString("不", 2, 100);
		}

	}

	void update() {

		solution.set(solution.input, this.input);

		outputRender();

		SwingUtilities.invokeLater(()->{
			inView.setImageRepaint(input);
			outView.setImageRepaint(output);
		});

	}

	private void outputRender() {
		if (output == null)
			output = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);

		Graphics2D g = (Graphics2D) output.getGraphics();
		g.clearRect(0,0,output.getWidth(), output.getHeight());
		g.setColor(new Color(1f, 1f, 1f)); //transparent
		g.fillRect(0, 0, output.getWidth(), output.getHeight());

		ProcessConnector<RetinaPrimitive>[] hh = solution.connectorDetectorsFromProcessHForEdge;
		if (hh!=null) {
			Stream.of(hh).flatMap(x -> x.out.stream()).forEach(r -> {
				outputRenderer.drawPrimitive(r, g);
			});
		}


		// mouse cursor
		//ellipse(mouseX, mouseY, 4, 4);

		g.dispose();
	}
}
