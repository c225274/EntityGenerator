package com.ak;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author pujan
 */
public class EntityDunlop {

    static String tablename = "M_ENTITY";
    static String pk = "ENTITY_ID";
    static String modelName = "EntityM";

    public static Map jdbcm = new HashMap() {
        {
            put("DATE", "Date");
            put("VARCHAR", "String");
            put("VARCHAR2", "String");
            put("LONGVARCHAR", "String");
            put("CHAR", "String");
            put("TIMESTAMP", "Date");
            put("BOOLEAN", "boolean");
            put("BIT", "boolean");
            put("NUMBER", "BigDecimal");
            put("NUMERIC", "BigDecimal");
            put("TINYINT", "byte");
            put("SMALLINT", "short");
        }
    };

    public static void main(String[] args) throws ClassNotFoundException, SQLException {

        //System.out.println(args.length);
//        if (args.length >= 2) {
//            tablename = args[0];
//            modelName = args[1];
//        } else {
//            System.out.println("java -cp /Users/pujan/Jar/ojdbc6.jar:. MetaDataDemo dbtablename modelname");
//            System.exit(0);
//        }
        getConnection(tablename, modelName);
    }

    public static void getConnection(String tablename, String modelName) throws ClassNotFoundException, SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

//        String url = "jdbc:oracle:thin:@172.16.220.133:1521/AVADB10G";
//        String username = "EAF_MASTER";
//        String password = "password";
        String url = "jdbc:db2://172.16.220.151:50000/OTPDB";
        String username = "db2admin";
        String password = "password";

        try {
            //Class.forName("oracle.jdbc.driver.OracleDriver");
            // 

            Class.forName("com.ibm.db2.jcc.DB2Driver");

            conn = DriverManager.getConnection(url, username, password);
            ps = conn.prepareStatement("SELECT * FROM EMPLOYEE");
            
            //ps = conn.prepareStatement("SELECT * FROM " + tablename);
            rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();

            Map m = new HashMap();
            int cnt = 0;
            while (cnt < rsmd.getColumnCount()) {
                ++cnt;
                m.put(rsmd.getColumnName(cnt), rsmd.getColumnTypeName(cnt));
            }

            StringBuffer sbmethod = new StringBuffer();

            StringBuffer sbmodel = new StringBuffer();
            sbmodel.append("public class " + modelName + " {\n");

            StringBuffer sbmodelToString = new StringBuffer();
            sbmodelToString.append("@Override\n    public String toString() {\n        return \"" + modelName + "[");

            sbmethod.append("public static ArrayList<" + modelName + "> listEntity() throws SQLException{\n");
            sbmethod.append("	Connection conn = null;\n");
            sbmethod.append("   PreparedStatement ps = null;\n");
            sbmethod.append("   ResultSet rs = null;\n");
            sbmethod.append("   ArrayList<" + modelName + "> result = new ArrayList<" + modelName + ">();\n");
            sbmethod.append("   try {\n");
            sbmethod.append("	  conn = getDSConnection();\n");
            sbmethod.append("	  String query = \"SELECT * FROM " + tablename + "\";\n");
            sbmethod.append("	  ps = conn.prepareStatement(query);\n");
            sbmethod.append("	  rs = ps.executeQuery();\n");
            sbmethod.append("     while (rs.next()) {\n");
            sbmethod.append("         " + modelName + " em=new " + modelName + "();\n");

            for (Iterator it = m.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                String item = namingConvert1(key);
                sbmethod.append("         em.set" + item + "(rs.get" + getJavaDbMap(value) + "(\"" + key + "\"));\n");

                sbmodel.append("      " + getJavaDbMap(value) + " " + namingConvert2(key) + ";\n");
                sbmodel.append("      public " + getJavaDbMap(value) + " get" + namingConvert1(key) + "(){\n");
                sbmodel.append("         return " + namingConvert2(key) + ";\n");
                sbmodel.append("      }\n\n");
                sbmodel.append("      public void set" + namingConvert1(key) + "(" + getJavaDbMap(value) + " " + namingConvert2(key) + "){\n");
                sbmodel.append("         this." + namingConvert2(key) + "=" + namingConvert2(key) + ";\n");
                sbmodel.append("      }\n\n");

                sbmodelToString.append(namingConvert2(key) + "=\"+" + namingConvert2(key) + "+\", ");
            }

            sbmethod.append("       result.add(em);\n");
            sbmethod.append("    }\n");
            sbmethod.append("    }catch (SQLException e) {\n");
            sbmethod.append("		e.printStackTrace();\n");
            sbmethod.append("    } finally {\n");
            sbmethod.append("        ps.close();\n");
            sbmethod.append("        rs.close();\n");
            sbmethod.append("        conn.close();\n");
            sbmethod.append("     }\n");
            sbmethod.append("   return result;\n");
            sbmethod.append("}\n");

            System.out.println(sbmethod.toString());

            System.out.println("\n----------------------------------------");
            sbmodelToString.append("]\";\n}\n\n");
            sbmodel.append(sbmodelToString.toString());
            sbmodel.append("}");
            System.out.println(sbmodel.toString());

            StringBuffer sbcount = new StringBuffer();
            sbcount.append("public static int count"+modelName+"() throws SQLException, ClassNotFoundException {\n"
                    + "        Connection conn = null;\n"
                    + "        PreparedStatement ps = null;\n"
                    + "        ResultSet rs = null;\n"
                    + "        int result = 0;\n"
                    + "        try {\n"
                    + "            conn = App.getConnection();\n"
                    + "            String query = \"SELECT COUNT(*) FROM " + tablename + " t\";\n"
                    + "            ps = conn.prepareStatement(query);\n"
                    + "            rs = ps.executeQuery();\n"
                    + "            rs.next();\n"
                    + "            result = rs.getInt(1);\n"
                    + "        } catch (SQLException e) {\n"
                    + "            e.printStackTrace();\n"
                    + "        } finally {\n"
                    + "            ps.close();\n"
                    + "            rs.close();\n"
                    + "            conn.close();\n"
                    + "        }\n"
                    + "        return result;\n"
                    + "    }");

            System.out.println("\n----------------------------------------");
            System.out.println(sbcount.toString());

            StringBuffer sbfindbyid = new StringBuffer();
            sbfindbyid.append("public static " + modelName + " find" + modelName + "(String id) throws SQLException, ClassNotFoundException {\n"
                    + "        Connection conn = null;\n"
                    + "        PreparedStatement ps = null;\n"
                    + "        ResultSet rs = null;\n"
                    + "        " + modelName + " em = null;\n"
                    + "        try {\n"
                    + "            conn = App.getConnection();\n"
                    + "            String query = \"SELECT * FROM " + tablename + " t WHERE t.ENTITY_ID='\" + id + \"'\";\n"
                    + "            ps = conn.prepareStatement(query);\n"
                    + "            rs = ps.executeQuery();\n"
                    + "            em = new " + modelName + "();\n"
                    + "            while (rs.next()) {\n");

            for (Iterator it = m.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                String item = namingConvert1(key);
                sbfindbyid.append("         em.set" + item + "(rs.get" + getJavaDbMap(value) + "(\"" + key + "\"));\n");
            }

            sbfindbyid.append("            }\n"
                    + "        } catch (SQLException e) {\n"
                    + "            e.printStackTrace();\n"
                    + "        } finally {\n"
                    + "            ps.close();\n"
                    + "            rs.close();\n"
                    + "            conn.close();\n"
                    + "        }\n"
                    + "        return em;\n"
                    + "    }");

            System.out.println("\n----------------------------------------");
            System.out.println(sbfindbyid.toString());

            StringBuffer sbedit = new StringBuffer();
            sbedit.append("public static void edit("+modelName+" em) throws Exception {\n"
                    + "        Connection conn = null;\n"
                    + "        PreparedStatement ps = null;\n"
                    + "        int parameterIndex = 0;\n"
                    + "        try {\n"
                    + "            conn = App.getConnection();\n"
                    + "            StringBuffer sql = new StringBuffer(\"\");\n"
                    + "            sql.append(\" UPDATE " + tablename + " SET \");\n");

            // + "            sql.append(\" ENTITY_NAME = '1', UPDATE_DATE = CURRENT_TIMESTAMP, UPDATE_BY = 'SYSTEM' \");\n"
            for (Iterator it = m.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                String item = namingConvert1(key);
                sbedit.append("            sql.append(\" " + key + "=?,\")\n");
            }
            sbedit.setLength(sbedit.length() - 4);
            sbedit.append("\");\n");
            sbedit.append("            sql.append(\" WHERE "+pk+"=?\");\n"
                    + "            String dSql = String.valueOf(sql);\n"
                    + "            ps = conn.prepareStatement(dSql);\n"
                    + "            if (em != null) {\n");

            for (Iterator it = m.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                String key = (String) entry.getKey();//dbcol
                String value = (String) entry.getValue();//dbcol var
                if (pk.equalsIgnoreCase(key)) {
                    continue;
                }
                String item = namingConvert1(key);
                sbedit.append("            ps.set" + getJavaDbMap(value) + "(++parameterIndex,em.get" + item + "());\n");
            }
            sbedit.append("            ps.set" + getJavaDbMap(pk) + "(++parameterIndex,em.get" + namingConvert1(pk) + "());\n");
            sbedit.append("                ps.executeUpdate();\n"
                    + "            }\n"
                    + "        } catch (Exception e) {\n"
                    + "            System.out.println(\"ERROR\" + e);\n"
                    + "        } finally {\n"
                    + "            ps.close();\n"
                    + "            conn.close();\n"
                    + "        }\n"
                    + "    }");

            System.out.println("\n----------------------------------------");
            System.out.println(sbedit.toString());

            StringBuffer sbdestroy = new StringBuffer();
            sbdestroy.append("public static void destroy(String id) throws ClassNotFoundException, SQLException {\n"
                    + "        Connection conn = null;\n"
                    + "        PreparedStatement ps = null;\n"
                    + "        ResultSet rs = null;\n"
                    + "        try {\n"
                    + "            conn = App.getConnection();\n"
                    + "            String query = \"DELETE FROM " + tablename + " t WHERE t."+pk+"='\" + id + \"'\";\n"
                    + "            ps = conn.prepareStatement(query);\n"
                    + "            rs = ps.executeQuery();\n"
                    + "        } catch (SQLException e) {\n"
                    + "            e.printStackTrace();\n"
                    + "        } finally {\n"
                    + "            ps.close();\n"
                    + "            rs.close();\n"
                    + "            conn.close();\n"
                    + "        }\n"
                    + "    }");

            System.out.println("\n----------------------------------------");
            System.out.println(sbdestroy.toString());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rs.close();
            ps.close();
            conn.close();
        }
    }

    public static String namingConvert1(String input) {
        input = input.toLowerCase();
        String output = "";

        if (!input.contains("_")) {
            for (int i = 0; i < input.length(); i++) {
                if (i == 0) {
                    output = output + String.valueOf(input.charAt(i)).toUpperCase();
                } else {
                    output = output + String.valueOf(input.charAt(i));
                }
            }
            return output;
        }

        String[] strArr = input.split("_");

        for (String s : strArr) {
            for (int i = 0; i < s.length(); i++) {
                if (i == 0) {
                    output = output + String.valueOf(s.charAt(i)).toUpperCase();
                } else {
                    output = output + String.valueOf(s.charAt(i));
                }
            }
        }
        return output;
    }

    public static String namingConvert2(String input) {
        input = input.toLowerCase();
        String output = "";

        if (!input.contains("_")) {
            for (int i = 0; i < input.length(); i++) {
                output = output + String.valueOf(input.charAt(i));
            }
            return output;
        }

        String[] strArr = input.split("_");

        for (int cnt = 0; cnt < strArr.length; cnt++) {
            for (int i = 0; i < strArr[cnt].length(); i++) {
                if (cnt == 0) {
                    output = output + String.valueOf(strArr[cnt].charAt(i));
                } else {
                    if (i == 0) {
                        output = output + String.valueOf(strArr[cnt].charAt(i)).toUpperCase();
                    } else {
                        output = output + String.valueOf(strArr[cnt].charAt(i));
                    }
                }

            }
        }
        return output;
    }

    public static String getJavaDbMap(String in) {
        String result = (String) jdbcm.get(in);
        if (result == null) {
            return "String";
        }
        return result;
    }

}

