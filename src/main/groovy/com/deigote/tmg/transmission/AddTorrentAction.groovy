package com.deigote.tmg.transmission

class AddTorrentAction {

	public final String method
	public final Map arguments

	private AddTorrentAction(String magnetLink) {
		method = 'torrent-add'
		arguments = [filename: magnetLink].asImmutable()
	}

	static AddTorrentAction "for"(String magnetLink) {
		new AddTorrentAction(magnetLink)
	}
}
