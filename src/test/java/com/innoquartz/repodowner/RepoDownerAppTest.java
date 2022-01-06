package com.innoquartz.repodowner;

import org.chaostocosmos.repo.RepoManagerApp;
import org.junit.Test;

public class RepoDownerAppTest {

    public RepoDownerAppTest() throws Exception {
        new RepoManagerApp("D:\\InnoQuartz\\2. Product\\Projects\\repository-downloader\\messages.yml");
    }

    @Test
    public void execute() throws Exception {
        new RepoManagerApp("D:\\InnoQuartz\\2. Product\\Projects\\repository-downloader\\messages.yml");
    }        

    public static void main(String[] args) throws Exception {
        new RepoManagerApp("D:\\InnoQuartz\\2. Product\\Projects\\repository-downloader\\messages.yml");        
    }
}
