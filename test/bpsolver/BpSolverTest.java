package bpsolver;

import Datastructures.Map2d;
import Datastructures.Vector2d;
import FargGeneral.network.Node;
import RetinaLevel.Intersection;
import RetinaLevel.ProcessA;
import RetinaLevel.ProcessB;
import RetinaLevel.ProcessC;
import RetinaLevel.ProcessD;
import RetinaLevel.ProcessE;
import RetinaLevel.ProcessH;
import RetinaLevel.ProcessM;
import RetinaLevel.SingleLineDetector;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;

public class BpSolverTest
{
    public BpSolverTest()
    {
        Parameters.init();
    }

    @Test
    public void testAnglePointV()
    {
        BpSolver bpSolver = new BpSolver();
        bpSolver.setImageSize(new Vector2d<>(100, 100));
        
        

        BufferedImage javaImage = drawToJavaImage(bpSolver);
        Map2d<Boolean> image = drawToImage(javaImage);

        ArrayList<Node> nodes = getNodesFromImage(image, bpSolver);
        
        int x = 0;
        
        // TODO< check for at least one V anglepoint >
    }
    
    private static Map2d<Boolean> drawToImage(BufferedImage javaImage)
    {
        DataBuffer imageBuffer = javaImage.getData().getDataBuffer();

        int bufferI;
        Map2d<Boolean> convertedToMap;

        convertedToMap = new Map2d<Boolean>(javaImage.getWidth(), javaImage.getHeight());

        for( bufferI = 0; bufferI < imageBuffer.getSize(); bufferI++ )
        {
            boolean convertedPixel;

            convertedPixel = imageBuffer.getElem(bufferI) != 0;
            convertedToMap.setAt(bufferI%convertedToMap.getWidth(), bufferI/convertedToMap.getWidth(), convertedPixel);
        }

        return convertedToMap;
    }
    
    private BufferedImage drawToJavaImage(BpSolver bpSolver)
    {
        BufferedImage off_Image = new BufferedImage(bpSolver.getImageSize().x, bpSolver.getImageSize().y, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = off_Image.createGraphics();
        g2.setColor(Color.BLACK);

        g2.drawLine(10, 10, 15, 30);
        g2.drawLine(20, 10, 15, 30);
        
        return off_Image;
    }
    
    private ArrayList<Node> getNodesFromImage(Map2d<Boolean> image, BpSolver bpSolver)
    {
        // TODO MAYBE < put this into a method in BpSolver, name "clearWorkspace()" (which cleans the ltm/workspace and the coderack) >
        bpSolver.coderack.flush();
        
        ProcessA processA = new ProcessA();
        ProcessB processB = new ProcessB();
        ProcessC processC = new ProcessC();
        ProcessD processD = new ProcessD();
        ProcessH processH = new ProcessH();
        ProcessE processE = new ProcessE();
        ProcessM processM = new ProcessM();
        
        processA.setWorkingImage(image);
        ArrayList<ProcessA.Sample> samples = processA.sampleImage();
        
        
        processB.process(samples, image);
        processC.process(samples);
        
        ArrayList<SingleLineDetector> lineDetectors = processD.detectLines(samples);
        
        ArrayList<Intersection> lineIntersections = new ArrayList<>();
        
        
        
        processH.process(lineDetectors);
        
        
        
        
        processE.process(lineDetectors, image);
        
        lineIntersections = getAllLineIntersections(lineDetectors);
        
        
        ArrayList<ProcessM.LineParsing> lineParsings = new ArrayList<>();
        
        processM.process(lineDetectors);
        
        lineParsings = processM.getLineParsings();
        
        
        
        
        RetinaToWorkspaceTranslator retinaToWorkspaceTranslator;
        
        retinaToWorkspaceTranslator = new RetinaToWorkspaceTranslator();
        
        ArrayList<Node> objectNodes = retinaToWorkspaceTranslator.createObjectsFromLines(lineDetectors, bpSolver.network, bpSolver.networkHandles, bpSolver.coderack, bpSolver.codeletLtmLookup, bpSolver.getImageSizeAsFloat());
        
        bpSolver.cycle(500);
        
        return objectNodes;
    }
    
    // TODO< refactor out >
    private static ArrayList<Intersection> getAllLineIntersections(ArrayList<SingleLineDetector> lineDetectors)
    {
        ArrayList<Intersection> uniqueIntersections;

        uniqueIntersections = new ArrayList<>();

        for( SingleLineDetector currentDetector : lineDetectors )
        {
            findAndAddUniqueIntersections(uniqueIntersections, currentDetector.intersections);
        }

        return uniqueIntersections;
    }


    // modifies uniqueIntersections
    private static void findAndAddUniqueIntersections(ArrayList<Intersection> uniqueIntersections, ArrayList<Intersection> intersections)
    {
        for( Intersection currentOuterIntersection : intersections )
        {
            boolean found;

            found = false;

            for( Intersection currentUnqiueIntersection : uniqueIntersections )
            {
                if( currentUnqiueIntersection.equals(currentOuterIntersection) )
                {
                    found = true;
                    break;
                }
            }

            if( !found )
            {
                uniqueIntersections.add(currentOuterIntersection);
            }
        }


    }
    
}
