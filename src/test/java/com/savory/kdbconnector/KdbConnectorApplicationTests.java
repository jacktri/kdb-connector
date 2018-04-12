package com.savory.kdbconnector;


import static com.savory.kdbconnector.driver.c.td;

import java.io.File;
import java.sql.Time;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import com.savory.kdbconnector.driver.c;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class KdbConnectorApplicationTests {

    private static String OS = System.getProperty("os.name").toLowerCase();

    private static Process process;

    @Test
    public void contextLoads() throws Exception{

//        if (isWindows()) {
//            System.out.println("This is Windows");
//        } else if (isMac()) {
//            System.out.println("This is Mac");
//        } else if (isUnix()) {
//            System.out.println("This is Unix or Linux");
//        } else {
//            System.out.println("Your OS is not support!!");
//        }
//
//
//        ClassLoader classLoader = getClass().getClassLoader();
//        File file = new File(classLoader.getResource("kdb/q.exe").getFile());
//
//        ProcessBuilder p = new ProcessBuilder();
//        p.command(file.getAbsolutePath(), "-p", "5001");
//        process = p.start();
//
        c kdbServer = new c("localhost",5001);


//        Object[] row= {new Time(System.currentTimeMillis()%86400000), "IBM", new Double(93.5), new Integer(300)};
        // insert the row into the trade table
//        kdbServer.ks("insert","trade", row);
        kdbServer.k("tab");
        // send a sync message (see below for an explanation)
//        kdbServer.k("");
        // execute a query in the server that returns a table
        c.Flip table = td(kdbServer.k("select sum size by sym from trade"));
        kdbServer.close();
    }

    @AfterClass
    public static void destroy(){
        process.destroy();
    }

    public static boolean isWindows() {

        return (OS.indexOf("win") >= 0);

    }

    public static boolean isMac() {

        return (OS.indexOf("mac") >= 0);

    }

    public static boolean isUnix() {

        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );

    }

}
