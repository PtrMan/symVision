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
