package com.bootdo.inverst.service.impl;

import java.util.List;
import java.util.Map;

import com.bootdo.common.utils.PageRequest;
import com.bootdo.inverst.dao.YatangDao;
import com.bootdo.inverst.domain.YatangDO;
import com.bootdo.inverst.service.YatangService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class YatangServiceImpl implements YatangService {
	@Autowired
	private YatangDao yatangDao;
	
	@Override
	public YatangDO get(Long id){
		return yatangDao.get(id);
	}
	
	@Override
	public List<YatangDO> list(Map<String, Object> map){
		return yatangDao.list(map);
	}
	
	@Override
	public int count(Map<String, Object> map){
		return yatangDao.count(map);
	}
	
	@Override
	public int save(YatangDO yatang){
		return yatangDao.save(yatang);
	}
	
	@Override
	public int update(YatangDO yatang){
		return yatangDao.update(yatang);
	}
	
	@Override
	public int remove(Long id){
		return yatangDao.remove(id);
	}
	
	@Override
	public int batchRemove(Long[] ids){
		return yatangDao.batchRemove(ids);
	}

    @Override
    public Page<YatangDO> queryPages(PageRequest pageRequest) {
        PageHelper.startPage(pageRequest);
        return yatangDao.list(pageRequest);
    }

}
