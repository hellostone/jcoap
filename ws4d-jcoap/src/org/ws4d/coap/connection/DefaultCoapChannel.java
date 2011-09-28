
package org.ws4d.coap.connection;

import java.net.InetAddress;

import org.ws4d.coap.interfaces.CoapChannel;
import org.ws4d.coap.interfaces.CoapChannelListener;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapMessage;
import org.ws4d.coap.interfaces.CoapSocketHandler;
import org.ws4d.coap.messages.CoapMessageCode;
import org.ws4d.coap.messages.CoapPacketType;
import org.ws4d.coap.messages.DefaultCoapMessage;

public class DefaultCoapChannel implements CoapChannel {
    private CoapSocketHandler socketHandler = null;
    private CoapChannelListener listener = null;
    private CoapChannelManager channelManager = null;
    private InetAddress remoteAddress;
    private int remotePort;
    private Object hook = null;

    public DefaultCoapChannel(CoapSocketHandler socketHandler, CoapChannelListener listener,
            InetAddress remoteAddress, int remotePort) {
        this.socketHandler = socketHandler;
        channelManager = socketHandler.getChannelManager();
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.listener = listener;
    }

    @Override
    public void close() {
        socketHandler.removeChannel(this);
    }

    @Override
    public void sendMessage(CoapMessage msg) {
        msg.setChannel(this);
        socketHandler.sendMessage(msg);
    }

    @Override
    public InetAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public int getRemotePort() {
        return remotePort;
    }

    @Override
    public CoapChannelListener getCoapChannelHandler() {
        return listener;
    }

    @Override
    public void setHookObject(Object o) {
        hook = o;
    }

    @Override
    public Object getHookObject() {
        return hook;
    }

    @Override
    public void setCoapChannelHandler(CoapChannelListener listener) {
        this.listener = listener;

    }

    @Override
    public CoapMessage createRequest(boolean reliable, CoapMessageCode.MessageCode messageCode) {
        CoapMessage msg = new DefaultCoapMessage(
                reliable ? CoapPacketType.CON : CoapPacketType.NON, messageCode,
                channelManager.getNewMessageID());
        msg.setChannel(this);
        return msg;
    }

    @Override
    public CoapMessage createResponse(CoapMessage request, CoapMessageCode.MessageCode messageCode) {
        if (request.getPacketType() == CoapPacketType.CON) {
            CoapMessage msg = new DefaultCoapMessage(CoapPacketType.ACK, messageCode,
                    request.getMessageID());
            msg.setChannel(this);
            return msg;
        }

        if (request.getPacketType() == CoapPacketType.NON) {
            CoapMessage msg = new DefaultCoapMessage(CoapPacketType.NON, messageCode,
                    channelManager.getNewMessageID());
            msg.setChannel(this);
            return msg;
        }

        return null;
    }

	@Override
	public void newIncommingMessage(CoapMessage message) {
		getCoapChannelHandler().onReceivedMessage(message);
	}

}
