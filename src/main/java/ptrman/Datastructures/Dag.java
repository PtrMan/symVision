package ptrman.Datastructures;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Dag<Type> {
    public static class Element<Type2> {
        public Element(Type2 content)
        {
            this.content = content;
        }

        public List<Integer> childIndices = new ArrayList<>();
        public Type2 content;
    }

    public void addElement(Element element)
    {
        elements.add(element);
    }

    public List<Element> elements = new ArrayList<>();
}
