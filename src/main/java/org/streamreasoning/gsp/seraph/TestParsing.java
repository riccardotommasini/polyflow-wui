package org.streamreasoning.gsp.seraph;

//import org.streamreasoning.rsp4j.yasper.querying.syntax.QueryFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.streamreasoning.gsp.seraph.data.PGStream;
import org.streamreasoning.gsp.seraph.data.PGraph;
import org.streamreasoning.gsp.seraph.engine.QueryFactory;
import org.streamreasoning.gsp.seraph.engine.Seraph;
import org.streamreasoning.gsp.seraph.syntax.SeraphQL;
import org.streamreasoning.rsp4j.api.engine.config.EngineConfiguration;
import org.streamreasoning.rsp4j.api.querying.ContinuousQuery;
import org.streamreasoning.rsp4j.api.querying.ContinuousQueryExecution;


public class TestParsing {

    static Seraph sr;
    public static EngineConfiguration aDefault;

    public static void main(String[] args) throws IOException, ConfigurationException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        //Load engine configuration from yasper/target/classes/csparql.properties
        EngineConfiguration ec = EngineConfiguration.loadConfig("/seraph.properties");

        //Create new seraph engine with the loaded configuration
        Seraph sr = new Seraph(ec);


        // QUERY STUDENT TRICK TEST

        // TIMESTAMPS IN EPOCH MILLIS:
        //     14.10.2022 14:45 Uhr --> 1665758700000
        //     14.10.2022 15:00 Uhr --> 1665759600000
        //     14.10.2022 15:15 Uhr --> 1665760500000
        //     14.10.2022 15:20 Uhr --> 1665760800000
        //     14.10.2022 15:40 Uhr --> 1665762000000

        try {

            SeraphQL studentTrick = (SeraphQL) QueryFactory.parse(
                    "REGISTER QUERY <student_trick> STARTING AT 2022-10-14T14:45 {\n" +
                            "WITH duration({minutes : 5}) as _5m,\n" +
                            "duration({minutes : 20}) as _20m\n" +
                            "MATCH (s:Station)<-[r1:rentedAt]-(b1:Bike),\n" +
                            "(b1)-[n1:returnedAt]->(p:Station),\n" +
                            "(p)<-[r2:rentedAt]-(b2:Bike),\n" +
                            "(b2)-[n2:returnedAt]->(o:Station)\n" +
                            "WITHIN PT1H\n" +
                            "WITH duration({minutes : 5}) as _5m\n" +
                            "MATCH (s:Station)<-[r1:rentedAt]-(b1:Bike)\n" +
                            "WITHIN PT5M\n" +
                            "WHERE r1.user_id = n1.user_id AND\n" +
                            "n1.user_id = r2.user_id AND r2.user_id = n2.user_id AND\n" +
                            "n1.val_time < r2.val_time AND\n" +
                            "duration.between(n1.val_time,r2.val_time) < _5m AND\n" +
                            "duration.between(r1.val_time,n1.val_time) < _20m AND\n" +
                            "duration.between(r2.val_time,n2.val_time) < _20m\n" +
                            "EMIT r1.user_id, s.id, p.id, o.id\n" +
                            "ON ENTERING\n" +
                            "EVERY PT5M\n" +
                            "}"
            );

            sr.register(new PGStream("http://stream1"));
//            studentTrick.setInputStream("http://stream1");
            studentTrick.setOutputStream("http://stream2");

            //register the parsed seraph query as Neo4jContinuousQueryExecution
            ContinuousQueryExecution<PGraph, PGraph, Map<String, Object>, Map<String, Object>> cqe = sr.register(studentTrick);
            System.out.println("-------PARSING SUCCESSFUL-------");


        } catch (RuntimeException e) {
            System.out.println(e);
        }



    }
}

