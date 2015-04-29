package ptrman.Gui;

import ptrman.bpsolver.HardParameters;
import ptrman.bpsolver.Parameters;
import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * 
 */
public class TuningWindow extends JFrame
{
    public TuningWindow()
    {
        super("");
        setBounds(50,50,300,300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildGui();
        setVisible(true);
    }
    
    private class ChangeListenerForProcessdMaxMse implements ChangeListener 
    {
        public ChangeListenerForProcessdMaxMse(JLabel label)
        {
            this.label = label;
        }
        
        public void stateChanged(ChangeEvent e)
        {
            JSlider source = (JSlider)e.getSource();
    
            int valueInt = (int)source.getValue();
            float value = (float)valueInt*0.01f;
            
            changeTo(value);
            Parameters.currentProcessdMaxMse = value;
        }
        
        public void changeTo(float value)
        {
            label.setText(getTextFromInt("Process D max mse", value));
        }
        
        private JLabel label;
    }
    
    private class ChangeListenerForProcessdLockingActivationOffset implements ChangeListener 
    {
        public ChangeListenerForProcessdLockingActivationOffset(JLabel label)
        {
            this.label = label;
        }
        
        public void stateChanged(ChangeEvent e)
        {
            JSlider source = (JSlider)e.getSource();
    
            int valueInt = (int)source.getValue();
            float value = (float)valueInt*0.1f;
            
            changeTo(value);
            Parameters.currentProcessdLockingActivationOffset = value;
        }
        
        public void changeTo(float value)
        {
            label.setText(getTextFromInt("Process D locking activation offset", value));
        }
        
        private JLabel label;
    }
    
    private class ChangeListenerForProcessdLockingActivationScale implements ChangeListener 
    {
        public ChangeListenerForProcessdLockingActivationScale(JLabel label)
        {
            this.label = label;
        }
        
        public void stateChanged(ChangeEvent e)
        {
            JSlider source = (JSlider)e.getSource();
    
            int valueInt = (int)source.getValue();
            float value = (float)valueInt*0.1f;
            
            changeTo(value);
            Parameters.currentProcessdLockingActivationScale = value;
        }
        
        public void changeTo(float value)
        {
            label.setText(getTextFromInt("Process D locking activation scale", value));
        }
        
        private JLabel label;
    }
    
    private void buildGui()
    {
        setLayout(new GridLayout(6,1));
        
        labelProcessdMaxMse = new JLabel();
        add(labelProcessdMaxMse); 
        
        sliderProcessdMaxMse = new JSlider(JSlider.HORIZONTAL, 0, 8000, Math.round(HardParameters.ProcessD.MAXMSE*100));
        ChangeListenerForProcessdMaxMse changeListenerMaxMse = new ChangeListenerForProcessdMaxMse(labelProcessdMaxMse);
        changeListenerMaxMse.changeTo(HardParameters.ProcessD.MAXMSE);
        sliderProcessdMaxMse.addChangeListener(changeListenerMaxMse);
        add(sliderProcessdMaxMse);
        
        
        labelProcessdLockingActivationOffset = new JLabel();
        add(labelProcessdLockingActivationOffset); 
        
        sliderProcessdLockingActivationOffset = new JSlider(JSlider.HORIZONTAL, 0, 100, Math.round(HardParameters.ProcessD.LOCKINGACTIVATIONOFFSET*10));
        ChangeListenerForProcessdLockingActivationOffset changeListenerOffset = new ChangeListenerForProcessdLockingActivationOffset(labelProcessdLockingActivationOffset);
        changeListenerOffset.changeTo(HardParameters.ProcessD.LOCKINGACTIVATIONOFFSET);
        sliderProcessdLockingActivationOffset.addChangeListener(changeListenerOffset);
        add(sliderProcessdLockingActivationOffset);
        
        
        labelProcessdLockingActivationScale = new JLabel();
        add(labelProcessdLockingActivationScale); 
        
        sliderProcessdLockingActivationScale = new JSlider(JSlider.HORIZONTAL, 0, 10, Math.round(HardParameters.ProcessD.LOCKINGACTIVATIONMSESCALE*10));
        ChangeListenerForProcessdLockingActivationScale changeListenerScale = new ChangeListenerForProcessdLockingActivationScale(labelProcessdLockingActivationScale);
        changeListenerScale.changeTo(HardParameters.ProcessD.LOCKINGACTIVATIONMSESCALE);
        sliderProcessdLockingActivationScale.addChangeListener(changeListenerScale);
        add(sliderProcessdLockingActivationScale);
    }
    
    
    private static String getTextFromInt(String text, float value)
    {
        String valueAsString;
        
        valueAsString = Float.toString(value);
        
        return text + ": " + valueAsString;
    }
    
    
    private JSlider sliderProcessdMaxMse;
    private JLabel labelProcessdMaxMse;
    
    private JSlider sliderProcessdLockingActivationOffset;
    private JLabel labelProcessdLockingActivationOffset;
    
    private JSlider sliderProcessdLockingActivationScale;
    private JLabel labelProcessdLockingActivationScale;
}
