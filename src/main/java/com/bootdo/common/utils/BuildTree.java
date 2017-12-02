package com.bootdo.common.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.bootdo.common.domain.Tree;

public class BuildTree {

	public static <T> Tree<T> build(List<Tree<T>> nodes) {

		if (nodes == null) {
			return null;
		}
		List<Tree<T>> topNodes = new ArrayList<Tree<T>>();

		for (Tree<T> children : nodes) {

			String pid = children.getParentId();
			if (pid == null || "0".equals(pid)) {
				topNodes.add(children);

				continue;
			}

			for (Tree<T> parent : nodes) {
				String id = parent.getId();
				if (id != null && id.equals(pid)) {
					parent.getChildren().add(children);
					children.setHasParent(true);
					parent.setChildren(true);
					continue;
				}
			}

		}

		Tree<T> root = new Tree<T>();
		if (topNodes.size() == 1) {
			root = topNodes.get(0);
		} else {
			root.setId("-1");
			root.setParentId("");
			root.setHasParent(false);
			root.setChildren(true);
			root.setChecked(true);
			root.setChildren(topNodes);
			root.setText("顶级节点");
			Map<String, Object> state = new HashMap<>(16);
			state.put("opened", true);
			root.setState(state);
		}

		return root;
	}

	public static <T> List<Tree<T>> buildList(List<Tree<T>> nodes, String idParam) {
		if (nodes == null) {
			return null;
		}
		List<Tree<T>> topNodes = new ArrayList<Tree<T>>();
		for (Tree<T> children : nodes) {
			String pid = children.getParentId();
			if (pid == null || idParam.equals(pid)) {
				topNodes.add(children);
				continue;
			}
			for (Tree<T> parent : nodes) {
				String id = parent.getId();
				if (id != null && id.equals(pid)) {
					parent.getChildren().add(children);
					children.setHasParent(true);
					parent.setChildren(true);
					continue;
				}
			}
		}
		return topNodes;
	}

	public static <T> List<Tree<T>> getChild(List<Tree<T>> nodes, String id){
        List<Tree<T>> topNodes = new ArrayList<Tree<T>>();
        Iterator<Tree<T>> iterator = nodes.iterator();
        while (iterator.hasNext()){
            Tree next = iterator.next();
            if (hasChild(nodes, next.getId())){
                next.setChildren(true);
                next.setChildren(getChildList(nodes, next.getId()));
                topNodes.add(next);
            }
        }
        return topNodes;
    }

    private static <T> boolean hasChild(List<Tree<T>> list, String id){
        return getChildList(list, id).size() > 0 ? true:false;
    }

    // 得到子节点列表
    private static <T> List<Tree<T>> getChildList(List<Tree<T>> list, String id) {
        List<Tree<T>> nodeList = new ArrayList<Tree<T>>();
        Iterator<Tree<T>> iterator = list.iterator();
        while (iterator.hasNext()) {
            Tree next = iterator.next();
            if (next.getParentId().equals(id)){
                nodeList.add(next);
            }
        }
        return nodeList;
    }
}