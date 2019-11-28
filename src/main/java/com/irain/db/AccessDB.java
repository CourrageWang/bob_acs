package com.irain.db;

import com.irain.entity.Account;
import lombok.extern.log4j.Log4j;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.*;
import java.util.List;

/**
 * @Author: w
 * @Date: 2019/11/26 8:54 上午
 * Access数据库操作
 */
@Log4j
public class AccessDB {

    /**
     * 获取所有的员工账号
     *
     * @return
     */
    public static List<Account> getAllAccount() {

        String url = "jdbc:Access:///h:/Database31.mdb";
        List<Account> allColumns = null;
        try {

            Class.forName("com.hxtt.sql.access.AccessDriver");
            Connection conn = DriverManager.getConnection(url, "", "");

            Statement stat = conn.createStatement();

            String sql = "SELECT  Name ,Age ,Sex,Account,BackNUM FROM User";

            ResultSet rs = stat.executeQuery(sql);
            BeanListHandler<Account> handler = new BeanListHandler<Account>(Account.class);
            allColumns = handler.handle(rs);
            allColumns.stream().forEach(x -> System.out.println(x.toString()));

        } catch (ClassNotFoundException | SQLException e) {
            log.error("when connect Access DB happened error " + e.getMessage());
        }
        return allColumns;
    }
}