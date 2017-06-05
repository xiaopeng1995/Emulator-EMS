import java.util.Date;

/**
 * Created by xiaopeng on 2017/5/3.
 */
public class Test01 {
    public static void main(String[] args) {
        String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(1496485111 * 1000));
        System.out.println();
    }

}
