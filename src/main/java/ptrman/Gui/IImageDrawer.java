package ptrman.Gui;

import ptrman.bpsolver.Solver;

import java.awt.image.BufferedImage;

/**
 *
 */
public interface IImageDrawer
{
    BufferedImage drawToJavaImage(Solver bpSolver);
}
