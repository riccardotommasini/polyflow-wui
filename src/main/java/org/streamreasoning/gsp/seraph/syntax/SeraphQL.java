package org.streamreasoning.gsp.seraph.syntax;


import org.streamreasoning.gsp.seraph.data.PGStream;
import org.streamreasoning.rsp4j.api.RDFUtils;
import org.streamreasoning.rsp4j.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.rsp4j.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.rsp4j.api.operators.s2r.execution.assigner.StreamToRelationOp;
import org.streamreasoning.rsp4j.api.operators.s2r.syntax.WindowNode;
import org.streamreasoning.rsp4j.api.querying.Aggregation;
import org.streamreasoning.rsp4j.api.querying.ContinuousQuery;
import org.streamreasoning.rsp4j.api.secret.time.Time;
import org.streamreasoning.rsp4j.api.secret.time.TimeFactory;
import org.streamreasoning.rsp4j.api.stream.data.DataStream;
import org.streamreasoning.rsp4j.yasper.querying.operators.windowing.WindowNodeImpl;


import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

//Seraph query
public class SeraphQL implements ContinuousQuery {

    private final List<String> projections;
    private R2R r2r;
    private Map<String, S2R> inputs = new HashMap<>();
    private Map<String, R2S> outputs = new HashMap<>();
    private Map<WindowNode, PGStream> map = new HashMap<>();

    public SeraphQL(R2R r2r, Map<String, S2R> inputs, Map<String, R2S> outputs, List<String> projections) {
        this.r2r = r2r;
        this.inputs = inputs;
        this.outputs = outputs;
        this.projections = projections;
    }

    @Override
    public void addNamedWindow(String s, WindowNode windowNode) {

    }

    public void setInputStream(String uri) {

        S2R s2r = inputs.entrySet().iterator().next().getValue();

        inputs.clear();
        inputs.put(uri, s2r);

    }


    @Override
    public void setIstream() {

    }

    @Override
    public void setRstream() {

    }

    @Override
    public void setDstream() {

    }

    @Override
    public boolean isIstream() {
        return false;
    }

    @Override
    public boolean isRstream() {
        return false;
    }

    @Override
    public boolean isDstream() {
        return false;
    }

    @Override
    public void setSelect() {

    }

    @Override
    public void setConstruct() {

    }

    @Override
    public boolean isSelectType() {
        return false;
    }

    @Override
    public boolean isConstructType() {
        return false;
    }

    @Override
    public void setOutputStream(String uri) {

        R2S r2s = outputs.entrySet().iterator().next().getValue();

        //TODO change map to list data structure because of possible duplicated keys
        outputs.clear();
        outputs.put(uri, r2s);
    }


    @Override
    public DataStream getOutputStream() {
        return null;
    }

    @Override
    public String getID() {
        return null;
    }

    @Override
    public Map<? extends WindowNode, PGStream> getWindowMap() {
        AtomicInteger i = new AtomicInteger();
        outputs.forEach((out, r2S) -> {
            inputs.forEach((k, v) -> {
                i.getAndIncrement();
                PGStream webStream = new PGStream(k);
                WindowNodeImpl windowNode = new WindowNodeImpl(RDFUtils.createIRI(k + "/w" + i), v.range, r2S.period, 0);
                //add windownode and webstream to the map, if the key value pair doesnt already exist
                map.putIfAbsent(windowNode, webStream);
            });
        });
        return map;
    }

    public List<String> getInputStreams() {
        return new ArrayList<>(inputs.keySet());
    }


    @Override
    public Time getTime() {
        return TimeFactory.getInstance();
    }

    @Override
    public RelationToRelationOperator r2r() {
        return null;
    }

    public String getR2R() {
        return r2r.toString();
    }

    @Override
    public StreamToRelationOp[] s2r() {
        return new StreamToRelationOp[0];
    }

    @Override
    public RelationToStreamOperator r2s() {
        return null;
    }

    @Override
    public List<Aggregation> getAggregations() {
        return null;
    }

    public List<String> getResultVars() {
        return projections;
    }

}
