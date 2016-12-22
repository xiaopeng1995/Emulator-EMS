/**
 * Created by xiaopeng on 2016/12/13.
 */


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


enum Shrubbery {GROUND, CRAWING, HANGING}

public class TestMethods {
    private static Logger log = LoggerFactory.getLogger(TestMethods.class);

    @Test
    public void testLogBack() {
        System.out.println(((1.0 + Math.random() * (10.0 - 1.0 + 1.0)) / 100.0 + 0.75));
    }
}