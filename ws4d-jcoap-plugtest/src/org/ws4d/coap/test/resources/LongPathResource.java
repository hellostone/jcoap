package org.ws4d.coap.test.resources;

import org.ws4d.coap.rest.Resource;

public class LongPathResource implements Resource {
    @Override
	public String getMimeType() {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public String getPath() {
	    return "/seg1/seg2/seg3";
	}

	@Override
	public String getShortName() {
	    return getPath();
	}

	@Override
	public byte[] getValue() {
	    return "Test".getBytes();
	}

	@Override
	public byte[] getValue(String query) {
	    // TODO Auto-generated method stub
	    return null;
	}
}