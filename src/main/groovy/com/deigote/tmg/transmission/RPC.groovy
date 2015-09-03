package com.deigote.tmg.transmission

import retrofit.http.Body
import retrofit.http.Header
import retrofit.http.POST

interface RPC {

	public static String sessionIdHeader = "X-Transmission-Session-Id"

	@POST('/')
	Result addTorrent(
		@Header("Authorization") String authorization,
		@Header(RPC.sessionIdHeader) String sessionId,
		@Body AddTorrentAction action
	)

}
