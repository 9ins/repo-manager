package com.innoquartz.repodowner;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathTest {
    

    public static void main(String[] args) {
        Path p1 = Paths.get("D:\\1.iq-designer\\project\\repository-downloader");
        Path p2 = Paths.get("D:\\1.iq-designer\\designer\\release\\20211122\\IQD_BD-V7.3.1.20211122\\workspace\\LOCAL_PROJECT\\poms\\jobs\\process");
        System.out.println(p2.toString().indexOf("jobs")+"   "+p2.toString().indexOf("process"));
        Path p3 = p2.subpath(7, 8);
        System.out.println(p3);
    }
}
