package ptrman.Gui;

import ptrman.bpsolver.BpSolver;

import java.awt.image.BufferedImage;

/**
 *
 */
public interface IImageDrawer
{
    BufferedImage drawToJavaImage(BpSolver bpSolver);
}
