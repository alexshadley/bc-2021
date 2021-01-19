package oldplayer;

public class Logging {
    // Logging def
    public static final boolean ENABLED = false;

    // Always log stmts that use this flag
    public static final boolean ALWAYS_LOG = true;

    // This is so bad haha
    public static void log( String str ) {
        System.out.println( str );
    }
}
