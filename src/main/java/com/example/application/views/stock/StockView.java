package com.example.application.views.stock;

import com.example.application.service.StockService;
import com.example.application.service.StockSymbolUtil;
import com.example.application.service.StockService.StockQuote;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.time.LocalDateTime;
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
    private UI ui;
    private List<String> selectedSymbols = new ArrayList<>();

    public StockView(StockService stockService) {
        this.stockService = stockService;
        
        setSpacing(false);
        setSizeFull();
        setPadding(true);

        H2 title = new H2("Stock Tracker");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        title.getStyle()
            .set("color", "var(--lumo-primary-color)")
            .set("font-size", "var(--lumo-font-size-xxxl)")  // Making the title larger
            .set("font-weight", "600");  // Making it semi-bold for better visibility

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

        // Show all available stocks in the dropdown
        symbolSelector.setItems(StockSymbolUtil.getAllStocks());
        symbolSelector.setWidth("300px");
        symbolSelector.addValueChangeListener(event -> {
            selectedSymbols = event.getValue().stream()
                .map(item -> item.split(" - ")[0])  // Extract symbol from "SYMBOL - Name" format
                .collect(Collectors.toList());
            updateStockInfo();
        });

        stockGrid = new Grid<>();
        stockGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        stockGrid.setSizeFull();
        
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

        updatedSpan = new Span();
        updatedSpan.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-size", "14px");

        add(title, description, symbolSelector, stockGrid, updatedSpan);
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
