package basicplayer;

public class Logging {
    // Logging def
    public static final boolean LOGGING = true;

    // Always log stmts that use this flag
    public static final boolean ALWAYS_LOG = true;

    public static void log( String str ) {
        if (LOGGING) {
            System.out.println( str );
        }
    }
}
