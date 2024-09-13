package org.streamreasoning.gsp.services;

import graph.seraph.events.PGraph;
import graph.seraph.events.PGraphImpl;
import graph.seraph.events.PGraphOrResult;
import graph.seraph.events.Result;
import graph.seraph.op.PGraphStreamGenerator;
import graph.seraph.streams.PGStream;
import graph.seraph.syntax.QueryFactory;
import graph.seraph.syntax.SeraphQuery;
import org.springframework.stereotype.Service;
import org.streamreasoning.gsp.views.OldPGS;
import org.streamreasoning.rsp4j.api.coordinators.ContinuousProgram;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;
import shared.coordinators.ContinuousProgramImpl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class SeraphService {

    private final AtomicInteger eventCounter = new AtomicInteger(1);
    private final ContinuousProgram<PGraph, PGraph, PGraphOrResult, Result> cp;
    private final PGraphStreamGenerator generator;
    List<SeraphQuery<PGraph, PGraph, PGraphOrResult, Result>> queries = new ArrayList<>();
    private final Map<String, DataStream<PGraph>> streams = new HashMap<>();


    public SeraphService() {
        this.cp = new ContinuousProgramImpl<>();
        this.generator = new PGraphStreamGenerator();

    }

    public static PGraph fromJson(String fileName) {
        System.out.println(fileName);
        try {
            return PGraphImpl.fromJson(new FileReader(OldPGS.class.getClassLoader().getResource(fileName).getPath()));
        } catch (FileNotFoundException e) {
            return PGraphImpl.createEmpty();
        }
    }

    public static PGraph fromJson(FileReader fileReader) {
        return PGraphImpl.fromJson(fileReader);
    }

    public SeraphQuery<PGraph, PGraph, PGraphOrResult, Result> register(String seraphQL, String stream) {
        SeraphQuery<PGraph, PGraph, PGraphOrResult, Result> q = parse(seraphQL, stream);
        DataStream<PGraph> inputStreamColors = q.instream();
        streams.put(stream, inputStreamColors);
        DataStream<Result> outStream = q.outstream();
//        streams.put(q.id(), outStream);
        queries.add(q);
        cp.buildTask(q.getTask(), Collections.singletonList(inputStreamColors), Collections.singletonList(outStream));
        return q;
    }

    public List<SeraphQuery<PGraph, PGraph, PGraphOrResult, Result>> listQueries() {
        return queries;
    }

    public void unregisterQuery(String s) {
//        seraph.unregisterQuery(s);
    }

    public PGraph nextEvent(String event, String stream) {
        System.out.println(event);
        URL url = SeraphService.class.getClassLoader().getResource(event + ".json");
        try (FileReader fileReader = new FileReader(url.getPath())) {
            PGraph pGraph2 = PGraphImpl.fromJson(fileReader);
            streams.computeIfPresent(stream, (s, pGraphDataStream) -> {
                pGraphDataStream.put(pGraph2, System.currentTimeMillis());
                return pGraphDataStream;
            });
            return pGraph2;
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    public PGraph nextEvent(String event) {
        eventCounter.compareAndSet(10, 1);
        String fileName = "testGraph" + (eventCounter.getAndIncrement());
        return nextEvent(fileName, event);
    }

    public void send(String stream, PGraph e) {
        streams.computeIfPresent(stream, (s, pGraphDataStream) -> {
            pGraphDataStream.put(e, System.currentTimeMillis());
            return pGraphDataStream;
        });
    }

    public PGStream register(PGStream s) {
//        return seraph.register(s);
        return s;
    }


    public SeraphQuery<PGraph, PGraph, PGraphOrResult, Result> parse(String value, String stream) {
        try {
            return QueryFactory.parse(value, stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
