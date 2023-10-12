package com.atguigu.gulimall.product.entity;

import com.atguigu.common.valid.*;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author leifengyang
 * @email leifengyang@gmail.com
 * @date 2019-10-01 21:08:49
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
//	@NotNull(message = "修改必须指定品牌id",groups = {UpdateGroup.class})
//	@Null(message = "新增不能指定id",groups = {AddGroup.class})
//	@NotNull("")
	@TableId
	@Null(message = "追加时品牌ID不能有值", groups = {Add2Group.class})
	@NotNull(message = "修改时不能为空品牌ID", groups ={Update2Group.class} )
	private Long brandId;
	/**
	 * 品牌名
	 */
//	@NotBlank(message = "品牌名必须提交",groups = {AddGroup.class,UpdateGroup.class})
	@NotBlank(message = "品牌名必须提交", groups = {Add2Group.class, Update2Group.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotBlank(groups = {Add2Group.class})
	@URL(message = "logo必须是一个合法的url地址",groups={Add2Group.class,Update2Group.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
//	@Pattern()
	@NotNull(groups = {Add2Group.class, Update2Group.class})
	@List2Value(values={0,1},groups = {Add2Group.class, UpdateStatusGroup.class})
	//@NotNull
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotEmpty(groups={Add2Group.class})
	@Pattern(regexp="^[a-zA-Z]$",message = "检索首字母必须是一个字母",groups={Add2Group.class,Update2Group.class})
	private String firstLetter;
	/**
	 * 排序
	 */
//	@NotNull(groups={AddGroup.class})
//	@Min(value = 0,message = "排序必须大于等于0",groups={AddGroup.class,UpdateGroup.class})
	@NotNull(groups = {Add2Group.class})
	@Min(value=0, message = "排序必须大于等于0", groups = {Add2Group.class, Update2Group.class})
	private Integer sort;

}
