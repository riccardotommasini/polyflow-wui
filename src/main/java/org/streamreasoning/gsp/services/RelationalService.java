package org.streamreasoning.gsp.services;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.f0rce.ace.AceEditor;
import graph.ContinuousQuery;
import graph.seraph.events.Result;
import org.javatuples.Quintet;
import org.javatuples.Tuple;
import org.springframework.stereotype.Service;
import org.streamreasoning.gsp.data.TableDataComponent;
import org.streamreasoning.polyflow.api.enums.Tick;
import org.streamreasoning.polyflow.api.operators.r2r.RelationToRelationOperator;
import org.streamreasoning.polyflow.api.operators.r2s.RelationToStreamOperator;
import org.streamreasoning.polyflow.api.operators.s2r.execution.assigner.StreamToRelationOperator;
import org.streamreasoning.polyflow.api.processing.ContinuousProgram;
import org.streamreasoning.polyflow.api.processing.Task;
import org.streamreasoning.polyflow.api.secret.report.Report;
import org.streamreasoning.polyflow.api.secret.report.ReportImpl;
import org.streamreasoning.polyflow.api.secret.report.strategies.OnWindowClose;
import org.streamreasoning.polyflow.api.secret.time.Time;
import org.streamreasoning.polyflow.api.secret.time.TimeImpl;
import org.streamreasoning.polyflow.api.stream.data.DataStream;
import org.streamreasoning.polyflow.base.contentimpl.factories.AccumulatorContentFactory;
import org.streamreasoning.polyflow.base.operatorsimpl.dag.DAGImpl;
import org.streamreasoning.polyflow.base.operatorsimpl.s2r.HoppingWindowOpImpl;
import org.streamreasoning.polyflow.base.processing.ContinuousProgramImpl;
import org.streamreasoning.polyflow.base.processing.TaskImpl;
import org.vaadin.addons.visjs.network.main.NetworkDiagram;
import relational.operatorsimpl.r2r.CustomRelationalQuery;
import relational.operatorsimpl.r2r.R2RjtablesawJoin;
import relational.operatorsimpl.r2r.R2RjtablesawSelection;
import relational.operatorsimpl.r2s.RelationToStreamjtablesawImpl;
import relational.sds.SDSjtablesaw;
import relational.stream.RowStream;
import tech.tablesaw.api.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;


@Service
public class RelationalService extends QueryService<Tuple, Tuple, Table, Tuple> {

    public RelationalService() throws FileNotFoundException {
        super(new ContinuousProgramImpl<>());
    }

    private static Scanner s1;

    public static Grid<Tuple> loadEvent(String filename) {
        System.out.println(filename);
        try {
            if (s1 == null || !s1.hasNext()) {
                File f1 = new File(RelationalService.class.getClassLoader().getResource(filename + ".csv").getPath());
                s1 = new Scanner(f1);
            }
            return loadEvent(fromFile(s1));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Grid<Tuple> loadEvent(Tuple tuple) {
        Grid<Tuple> eventGrid = new Grid<>();

        for (int i = 0; i < tuple.toList().size(); i++) {
            int finalI = i;
            eventGrid.addColumn(t -> t.getValue(finalI)).setHeader(String.valueOf(i));
        }

        ListDataProvider<Tuple> resultDataProvider = new MyDataProvider<>(List.of(tuple));
        eventGrid.setDataProvider(resultDataProvider);
        resultDataProvider.refreshAll();
        return eventGrid;
    }

    private String register(String seraphQL, String stream) {
        // define output stream
        String q1 = "Q1";
        DataStream<Tuple> outStream = new RowStream(q1);
        RowStream inStream = new RowStream(stream);

        // Engine properties
        Report report = new ReportImpl();
        report.add(new OnWindowClose());

        Tick tick = Tick.TIME_DRIVEN;
        Time instance = new TimeImpl(0);
        Table emptyContent = Table.create();

        AccumulatorContentFactory<Tuple, Tuple, Table> accumulatorContentFactory = new AccumulatorContentFactory<>(
                t -> t,
                (t) -> {
                    Table r = Table.create();

                    for (int i = 0; i < t.getSize(); i++) {
                        if (t.getValue(i) instanceof Long) {
                            String columnName = "c" + (i + 1);
                            if (!r.containsColumn(columnName)) {
                                LongColumn lc = LongColumn.create(columnName);
                                lc.append((Long) t.getValue(i));
                                r.addColumns(lc);
                            } else {
                                LongColumn lc = (LongColumn) r.column(columnName);
                                lc.append((Long) t.getValue(i));
                            }

                        } else if (t.getValue(i) instanceof Integer) {
                            String columnName = "c" + (i + 1);
                            if (!r.containsColumn(columnName)) {
                                IntColumn lc = IntColumn.create(columnName);
                                lc.append((Integer) t.getValue(i));
                                r.addColumns(lc);
                            } else {
                                IntColumn lc = (IntColumn) r.column(columnName);
                                lc.append((Integer) t.getValue(i));
                            }
                        } else if (t.getValue(i) instanceof Boolean) {
                            String columnName = "c" + (i + 1);
                            if (!r.containsColumn(columnName)) {
                                BooleanColumn lc = BooleanColumn.create(columnName);
                                lc.append((Boolean) t.getValue(i));
                                r.addColumns(lc);
                            } else {
                                BooleanColumn lc = (BooleanColumn) r.column(columnName);
                                lc.append((Boolean) t.getValue(i));
                            }
                        } else if (t.getValue(i) instanceof String) {
                            String columnName = "c" + (i + 1);
                            if (!r.containsColumn(columnName)) {
                                StringColumn lc = StringColumn.create(columnName);
                                lc.append((String) t.getValue(i));
                                r.addColumns(lc);
                            } else {
                                StringColumn lc = (StringColumn) r.column(columnName);
                                lc.append((String) t.getValue(i));
                            }
                        }
                    }
                    return r;
                },
                (r1, r2) -> r1.isEmpty() ? r2 : r1.append(r2),
                emptyContent

        );

        ContinuousProgram<Tuple, Tuple, Table, Tuple> cp = new ContinuousProgramImpl<>();

        StreamToRelationOperator<Tuple, Tuple, Table> s2rOp_1 =
                new HoppingWindowOpImpl<>(
                        tick,
                        instance,
                        "w1",
                        accumulatorContentFactory,
                        report,
                        1000,
                        1000);

        StreamToRelationOperator<Tuple, Tuple, Table> s2rOp_2 =
                new HoppingWindowOpImpl<>(
                        tick,
                        instance,
                        "w2",
                        accumulatorContentFactory,
                        report,
                        1000,
                        1000);


        CustomRelationalQuery selection = new CustomRelationalQuery(4, "c3");
        CustomRelationalQuery join = new CustomRelationalQuery("c1");

        RelationToRelationOperator<Table> r2rOp = new R2RjtablesawSelection(selection, Collections.singletonList(s2rOp_1.getName()), "partial_1");
        RelationToRelationOperator<Table> r2rBinaryOp = new R2RjtablesawJoin(join, Arrays.asList(s2rOp_2.getName(), "partial_1"), "partial_2");

        RelationToStreamOperator<Table, Tuple> r2sOp = new RelationToStreamjtablesawImpl();

        Task<Tuple, Tuple, Table, Tuple> task = new TaskImpl<>();
        task = task
                .addS2ROperator(s2rOp_1, inStream)
                .addS2ROperator(s2rOp_2, inStream)
                .addR2ROperator(r2rOp)
                .addR2ROperator(r2rBinaryOp)
                .addR2SOperator(r2sOp)
                .addSDS(new SDSjtablesaw())
                .addDAG(new DAGImpl<>())
                .addTime(instance);
        task.initialize();


        List<DataStream<Tuple>> inputStreams = new ArrayList<>();
        inputStreams.add(inStream);
        inputStreams.add(inStream);

        ContinuousQuery<Tuple, Tuple, Table, Tuple> value = new ContinuousQuery<>(q1, task, List.of("c3"), outStream, inputStreams);
        queries.put(q1, value);

        List<DataStream<Tuple>> outputStreams = new ArrayList<>();
        outputStreams.add(outStream);

        cp.buildTask(task, inputStreams, outputStreams);

        return value.id();
    }

    @Override
    public Grid<Tuple> sendEvent(String event, String stream) {
        Tuple pGraph = nextEvent(event, stream);
        return loadEvent(pGraph);
    }

    @Override
    public void registerNewQuery(String inputStream, DataComponent snapshotGraphFunction, DataComponent snapshotGraphSolo, HorizontalLayout tvttab, TimePicker timePicker1, TabSheet processingTabSheet, AceEditor editor, VerticalLayout outputRowContainer) {
        //todo if processingTabSheet contains already, ti should not recreate it.
        Grid<Tuple> lastTAT;

        Component component = processingTabSheet.getComponent(lastTATTab);
        if (component == null) {
            processingTabSheet.add(lastTATTab, lastTAT = new Grid<>(Tuple.class));
            lastTAT.setId("lastTAT");
            lastTAT.setWidth("100%");
            lastTAT.setHeight("100%");
            lastTAT.setPageSize(10);
            lastTAT.getStyle().setFontSize("1em");
        } else
            lastTAT = (Grid<Tuple>) component;

        Grid<Tuple> nowgrid;

        tvttab.replace(tvttab.getChildren().toList().get(2), nowgrid = new Grid<>(Tuple.class));

        nowgrid.setId("nowgrid");
        nowgrid.setWidth("100%");
        nowgrid.setHeight("100%");
        nowgrid.setPageSize(10);
        nowgrid.getStyle().setFontSize("12px");

        String cqe = register(editor.getValue(), inputStream);

        HorizontalLayout outputRow = new HorizontalLayout();
        String id = cqe; //TODO nel task
        outputRow.setId(id);
        outputRow.addClassName(LumoUtility.Gap.MEDIUM);
        outputRow.setWidth("100%");
        outputRow.setHeight("200px");

        outputRow.add(new H4(id));

        outputRowContainer.add(outputRow);

        List<Tuple> res = new ArrayList<>();
        ListDataProvider<Tuple> resultDataProvider = new MyDataProvider<>(res);

        lastTAT.setDataProvider(resultDataProvider);
        nowgrid.setDataProvider(resultDataProvider);

//        addConsumer(id, lastTAT, nowgrid, res, resultDataProvider, timePicker1, outputRow, snapshotGraphFunction, snapshotGraphSolo, nodes, edges);

        queries.get(id).outstream().addConsumer((out, result, ts) -> {
//            result.put("Id", idCounter.getAndIncrement());

            lastTAT.getColumnByKey("empty").setVisible(false);
            nowgrid.getColumnByKey("empty").setVisible(false);

            res.add(result);

            resultDataProvider.refreshAll();

            //TODO add focus based on selected query
            appendResultTable(outputRow, result, ts, res);

            updateSnapshotGraphFromContent((NetworkDiagram) snapshotGraphFunction, cqe);
            updateSnapshotGraphFromContent((NetworkDiagram) snapshotGraphSolo, cqe);
            timePicker1.setValue(LocalTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault()));
            System.out.println(result);
        });

        Notification.show("Query " + cqe + " Was successfully registered");
    }

    private Tuple nextEvent(String event, String stream) {
        try {
            if (s1 == null || !s1.hasNext()) {
                File f1 = new File(RelationalService.class.getClassLoader().getResource(event + ".csv").getPath());
                s1 = new Scanner(f1);
            }
            return fromFile(s1);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public void updateSnapshotGraphFromContent(NetworkDiagram snapshotGraph, String id) {

    }

    private void appendResultTable(HorizontalLayout outputRow, Tuple arg, long ts, List<Tuple> res) {
        outputRow.getChildren()
                .filter(component -> !(component instanceof H4))
                .map(obj -> (Grid) obj)
                .filter(grid -> grid.getId().filter(id -> id.equals(ts + ""))
                        .isPresent())
                .findFirst().or(() -> {
                    Grid<Result> g = new Grid(Result.class);
                    List<Result> items = new ArrayList<>();
                    MyDataProvider<Result> mapDP = new MyDataProvider<>(items);
                    g.setDataProvider(mapDP);
                    g.setId(ts + "");

                    //This part depends on query output
                    arg.toList().forEach(k -> g.addColumn(map -> map.get(k)));

                    res.clear();

                    g.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
                    g.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
                    g.setWidth("100%");
                    g.setHeight("100%");
                    g.setPageSize(10);
                    outputRow.add(g);
                    return Optional.of(g);
                }).ifPresent(g -> ((ListDataProvider) g.getDataProvider()).getItems().add(arg));
    }


    private static Tuple fromFile(Scanner s1) throws FileNotFoundException {
        Tuple row;
        String[] items;
        String s = s1.nextLine();
        System.out.println(s);
        items = s.split(",");
        row = new Quintet<>(Long.parseLong(items[0]), items[1], Integer.parseInt(items[2]), Boolean.parseBoolean(items[3]), System.currentTimeMillis());
        return row;
    }


}
