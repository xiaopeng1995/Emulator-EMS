/**
 * Created by xiaopeng on 2016/12/13.
 */
import io.j1st.data.entity.config.BatConfig;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class TestMethods {
    private static Logger log = LoggerFactory.getLogger(TestMethods.class);

    @Test
    public void testLogBack() {
        BatConfig b=new BatConfig();
        System.out.println(b.Ran);
        System.out.println(b.Ran);
        BatConfig c=new BatConfig();
        System.out.println(c.Ran);
        System.out.println(c.Ran);
    }
}