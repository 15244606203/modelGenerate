package com.lfs.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

public class GetTables {

    String packageName = "package com.lfs.utils.model;";

    String sqlUrl = "jdbc:mysql://127.0.0.1:3306/a?characterEncoding=UTF-8";

    String sqlUser = "root";

    String sqlPassword = "123456";

    String tableSpace = "a";

    static List<String> tables = Arrays.asList(new String[]{"a"});


    public static void main(String[] args) throws SQLException, IOException {
        //new GetTables().makeClasses();
        new GetTables().makeClass(tables);
    }

    Connection con = getConnection();

    //数据库连接
    public Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("数据库驱动加载成功");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            con = DriverManager.getConnection(sqlUrl, sqlUser, sqlPassword);
            System.out.println("数据库连接成功");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }



    //获取数据库表
    public List<Table> getTables(String tableSchema) throws SQLException {

        List<Table> resp = new LinkedList<Table>();
        Statement statement = con.createStatement();
        //要执行的SQL语句
        String sql = "select * from information_schema.tables where table_schema='" + tableSchema + "'";
        //3.ResultSet类，用来存放获取的结果集！！
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Table table = new Table();
            table.setTable_name(rs.getString("table_name"));
            table.setTable_collation(rs.getString("table_comment"));
            resp.add(table);
        }

        return resp;
    }


    //获取表字段
    public List<Colum> getColums(String table) throws SQLException {
        List<Colum> resp = new LinkedList<Colum>();
        Statement statement = con.createStatement();
        //要执行的SQL语句
        String sql = "select * from information_schema.COLUMNS where table_schema='" + table + "'";
        //3.ResultSet类，用来存放获取的结果集！！
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Colum colum = new Colum();
            colum.setColumn_name(rs.getString("column_name"));
            colum.setColumn_type(rs.getString("column_type"));
            colum.setColumn_comment(rs.getString("column_comment"));
            resp.add(colum);
        }
        return resp;
    }

    public void makeClass(List<String> tables) throws SQLException, IOException {
        for (String s : tables) {
            List<Colum> colums = getColums(s);
            if (makeJava(s)){
                return;
            }
        }

    }

    public void makeClasses() throws SQLException, IOException {
        List<Table> a = getTables(tableSpace);
        for (Table table : a) {
            if (makeJava(table.getTable_name())) return;
        }
        System.out.println("成功生成");
    }

    private boolean makeJava(String table) throws SQLException, IOException {
        List<Colum> colums = getColums(table);
        String url = System.getProperty("user.dir") + "/src/main/java" + "/com/lfs/utils/model/";
        File file = new File(url + tableName(table) + ".java");
        if (file.exists()) {
            System.out.println("文件存在！");
            return true;
        }
        String parse = parse(table, colums);
        PrintWriter printWriter = new PrintWriter(new FileWriter(file, true), true);//第二个参数为true，从文件末尾写入 为false则从开头写入
        printWriter.println(parse);
        printWriter.close();//记得关闭输入流
        return false;
    }

    private String parse(String table, List<Colum> colum) {

        StringBuffer sb = new StringBuffer();
        sb.append( packageName + "\r\n\r\n\r\n");
        sb.append("import lombok.Data;\r\n");
        sb.append("import java.util.Date;\r\n");
        sb.append("import java.sql.*;\r\n\r\n\r\n");
        sb.append("@Data\n");
        sb.append("public class " + tableName(table) + " {\r\n");
        processAllAttrs(sb, colum);
        sb.append("}\r\n");

        return sb.toString();

    }

    /**
     * 解析输出属性
     *
     * @return
     */
    private void processAllAttrs(StringBuffer sb, List<Colum> columList) {
        for (Colum colum : columList) {
            sb.append("\t//" + colum.getColumn_comment() + "\r\n");
            sb.append("\tprivate " + sqlType2JavaType(colum.getColumn_type()) + " " +
                    camelCaseName(colum.getColumn_name()) + ";\r\n");
        }
    }

    /**
     * 转换为驼峰
     *
     * @param underscoreName
     * @return
     */
    public static String camelCaseName(String underscoreName) {
        StringBuilder result = new StringBuilder();
        if (underscoreName != null && underscoreName.length() > 0) {
            boolean flag = false;
            for (int i = 0; i < underscoreName.length(); i++) {
                char ch = underscoreName.charAt(i);
                if ("_".charAt(0) == ch) {
                    flag = true;
                } else {
                    if (flag) {
                        result.append(Character.toUpperCase(ch));
                        flag = false;
                    } else {
                        result.append(ch);
                    }
                }
            }
        }
        return result.toString();
    }

    //表明驼峰
    public static String tableName(String underscoreName) {
        String str = camelCaseName(underscoreName);
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    private String sqlType2JavaType(String sqlType) {
        if (sqlType.equalsIgnoreCase("bit")) {
            return "boolean";
        } else if (sqlType.equalsIgnoreCase("tinyint")) {
            return "byte";
        } else if (sqlType.equalsIgnoreCase("smallint")) {
            return "short";
        } else if (sqlType.equalsIgnoreCase("int")) {
            return "int";
        } else if (sqlType.equalsIgnoreCase("bigint")) {
            return "long";
        } else if (sqlType.equalsIgnoreCase("float")) {
            return "float";
        } else if (sqlType.equalsIgnoreCase("decimal")
                || sqlType.equalsIgnoreCase("numeric")
                || sqlType.equalsIgnoreCase("real")) {
            return "double";
        } else if (sqlType.equalsIgnoreCase("money")
                || sqlType.equalsIgnoreCase("smallmoney")) {
            return "double";
        } else if (sqlType.equalsIgnoreCase("varchar")
                || sqlType.equalsIgnoreCase("char")
                || sqlType.equalsIgnoreCase("nvarchar")
                || sqlType.equalsIgnoreCase("nchar")
                || sqlType.equalsIgnoreCase("uniqueidentifier")
                || sqlType.equalsIgnoreCase("ntext")) {
            return "String";
        } else if (sqlType.equalsIgnoreCase("datetime")
                || sqlType.equalsIgnoreCase("date")) {
            return "Date";
        } else if (sqlType.equalsIgnoreCase("image")) {
            return "Blob";
        } else if (sqlType.equalsIgnoreCase("text")) {
            return "Clob";
        }
        return "String";
    }
}
