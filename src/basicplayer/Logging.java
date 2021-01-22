package basicplayer;

public class Logging {

    // Logging def - toggle to display all log messages
    public static final boolean ENABLED = true;

    // Add this flag as an or stmt below to always log certain level
    public static final boolean ALWAYS = true;

    // Toggle this flag to display warning messages
    public static final boolean LOG_WARNINGS = false;

    // Toggle this flag to display critical messages
    public static final boolean LOG_CRITICALS = false;

    // Toggle this flag to display error messages
    public static final boolean LOG_ERRORS = false;

    /**
     * Generic Logging method
     */
    public static void log( LogLevel logLevel, String msg ) {
        if ( ENABLED ) {
            // Construct message and call respective log level
            switch ( logLevel ) {
                case CRITICAL:
                    critical( msg );
                    break;
                case ERROR:
                    error( msg );
                    break;
                case WARNING:
                    warn( msg );
                    break;
                case INFO:
                    info( msg );
                    break;
                case DEBUG:
                    debug( msg );
                    break;
                case NOTSET:
                default:
                    other( msg );
                    break;
            }
        }
    }

    /**
     * Generic Logging method with no level
     */
    public static void log( String msg ) {
        log( LogLevel.NOTSET, msg );
    }

    /**
     * Debug log
     */
    public static void debug( String msg ) {
        if ( ENABLED ) {
            String debugMsg = generateMsgHeader( LogLevel.DEBUG ) + msg;
            System.out.println( debugMsg );
        }
    }

    /**
     * Infomation log
     */
    public static void info( String msg ) {
        if ( ENABLED ) {
            String infoMsg = generateMsgHeader( LogLevel.INFO ) + msg;
            System.out.println( infoMsg );
        }
    }

    /**
     * Warning log
     */
    public static void warn( String msg ) {
        if ( ENABLED || LOG_WARNINGS ) {
            String warnMsg = generateMsgHeader( LogLevel.WARNING ) + msg;
            System.out.println( warnMsg );
        }
    }

    /**
     * Critical Message log
     */
    public static void critical( String msg ) {
        if ( ENABLED || LOG_CRITICALS ) {
            String critMsg = generateMsgHeader( LogLevel.CRITICAL ) + msg;
            System.out.println( critMsg );
        }
    }

    /**
     * Error log
     */
    public static void error( String err ) {
        if ( ENABLED || LOG_ERRORS ) {
            String errMsg = generateMsgHeader( LogLevel.ERROR ) + err;
            // Note: System.err is illegal
            System.out.println( errMsg );
        }
    }

    /**
     * Misc logging - only accesible from generic logger
     */
    private static void other( String msg ) {
        if ( ENABLED ) {
            String logMsg = generateMsgHeader( LogLevel.NOTSET ) + msg;
            System.out.println( logMsg );
        }
    }

    /**
     * Force log message
     */
     public static void force( String msg ) {
         System.out.println( "[FORCED!!!] " + msg  );
     }

    /**
     * Create logging message header
     */
    private static String generateMsgHeader( LogLevel lvl ) {
        // Note: Getting system time is illegal
        String msgHeader = "[" + lvl.toString() + "]";
        msgHeader += " ";

        return ( msgHeader );
    }
}
