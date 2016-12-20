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
        for (Shrubbery s : Shrubbery.values()) {
            //ordinal()方法返回一个int值,这是每个enum实例在声明时的次序,从0开始,
            System.out.println(s + "ordinal:" + s.ordinal());
            //Enum类实现呢Comparable接口
            System.out.println(s.compareTo(Shrubbery.CRAWING) + "");
            //编译器会自动帮你找到equals()和hashCode()方法
            System.out.println(s.equals(Shrubbery.CRAWING) + "");
            //可以用==来比较enum
            System.out.println(s == Shrubbery.CRAWING);
            //调用getDeclaringClass()这个方法就知道所属的enum类了
            System.out.println(s.getDeclaringClass());
            //name()方法返回enum实例声明时的名字,和toString()方法效果一样
            System.out.println(s.name());

            System.out.println("---------------------------------");
        }
        for (String s:"HANGING CRAWING GROUND".split(" ")){
            Shrubbery shrub=Enum.valueOf(Shrubbery.class,s);
            System.out.println(shrub);
        }
    }
}