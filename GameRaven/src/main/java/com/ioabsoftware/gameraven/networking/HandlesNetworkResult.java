package com.ioabsoftware.gameraven.networking;

import org.jsoup.Connection.Response;

/**
 * Interface defines that a class has the ability to handle a network response.
 *
 * @author Charles
 */
public interface HandlesNetworkResult {

    /**
     * Method fired when there is a response to a network request.
     *
     * @param res  The response from the network
     * @param desc The description of the preceding network request
     */
    public void handleNetworkResult(Response res, NetDesc desc);

    /**
     * Method fired before the network request is sent
     *
     * @param desc The description of the request about to be sent
     */
    public void preExecuteSetup(NetDesc desc);

    /**
     * Method fired after the network has responded to the request, and after
     * handleNetworkResult has finished
     *
     * @param desc The description of the preceding network request
     */
    public void postExecuteCleanup(NetDesc desc);

}
