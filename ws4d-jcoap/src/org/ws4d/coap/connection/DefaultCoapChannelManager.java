/* Copyright [2011] [University of Rostock]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************/

package org.ws4d.coap.connection;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Random;

import org.ws4d.coap.Constants;
import org.ws4d.coap.interfaces.CoapChannel;
import org.ws4d.coap.interfaces.CoapChannelListener;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapServerListener;
import org.ws4d.coap.interfaces.CoapSocketHandler;

public class DefaultCoapChannelManager implements CoapChannelManager {
    // global message id
    private int globalMessageId;
    private static DefaultCoapChannelManager instance;
    private HashMap<Integer, SocketInformation> socketMap = new HashMap<Integer, SocketInformation>();
    CoapServerListener serverListener = null;

    private DefaultCoapChannelManager() {
        reset();
    }

    public synchronized static CoapChannelManager getInstance() {
        if (instance == null) {
            instance = new DefaultCoapChannelManager();
        }
        return instance;
    }
  
    /**
     * Creates a new server channel
     */
    @Override
    public synchronized CoapChannel createServerChannel(CoapSocketHandler socketHandler, InetAddress addr, int port){
    	SocketInformation socketInfo = socketMap.get(socketHandler.getLocalPort());
    	
    	if (socketInfo.serverListener == null) {
			/* this is not a server socket */
    		return null;
		}

    	CoapChannel newChannel= new DefaultCoapChannel( socketHandler, null, addr, port);
    	
    	if (!socketInfo.serverListener.onAccept(newChannel)){
    		/* Server rejected channel */
    		return null;
    	}
    	
    	return newChannel;
    }

    /**
     * Creates a new, global message id for a new COAP message
     */
    @Override
    public synchronized int getNewMessageID() {
        if (globalMessageId < Constants.MESSAGE_ID_MAX) {
            ++globalMessageId;
        } else
            globalMessageId = Constants.MESSAGE_ID_MIN;
        return globalMessageId;
    }

    @Override
    public synchronized void reset() {
        // generate random 16 bit messageId
        Random random = new Random();
        globalMessageId = random.nextInt(Constants.MESSAGE_ID_MAX + 1);
    }

   
    @Override
    public void createServerListener(CoapServerListener serverListener, int localPort) {
        if (!socketMap.containsKey(localPort)) {
            try {
            	SocketInformation socketInfo = new SocketInformation(new DefaultCoapSocketHandler(this, localPort), serverListener);
            	socketMap.put(localPort, socketInfo);
            } catch (IOException e) {
				e.printStackTrace();
			}
        } else {
        	/*TODO: raise exception: address already in use */
        	throw new IllegalStateException();
        }
    }

    @Override
	public CoapChannel connect(CoapChannelListener channelListener, InetAddress addr, int port) {
    	CoapSocketHandler socketHandler = null;
		try {
			socketHandler = new DefaultCoapSocketHandler(this);
			SocketInformation sockInfo = new SocketInformation(socketHandler, null); 
			socketMap.put(socketHandler.getLocalPort(), sockInfo);
			return socketHandler.connect(channelListener, addr, port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private class SocketInformation {
		public CoapSocketHandler socketHandler = null;
		public CoapServerListener serverListener = null;
		public SocketInformation(CoapSocketHandler socketHandler,
				CoapServerListener serverListener) {
			super();
			this.socketHandler = socketHandler;
			this.serverListener = serverListener;
		}
	}
}
