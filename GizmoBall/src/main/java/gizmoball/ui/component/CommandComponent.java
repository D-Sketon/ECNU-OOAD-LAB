package gizmoball.ui.component;

import javafx.scene.Cursor;
import javafx.scene.layout.VBox;
import lombok.Getter;

@Getter
public class CommandComponent extends ImageLabelComponent {

    private final GizmoCommand gizmoCommand;

    public CommandComponent(String resource, String labelText, GizmoCommand gizmoCommand) {
        super(resource, labelText);
        this.gizmoCommand = gizmoCommand;
    }

    @Override
    public VBox createVBox() {
        VBox vBox = super.createVBox();
        getImageView().setCursor(Cursor.HAND);
        return vBox;
    }
}
