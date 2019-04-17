package com.danke.common;

import com.dankegongyu.app.common.canal.Message;
import com.dankegongyu.app.common.canal.Process;
import org.junit.Test;

import java.util.List;

public class CanalTest {
    @Test
    public void testMessage() {
        String json = "{\"database\":\"Laputa\",\"sqlType\":{\"agent_id\":4,\"referrer_id\":4,\"terminate_by\":4,\"channel\":12,\"finance_id\":4,\"rent_approve_by\":4,\"sign_at\":93,\"terminate_remain_water_fuel_fen\":4,\"isz_id\":12,\"number\":12,\"service_pay_cycle\":12,\"urgency_relation\":12,\"housekeeper_id\":4,\"price\":4,\"terminate_refund_yuan\":4,\"terminate_fine_yuan\":4,\"first_approve_status\":4,\"product_sources\":12,\"id\":4,\"second_approve_note\":12,\"sign_type\":12,\"wuye_approve_status\":4,\"keeper_id\":4,\"first_approve_note\":12,\"wuye_approve_note\":12,\"ver\":1,\"terminate_compensation_reason\":12,\"images\":-4,\"terminate_at\":93,\"agent_dealer_payment_yuan\":4,\"sharer_id\":4,\"sign_date\":91,\"delivery_items\":2005,\"service_fee_fen\":4,\"urgency_contract\":12,\"terminate_compensation_note\":12,\"terminate_note\":12,\"payment_cycle\":4,\"second_approve_status\":4,\"terminate_remain_rent_yuan\":4,\"maintain_fee_fen\":4,\"terminate_remain_deposit_yuan\":4,\"dealer_receive_at\":93,\"hand_in_at\":93,\"terminate_remain_service_fee_fen\":4,\"change_id\":4,\"rent_months\":-6,\"deposit\":4,\"terminate_fine_note\":12,\"coupon_type\":4,\"rent_approve_status\":4,\"status\":4,\"room_id\":4,\"end_date\":91,\"urgency_mobile\":-5,\"note\":12,\"manage_status\":4,\"keys_voucher\":2005,\"second_approve_at\":93,\"created_at\":93,\"real_price\":4,\"subcompany_id\":5,\"referrer_mobile\":-5,\"booking_id\":4,\"terminate_activity_withhold_yuan\":4,\"water_fuel_fen\":4,\"first_approve_at\":93,\"updated_at\":93,\"terminate_date\":91,\"customer_source\":12,\"wuye_approve_by\":4,\"terminate_refund_status\":4,\"terminate_compensation_yuan\":4,\"dealer_id\":4,\"start_date\":91,\"coupon_amount\":4,\"monthly_pay_way\":12,\"second_approve_by\":4,\"terminate_bill_fine_yuan\":4,\"housekeeper_team_id\":4,\"previous_id\":4,\"mobile\":12,\"rent_approve_at\":93,\"terminate_remain_maintain_fee_fen\":4,\"rent_approve_note\":12,\"effective_at\":93,\"daikan_id\":4,\"stage\":4,\"first_approve_by\":4,\"terminate_fine_reason\":12,\"sign_source\":12,\"terminate_type\":12,\"customer_id\":4,\"wuye_approve_at\":93},\"data\":[{\"agent_id\":null,\"referrer_id\":null,\"terminate_by\":\"13983\",\"channel\":null,\"finance_id\":\"387904\",\"rent_approve_by\":null,\"sign_at\":\"2019-02-24 11:14:59\",\"terminate_remain_water_fuel_fen\":null,\"isz_id\":null,\"number\":\"TJZFCTJE19026835834\",\"service_pay_cycle\":\"月付\",\"urgency_relation\":\"朋友\",\"housekeeper_id\":null,\"price\":\"1960\",\"terminate_refund_yuan\":null,\"terminate_fine_yuan\":null,\"first_approve_status\":\"1\",\"product_sources\":null,\"id\":\"634250\",\"second_approve_note\":null,\"sign_type\":\"年租\",\"wuye_approve_status\":\"2\",\"keeper_id\":\"4627\",\"first_approve_note\":null,\"wuye_approve_note\":\"\",\"ver\":\"4.0\",\"terminate_compensation_reason\":null,\"images\":null,\"terminate_at\":\"2019-04-17 00:00:00\",\"agent_dealer_payment_yuan\":\"0\",\"sharer_id\":null,\"sign_date\":\"2019-02-24\",\"delivery_items\":\"{\\\"房间\\\":{\\\"台灯\\\":{\\\"name\\\":\\\"台灯\\\",\\\"num\\\":\\\"1\\\",\\\"note\\\":\\\"\\\"},\\\"床\\\":{\\\"name\\\":\\\"床\\\",\\\"num\\\":\\\"1\\\",\\\"note\\\":\\\"\\\"},\\\"床垫\\\":{\\\"name\\\":\\\"床垫\\\",\\\"num\\\":\\\"1\\\",\\\"note\\\":\\\"\\\"},\\\"空调\\\":{\\\"name\\\":\\\"空调\\\",\\\"num\\\":\\\"1\\\",\\\"note\\\":\\\"\\\"},\\\"晾衣架\\\":{\\\"name\\\":\\\"晾衣架\\\",\\\"num\\\":\\\"1\\\",\\\"note\\\":\\\"\\\"},\\\"垃圾桶\\\":{\\\"name\\\":\\\"垃圾桶\\\",\\\"num\\\":\\\"1\\\",\\\"note\\\":\\\"\\\"},\\\"装饰画\\\":{\\\"name\\\":\\\"装饰画\\\",\\\"num\\\":\\\"2\\\",\\\"note\\\":\\\"\\\"},\\\"衣柜\\\":{\\\"name\\\":\\\"衣柜\\\",\\\"num\\\":\\\"1\\\",\\\"note\\\":\\\"\\\"},\\\"床头柜\\\":{\\\"name\\\":\\\"床头柜\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"桌子\\\":{\\\"name\\\":\\\"桌子\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"椅子\\\":{\\\"name\\\":\\\"椅子\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"电视\\\":{\\\"name\\\":\\\"电视\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"电视柜\\\":{\\\"name\\\":\\\"电视柜\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"落地灯\\\":{\\\"name\\\":\\\"落地灯\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"沙发\\\":{\\\"name\\\":\\\"沙发\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"茶几\\\":{\\\"name\\\":\\\"茶几\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"休闲桌椅\\\":{\\\"name\\\":\\\"休闲桌椅\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"洗衣机\\\":{\\\"name\\\":\\\"洗衣机\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"热水器\\\":{\\\"name\\\":\\\"热水器\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"}},\\\"厨房\\\":{\\\"油烟机\\\":{\\\"name\\\":\\\"油烟机\\\",\\\"num\\\":\\\"1\\\",\\\"note\\\":\\\"\\\"},\\\"燃气灶\\\":{\\\"name\\\":\\\"燃气灶\\\",\\\"num\\\":\\\"1\\\",\\\"note\\\":\\\"\\\"},\\\"微波炉\\\":{\\\"name\\\":\\\"微波炉\\\",\\\"num\\\":\\\"1\\\",\\\"note\\\":\\\"\\\"},\\\"电冰箱\\\":{\\\"name\\\":\\\"电冰箱\\\",\\\"num\\\":\\\"1\\\",\\\"note\\\":\\\"\\\"},\\\"热水器\\\":{\\\"name\\\":\\\"热水器\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"}},\\\"卫生间\\\":{\\\"马桶\\\":{\\\"name\\\":\\\"马桶\\\",\\\"num\\\":\\\"1\\\",\\\"note\\\":\\\"\\\"},\\\"洗手盆\\\":{\\\"name\\\":\\\"洗手盆\\\",\\\"num\\\":\\\"1\\\",\\\"note\\\":\\\"\\\"},\\\"热水器\\\":{\\\"name\\\":\\\"热水器\\\",\\\"num\\\":\\\"1\\\",\\\"note\\\":\\\"\\\"},\\\"洗衣机\\\":{\\\"name\\\":\\\"洗衣机\\\",\\\"num\\\":\\\"1\\\",\\\"note\\\":\\\"\\\"},\\\"排风扇\\\":{\\\"name\\\":\\\"排风扇\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"浴霸\\\":{\\\"name\\\":\\\"浴霸\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"淋浴\\\":{\\\"name\\\":\\\"淋浴\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"}},\\\"公共区域\\\":{\\\"餐桌\\\":{\\\"name\\\":\\\"餐桌\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"餐椅\\\":{\\\"name\\\":\\\"餐椅\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"脚垫\\\":{\\\"name\\\":\\\"脚垫\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"沙发\\\":{\\\"name\\\":\\\"沙发\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"茶几\\\":{\\\"name\\\":\\\"茶几\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"}},\\\"其他\\\":[{\\\"name\\\":\\\"\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},{\\\"name\\\":\\\"\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},{\\\"name\\\":\\\"\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},{\\\"name\\\":\\\"\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},{\\\"name\\\":\\\"\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},{\\\"name\\\":\\\"\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},{\\\"name\\\":\\\"\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},{\\\"name\\\":\\\"\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"}]}\",\"service_fee_fen\":\"13700\",\"urgency_contract\":\"梦娜\",\"terminate_compensation_note\":null,\"terminate_note\":\"租户要更换支付方式，扣除30%的押金，返还剩余房租  \",\"payment_cycle\":\"1\",\"second_approve_status\":\"1\",\"terminate_remain_rent_yuan\":null,\"maintain_fee_fen\":\"1900\",\"terminate_remain_deposit_yuan\":null,\"dealer_receive_at\":null,\"hand_in_at\":null,\"terminate_remain_service_fee_fen\":null,\"change_id\":null,\"rent_months\":\"12\",\"deposit\":\"1960\",\"terminate_fine_note\":null,\"coupon_type\":null,\"rent_approve_status\":\"2\",\"status\":\"9\",\"room_id\":\"201501\",\"end_date\":\"2020-02-22\",\"urgency_mobile\":\"15901882894\",\"note\":null,\"manage_status\":\"8\",\"keys_voucher\":\"{\\\"房门钥匙\\\":{\\\"name\\\":\\\"房门钥匙\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"单元门钥匙\\\":{\\\"name\\\":\\\"单元门钥匙\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"门禁卡\\\":{\\\"name\\\":\\\"门禁卡\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"信箱钥匙\\\":{\\\"name\\\":\\\"信箱钥匙\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"电卡\\\":{\\\"name\\\":\\\"电卡\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"水卡\\\":{\\\"name\\\":\\\"水卡\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"},\\\"煤气卡\\\":{\\\"name\\\":\\\"煤气卡\\\",\\\"num\\\":\\\"0\\\",\\\"note\\\":\\\"\\\"}}\",\"second_approve_at\":null,\"created_at\":\"2019-02-24 11:14:59\",\"real_price\":\"1960\",\"subcompany_id\":\"5\",\"referrer_mobile\":null,\"booking_id\":\"409616\",\"terminate_activity_withhold_yuan\":null,\"water_fuel_fen\":\"4500\",\"first_approve_at\":null,\"updated_at\":\"2019-04-17 16:58:54\",\"terminate_date\":\"2019-04-17\",\"customer_source\":\"58\",\"wuye_approve_by\":\"2064\",\"terminate_refund_status\":\"1\",\"terminate_compensation_yuan\":null,\"dealer_id\":\"23518\",\"start_date\":\"2019-02-23\",\"coupon_amount\":null,\"monthly_pay_way\":\"微众银行\",\"second_approve_by\":null,\"terminate_bill_fine_yuan\":null,\"housekeeper_team_id\":null,\"previous_id\":null,\"mobile\":\"15046416555\",\"rent_approve_at\":\"2019-02-24 11:29:57\",\"terminate_remain_maintain_fee_fen\":null,\"rent_approve_note\":null,\"effective_at\":\"2019-02-25 20:00:13\",\"daikan_id\":\"2312020\",\"stage\":\"3\",\"first_approve_by\":null,\"terminate_fine_reason\":null,\"sign_source\":null,\"terminate_type\":\"重签\",\"customer_id\":\"481805\",\"wuye_approve_at\":null}],\"old\":[{\"terminate_note\":null,\"terminate_at\":null,\"stage\":\"2\",\"updated_at\":\"2019-04-16 11:46:23\",\"terminate_date\":null,\"terminate_by\":null,\"terminate_refund_status\":null,\"terminate_type\":null,\"status\":\"6\"}],\"mysqlType\":{\"agent_id\":\"int(10) unsigned\",\"referrer_id\":\"int(10) unsigned\",\"terminate_by\":\"int(10) unsigned\",\"channel\":\"varchar(10)\",\"finance_id\":\"int(11)\",\"rent_approve_by\":\"int(10) unsigned\",\"sign_at\":\"datetime\",\"terminate_remain_water_fuel_fen\":\"int(11)\",\"isz_id\":\"varchar(35)\",\"number\":\"varchar(32)\",\"service_pay_cycle\":\"varchar(10)\",\"urgency_relation\":\"varchar(16)\",\"housekeeper_id\":\"int(10) unsigned\",\"price\":\"int(10) unsigned\",\"terminate_refund_yuan\":\"int(10)\",\"terminate_fine_yuan\":\"int(10) unsigned\",\"first_approve_status\":\"enum('待审核','合格','不合格','作废')\",\"product_sources\":\"varchar(64)\",\"id\":\"int(10) unsigned\",\"second_approve_note\":\"varchar(100)\",\"sign_type\":\"varchar(10)\",\"wuye_approve_status\":\"enum('待审核','合格','不合格','作废')\",\"keeper_id\":\"int(10) unsigned\",\"first_approve_note\":\"varchar(100)\",\"wuye_approve_note\":\"varchar(100)\",\"ver\":\"char(4)\",\"terminate_compensation_reason\":\"varchar(32)\",\"images\":\"text\",\"terminate_at\":\"datetime\",\"agent_dealer_payment_yuan\":\"int(10) unsigned\",\"sharer_id\":\"int(10) unsigned\",\"sign_date\":\"date\",\"delivery_items\":\"text\",\"service_fee_fen\":\"int(10) unsigned\",\"urgency_contract\":\"varchar(16)\",\"terminate_compensation_note\":\"varchar(255)\",\"terminate_note\":\"varchar(255)\",\"payment_cycle\":\"enum('月付','季付','半年付','年付','一次付清','四月付','二月付')\",\"second_approve_status\":\"enum('待审核','合格','不合格','作废')\",\"terminate_remain_rent_yuan\":\"int(11)\",\"maintain_fee_fen\":\"int(10) unsigned\",\"terminate_remain_deposit_yuan\":\"int(11)\",\"dealer_receive_at\":\"datetime\",\"hand_in_at\":\"datetime\",\"terminate_remain_service_fee_fen\":\"int(11)\",\"change_id\":\"int(10) unsigned\",\"rent_months\":\"tinyint(3) unsigned\",\"deposit\":\"int(10) unsigned\",\"terminate_fine_note\":\"varchar(255)\",\"coupon_type\":\"enum('减租金','加租期')\",\"rent_approve_status\":\"enum('待审核','合格','不合格','作废')\",\"status\":\"enum('未生效','有效','未签约','待签约','签约待确认','已签约','期满','甲方违约终止','乙方违约终止','双方协商终止')\",\"room_id\":\"int(10) unsigned\",\"end_date\":\"date\",\"urgency_mobile\":\"bigint(20) unsigned\",\"note\":\"varchar(50)\",\"manage_status\":\"enum('未领取','已领取','变更领取','请求退回','请求作废','未提交审核','待审批','合格','不合格','作废')\",\"keys_voucher\":\"text\",\"second_approve_at\":\"datetime\",\"created_at\":\"datetime\",\"real_price\":\"int(10) unsigned\",\"subcompany_id\":\"smallint(5) unsigned\",\"referrer_mobile\":\"bigint(20) unsigned\",\"booking_id\":\"int(10) unsigned\",\"terminate_activity_withhold_yuan\":\"int(10) unsigned\",\"water_fuel_fen\":\"int(10) unsigned\",\"first_approve_at\":\"datetime\",\"updated_at\":\"datetime\",\"terminate_date\":\"date\",\"customer_source\":\"varchar(50)\",\"wuye_approve_by\":\"int(10) unsigned\",\"terminate_refund_status\":\"enum('未退款','已存到余额','申请提现','驳回申请','等待财务付款','已付款','待补交罚款','已补交罚款','未补交罚款')\",\"terminate_compensation_yuan\":\"int(10) unsigned\",\"dealer_id\":\"int(10) unsigned\",\"start_date\":\"date\",\"coupon_amount\":\"int(10) unsigned\",\"monthly_pay_way\":\"varchar(16)\",\"second_approve_by\":\"int(10) unsigned\",\"terminate_bill_fine_yuan\":\"int(10) unsigned\",\"housekeeper_team_id\":\"int(10) unsigned\",\"previous_id\":\"int(10) unsigned\",\"mobile\":\"varchar(11)\",\"rent_approve_at\":\"datetime\",\"terminate_remain_maintain_fee_fen\":\"int(10)\",\"rent_approve_note\":\"varchar(255)\",\"effective_at\":\"datetime\",\"daikan_id\":\"int(10) unsigned\",\"stage\":\"enum('未执行','执行中','执行结束')\",\"first_approve_by\":\"int(10) unsigned\",\"terminate_fine_reason\":\"varchar(32)\",\"sign_source\":\"varchar(16)\",\"terminate_type\":\"varchar(16)\",\"customer_id\":\"int(10) unsigned\",\"wuye_approve_at\":\"datetime\"},\"id\":613,\"type\":\"UPDATE\",\"es\":1555491534000,\"isDdl\":false,\"table\":\"contract_with_customers\",\"sql\":\"\",\"ts\":1555491534767}\n";

        List<Message> listMsg = com.dankegongyu.app.common.canal.Message.fromJson(json);
        String filter = listMsg.get(0).getDatabase() + "." + listMsg.get(0).getTable();
//        List<Process> list = getProcess(filter);
//        for (Process process : list) {
//            doProcess(process, listMsg);
//        }
        System.out.println("OK");

    }

    @Test
    public void testMatch() {
        String pattern = "Laputa.aa";
        String tableName = "Laputa.aa";
        boolean ret = tableName.matches(pattern);
        ret = tableName.matches("Laputa\\..*");
        System.out.println("OK");

    }
}
