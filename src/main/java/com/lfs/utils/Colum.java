package com.lfs.utils;

import lombok.Data;

@Data
public class Colum {

    private String column_name;

    private String column_type;

    private String column_comment;

    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir")+"/src/main/java"+"/com/lfs/utils");
    }
}
