package ptrman.Gui;

import javax.swing.*;

/**
 *
 */
public class IntrospectControlPanel
{


    public interface IHandler
    {
        void fire();
    }

    public IntrospectControlPanel()
    {
        controlAreaPanel = new JPanel();

        controlAreaPanel.add(new JLabel("Frame "));
        controlAreaPanel.add(frameTextfield);
        controlAreaPanel.add(new JLabel("from x"));
        controlAreaPanel.add(new JButton("<"));
        controlAreaPanel.add(pauseResumeButton);
        controlAreaPanel.add(new JButton(">"));
        controlAreaPanel.add(introspectButton);

        introspectButton.addActionListener(e -> {
            setIntrospectState(!introspectState);
            handlerIntrospectionChanged.fire();
        });

        pauseResumeButton.addActionListener(e -> {
            setRunningState(!runningState);
            handlerPauseContinue.fire();
        });
    }

    private void setRunningState(boolean runningState)
    {
        this.runningState = runningState;
        pauseResumeButton.setText(getTextForPauseResumeButton());
    }

    private String getTextForPauseResumeButton() {
        if( runningState )
        {
            return "||";
        }
        else
        {
            return ">>";
        }
    }

    private String getTextForIntrospect()
    {
        return "Introspect " + (introspectState ? "on" : "off");
    }

    public void setIntrospectState(boolean state)
    {
        introspectState = state;
        introspectButton.setText(getTextForIntrospect());
    }

    public boolean getIntrospectionState()
    {
        return introspectState;
    }

    public JPanel getPanel()
    {
        return controlAreaPanel;
    }

    public IHandler handlerIntrospectionChanged;
    public IHandler handlerPauseContinue;

    private JPanel controlAreaPanel;
    private JTextField frameTextfield = new JTextField();
    private JButton introspectButton = new JButton(getTextForIntrospect());
    private JButton pauseResumeButton = new JButton("||");

    private boolean introspectState = false;
    private boolean runningState = true;
}
