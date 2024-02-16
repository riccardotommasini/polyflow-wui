package org.streamreasoning.gsp.seraph.data;

import org.streamreasoning.rsp4j.api.stream.data.DataStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;

//TODO create method to set a source from either the query or the example class
public class Source implements Runnable {

    public Source(DataStream<PGraph> stream) {
        this.stream = stream;
    }

    private final DataStream<PGraph> stream;

    @Override
    public void run() {
        try {
            String topic = "sne-avro";
//        AvroConsumer ac = new AvroConsumer(topic);

            int count = 0;

            while (true) {
//            ac.consume();
                try {
                    String fileName = "testGraph" + (count % 5 + 1) + ".json";
                    //Create a property graph using the test.json as a base
                    URL url = Source.class.getClassLoader().getResource(fileName);
                    FileReader fileReader = new FileReader(url.getPath());
                    PGraph pGraph = PGraphImpl.fromJson(fileReader);
                    stream.put(pGraph, pGraph.timestamp());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Thread.sleep(5000);
                count++;
            }
        } catch (
                InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }


}
