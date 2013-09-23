package com.ioabsoftware.gameraven.networking;

import org.jsoup.Connection.Response;

/**
 * Interface defines that a class has the ability to handle a network response.
 * @author Charles
 *
 */
public interface HandlesNetworkResult {
	
	/**
	 * Enum for every kind of network request this app can send.
	 * Used to identify responses when they arrive.
	 * @author Charles
	 *
	 */
	public static enum NetDesc {
		// contains 23 elements
		DEV_UPDATE_CHECK, UNSPECIFIED, VERIFY_ACCOUNT_S1, VERIFY_ACCOUNT_S2,
		BOARD_JUMPER, GAME_SEARCH, BOARD_LIST, AMP_LIST, TRACKED_TOPICS, BOARD, TOPIC, MESSAGE_DETAIL, USER_DETAIL,
		PM_INBOX, PM_DETAIL, SEND_PM_S1, SEND_PM_S2,
		MARKMSG_S1, MARKMSG_S2, DLTMSG_S1, DLTMSG_S2, CLOSE_TOPIC,
		QEDIT_MSG,
		LOGIN_S1, LOGIN_S2,
		QPOSTMSG_S1, QPOSTMSG_S3,
		QPOSTTPC_S1, QPOSTTPC_S3,
		POSTMSG_S1, POSTMSG_S2, POSTMSG_S3,
		POSTTPC_S1, POSTTPC_S2, POSTTPC_S3
	}
	
	/**
	 * Method fired when there is a response to a network request.
	 * @param res The response from the network
	 * @param desc The description of the preceding network request
	 */
	public void handleNetworkResult(Response res, NetDesc desc);
	
	/**
	 * Method fired before the network request is sent
	 * @param desc The description of the request about to be sent
	 */
	public void preExecuteSetup(NetDesc desc);
	
	/**
	 * Method fired after the network has responded to the request, and after
	 * handleNetworkResult has finished
	 * @param desc The description of the preceding network request
	 */
	public void postExecuteCleanup(NetDesc desc);
	
}
