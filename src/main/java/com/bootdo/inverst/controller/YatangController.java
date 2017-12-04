package com.bootdo.inverst.controller;

import java.util.List;
import java.util.Map;

import com.bootdo.common.utils.PageUtils;
import com.bootdo.common.utils.PageRequest;
import com.bootdo.common.utils.R;
import com.bootdo.inverst.domain.YatangDO;
import com.bootdo.inverst.service.YatangService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * 
 * @author yuefeng.liu
 * @email lyfai521@163.com
 * @date 2017-12-04 09:48:36
 */
 
@Controller
@RequestMapping("/inverst/yatang")
public class YatangController {
	@Autowired
	private YatangService yatangService;
	
	@GetMapping(value = "index")
	@RequiresPermissions("inverst:yatang:index")
	String Yatang(){
	    return "inverst/yatang/index";
	}
	
	@ResponseBody
	@GetMapping("/list")
	@RequiresPermissions("inverst:yatang:list")
	public PageUtils list(@RequestParam Map<String, Object> params){
		//查询列表数据
        PageRequest pageRequest = new PageRequest(params);
		List<YatangDO> yatangList = yatangService.list(pageRequest);
		int total = yatangService.count(pageRequest);
		PageUtils pageUtils = new PageUtils(yatangList, total);
		return pageUtils;
	}
	
	@GetMapping("/add")
	@RequiresPermissions("inverst:yatang:add")
	String add(){
	    return "inverst/yatang/add";
	}

	@GetMapping("/edit/{id}")
	@RequiresPermissions("inverst:yatang:edit")
	String edit(@PathVariable("id") Long id,Model model){
		YatangDO yatang = yatangService.get(id);
		model.addAttribute("yatang", yatang);
	    return "inverst/yatang/edit";
	}
	
	/**
	 * 保存
	 */
	@ResponseBody
	@PostMapping("/save")
	@RequiresPermissions("inverst:yatang:add")
	public R save( YatangDO yatang){
		if(yatangService.save(yatang)>0){
			return R.ok();
		}
		return R.error();
	}
	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("inverst:yatang:edit")
	public R update( YatangDO yatang){
		yatangService.update(yatang);
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@PostMapping( "/remove")
	@ResponseBody
	@RequiresPermissions("inverst:yatang:remove")
	public R remove( Long id){
		if(yatangService.remove(id)>0){
		return R.ok();
		}
		return R.error();
	}
	
	/**
	 * 删除
	 */
	@PostMapping( "/batchRemove")
	@ResponseBody
	@RequiresPermissions("inverst:yatang:batchRemove")
	public R remove(@RequestParam("ids[]") Long[] ids){
		yatangService.batchRemove(ids);
		return R.ok();
	}
	
}
