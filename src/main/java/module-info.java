module org.example.tuneupv2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.tuneupv2 to javafx.fxml;
    exports org.example.tuneupv2;
}