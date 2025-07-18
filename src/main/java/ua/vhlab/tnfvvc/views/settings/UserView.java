package ua.vhlab.tnfvvc.views.settings;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import jakarta.annotation.security.RolesAllowed;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ua.vhlab.tnfvvc.data.User;
import ua.vhlab.tnfvvc.services.UserService;
import ua.vhlab.tnfvvc.data.Role;
import java.util.List;
import java.util.Set;

@RolesAllowed("ADMIN")
@Component
@Scope("prototype")
public class UserView extends Composite<VerticalLayout> {

    private final Grid<User> grid = new Grid<>(User.class, false);
    private final UserService service;
    private final TextField userNameField = new TextField("Username");
    private final TextField nameField = new TextField("Name");
    private final PasswordField passwordField = new PasswordField("Password");
    private final PasswordField changePasswordField = new PasswordField("Password");
    private final RadioButtonGroup<String> radioGroup = new RadioButtonGroup<>();
    private UI ui = UI.getCurrent();

    public UserView(UserService service) {
        this.service = service;
        VerticalLayout layout = getContent();
        styleMainLayout(layout);

        Button buttonAdd = new Button("Add");
        buttonAdd.getStyle().set("width", "min-content");
        buttonAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        configureGrid();

        layout.add(buttonAdd, grid);
        layout.expand(grid);

        Dialog createUserDialog = new Dialog();
        createUserDialog.setHeaderTitle("New user");
        createUserDialog.add(createUserDialogLayout());
        createUserDialog.getFooter().add(new Button("Cancel", e -> createUserDialog.close()));
        createUserDialog.getFooter().add(createSaveButton(createUserDialog));

        buttonAdd.addClickListener(user -> createUserDialog.open());
    }

    @Override
    protected void onAttach(AttachEvent event) {
        ui = event.getUI();
        // Тепер це гарантовано valid UI
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(User::getUsername).setHeader("Username");
        grid.addColumn(User::getName).setHeader("Name");
        grid.addColumn(user -> user.getRoles() == null ? ""
                : String.join(", ", user.getRoles().stream().map(Role::name).toList()))
                .setHeader("Roles");

        grid.addColumn(new ComponentRenderer<>(Button::new, (button, person) -> {
            button.setIcon(new Icon(VaadinIcon.TRASH));
            button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            button.addClickListener(e -> removeUser(person));
        })).setHeader("Manage");

        grid.addColumn(new ComponentRenderer<>(Button::new, (button, person) -> {
            button.setIcon(new Icon(VaadinIcon.KEY));
            button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);
            button.addClickListener(e -> openChangePasswordDialog(person));
        })).setHeader("Set password");

        grid.setItems(service.list());
    }

    private void openChangePasswordDialog(User person) {
        Dialog changePasswordDialog = new Dialog();
        changePasswordDialog.setHeaderTitle("New password");
        changePasswordDialog.add(changePasswordLayout());
        changePasswordDialog.getFooter().add(new Button("Cancel", e -> changePasswordDialog.close()));
        changePasswordDialog.getFooter().add(passwordSaveButton(changePasswordDialog, person));
        changePasswordDialog.open();
    }

    private VerticalLayout changePasswordLayout() {
        VerticalLayout dialogLayout = new VerticalLayout(changePasswordField);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(false);
        dialogLayout.setAlignItems(Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");
        return dialogLayout;
    }

    private VerticalLayout createUserDialogLayout() {
        radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroup.setLabel("Role");
        radioGroup.setItems("USER", "ADMIN");
        radioGroup.setValue("USER");
        VerticalLayout dialogLayout = new VerticalLayout(userNameField, nameField, passwordField, radioGroup);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(false);
        dialogLayout.setAlignItems(Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");
        return dialogLayout;
    }

    private Button passwordSaveButton(Dialog dialog, User person) {
        Button saveButton = new Button("Save", e -> {

            if (ui != null && ui.isAttached()) {
                ui.access(() -> {
                    person.setHashedPassword(changePasswordField.getValue());
                    service.saveUsers(service.list());
                    refreshGrid();
                    dialog.close();
                    if (ui.isAttached()) {
                        ui.push();
                    }
                });
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return saveButton;
    }

    private Button createSaveButton(Dialog dialog) {
        Button saveButton = new Button("Add", e -> {
            if (ui != null && ui.isAttached()) {
                ui.access(() -> {
                    Set<Role> roles = radioGroup.getValue() != null
                            ? Set.of(Role.valueOf(radioGroup.getValue())) : Set.of();
                    List<User> users = service.list();
                    User user = new User(userNameField.getValue(), nameField.getValue(), passwordField.getValue(), roles, null);
                    users.add(user);
                    service.saveUsers(users);
                    refreshGrid();
                    dialog.close();
                    if (ui.isAttached()) {
                        ui.push();
                    }
                });
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return saveButton;
    }

    private void removeUser(User person) {
        if ("admin".equals(person.getUsername())) {
            Notification.show("User \"admin\" cannot be deleted!", 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } else {
            if (ui != null && ui.isAttached()) {
                ui.access(() -> {
                    List<User> users = service.list();
                    users.remove(person);
                    service.saveUsers(users);
                    refreshGrid();
                    if (ui.isAttached()) {
                        ui.push();
                    }
                });
            }
        }
    }

    private void refreshGrid() {
        grid.setItems(service.list());
    }

    private void styleMainLayout(VerticalLayout layout) {
        layout.setSizeFull();
        layout.addClassNames(Gap.SMALL, Padding.SMALL);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        layout.setAlignItems(Alignment.STRETCH);
        layout.getStyle()
                .set("border", "1px solid lightgray")
                .set("border-radius", "8px")
                .set("padding", "10px")
                .set("margin", "10px");
    }

    void onTabActivated() {
  
    }
}
