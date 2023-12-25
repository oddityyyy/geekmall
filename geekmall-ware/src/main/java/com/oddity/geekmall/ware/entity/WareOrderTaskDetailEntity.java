package com.oddity.geekmall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存工作单
 * 
 * @author oddity
 * @email aa1051953407@gmail.com
 * @date 2023-06-24 22:28:36
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("wms_ware_order_task_detail")
public class WareOrderTaskDetailEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * sku_id
	 */
	private Long skuId;
	/**
	 * sku_name
	 */
	private String skuName;
	/**
	 * 购买个数
	 */
	private Integer skuNum;
	/**
	 * 工作单id
	 */
	private Long taskId;
	/**
	 * 仓库id
	 */
	private Long wareId;
	/**
	 * 锁定状态 1-已锁定 2-已解锁
	 */
	private Integer lockStatus;

}
