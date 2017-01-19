/**
 * Created by xiaopeng on 2016/12/13.
 */



import io.j1st.data.entity.Registry;
import io.j1st.data.job.Clculate;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


enum Shrubbery {GROUND, CRAWING, HANGING}

public class TestMethods {
    private static Logger log = LoggerFactory.getLogger(TestMethods.class);

    @Test
    public void testLogBack() {
        Clculate clculate=new Clculate();
        clculate.TotalCalc();
    }

}