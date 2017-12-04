package com.bootdo.inverst.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;



/**
 * 
 * 
 * @author yuefeng.liu
 * @email lyfai521@163.com
 * @date 2017-12-04 09:48:36
 */
public class YatangDO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Long id;
	//登陆用户名
	private String userName;
	//登陆密码
	private String password;
	//支付密码
	private String payPassword;
	//使用的最大红包倍数
	private Integer maxCoupon;
	//保留账号剩余款
	private BigDecimal keepMoney;
	//投资金额
	private BigDecimal inverstMoney;
	//0:正常  1：已删除
	private String isDel;
	//
	private Long createBy;
	//
	private Date createDate;
	//
	private Long updateBy;
	//
	private Date updateDate;

	/**
	 * 设置：
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * 获取：
	 */
	public Long getId() {
		return id;
	}
	/**
	 * 设置：登陆用户名
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * 获取：登陆用户名
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * 设置：登陆密码
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * 获取：登陆密码
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * 设置：支付密码
	 */
	public void setPayPassword(String payPassword) {
		this.payPassword = payPassword;
	}
	/**
	 * 获取：支付密码
	 */
	public String getPayPassword() {
		return payPassword;
	}
	/**
	 * 设置：使用的最大红包倍数
	 */
	public void setMaxCoupon(Integer maxCoupon) {
		this.maxCoupon = maxCoupon;
	}
	/**
	 * 获取：使用的最大红包倍数
	 */
	public Integer getMaxCoupon() {
		return maxCoupon;
	}
	/**
	 * 设置：保留账号剩余款
	 */
	public void setKeepMoney(BigDecimal keepMoney) {
		this.keepMoney = keepMoney;
	}
	/**
	 * 获取：保留账号剩余款
	 */
	public BigDecimal getKeepMoney() {
		return keepMoney;
	}
	/**
	 * 设置：投资金额
	 */
	public void setInverstMoney(BigDecimal inverstMoney) {
		this.inverstMoney = inverstMoney;
	}
	/**
	 * 获取：投资金额
	 */
	public BigDecimal getInverstMoney() {
		return inverstMoney;
	}
	/**
	 * 设置：0:正常  1：已删除
	 */
	public void setIsDel(String isDel) {
		this.isDel = isDel;
	}
	/**
	 * 获取：0:正常  1：已删除
	 */
	public String getIsDel() {
		return isDel;
	}
	/**
	 * 设置：
	 */
	public void setCreateBy(Long createBy) {
		this.createBy = createBy;
	}
	/**
	 * 获取：
	 */
	public Long getCreateBy() {
		return createBy;
	}
	/**
	 * 设置：
	 */
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	/**
	 * 获取：
	 */
	public Date getCreateDate() {
		return createDate;
	}
	/**
	 * 设置：
	 */
	public void setUpdateBy(Long updateBy) {
		this.updateBy = updateBy;
	}
	/**
	 * 获取：
	 */
	public Long getUpdateBy() {
		return updateBy;
	}
	/**
	 * 设置：
	 */
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	/**
	 * 获取：
	 */
	public Date getUpdateDate() {
		return updateDate;
	}
}
