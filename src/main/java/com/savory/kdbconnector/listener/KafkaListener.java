package com.savory.kdbconnector.listener;

import static com.savory.kdbconnector.driver.c.td;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;


@Component
public class KafkaListener {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    //
    @PostConstruct
    public void listen() throws SQLException, IOException, com.savory.kdbconnector.driver.c.KException {
        PreparedStatement preparedStatement = jdbcTemplate.getDataSource().getConnection().prepareStatement("select from ?");
        preparedStatement.setString(1, "tab");
//        System.out.println("a");
        ResultSet resultSet = preparedStatement.executeQuery();
        System.out.println("a");
    }
//
    private static final ResultSetExtractor<String> extractor =  a -> "";
//        @Override
//        public List<String> extractData(ResultSet rs) throws SQLException {
//
//            List<String> list = new ArrayList<>();
//            while (rs.next()) {
//                list.add("a");
//            }
//            return list;
//        }
//    });

//        c kdbServer = new c("localhost",5001);
//        // create a row (array of objects)
//        Object[] row= {new Time(System.currentTimeMillis()%86400000), "IBM", new Double(93.5), new Integer(300)};
//        // insert the row into the trade table
//        kdbServer.ks("insert","trade", row);
//        // send a sync message (see below for an explanation)
//        kdbServer.k("");
//        // execute a query in the server that returns a table
//        c.Flip table = td(kdbServer.k("select sum size by sym from trade"));
//        // read the data from the Flip object ...
//        // close connection to kdb+ server
//        kdbServer.close();
////        Connection connection = jdbcTemplate.getDataSource().getConnection();
////        System.out.println(connection.getSchema());
//    }
}
