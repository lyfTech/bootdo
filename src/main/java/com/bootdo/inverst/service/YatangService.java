package com.bootdo.inverst.service;

import java.util.List;
import java.util.Map;

import com.bootdo.common.pagehelper.PageResponse;
import com.bootdo.inverst.domain.YatangDO;

/**
 * 
 * 
 * @author yuefeng.liu
 * @email lyfai521@163.com
 * @date 2017-12-04 09:48:36
 */
public interface YatangService {
	
	YatangDO get(Long id);
	
	List<YatangDO> list(Map<String, Object> map);
	
	int count(Map<String, Object> map);
	
	int save(YatangDO yatang);
	
	int update(YatangDO yatang);
	
	int remove(Long id);
	
	int batchRemove(Long[] ids);

    PageResponse<YatangDO> queryPages(Map<String, Object> paramMap);
}
