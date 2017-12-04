package com.bootdo.common.pagehelper;

import java.io.Serializable;
import java.util.List;

import com.github.pagehelper.Page;

/**
 * @author yuefeng.liu
 */
public class PageResponse<T> implements Serializable{

    // 列表数据
    private List<T> rows;

    private Long total;

    public PageResponse(Page<T> page) {
        super();
        this.rows = page.getResult();
        this.total = page.getTotal();
    }

    public List<T> getRows() {
        return rows;
    }

    public Long getTotal() {
        return total;
    }
}
