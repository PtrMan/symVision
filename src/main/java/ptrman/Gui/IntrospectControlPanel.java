/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.Gui;

import javax.swing.*;

/**
 *
 */
public class IntrospectControlPanel {
	public Runnable handlerIntrospectionChanged = () -> { };
	public Runnable handlerPauseContinue = () -> { };
	private JPanel controlAreaPanel;
	private JTextField frameTextfield = new JTextField();
	private JButton pauseResumeButton = new JButton("||");
	private JCheckBox introspectButton = new JCheckBox("introspect");
	private boolean runningState = true;
	public IntrospectControlPanel() {
		controlAreaPanel = new JPanel();

		controlAreaPanel.add(new JLabel("Frame "));
		controlAreaPanel.add(frameTextfield);
		controlAreaPanel.add(new JLabel("from x"));
		controlAreaPanel.add(new JButton("<"));
		controlAreaPanel.add(pauseResumeButton);
		controlAreaPanel.add(new JButton(">"));
		controlAreaPanel.add(introspectButton);

		introspectButton.addChangeListener(e -> {
			setIntrospectState(getIntrospectionState());
			handlerIntrospectionChanged.run();
		});

		pauseResumeButton.addActionListener(e -> {
			setRunningState(!runningState);
			handlerPauseContinue.run();
		});
	}

	private void setRunningState(boolean runningState) {
		this.runningState = runningState;
		pauseResumeButton.setText(getTextForPauseResumeButton());
	}

	private String getTextForPauseResumeButton() {
        return runningState ? "||" : ">>";
	}

	public void setIntrospectState(boolean state) {
		introspectButton.setSelected(state);
	}

	public boolean getIntrospectionState() {
		return introspectButton.isSelected();
	}

	public JPanel getPanel() {
		return controlAreaPanel;
	}
}
