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

import ptrman.bpsolver.HardParameters;
import ptrman.bpsolver.Parameters;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 *
 * 
 */
public class TuningWindow extends JPanel
{
    public TuningWindow()
    {
        super();
        //setBounds(50,50,300,300);

        buildGui();
        setVisible(true);
    }
    
    private static class ChangeListenerForProcessdMaxMse implements ChangeListener
    {
        public ChangeListenerForProcessdMaxMse(JLabel label)
        {
            this.label = label;
        }
        
        public void stateChanged(ChangeEvent e)
        {
            JSlider source = (JSlider)e.getSource();
    
            int valueInt = source.getValue();
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
    
    private static class ChangeListenerForProcessdLockingActivationOffset implements ChangeListener
    {
        public ChangeListenerForProcessdLockingActivationOffset(JLabel label)
        {
            this.label = label;
        }
        
        public void stateChanged(ChangeEvent e)
        {
            JSlider source = (JSlider)e.getSource();
    
            int valueInt = source.getValue();
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
    
    private static class ChangeListenerForProcessdLockingActivationScale implements ChangeListener
    {
        public ChangeListenerForProcessdLockingActivationScale(JLabel label)
        {
            this.label = label;
        }
        
        public void stateChanged(ChangeEvent e)
        {
            JSlider source = (JSlider)e.getSource();
    
            int valueInt = source.getValue();
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
