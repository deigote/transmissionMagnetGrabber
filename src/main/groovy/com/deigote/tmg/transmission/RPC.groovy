package com.deigote.tmg.transmission

import retrofit.http.Body
import retrofit.http.Header
import retrofit.http.POST

interface RPC {

	public static String sessionIdHeader = "X-Transmission-Session-Id"

	@POST('/')
	Result addTorrent(
		@Header(sessionIdHeader) String sessionId,
		@Header("Authorization") String authorization,
		@Body AddTorrentAction action
	)

}
