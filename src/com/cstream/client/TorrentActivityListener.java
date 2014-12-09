package com.cstream.client;

import java.util.EventListener;

import com.turn.ttorrent.client.SharedTorrent;

public interface TorrentActivityListener extends EventListener {

	public void handleTorrentAdded(SharedTorrent torrent);
	
	public void handleTorrentChanged(SharedTorrent torrent);

}
