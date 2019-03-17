package View;

import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.io.IOException;

public class ProfileView extends View {

    @FXML
    private Button editeverything, save, cancel, closeprofile;
    @FXML
    private Label displayusername, displayemail, displaypassword;
    @FXML
    private TextField newusername, newfullname;
    @FXML
    private PasswordField newpassword;

    private FXMLLoader loader;
    private Stage stage;
    private Scene scene;
    private Parent root;

    public ProfileView () {
        try {
            loader = new FXMLLoader(getClass().getResource("/View/Profile.fxml"));
            loader.setController(this);
            root = (Parent) loader.load();
            scene = new Scene(root);

            stage.setTitle("Profile");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.show();

        } catch (IOException ex) {
        }
    }

    @Override
    public void update() {

    }


    public void initialize () {
        closeprofile.setOnAction(event -> {
            stage.close();
        });

        editeverything.setOnAction(event -> {
            newfullname.setVisible(true);
            newfullname.setEditable(true);
            newusername.setVisible(true);
            newusername.setEditable(true);
            newpassword.setVisible(true);
            newpassword.setEditable(true);
            save.setVisible(true);
            save.setDisable(false);
            cancel.setVisible(true);
            cancel.setDisable(false);
            displ
        });

        save.setOnAction(event -> {
        	stage.close();
        });

        cancel.setOnAction(event -> {
        	stage.close();
        });
    }

    private void init () {
        newfullname.setVisible(false);
        newusername.setVisible(false);
        newpassword.setVisible(false);
        newfullname.setEditable(false);
        newusername.setEditable(false);
        newpassword.setEditable(false);
        save.setVisible(false);
        cancel.setVisible(false);
        save.setDisable(true);
        cancel.setDisable(true);
    }
}
