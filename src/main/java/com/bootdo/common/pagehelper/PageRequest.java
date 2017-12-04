package com.bootdo.common.pagehelper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 查询参数
 * @author yuefeng.liu
 */
public class PageRequest extends LinkedHashMap<String, Object> {
	private static final long serialVersionUID = 1L;
	private int pageNum;
	private int pageSize;

	public PageRequest(Map<String, Object> params) {
		this.putAll(params);
		this.pageNum = Integer.parseInt(params.get("pageNum").toString());
		this.pageSize = Integer.parseInt(params.get("pageSize").toString());
		this.put("pageNum", pageNum);
		this.put("pageSize",pageSize);
	}

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
