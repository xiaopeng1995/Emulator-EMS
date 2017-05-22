

/**
 * Created by xiaopeng on 2017/5/3.
 */
public class Test01 {
    public static void main(String[] args) {
        Integer i1 = 127;
        Integer i2 = 127;
        System.out.println(i1 == i2);//true
        Integer j1 = 128;
        Integer j2 = 128;
        int a = Integer.valueOf(4);
        System.out.println(j1 == j2);//false
        //java在编译的时候,被翻译成-> Integer i1 = Integer.valueOf(127);
        //看一下Integer.valueOf()这个方法的源码大家都会明白，对于-128到127之间的数，会进行缓存，
        //Integer i1 = 127时，会将127进行缓存，下次再写Integer i2 = 127时，
        //就会直接从缓存中取，就不会new了。所以i1 == i2的结果为true,而j1 == j2行为false。
    }

}
