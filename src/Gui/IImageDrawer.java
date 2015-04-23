package Gui;

import bpsolver.BpSolver;

import java.awt.image.BufferedImage;

/**
 *
 */
public interface IImageDrawer
{
    BufferedImage drawToJavaImage(float time, BpSolver bpSolver);
}
