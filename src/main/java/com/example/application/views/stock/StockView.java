package com.example.application.views.stock;

import com.example.application.service.StockService;
import com.example.application.service.StockSymbolUtil;
import com.example.application.service.StockService.StockQuote;
import com.example.application.service.StockService.HistoricalDataPoint;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@PageTitle("Stock Tracker")
@Route("")
@Menu(order = 0, icon = LineAwesomeIconUrl.CHART_LINE_SOLID)
public class StockView extends VerticalLayout {

    private final StockService stockService;
    private final MultiSelectComboBox<String> symbolSelector;
    private final Grid<StockQuote> stockGrid;
    private final Span updatedSpan;
    private final Chart chart;
    private final Select<String> timeRangeSelect;
    private UI ui;
    private List<String> selectedSymbols = new ArrayList<>();
    private String selectedSymbol = "";

    public StockView(StockService stockService) {
        this.stockService = stockService;
        
        setSpacing(false);
        setSizeFull();
        setPadding(true);

        H2 title = new H2("Stock Tracker");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        title.getStyle()
            .set("color", "var(--lumo-primary-color)")
            .set("font-size", "var(--lumo-font-size-xxxl)")
            .set("font-weight", "600");

        Span description = new Span("Enter company name or stock symbol (e.g. 'Tesla' or 'TSLA')");
        description.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        symbolSelector = new MultiSelectComboBox<>("Enter Stocks");
        symbolSelector.setAllowCustomValue(true);
        symbolSelector.setHelperText("Type company name or symbol and press Enter");
        symbolSelector.addCustomValueSetListener(event -> {
            String customValue = event.getDetail();
            String symbol = StockSymbolUtil.getSymbol(customValue);
            Set<String> currentValues = new HashSet<>(symbolSelector.getValue());
            String formattedValue = symbol + " - " + customValue;
            currentValues.add(formattedValue);
            symbolSelector.setValue(currentValues);
        });

        symbolSelector.setItems(StockSymbolUtil.getAllStocks());
        symbolSelector.setWidth("300px");
        symbolSelector.addValueChangeListener(event -> {
            selectedSymbols = event.getValue().stream()
                .map(item -> item.split(" - ")[0])
                .collect(Collectors.toList());
            updateStockInfo();
            if (!selectedSymbols.isEmpty() && (selectedSymbol.isEmpty() || !selectedSymbols.contains(selectedSymbol))) {
                selectedSymbol = selectedSymbols.get(0);
                updateChart();
            }
        });

        stockGrid = new Grid<>();
        stockGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        stockGrid.setHeight("200px");
        stockGrid.setMinWidth("600px");
        
        stockGrid.addColumn(StockQuote::getSymbol)
            .setHeader("Symbol")
            .setWidth("100px")
            .setFlexGrow(0);
        
        stockGrid.addColumn(quote -> "$" + quote.getPrice())
            .setHeader("Price")
            .setWidth("100px")
            .setFlexGrow(0);
        
        stockGrid.addComponentColumn(quote -> {
            Span span = new Span(quote.getChange());
            boolean isNegative = Double.parseDouble(quote.getChange()) < 0;
            span.getElement().getStyle().set("color", 
                isNegative ? "var(--lumo-error-color)" : "var(--lumo-success-color)");
            return span;
        })
        .setHeader("Change")
        .setWidth("100px")
        .setFlexGrow(0);
        
        stockGrid.addComponentColumn(quote -> {
            Span span = new Span(quote.getChangePercent());
            String changePercent = quote.getChangePercent().replace("%", "");
            boolean isNegative = Double.parseDouble(changePercent) < 0;
            span.getElement().getStyle().set("color", 
                isNegative ? "var(--lumo-error-color)" : "var(--lumo-success-color)");
            return span;
        })
        .setHeader("Change %")
        .setWidth("100px")
        .setFlexGrow(0);

        stockGrid.addItemClickListener(event -> {
            selectedSymbol = event.getItem().getSymbol();
            updateChart();
        });

        // Create time range selector
        timeRangeSelect = new Select<>();
        timeRangeSelect.setItems("1D", "1W", "1M", "1Y", "2Y", "5Y", "10Y", "MAX");
        timeRangeSelect.setValue("1M");
        timeRangeSelect.setWidth("100px");
        timeRangeSelect.addValueChangeListener(event -> updateChart());

        // Initialize the chart
        chart = new Chart();
        Configuration conf = chart.getConfiguration();
        conf.getChart().setType(ChartType.LINE);
        conf.getChart().setZoomType(Dimension.X);
        
        YAxis yAxis = new YAxis();
        yAxis.setTitle(new AxisTitle("Price ($)"));
        conf.addyAxis(yAxis);
        
        XAxis xAxis = new XAxis();
        xAxis.setType(AxisType.DATETIME);
        conf.addxAxis(xAxis);
        
        chart.setWidth("100%");
        chart.setHeight("400px");

        updatedSpan = new Span();
        updatedSpan.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-size", "14px");

        // Create layouts
        HorizontalLayout mainContent = new HorizontalLayout();
        VerticalLayout leftSide = new VerticalLayout();
        VerticalLayout rightSide = new VerticalLayout();

        leftSide.add(symbolSelector, stockGrid, updatedSpan);
        leftSide.setSpacing(false);
        leftSide.setPadding(false);

        rightSide.add(timeRangeSelect, chart);
        rightSide.setSpacing(false);
        rightSide.setPadding(false);

        mainContent.add(leftSide, rightSide);
        mainContent.setSizeFull();
        mainContent.setSpacing(true);
        mainContent.setPadding(false);

        add(title, description, mainContent);
    }

    private void updateStockInfo() {
        if (!selectedSymbols.isEmpty()) {
            List<StockQuote> quotes = stockService.getStockQuotes(selectedSymbols);
            stockGrid.setItems(quotes);
            
            updatedSpan.setText("Last updated: " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss")));
        } else {
            stockGrid.setItems(new ArrayList<>());
            updatedSpan.setText("");
        }
    }

    private void updateChart() {
        if (selectedSymbol.isEmpty()) return;

        long to = Instant.now().getEpochSecond();
        long from = calculateFromTimestamp(timeRangeSelect.getValue());

        List<HistoricalDataPoint> historicalData = stockService.getHistoricalData(
            selectedSymbol,
            getResolution(timeRangeSelect.getValue()),
            from,
            to
        );

        Configuration conf = chart.getConfiguration();
        conf.setTitle(selectedSymbol + " Stock Price");
        conf.getSeries().clear();

        DataSeries series = new DataSeries();
        series.setName(selectedSymbol);

        for (HistoricalDataPoint point : historicalData) {
            series.add(new DataSeriesItem(point.getTimestamp() * 1000L, point.getClose()));
        }

        conf.addSeries(series);
        chart.drawChart(true);
    }

    private long calculateFromTimestamp(String timeRange) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = switch (timeRange) {
            case "1D" -> now.minusDays(1);
            case "1W" -> now.minusWeeks(1);
            case "1M" -> now.minusMonths(1);
            case "1Y" -> now.minusYears(1);
            case "2Y" -> now.minusYears(2);
            case "5Y" -> now.minusYears(5);
            case "10Y" -> now.minusYears(10);
            case "MAX" -> now.minusYears(30); // Maximum data available
            default -> now.minusMonths(1);
        };
        return from.toInstant(ZoneOffset.UTC).getEpochSecond();
    }

    private String getResolution(String timeRange) {
        return switch (timeRange) {
            case "1D" -> "5";      // 5 minutes
            case "1W" -> "60";     // 1 hour
            case "1M" -> "D";      // 1 day
            case "1Y" -> "D";      // 1 day
            case "2Y" -> "W";      // 1 week
            case "5Y" -> "W";      // 1 week
            case "10Y" -> "M";     // 1 month
            case "MAX" -> "M";     // 1 month
            default -> "D";
        };
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        ui = attachEvent.getUI();
        ui.setPollInterval(60000); // Poll every 60 seconds
        ui.addPollListener(event -> updateStockInfo());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (ui != null) {
            ui.setPollInterval(-1); // Disable polling
            ui = null;
        }
    }
}
