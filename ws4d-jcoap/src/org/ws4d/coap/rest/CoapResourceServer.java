package org.ws4d.coap.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Vector;

import org.ws4d.coap.Constants;
import org.ws4d.coap.connection.DefaultCoapChannelManager;
import org.ws4d.coap.interfaces.CoapChannel;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapMessage;
import org.ws4d.coap.interfaces.CoapServer;
import org.ws4d.coap.messages.CoapHeaderOption;
import org.ws4d.coap.messages.CoapHeaderOptions;
import org.ws4d.coap.messages.CoapHeaderOptions.HeaderOptionNumber;
import org.ws4d.coap.messages.CoapMessageCode.MessageCode;

public class CoapResourceServer extends AbstractResourceServer implements
	CoapServer {
    private static final int PORT = Constants.COAP_DEFAULT_PORT;

    @Override
    public void start() throws Exception {
	super.start();
	CoapChannelManager channelManager = DefaultCoapChannelManager
		.getInstance();
	channelManager.createServerListener(this, PORT);
    }

    @Override
    public void stop() {
    }

    public int getPort() {
	return PORT;
    }

    @Override
    public URI getHostUri() {
	URI hostUri = null;
	try {
	    hostUri = new URI("coap://" + this.getLocalIpAddress() + ":"
		    + getPort());
	} catch (URISyntaxException e) {
	    e.printStackTrace();
	}
	return hostUri;
    }

    @Override
    public CoapServer onAccept(CoapMessage request) {
	return this;
    }
    
    // TODO The URI Query management should be done somewhere else
    private Vector<String> returnUriQueries(CoapMessage message) {
	CoapHeaderOption option = null;
	Vector<String> uriQueries = new Vector<String>();
	for (Iterator<CoapHeaderOption> options = message.getHeader().getCoapHeaderOptions().iterator(); options.hasNext();) {
	    option=options.next();
	    if (option!=null && HeaderOptionNumber.Uri_Query == option.getOptionNumber()) {
		
		byte[] data = option.getOptionValue();
		if (data!=null) {
		    uriQueries.add(data.toString());
		}
	    }
	}
	return null;
    }
    
    @Override
    public void handleRequest(CoapChannel channel, CoapMessage request) {
	CoapMessage response = null;
	MessageCode messageCode = request.getMessageCode();
	if (messageCode == MessageCode.GET) {
	    // create response with value from responsible Resource object
	    String targetPath = request.getUriPath();
	    final Resource resource = readResource(targetPath);

	    if (resource != null) {
		// URI queries
		Vector<String> uriQueries = returnUriQueries(request);
		final byte[] responseValue;
		if (uriQueries!=null) 
		    responseValue = resource.getValue(uriQueries);
		else
		    responseValue = resource.getValue();
		response = channel.createResponse(request,
			MessageCode.Content_205);
		// TODO content type handling needs to be implemented
		if (resource.getClass().equals(CoreResource.class)) {
		    // hack to add content type for core resource (currently content types are not handled in responses)
		    byte[] value = new byte[1];
		    value[0] = 40; //
		    response.getHeader()
			    .addOption(
				    new CoapHeaderOption(
					    CoapHeaderOptions.CoapHeaderOptionType.Content_Type,
					    value));
		}
		response.setPayload(responseValue);
	    } else {
		response = channel.createResponse(request,
			MessageCode.Not_Found_404);
	    }
	    channel.sendMessage(response);
	} else if (messageCode == MessageCode.DELETE) {
	    String targetPath = request.getUriPath();
	    deleteResource(targetPath);
	    response = channel.createResponse(request,
			MessageCode.Deleted_202);
	} else if (messageCode == MessageCode.POST || messageCode == MessageCode.PUT) {
	    CoapResource resource = parseMessage(request);
	    if (createResource(resource)) {
		response = channel.createResponse(request,
			MessageCode.Created_201);
	    } else if (updateResource(resource)) {
		response = channel.createResponse(request,
			MessageCode.Changed_204);
	    } else
		response = channel.createResponse(request,
			MessageCode.Bad_Request_400);
	} else {
	    response = channel.createResponse(request,
		    MessageCode.Bad_Request_400);
	    return;
	}
	channel.sendMessage(response);
    }
    
    private CoapResource parseMessage(CoapMessage message) {
	CoapResource resource = new BasicResource(message.getUriPath(), message.getPayload());
	// TODO add content type
	return resource;
    }
}
