package csw.logging.javadsl;

import csw.logging.scaladsl.Keys$;

/**
 * Helper class for Java to get the handle of predefined keys while calling Logger api methods
 */
public class JKeys {

    /**
     * ObsId key used in logging
     */
    public static final String OBS_ID = Keys$.MODULE$.OBS_ID();
}