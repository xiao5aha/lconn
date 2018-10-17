package com.huo.lconn.node;

import java.util.List;

public interface NodeListener {
	void notifyChanged(List<Node> nodeList);
}