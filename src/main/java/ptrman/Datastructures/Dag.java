package ptrman.Datastructures;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Dag<Type> {
    public static class Element<ElementType> {
        public Element(ElementType content)
        {
            this.content = content;
        }

        public List<Integer> childIndices = new ArrayList<>();
        public ElementType content;
    }

    public void addElement(Element<Type> element)
    {
        elements.add(element);
    }

    public List<Element<Type>> elements = new ArrayList<>();
}
