package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.entities.Evenement;
import org.example.entities.EvenementMondialDTO;
import org.example.entities.Organisme;
import org.example.Services.EvenementService;
import org.example.Services.OrganismeService;
import org.example.Services.RecommendationService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EvenementController {

    // ── Table ─────────────────────────────────────────────────────────
    @FXML private TableView<Evenement>           table;
    @FXML private TableColumn<Evenement,Integer> colId;
    @FXML private TableColumn<Evenement,String>  colTitre;
    @FXML private TableColumn<Evenement,String>  colLieu;
    @FXML private TableColumn<Evenement,String>  colMode;
    @FXML private TableColumn<Evenement,String>  colCapacite;
    @FXML private TableColumn<Evenement,String>  colDateDebut;
    @FXML private TableColumn<Evenement,String>  colDateFin;
    @FXML private TableColumn<Evenement,String>  colPublicCible;
    @FXML private TableColumn<Evenement,String>  colOrganisme;
    @FXML private TableColumn<Evenement,String>  colEmail;
    @FXML private TableColumn<Evenement,String>  colTelephone;
    @FXML private TableColumn<Evenement,Void>    colActions;

    // ── Toolbar ───────────────────────────────────────────────────────
    @FXML private TextField searchField;

    // ── Edit Dialog ───────────────────────────────────────────────────
    @FXML private StackPane        dialogOverlay;
    @FXML private Label            dialogTitle;
    @FXML private TextField        fTitre;
    @FXML private TextField        fLieu;
    @FXML private ComboBox<String> fMode;
    @FXML private TextField        fCapaciteMax;
    @FXML private TextField        fDateDebut;
    @FXML private TextField        fDateFin;
    @FXML private TextField        fPublicCible;
    @FXML private ComboBox<String> fOrganisme;
    @FXML private TextField        fContactEmail;
    @FXML private TextField        fContactTelephone;
    @FXML private TextField        fLienInscription;
    @FXML private TextArea         fDescription;

    // ── State ─────────────────────────────────────────────────────────
    private final EvenementService       service               = new EvenementService();
    private final OrganismeService       orgService            = new OrganismeService();
    private final RecommendationService  recommendationService = new RecommendationService();
    private final ObservableList<Evenement> data = FXCollections.observableArrayList();
    private Evenement editingEvenement = null;
    private DashboardController dashboardController;

    // ── Drag & Drop state ─────────────────────────────────────────────
    private static final DataFormat EVENEMENT_FORMAT = new DataFormat("application/x-evenement-id");
    private Evenement draggedEvenement = null;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void setDashboardController(DashboardController dc) {
        this.dashboardController = dc;
    }

    // ══════════════════════════════════════════════════════════════════
    //  INITIALIZE
    // ══════════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupColumns();
        setupComboBoxes();
        loadData();
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colMode.setCellValueFactory(new PropertyValueFactory<>("mode"));
        colCapacite.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getCapaciteMax())));
        colDateDebut.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDateDebut() != null
                        ? c.getValue().getDateDebut().format(FMT) : "-"));
        colDateFin.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDateFin() != null
                        ? c.getValue().getDateFin().format(FMT) : "-"));
        colPublicCible.setCellValueFactory(new PropertyValueFactory<>("publicCible"));
        colOrganisme.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getOrganisme() != null
                        ? c.getValue().getOrganisme().getNom() : "-"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("contactTelephone"));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit   = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final Button btnShare  = new Button("🌐 Partager");
            private final HBox   box       = new HBox(8, btnEdit, btnDelete, btnShare);
            {
                btnEdit.setStyle(
                        "-fx-background-color:#f0ad4e; -fx-text-fill:white; -fx-cursor:hand;" +
                                "-fx-background-radius:4; -fx-padding:6 12; -fx-font-size:12px; -fx-font-weight:bold;");
                btnDelete.setStyle(
                        "-fx-background-color:#d9534f; -fx-text-fill:white;" +
                                "-fx-cursor:hand; -fx-background-radius:4; -fx-padding:6 12; -fx-font-size:12px; -fx-font-weight:bold;");
                btnShare.setStyle(
                        "-fx-background-color:#2c3e50; -fx-text-fill:white; -fx-cursor:hand;" +
                                "-fx-background-radius:4; -fx-padding:6 12; -fx-font-size:12px; -fx-font-weight:bold;");
                btnEdit.setOnAction(e -> openEditDialog(
                        getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> confirmDelete(
                        getTableView().getItems().get(getIndex())));
                btnShare.setOnAction(e -> showShareDialog(
                        getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        // ✅ Double-clic sur une ligne → ouvre le calendrier avec drag & drop
        table.setRowFactory(tv -> {
            TableRow<Evenement> tableRow = new TableRow<>();
            tableRow.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !tableRow.isEmpty()) {
                    showCalendarForEvent(tableRow.getItem());
                }
            });
            return tableRow;
        });

        table.setItems(data);
    }

    private void setupComboBoxes() {
        fMode.setItems(FXCollections.observableArrayList(
                "Présentiel", "En ligne", "Hybride"));
        try {
            ObservableList<String> orgNames = FXCollections.observableArrayList();
            orgNames.add("-- Aucun --");
            orgService.findAll().forEach(o -> orgNames.add(o.getId() + " - " + o.getNom()));
            fOrganisme.setItems(orgNames);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOAD / SEARCH / SORT
    // ══════════════════════════════════════════════════════════════════
    private void loadData() {
        try {
            data.setAll(service.findAll());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur chargement", e.getMessage());
        }
    }

    @FXML
    private void onSearch() {
        String kw = searchField.getText().trim();
        try {
            data.setAll(kw.isEmpty() ? service.findAll() : service.search(kw));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur recherche", e.getMessage());
        }
    }

    @FXML
    private void sortAsc()  { sortBy(true); }
    @FXML
    private void sortDesc() { sortBy(false); }

    private void sortBy(boolean asc) {
        try {
            data.setAll(service.sortByCapacite(asc));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur tri", e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  📅 CALENDAR VIEW (bouton toolbar)
    // ══════════════════════════════════════════════════════════════════
    @FXML
    private void showCalendarView() {
        Stage calendarStage = new Stage();
        calendarStage.setTitle("📅 Calendrier des Événements");
        calendarStage.initModality(Modality.APPLICATION_MODAL);
        calendarStage.setWidth(900);
        calendarStage.setHeight(700);

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:#f4f6f9;");

        HBox header = createCalendarHeader();
        root.getChildren().add(header);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        GridPane calendarGrid = createCalendarGrid();
        scrollPane.setContent(calendarGrid);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        root.getChildren().add(scrollPane);

        Scene scene = new Scene(root);
        calendarStage.setScene(scene);
        calendarStage.show();
    }

    private HBox createCalendarHeader() {
        YearMonth currentMonth = YearMonth.now();
        Label titleLabel = new Label(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#1f4f65"));

        Button prevBtn  = new Button("← Précédent");
        Button nextBtn  = new Button("Suivant →");
        Button todayBtn = new Button("Aujourd'hui");

        prevBtn.setStyle("-fx-padding:8 16; -fx-background-color:#e8f4fd; -fx-cursor:hand;");
        nextBtn.setStyle("-fx-padding:8 16; -fx-background-color:#e8f4fd; -fx-cursor:hand;");
        todayBtn.setStyle("-fx-padding:8 16; -fx-background-color:#1f4f65; -fx-text-fill:white; -fx-cursor:hand;");

        HBox header = new HBox(15, prevBtn, titleLabel, nextBtn, todayBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color:white; -fx-border-color:#e0e0e0; -fx-border-width:0 0 1 0;");

        return header;
    }

    private GridPane createCalendarGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(15));
        grid.setStyle("-fx-background-color:white;");

        try {
            List<Evenement> allEvents = service.findAll();

            String[] daysOfWeek = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
            for (int i = 0; i < 7; i++) {
                Label dayLabel = new Label(daysOfWeek[i]);
                dayLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
                dayLabel.setTextFill(Color.web("#64748b"));
                dayLabel.setPadding(new Insets(10));
                dayLabel.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-width:0 0 1 0;");
                dayLabel.setMaxWidth(Double.MAX_VALUE);
                dayLabel.setAlignment(Pos.CENTER);
                grid.add(dayLabel, i, 0);
            }

            LocalDate today = LocalDate.now();
            YearMonth currentMonth = YearMonth.now();
            LocalDate firstDay = currentMonth.atDay(1);
            LocalDate lastDay  = currentMonth.atEndOfMonth();
            int dayOfWeekOffset = firstDay.getDayOfWeek().getValue() % 7;

            int row = 1;
            int col = dayOfWeekOffset;

            for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
                VBox dayCell = createDayCell(date, allEvents, today);
                grid.add(dayCell, col, row);
                col++;
                if (col > 6) { col = 0; row++; }
            }

            for (int i = 0; i < 7; i++) {
                ColumnConstraints cc = new ColumnConstraints(120);
                cc.setHgrow(Priority.ALWAYS);
                grid.getColumnConstraints().add(cc);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le calendrier: " + e.getMessage());
        }

        return grid;
    }

    private VBox createDayCell(LocalDate date, List<Evenement> allEvents, LocalDate today) {
        VBox cellBox = new VBox(5);
        cellBox.setMinHeight(100);
        cellBox.setPadding(new Insets(8));
        cellBox.setStyle("-fx-border-color:#e2e8f0; -fx-border-width:1;");
        cellBox.setAlignment(Pos.TOP_LEFT);

        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        if (date.equals(today)) {
            dayLabel.setStyle("-fx-text-fill:#1f7a5e; -fx-background-color:#d1fae5; -fx-padding:4 8; -fx-border-radius:4;");
        } else {
            dayLabel.setStyle("-fx-text-fill:#4a5568;");
        }
        cellBox.getChildren().add(dayLabel);

        List<Evenement> dayEvents = allEvents.stream()
                .filter(e -> e.getDateDebut() != null && e.getDateDebut().toLocalDate().equals(date))
                .collect(Collectors.toList());

        for (Evenement event : dayEvents.stream().limit(3).collect(Collectors.toList())) {
            Label eventLabel = new Label(event.getTitre());
            eventLabel.setFont(Font.font("System", 11));
            eventLabel.setTextFill(Color.WHITE);
            eventLabel.setWrapText(true);
            eventLabel.setStyle("-fx-background-color:#1f4f65; -fx-padding:4 6; -fx-border-radius:3; -fx-cursor:hand;");
            eventLabel.setMaxWidth(Double.MAX_VALUE);
            eventLabel.setOnMouseClicked(e -> showEventDetailsModal(event));
            cellBox.getChildren().add(eventLabel);
        }

        if (dayEvents.size() > 3) {
            Label moreLabel = new Label("+" + (dayEvents.size() - 3) + " plus");
            moreLabel.setFont(Font.font("System", 10));
            moreLabel.setStyle("-fx-text-fill:#3b82f6; -fx-cursor:hand;");
            cellBox.getChildren().add(moreLabel);
        }

        return cellBox;
    }

    // ══════════════════════════════════════════════════════════════════
    //  📅 CALENDAR FOR SPECIFIC EVENT — avec DRAG & DROP
    // ══════════════════════════════════════════════════════════════════
    private void showCalendarForEvent(Evenement evenement) {
        Stage calendarStage = new Stage();
        calendarStage.setTitle("📅 Calendrier — " + evenement.getTitre());
        calendarStage.initModality(Modality.APPLICATION_MODAL);
        calendarStage.setWidth(950);
        calendarStage.setHeight(720);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:#f4f6f9;");

        // ── Highlight banner ──
        Label highlightLabel = new Label("📌 Événement sélectionné : " + evenement.getTitre()
                + "   •   Glissez-déposez vers un autre jour pour changer la date");
        highlightLabel.setStyle("-fx-background-color:#1f4f65; -fx-text-fill:white; " +
                "-fx-padding:10 20; -fx-font-weight:bold; -fx-font-size:13px;");
        highlightLabel.setMaxWidth(Double.MAX_VALUE);

        // ── Mois courant ──
        YearMonth[] currentMonth = {
                evenement.getDateDebut() != null
                        ? YearMonth.from(evenement.getDateDebut())
                        : YearMonth.now()
        };

        Label titleLabel = new Label();
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#1f4f65"));

        Button prevBtn  = new Button("← Précédent");
        Button nextBtn  = new Button("Suivant →");
        Button closeBtn = new Button("✖ Fermer");

        prevBtn.setStyle("-fx-padding:8 16; -fx-background-color:#e8f4fd; -fx-cursor:hand;");
        nextBtn.setStyle("-fx-padding:8 16; -fx-background-color:#e8f4fd; -fx-cursor:hand;");
        closeBtn.setStyle("-fx-padding:8 16; -fx-background-color:#d9534f; -fx-text-fill:white; " +
                "-fx-cursor:hand; -fx-font-weight:bold;");
        closeBtn.setOnAction(e -> calendarStage.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(15, prevBtn, titleLabel, nextBtn, spacer, closeBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 20, 12, 20));
        header.setStyle("-fx-background-color:white; -fx-border-color:#e0e0e0; -fx-border-width:0 0 1 0;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        root.getChildren().addAll(highlightLabel, header, scrollPane);

        // ── Conteneur mutable pour l'événement (peut être déplacé) ──
        final Evenement[] currentEvenement = {evenement};

        // ── Render ──
        Runnable[] renderRef = new Runnable[1];

        renderRef[0] = () -> {
            titleLabel.setText(currentMonth[0].format(
                    DateTimeFormatter.ofPattern("MMMM yyyy")));
            try {
                List<Evenement> allEvents = service.findAll();
                LocalDate today    = LocalDate.now();
                LocalDate firstDay = currentMonth[0].atDay(1);
                LocalDate lastDay  = currentMonth[0].atEndOfMonth();

                GridPane grid = new GridPane();
                grid.setHgap(5);
                grid.setVgap(5);
                grid.setPadding(new Insets(15));
                grid.setStyle("-fx-background-color:white;");

                String[] days = {"Lun","Mar","Mer","Jeu","Ven","Sam","Dim"};
                for (int i = 0; i < 7; i++) {
                    Label d = new Label(days[i]);
                    d.setFont(Font.font("System", FontWeight.BOLD, 13));
                    d.setTextFill(Color.web("#64748b"));
                    d.setPadding(new Insets(10));
                    d.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-width:0 0 1 0;");
                    d.setMaxWidth(Double.MAX_VALUE);
                    d.setAlignment(Pos.CENTER);
                    grid.add(d, i, 0);
                    ColumnConstraints cc = new ColumnConstraints(120);
                    cc.setHgrow(Priority.ALWAYS);
                    grid.getColumnConstraints().add(cc);
                }

                int col = firstDay.getDayOfWeek().getValue() % 7;
                int row = 1;

                for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
                    final LocalDate cellDate = date;

                    VBox cell = new VBox(5);
                    cell.setMinHeight(100);
                    cell.setPadding(new Insets(8));
                    cell.setAlignment(Pos.TOP_LEFT);

                    boolean isEventDay = currentEvenement[0].getDateDebut() != null &&
                            currentEvenement[0].getDateDebut().toLocalDate().equals(cellDate);

                    cell.setStyle(isEventDay
                            ? "-fx-border-color:#1f4f65; -fx-border-width:2; -fx-background-color:#e8f4fd;"
                            : "-fx-border-color:#e2e8f0; -fx-border-width:1;");

                    Label dayNum = new Label(String.valueOf(cellDate.getDayOfMonth()));
                    dayNum.setFont(Font.font("System", FontWeight.BOLD, 14));
                    dayNum.setStyle(cellDate.equals(today)
                            ? "-fx-text-fill:#1f7a5e; -fx-background-color:#d1fae5; -fx-padding:4 8;"
                            : "-fx-text-fill:#4a5568;");
                    cell.getChildren().add(dayNum);

                    // ── Événements du jour ──
                    List<Evenement> dayEvents = allEvents.stream()
                            .filter(e -> e.getDateDebut() != null &&
                                    e.getDateDebut().toLocalDate().equals(cellDate))
                            .collect(Collectors.toList());

                    for (Evenement ev : dayEvents.stream().limit(3).collect(Collectors.toList())) {
                        boolean isSelected = ev.getId() == currentEvenement[0].getId();
                        Label evLabel = new Label(ev.getTitre());
                        evLabel.setFont(Font.font("System", 11));
                        evLabel.setTextFill(Color.WHITE);
                        evLabel.setWrapText(true);
                        evLabel.setMaxWidth(Double.MAX_VALUE);
                        evLabel.setStyle(isSelected
                                ? "-fx-background-color:#d9534f; -fx-padding:4 6; " +
                                "-fx-background-radius:3; -fx-cursor:move;"
                                : "-fx-background-color:#1f4f65; -fx-padding:4 6; " +
                                "-fx-background-radius:3; -fx-cursor:hand;");

                        // ── DRAG SOURCE : uniquement l'événement sélectionné ──
                        if (isSelected) {
                            evLabel.setOnDragDetected(mouseEvent -> {
                                draggedEvenement = currentEvenement[0];
                                Dragboard db = evLabel.startDragAndDrop(TransferMode.MOVE);
                                ClipboardContent content = new ClipboardContent();
                                content.put(EVENEMENT_FORMAT,
                                        String.valueOf(currentEvenement[0].getId()));
                                db.setContent(content);
                                mouseEvent.consume();
                            });
                            evLabel.setOnDragDone(dragEvent -> {
                                draggedEvenement = null;
                                dragEvent.consume();
                            });
                        }

                        evLabel.setOnMouseClicked(e -> {
                            if (e.getClickCount() == 1) showEventDetailsModal(ev);
                        });

                        cell.getChildren().add(evLabel);
                    }

                    if (dayEvents.size() > 3) {
                        Label more = new Label("+" + (dayEvents.size() - 3) + " plus");
                        more.setStyle("-fx-text-fill:#3b82f6; -fx-font-size:10px;");
                        cell.getChildren().add(more);
                    }

                    // ── DROP TARGET : chaque cellule accepte le drop ──
                    cell.setOnDragOver(dragEvent -> {
                        if (dragEvent.getGestureSource() != cell
                                && dragEvent.getDragboard().hasContent(EVENEMENT_FORMAT)) {
                            dragEvent.acceptTransferModes(TransferMode.MOVE);

                            // Highlight visuel de la cellule cible
                            cell.setStyle("-fx-border-color:#27ae60; -fx-border-width:3; " +
                                    "-fx-background-color:#eafaf1;");
                        }
                        dragEvent.consume();
                    });

                    cell.setOnDragExited(dragEvent -> {
                        // Restaurer le style d'origine en quittant la cellule
                        boolean wasEventDay = currentEvenement[0].getDateDebut() != null &&
                                currentEvenement[0].getDateDebut().toLocalDate().equals(cellDate);
                        cell.setStyle(wasEventDay
                                ? "-fx-border-color:#1f4f65; -fx-border-width:2; -fx-background-color:#e8f4fd;"
                                : "-fx-border-color:#e2e8f0; -fx-border-width:1;");
                        dragEvent.consume();
                    });

                    final Runnable render = renderRef[0];
                    cell.setOnDragDropped(dragEvent -> {
                        Dragboard db = dragEvent.getDragboard();
                        boolean success = false;

                        if (db.hasContent(EVENEMENT_FORMAT) && draggedEvenement != null) {
                            LocalDateTime oldDebut = draggedEvenement.getDateDebut();

                            // Calculer le décalage en jours et l'appliquer à dateDebut et dateFin
                            long daysDelta = java.time.temporal.ChronoUnit.DAYS.between(
                                    oldDebut.toLocalDate(), cellDate);

                            LocalDateTime newDebut = oldDebut.plusDays(daysDelta);
                            LocalDateTime newFin   = draggedEvenement.getDateFin() != null
                                    ? draggedEvenement.getDateFin().plusDays(daysDelta)
                                    : null;

                            // Confirmation avant déplacement
                            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                            confirm.setTitle("Déplacer l'événement");
                            confirm.setHeaderText("Déplacer « " + draggedEvenement.getTitre() + " » ?");
                            confirm.setContentText(
                                    "Nouvelle date de début : "
                                            + newDebut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                                            + (newFin != null
                                            ? "\nNouvelle date de fin : "
                                            + newFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                                            : ""));

                            boolean[] confirmed = {false};
                            confirm.showAndWait().ifPresent(btn -> {
                                if (btn == ButtonType.OK) confirmed[0] = true;
                            });

                            if (confirmed[0]) {
                                try {
                                    draggedEvenement.setDateDebut(newDebut);
                                    draggedEvenement.setDateFin(newFin);
                                    service.update(draggedEvenement);

                                    // Mettre à jour currentEvenement et la table principale
                                    currentEvenement[0] = draggedEvenement;
                                    loadData();

                                    // Re-rendre le calendrier sur le nouveau mois si besoin
                                    currentMonth[0] = YearMonth.from(newDebut);
                                    render.run();

                                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                                            "La date de l'événement a été mise à jour.");
                                } catch (SQLException ex) {
                                    showAlert(Alert.AlertType.ERROR,
                                            "Erreur mise à jour", ex.getMessage());
                                    // Annuler le changement local
                                    draggedEvenement.setDateDebut(oldDebut);
                                    draggedEvenement.setDateFin(
                                            newFin != null ? newFin.minusDays(daysDelta) : null);
                                }
                                success = true;
                            }
                        }

                        dragEvent.setDropCompleted(success);
                        dragEvent.consume();
                    });

                    grid.add(cell, col, row);
                    col++;
                    if (col > 6) { col = 0; row++; }
                }

                scrollPane.setContent(grid);

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        };

        // Assigner le runnable dans le tableau pour que les lambdas y accèdent
        renderRef[0] = renderRef[0]; // déjà assigné ci-dessus

        prevBtn.setOnAction(e -> { currentMonth[0] = currentMonth[0].minusMonths(1); renderRef[0].run(); });
        nextBtn.setOnAction(e -> { currentMonth[0] = currentMonth[0].plusMonths(1);  renderRef[0].run(); });

        renderRef[0].run();

        Scene scene = new Scene(root);
        calendarStage.setScene(scene);
        calendarStage.show();
    }

    // ══════════════════════════════════════════════════════════════════
    //  EVENT DETAILS MODAL
    // ══════════════════════════════════════════════════════════════════
    private void showEventDetailsModal(Evenement event) {
        Stage modalStage = new Stage();
        modalStage.setTitle("Détails de l'événement");
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setWidth(500);
        modalStage.setHeight(600);

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color:#f4f6f9;");

        Label titleLabel = new Label(event.getTitre());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web("#1f4f65"));

        Label dateLabel = new Label("📅 " + (event.getDateDebut() != null ?
                event.getDateDebut().format(DateTimeFormatter.ofPattern("dd MMMM yyyy - HH:mm")) : "Date non définie"));
        dateLabel.setStyle("-fx-font-size:13px; -fx-text-fill:#3b82f6; -fx-font-weight:bold;");

        Label locationLabel = new Label("📍 " + (event.getLieu() != null ? event.getLieu() : "-"));
        locationLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#ecc94b;");

        Label modeLabel = new Label("🎯 Mode: " + (event.getMode() != null ? event.getMode() : "-"));
        modeLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#0f6b5e;");

        Label capacityLabel = new Label("👥 Capacité: " + event.getCapaciteMax());
        capacityLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#666;");

        Label publicLabel = new Label("🎓 Public cible: " + (event.getPublicCible() != null ? event.getPublicCible() : "-"));
        publicLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#666;");

        Label contactLabel = new Label("✉️ Contact: " + (event.getContactEmail() != null ? event.getContactEmail() : "-"));
        contactLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#666;");

        Label descLabel = new Label("Description:");
        descLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        TextArea descArea = new TextArea(event.getDescription() != null ? event.getDescription() : "-");
        descArea.setWrapText(true);
        descArea.setPrefRowCount(5);
        descArea.setStyle("-fx-control-inner-background:#ffffff; -fx-font-size:12px;");
        descArea.setEditable(false);

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-padding:10 30; -fx-background-color:#e0e0e0; -fx-cursor:hand; -fx-font-weight:bold;");
        closeBtn.setOnAction(e -> modalStage.close());

        HBox btnBox = new HBox(closeBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        content.getChildren().addAll(
                titleLabel, dateLabel, locationLabel, modeLabel, capacityLabel,
                publicLabel, contactLabel, descLabel, descArea, btnBox);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#f4f6f9;");

        Scene scene = new Scene(scroll);
        modalStage.setScene(scene);
        modalStage.showAndWait();
    }

    // ══════════════════════════════════════════════════════════════════
    //  ADD
    // ══════════════════════════════════════════════════════════════════
    @FXML
    private void openAddPage() {
        if (dashboardController != null) {
            dashboardController.showAddEvenement();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  EDIT DIALOG
    // ══════════════════════════════════════════════════════════════════
    private void openEditDialog(Evenement e) {
        if (dashboardController != null) {
            dashboardController.showEditEvenement(e);
        }
    }

    @FXML
    private void closeDialog() {
        dialogOverlay.setVisible(false);
        dialogOverlay.setManaged(false);
    }

    @FXML
    private void saveEvenement() {
        if (!validateForm()) return;
        Evenement e = editingEvenement != null ? editingEvenement : new Evenement();
        fillModel(e);
        try {
            if (editingEvenement == null) service.create(e);
            else                          service.update(e);
            closeDialog();
            loadData();
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur sauvegarde", ex.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  DELETE
    // ══════════════════════════════════════════════════════════════════
    private void confirmDelete(Evenement e) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirmer la suppression");
        a.setHeaderText("Supprimer « " + e.getTitre() + " » ?");
        a.setContentText("Cette action est irréversible.");
        a.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    service.delete(e.getId());
                    loadData();
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Erreur suppression", ex.getMessage());
                }
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════
    //  🌍 RECOMMANDATIONS MONDIALES
    // ══════════════════════════════════════════════════════════════════
    @FXML
    private void showRecommandationsMondiales() {
        Stage stage = new Stage();
        stage.setTitle("🌍 Recommandations Mondiales d'Orientation");
        stage.initModality(Modality.APPLICATION_MODAL);

        Label titre = new Label("🌍  Événements d'Orientation dans le Monde");
        titre.setFont(Font.font("System", FontWeight.BOLD, 16));
        titre.setTextFill(Color.WHITE);

        HBox header = new HBox(titre);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setStyle("-fx-background-color:#2c3e50;");

        Label loading = new Label("⏳  Chargement des événements mondiaux...");
        loading.setFont(Font.font("System", 14));
        loading.setPadding(new Insets(20));

        VBox cardsBox = new VBox(12);
        cardsBox.setPadding(new Insets(16));

        ScrollPane scroll = new ScrollPane(cardsBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent;");

        VBox root = new VBox(header, loading, scroll);
        root.setStyle("-fx-background-color:#f4f6f9;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Scene scene = new Scene(root, 720, 580);
        stage.setScene(scene);
        stage.show();

        Thread thread = new Thread(() -> {
            try {
                List<EvenementMondialDTO> events = recommendationService.getEvenementsOrientation();
                javafx.application.Platform.runLater(() -> {
                    root.getChildren().remove(loading);
                    if (events.isEmpty()) {
                        Label empty = new Label("Aucun événement trouvé.");
                        empty.setFont(Font.font("System", 14));
                        empty.setPadding(new Insets(20));
                        cardsBox.getChildren().add(empty);
                        return;
                    }
                    Label counter = new Label("✅  " + events.size() + " événement(s) trouvé(s) dans le monde");
                    counter.setFont(Font.font("System", FontWeight.BOLD, 13));
                    counter.setTextFill(Color.web("#2c3e50"));
                    counter.setPadding(new Insets(0, 0, 8, 0));
                    cardsBox.getChildren().add(counter);
                    for (EvenementMondialDTO ev : events) {
                        cardsBox.getChildren().add(buildEventCard(ev));
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    loading.setText("❌  Erreur : " + e.getMessage());
                    loading.setStyle("-fx-text-fill:#d9534f; -fx-font-size:13px;");
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private VBox buildEventCard(EvenementMondialDTO ev) {
        Label lbTitre = new Label(ev.getTitle() != null ? ev.getTitle() : "Sans titre");
        lbTitre.setFont(Font.font("System", FontWeight.BOLD, 14));
        lbTitre.setWrapText(true);
        lbTitre.setTextFill(Color.web("#2c3e50"));

        String dateStr = (ev.getStart() != null && ev.getStart().length() >= 10)
                ? ev.getStart().substring(0, 10) : "Date inconnue";
        String paysStr = ev.getCountry() != null ? ev.getCountry().toUpperCase() : "Pays inconnu";
        String catStr  = ev.getCategory() != null ? ev.getCategory() : "—";

        Label lbMeta = new Label("📅 " + dateStr + "     🌍 " + paysStr + "     🏷️ " + catStr);
        lbMeta.setStyle("-fx-text-fill:#555555; -fx-font-size:12px;");

        String participants = ev.getPhqAttendance() > 0
                ? ev.getPhqAttendance() + " participants estimés"
                : "Nombre de participants non disponible";
        Label lbParticipants = new Label("👥 " + participants);
        lbParticipants.setStyle("-fx-text-fill:#888888; -fx-font-size:12px;");

        String finStr = (ev.getEnd() != null && ev.getEnd().length() >= 10)
                ? ev.getEnd().substring(0, 10) : null;
        Label lbFin = finStr != null ? new Label("🏁 Fin : " + finStr) : new Label("");
        lbFin.setStyle("-fx-text-fill:#888888; -fx-font-size:12px;");

        VBox card = new VBox(6, lbTitre, lbMeta, lbParticipants, lbFin);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle("-fx-background-color:white; -fx-background-radius:10;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.10),8,0,0,3);");
        return card;
    }

    // ══════════════════════════════════════════════════════════════════
    //  STATISTIQUES CAPACITÉ
    // ══════════════════════════════════════════════════════════════════
    @FXML
    private void showStatsCapacite() {
        try {
            List<Evenement> all = service.findAll();
            long petite  = all.stream().filter(e -> e.getCapaciteMax() <= 50).count();
            long moyenne = all.stream().filter(e -> e.getCapaciteMax() > 50 && e.getCapaciteMax() <= 200).count();
            long grande  = all.stream().filter(e -> e.getCapaciteMax() > 200).count();

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                    new PieChart.Data("Petite (≤ 50)",    petite),
                    new PieChart.Data("Moyenne (51-200)", moyenne),
                    new PieChart.Data("Grande (> 200)",   grande)
            );

            PieChart chart = new PieChart(pieData);
            chart.setTitle("Répartition par capacité");
            chart.setLegendVisible(true);
            chart.setLabelsVisible(true);
            chart.setPrefSize(500, 400);
            chart.setStyle("-fx-font-size:13px;");

            HBox cards = buildCapaciteCards(all);
            VBox root  = buildStatsRoot("📊  Statistiques Capacité", "#1f7a5e", chart, cards);
            showStatsWindow("Statistiques Capacité", root);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur statistiques", e.getMessage());
        }
    }

    private HBox buildCapaciteCards(List<Evenement> all) {
        int    total  = all.size();
        int    maxCap = all.stream().mapToInt(Evenement::getCapaciteMax).max().orElse(0);
        int    minCap = all.stream().mapToInt(Evenement::getCapaciteMax).min().orElse(0);
        double avg    = all.stream().mapToInt(Evenement::getCapaciteMax).average().orElse(0);
        return new HBox(12,
                buildCard("Total événements", String.valueOf(total),        "#1f7a5e"),
                buildCard("Capacité max",      String.valueOf(maxCap),      "#0d6efd"),
                buildCard("Capacité min",      String.valueOf(minCap),      "#fd7e14"),
                buildCard("Moyenne capacité",  String.format("%.0f", avg), "#6f42c1")
        );
    }

    // ══════════════════════════════════════════════════════════════════
    //  STATISTIQUES CRÉATIONS
    // ══════════════════════════════════════════════════════════════════
    @FXML
    private void showStatsCreations() {
        try {
            List<Evenement> all = service.findAll();
            Map<String, Long> byMode = all.stream().collect(Collectors.groupingBy(
                    e -> e.getMode() != null ? e.getMode() : "Non défini", Collectors.counting()));

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            byMode.forEach((mode, count) -> pieData.add(new PieChart.Data(mode, count)));

            PieChart chart = new PieChart(pieData);
            chart.setTitle("Répartition par mode de création");
            chart.setLegendVisible(true);
            chart.setLabelsVisible(true);
            chart.setPrefSize(500, 400);
            chart.setStyle("-fx-font-size:13px;");

            HBox cards = buildCreationsCards(all, byMode);
            VBox root  = buildStatsRoot("📊  Statistiques Créations", "#0d6efd", chart, cards);
            showStatsWindow("Statistiques Créations", root);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur statistiques", e.getMessage());
        }
    }

    private HBox buildCreationsCards(List<Evenement> all, Map<String, Long> byMode) {
        long presentiel = byMode.getOrDefault("Présentiel", 0L);
        long enligne    = byMode.getOrDefault("En ligne",   0L);
        long hybride    = byMode.getOrDefault("Hybride",    0L);
        return new HBox(12,
                buildCard("Total événements", String.valueOf(all.size()), "#0d6efd"),
                buildCard("Présentiel",        String.valueOf(presentiel), "#1f7a5e"),
                buildCard("En ligne",          String.valueOf(enligne),    "#fd7e14"),
                buildCard("Hybride",           String.valueOf(hybride),    "#6f42c1")
        );
    }

    // ══════════════════════════════════════════════════════════════════
    //  HELPERS — UI
    // ══════════════════════════════════════════════════════════════════
    private VBox buildStatsRoot(String titre, String headerColor, PieChart chart, HBox cards) {
        Label lbTitre = new Label(titre);
        lbTitre.setFont(Font.font("System", FontWeight.BOLD, 18));
        lbTitre.setTextFill(Color.WHITE);

        HBox header = new HBox(lbTitre);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setStyle("-fx-background-color:" + headerColor + ";");

        cards.setPadding(new Insets(0, 24, 0, 24));
        cards.setAlignment(Pos.CENTER);

        VBox chartBox = new VBox(chart);
        chartBox.setAlignment(Pos.CENTER);
        chartBox.setPadding(new Insets(8, 24, 8, 24));

        VBox root = new VBox(12, header, cards, chartBox);
        root.setStyle("-fx-background-color:#f4f6f9;");
        return root;
    }

    private void showStatsWindow(String windowTitle, VBox root) {
        Stage stage = new Stage();
        stage.setTitle(windowTitle);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(true);
        stage.setScene(new Scene(root, 640, 560));
        stage.showAndWait();
    }

    private VBox buildCard(String label, String value, String color) {
        Label lbValue = new Label(value);
        lbValue.setFont(Font.font("System", FontWeight.BOLD, 26));
        lbValue.setTextFill(Color.WHITE);

        Label lbLabel = new Label(label);
        lbLabel.setFont(Font.font("System", 12));
        lbLabel.setTextFill(Color.web("#ffffffcc"));

        VBox card = new VBox(4, lbValue, lbLabel);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setMinWidth(130);
        card.setStyle("-fx-background-color:" + color + "; -fx-background-radius:10;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.18),8,0,0,3);");
        return card;
    }

    // ══════════════════════════════════════════════════════════════════
    //  FORM HELPERS
    // ══════════════════════════════════════════════════════════════════
    private void fillForm(Evenement e) {
        fTitre.setText(e.getTitre());
        fLieu.setText(e.getLieu());
        fMode.setValue(e.getMode());
        fCapaciteMax.setText(String.valueOf(e.getCapaciteMax()));
        fDateDebut.setText(e.getDateDebut() != null ? e.getDateDebut().format(FMT) : "");
        fDateFin.setText(e.getDateFin()     != null ? e.getDateFin().format(FMT)   : "");
        fPublicCible.setText(e.getPublicCible());
        fContactEmail.setText(e.getContactEmail());
        fContactTelephone.setText(e.getContactTelephone());
        fLienInscription.setText(e.getLienInscription());
        fDescription.setText(e.getDescription());
        if (e.getOrganisme() != null) {
            fOrganisme.setValue(e.getOrganisme().getId() + " - " + e.getOrganisme().getNom());
        } else {
            fOrganisme.setValue("-- Aucun --");
        }
    }

    private void fillModel(Evenement e) {
        e.setTitre(fTitre.getText().trim());
        e.setLieu(fLieu.getText().trim());
        e.setMode(fMode.getValue());
        try { e.setCapaciteMax(Integer.parseInt(fCapaciteMax.getText().trim())); }
        catch (NumberFormatException ex) { e.setCapaciteMax(0); }
        try { e.setDateDebut(java.time.LocalDateTime.parse(fDateDebut.getText().trim(), FMT)); }
        catch (Exception ex) { e.setDateDebut(null); }
        try { e.setDateFin(java.time.LocalDateTime.parse(fDateFin.getText().trim(), FMT)); }
        catch (Exception ex) { e.setDateFin(null); }
        e.setPublicCible(fPublicCible.getText().trim());
        e.setContactEmail(fContactEmail.getText().trim());
        e.setContactTelephone(fContactTelephone.getText().trim());
        e.setLienInscription(fLienInscription.getText().trim());
        e.setDescription(fDescription.getText().trim());

        String orgVal = fOrganisme.getValue();
        if (orgVal != null && !orgVal.startsWith("--")) {
            try {
                int orgId = Integer.parseInt(orgVal.split(" - ")[0].trim());
                Organisme org = new Organisme();
                org.setId(orgId);
                e.setOrganisme(org);
            } catch (Exception ex) { e.setOrganisme(null); }
        } else {
            e.setOrganisme(null);
        }
    }

    private boolean validateForm() {
        if (fTitre.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le titre est obligatoire.");
            return false;
        }
        if (fMode.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner un mode.");
            return false;
        }
        return true;
    }

    // ══════════════════════════════════════════════════════════════════
    //  SHARE DIALOG
    // ══════════════════════════════════════════════════════════════════
    private void showShareDialog(Evenement evenement) {
        String shareLink = "https://de3-196-238-42-231.ngro";

        Stage stage = new Stage();
        stage.setTitle("Partager l'événement");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        VBox content = new VBox(16);
        content.setStyle("-fx-background-color:#f4f6f9; -fx-padding:20;");
        content.setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label("⮡ Partager l'événement");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        HBox linkBox = new HBox(8);
        linkBox.setStyle("-fx-background-color:white; -fx-border-color:#e0e0e0; " +
                "-fx-border-radius:6; -fx-padding:12;");
        linkBox.setAlignment(Pos.CENTER_LEFT);

        TextField linkField = new TextField(shareLink);
        linkField.setEditable(false);
        linkField.setStyle("-fx-font-size:12px; -fx-padding:8;");
        HBox.setHgrow(linkField, Priority.ALWAYS);

        Button copyBtn = new Button("📋");
        copyBtn.setStyle("-fx-background-color:#4caf50; -fx-text-fill:white; " +
                "-fx-cursor:hand; -fx-background-radius:4; -fx-padding:8 12;");
        copyBtn.setOnAction(e -> {
            Clipboard clip = Clipboard.getSystemClipboard();
            ClipboardContent c = new ClipboardContent();
            c.putString(shareLink);
            clip.setContent(c);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Lien copié dans le presse-papiers!");
        });

        Button editBtn = new Button("✏️");
        editBtn.setStyle("-fx-background-color:#2196f3; -fx-text-fill:white; " +
                "-fx-cursor:hand; -fx-background-radius:4; -fx-padding:8 12;");
        editBtn.setOnAction(e -> { linkField.setEditable(true); linkField.requestFocus(); });

        linkBox.getChildren().addAll(linkField, copyBtn, editBtn);

        VBox socialBox = new VBox(10);
        socialBox.setStyle("-fx-border-color:#e0e0e0; -fx-border-radius:6; " +
                "-fx-padding:12; -fx-background-color:white;");

        Label socialLabel = new Label("Partager sur les réseaux sociaux:");
        socialLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        socialLabel.setTextFill(Color.web("#1f4f65"));

        Button twitterBtn = new Button("𝕏  Partager sur Twitter");
        twitterBtn.setStyle("-fx-background-color:#000000; -fx-text-fill:white; " +
                "-fx-font-weight:bold; -fx-cursor:hand; -fx-background-radius:4; " +
                "-fx-padding:8 16; -fx-font-size:12px; -fx-max-width:Infinity;");
        twitterBtn.setOnAction(e -> {
            try {
                String url = "https://twitter.com/intent/tweet?text=" +
                        java.net.URLEncoder.encode("Découvrez cet événement: " +
                                evenement.getTitre() + " " + shareLink, "UTF-8");
                openUrl(url);
            } catch (java.io.UnsupportedEncodingException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'encodage: " + ex.getMessage());
            }
        });

        Button linkedinBtn = new Button("in  Partager sur LinkedIn");
        linkedinBtn.setStyle("-fx-background-color:#0a66c2; -fx-text-fill:white; " +
                "-fx-font-weight:bold; -fx-cursor:hand; -fx-background-radius:4; " +
                "-fx-padding:8 16; -fx-font-size:12px; -fx-max-width:Infinity;");
        linkedinBtn.setOnAction(e -> openUrl(
                "https://www.linkedin.com/sharing/share-offsite/?url=" + shareLink));

        socialBox.getChildren().addAll(socialLabel, twitterBtn, linkedinBtn);

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color:#e0e0e0; -fx-cursor:hand; " +
                "-fx-background-radius:4; -fx-padding:8 20;");
        closeBtn.setOnAction(e -> stage.close());

        HBox btnBox = new HBox(closeBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        content.getChildren().addAll(titleLabel, linkBox, socialBox, btnBox);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#f4f6f9;");

        Scene scene = new Scene(scroll, 500, 350);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void openUrl(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le navigateur: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}