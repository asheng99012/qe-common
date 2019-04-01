package com.danke.common;

import com.dankegongyu.app.common.canal.Message;
import org.junit.Test;

public class CanalTest {
    @Test
    public void testMessage() {
        String json = "{\"data\":[{\"id\":\"25\",\"title\":\"改个名字aa\",\"selectSql\":\"select faw.withdraw_successfully_at as 退件成功时间,\\r\\n       faw.status as 退件状态,\\r\\n       faw.company as 分期公司,\\r\\n       fi.third_party_id as 金融订单号,\\r\\n       fi.status as 订单状态,\\r\\n       cwc.number as 合同号,\\r\\n       cwc.status as 合同状态 ,\\r\\n       customer.id_name as 租户姓名,\\r\\n       customer.mobile as 租户手机\\r\\nfrom finance_approve_withdraws faw\\r\\nleft join finance_instalments fi on faw.installment_id=fi.id\\r\\nleft join contract_with_customers cwc on fi.contract_id=cwc.id\\r\\nleft join humans  customer on cwc.customer_id = customer.id\\r\\nwhere faw.company='微众银行'\\r\\n  and faw.status='退件成功'\\r\\n  and faw.operate_at = faw.updated_at\\r\\n  and faw.updated_at = faw.created_at\\r\\n  and faw.created_at = faw.withdraw_at\\r\\n  and faw.withdraw_at = faw.withdraw_successfully_at\\r\\norder by faw.id desc\",\"countSql\":null,\"dbName\":\"defaultSlave\",\"condition\":\"[]\",\"tongji\":\"\",\"relation\":\"[]\",\"status\":\"1\",\"permision\":\"\",\"operater\":null,\"addTime\":\"2019-03-30 13:35:29\",\"updateTime\":\"2019-03-30 13:35:29\"},{\"id\":\"25\",\"title\":\"改个名字aa\",\"selectSql\":\"select faw.withdraw_successfully_at as 退件成功时间,\\r\\n       faw.status as 退件状态,\\r\\n       faw.company as 分期公司,\\r\\n       fi.third_party_id as 金融订单号,\\r\\n       fi.status as 订单状态,\\r\\n       cwc.number as 合同号,\\r\\n       cwc.status as 合同状态 ,\\r\\n       customer.id_name as 租户姓名,\\r\\n       customer.mobile as 租户手机\\r\\nfrom finance_approve_withdraws faw\\r\\nleft join finance_instalments fi on faw.installment_id=fi.id\\r\\nleft join contract_with_customers cwc on fi.contract_id=cwc.id\\r\\nleft join humans  customer on cwc.customer_id = customer.id\\r\\nwhere faw.company='微众银行'\\r\\n  and faw.status='退件成功'\\r\\n  and faw.operate_at = faw.updated_at\\r\\n  and faw.updated_at = faw.created_at\\r\\n  and faw.created_at = faw.withdraw_at\\r\\n  and faw.withdraw_at = faw.withdraw_successfully_at\\r\\norder by faw.id desc\",\"countSql\":null,\"dbName\":\"defaultSlave\",\"condition\":\"[]\",\"tongji\":\"\",\"relation\":\"[]\",\"status\":\"1\",\"permision\":\"\",\"operater\":null,\"addTime\":\"2019-03-30 13:35:29\",\"updateTime\":\"2019-03-30 13:35:29\"}],\"database\":\"Laputa\",\"es\":1553924126000,\"id\":841,\"isDdl\":false,\"mysqlType\":{\"id\":\"int(11) unsigned\",\"title\":\"varchar(100)\",\"selectSql\":\"text\",\"countSql\":\"text\",\"dbName\":\"varchar(255)\",\"condition\":\"varchar(1000)\",\"tongji\":\"varchar(1000)\",\"relation\":\"varchar(1000)\",\"status\":\"int(11)\",\"permision\":\"varchar(1000)\",\"operater\":\"varchar(50)\",\"addTime\":\"datetime\",\"updateTime\":\"datetime\"},\"old\":[{\"title\":null,\"addTime\":\"2019-03-30 11:28:10\",\"updateTime\":\"2019-03-30 11:28:10\"}],\"sql\":\"\",\"sqlType\":{\"id\":4,\"title\":12,\"selectSql\":2005,\"countSql\":-4,\"dbName\":12,\"condition\":12,\"tongji\":12,\"relation\":12,\"status\":4,\"permision\":12,\"operater\":12,\"addTime\":93,\"updateTime\":93},\"table\":\"report\",\"ts\":1553924126282,\"type\":\"UPDATE\"}";

        Object message = Message.fromJson(json);
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
