package es.ladyr.dante.protocol;

import es.ladyr.dante.node.DanteNode;
import es.ladyr.util.dataStructs.SortedArrayList;

public class ReplicationMessage extends ProtocolMessage {
	
	
	protected int packetType = -1;	// 0: Request
									// 1: Response
	
	protected int mainResources = -1;
	protected int extraResources = -1;
	protected DanteNode messageOrigin;
	
	protected SortedArrayList resources;

	public ReplicationMessage(int packetType, DanteNode messageOrigin, int mainResources, int extraResources, SortedArrayList resources) {
		this.packetType = packetType;
		this.messageOrigin = messageOrigin;
		this.mainResources = mainResources;
		this.extraResources = extraResources;
		this.resources = resources;
		messageType = REPLICATION_MESSAGE;
	}

	public int getPacketType() {
		return packetType;
	}

	public int getMainResourcesCount() {
		return mainResources;
	}

	public int getExtraResourcesCount() {
		return extraResources;
	}
	
	public DanteNode getMessageOrigin() {
		return messageOrigin;
	}
	
	public SortedArrayList getResources() {
		return resources;
	}


}
