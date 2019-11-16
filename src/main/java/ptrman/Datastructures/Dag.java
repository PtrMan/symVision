package ptrman.Datastructures;

//import com.gs.collections.impl.list.mutable.FastList;
//import com.gs.collections.impl.list.mutable.primitive.IntArrayList;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

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

        public IntArrayList childIndices = new IntArrayList();
        public ElementType content;
    }

    public void addElement(Element<Type> element)
    {
        elements.add(element);
    }

    public List<Element<Type>> elements = new FastList<>();
}
