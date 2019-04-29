package com.danke.common;

import com.dankegongyu.app.common.canal.Mapping;
import com.dankegongyu.app.common.canal.Message;
import com.dankegongyu.app.common.canal.Process;
import org.junit.Test;

import java.util.List;

public class CanalTest {
    @Test
    public void testMessage() {
        String json = "{\"database\":\"Risk\",\"sqlType\":{\"get_city_date\":93,\"auth_date\":93,\"created_at\":93,\"user_resource\":12,\"scene\":12,\"human_id\":12,\"cert01\":12,\"cert03\":12,\"black01\":12,\"phone\":12,\"user_id\":12,\"contract_city\":12,\"idcard\":12,\"name\":12,\"id\":4,\"remarks\":12,\"match_rule_collection\":12,\"req_serial\":12},\"data\":[{\"get_city_date\":null,\"auth_date\":\"2019-04-29 10:28:00.000\",\"created_at\":\"2019-04-29 10:28:00\",\"user_resource\":\"10001\",\"scene\":\"011\",\"human_id\":\"762587\",\"cert01\":\"PASS\",\"cert03\":\"PASS\",\"black01\":\"NO\",\"phone\":\"13588213022\",\"user_id\":\"2256345\",\"contract_city\":\"杭州市\",\"idcard\":\"33010419950329381X\",\"name\":\"钱畅\",\"id\":\"252465\",\"remarks\":null,\"match_rule_collection\":null,\"req_serial\":\"7aa911e5d9446f989f8b0c05e83d20ed\"}],\"old\":null,\"mysqlType\":{\"get_city_date\":\"datetime\",\"auth_date\":\"datetime(3)\",\"created_at\":\"datetime\",\"user_resource\":\"varchar(21)\",\"scene\":\"varchar(10)\",\"human_id\":\"varchar(11)\",\"cert01\":\"varchar(21)\",\"cert03\":\"varchar(21)\",\"black01\":\"varchar(21)\",\"phone\":\"varchar(20)\",\"user_id\":\"varchar(11)\",\"contract_city\":\"varchar(21)\",\"idcard\":\"varchar(21)\",\"name\":\"varchar(50)\",\"id\":\"int(11) unsigned\",\"remarks\":\"varchar(100)\",\"match_rule_collection\":\"varchar(256)\",\"req_serial\":\"varchar(60)\"},\"id\":13507,\"type\":\"INSERT\",\"es\":1556504880000,\"isDdl\":false,\"table\":\"risk_city_report\",\"sql\":\"\",\"ts\":1556504880251}";

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

    @Test
    public void testNull(){
//        Mapping.toDate(null);
    }
}
