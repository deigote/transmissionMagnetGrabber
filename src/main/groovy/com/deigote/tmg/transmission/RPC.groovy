package com.deigote.tmg.transmission

import retrofit.http.Body
import retrofit.http.Header
import retrofit.http.POST

interface RPC {

	@POST('/')
	Result addTorrent(
		@Header("Authorization") String authorization,
		@Body AddTorrentAction action
	)

}
