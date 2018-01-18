
import java.util.Locale;

public class Logger {

    private static final String TAG = "[MobiChord]";

    private Logger() {
        throw new IllegalStateException("Logger is utility class.");
    }

    public static void log(String msg, Object... params) {
        System.out.println(String.format(Locale.ENGLISH, "Thread["+Thread.currentThread().getName()+"]:"+msg, params));
    }
}