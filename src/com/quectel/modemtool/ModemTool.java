package com.quectel.modemtool;

public class ModemTool{
    public static final String TAG = "ModemTool-local";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is a common api that can be used to send AT command to write nv.
     * @param commandId  which listed in {@link NvConstants} and start with REQUEST_COMMON_COMMAND_* .
     * @param atCommand treat the whole at command as a parameter.
     * @return  NULL: fail to send at command
     *          contain "ERROR" : at comand send successfully, but not get the result correctly;
     *          contain "OK" : get the result correctly,
     */
    public native String sendAtCommand(int commandId, String atCommand);
}