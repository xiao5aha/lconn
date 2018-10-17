package com.huo.lconn.rpc.client.common;

public class RpcResponse {
	
	private int requestId;
	
	private Object result;

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

}