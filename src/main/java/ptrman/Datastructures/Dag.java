package ptrman.Datastructures;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Dag<Type> {
    public class Element {
        public Element(Type content)
        {
            this.content = content;
        }

        public List<Integer> childIndices = new ArrayList<>();
        public Type content;
    }

    public void addElement(Element element)
    {
        elements.add(element);
    }

    public List<Element> elements = new ArrayList<>();
}
