package com.bootdo.inverst.dao;

import java.util.Map;

import com.bootdo.inverst.domain.YatangDO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * @author yuefeng.liu
 * @email lyfai521@163.com
 * @date 2017-12-04 09:48:36
 */
@Mapper
public interface YatangDao {

	YatangDO get(Long id);
	
	Page<YatangDO> list(Map<String, Object> map);
	
	int count(Map<String, Object> map);
	
	int save(YatangDO yatang);
	
	int update(YatangDO yatang);
	
	int remove(Long id);
	
	int batchRemove(Long[] ids);
}
