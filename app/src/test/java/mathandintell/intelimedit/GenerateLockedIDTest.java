package mathandintell.intelimedit;

import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/09/21
 *      version : 1.0
 * <pre>
 */
public class GenerateLockedIDTest {

    @Test
    public void test() {
        List<String> daoju = Arrays.asList("a", "b", "c");

        LinkedList<Integer> stepList = new LinkedList<>();
        stepList.add(1);
        stepList.add(2);
        ListIterator<Integer> iterator = (ListIterator<Integer>) stepList.iterator();
        iterator.next();
        System.out.println(iterator.nextIndex());
    }


}
