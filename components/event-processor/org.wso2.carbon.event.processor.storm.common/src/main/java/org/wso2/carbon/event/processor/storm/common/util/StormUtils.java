package org.wso2.carbon.event.processor.storm.common.util;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by sajith on 6/4/14.
 */
public class StormUtils {

    public static boolean isPortUsed(final int portNumber){
        boolean isPortUsed;
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(portNumber);
            isPortUsed = false;
        } catch (IOException ignored) {
            isPortUsed =  true;
        } finally {
            if (socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    isPortUsed = true;
                }
            }
        }
        return isPortUsed;
    }

}
