import com.nhnacademy.system.SystemOption;

public class SystemOptionsTest {
    public static void main(String[] args) {
        SystemOption a = new SystemOption(args);
        a.createNodes();
        a.createFlow();
    }
}
