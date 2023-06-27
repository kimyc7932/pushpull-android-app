package com.mm.android.mobilecommon.pps.provider;

import java.io.IOException;

public interface RemoteProvider  extends Provider<byte[]>{
	
	public void setConnetcionTimeout(int mills);
	public void setSoTimeout(int mills);

	public void destruct() throws IOException;
}
