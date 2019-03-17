/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Model.CreateSongFromLocal;
import Model.FacadeModel;
import Model.SongInterface;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author Stanley Sie
 */
public class SongListView {

    private FacadeModel model;
    private ObservableList<SongInterface> songList;

    private Label listLabel;
    private TableView table;
    private Stage stage;

    public SongListView(FacadeModel model, TableView table, Label listLabel, Stage stage) {
        this.model = model;
        this.table = table;
        this.listLabel = listLabel;
        this.stage = stage;
        songList = FXCollections.observableArrayList();

        init();
    }

    private void init() {
        listLabel.setText("Songs");

        try {
            loadSong();
        } catch (SQLException ex) {
        }

        TableColumn title = new TableColumn("Title");
        title.setCellValueFactory(new PropertyValueFactory("name"));
        title.setPrefWidth(220);
        title.setStyle("-fx-alignment: CENTER_LEFT;-fx-text-fill: white;");
        title.setResizable(false);
        TableColumn artist = new TableColumn("Artist");
        artist.setCellValueFactory(new PropertyValueFactory("artist"));
        artist.setPrefWidth(160);
        artist.setStyle("-fx-alignment: CENTER_LEFT;-fx-text-fill: white;");
        artist.setResizable(false);
        TableColumn album = new TableColumn("Album");
        album.setCellValueFactory(new PropertyValueFactory("album"));
        album.setPrefWidth(150);
        album.setStyle("-fx-alignment: CENTER_LEFT;-fx-text-fill: white;");
        album.setResizable(false);
        TableColumn year = new TableColumn("Year");
        year.setCellValueFactory(new PropertyValueFactory("year"));
        year.setStyle("-fx-alignment: CENTER_LEFT;-fx-text-fill: white;");
        year.setPrefWidth(80);
        year.setResizable(false);
        TableColumn duration = new TableColumn("Duration");
        duration.setCellValueFactory(new PropertyValueFactory("duration"));
        duration.setPrefWidth(90);
        duration.setStyle("-fx-alignment: CENTER_LEFT;-fx-text-fill: white;");
        duration.setResizable(false);
        TableColumn play = new TableColumn();
        play.setCellValueFactory(new PropertyValueFactory("play"));
        play.setPrefWidth(65);
        play.setResizable(false);
        TableColumn add = new TableColumn();
        add.setCellValueFactory(new PropertyValueFactory("add"));
        add.setPrefWidth(45);
        add.setResizable(false);
        TableColumn edit = new TableColumn();
        edit.setCellValueFactory(new PropertyValueFactory("edit"));
        edit.setPrefWidth(45);
        edit.setResizable(false);
        TableColumn del = new TableColumn();
        del.setCellValueFactory(new PropertyValueFactory("del"));
        del.setPrefWidth(45);
        del.setResizable(false);

        table.getColumns().setAll(play, title, artist, album, year, duration, add, edit, del);
        table.setItems(songList);
    }

    public ObservableList<SongInterface> getSongList() {
        return songList;
    }

    public void setSongList(ObservableList<SongInterface> songList) {
        this.songList = songList;
    }

    public void update() {
        table.getItems().clear();
        try {
            loadSong();
        } catch (SQLException ex) {
        }
    }

    public void updateTable(FilteredList<SongInterface> filteredData) {
        SortedList<SongInterface> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);
    }

    public void loadSong() throws SQLException {
        if(model.getUser() == null) {
            songList = model.getSongs();
        } else {
            songList = model.getUserSongs();
        }
        table.setItems(songList);
    }

    public void uploadSong() {
        FileChooser file = new FileChooser();
        List<File> list = file.showOpenMultipleDialog(stage);
        if (list != null) {
            if (model.getUser() == null) {
                for (File f : list) {
                    System.out.println(f.toURI().toString().substring(6, f.toURI().toString().length()).replaceAll("%20", " "));
                    model.addSong(CreateSongFromLocal.CreateSong(f.toURI().toString().substring(6, f.toURI().toString().length()).replaceAll("%20", " ")));
                }
            } else {
                for (File f : list) {
                    System.out.println(f.toURI().toString().substring(6, f.toURI().toString().length()).replaceAll("%20", " "));
                    try {
                        if(model.addSong(f.toURI().toString().substring(6, f.toURI().toString().length()).replaceAll("%20", " "))) {
                            loadSong();
                        }
                    } catch (SQLException ex) {

                    }
                }
            }
        }
    }
}
