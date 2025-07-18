package ua.vhlab.tnfvvc.views.settings;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ua.vhlab.tnfvvc.data.config.Config;
import ua.vhlab.tnfvvc.data.config.DashboardConfig;
import ua.vhlab.tnfvvc.data.config.TrueNASConfig;
import ua.vhlab.tnfvvc.services.ConfigService;
import ua.vhlab.tnfvvc.services.DashboardViewUpdateService;
import ua.vhlab.tnfvvc.util.AESCryptoUtil;

import java.time.Instant;
import java.util.List;

@Component
@Scope("prototype")
@RolesAllowed("ADMIN")
public class TrueNASView extends Composite<VerticalLayout> {

    private final ConfigService service;
    private final DashboardViewUpdateService dashboardViewUpdateService;
    private final Grid<DashboardConfig> grid = new Grid<>(DashboardConfig.class);
    private final H5 h5server = new H5();

    public TrueNASView(ConfigService service, DashboardViewUpdateService dashboardViewUpdateService) {
        this.service = service;
        this.dashboardViewUpdateService = dashboardViewUpdateService;
        VerticalLayout layout = getContent();
        styleMainLayout(layout);

        Button buttonEdit = new Button("Edit");
        buttonEdit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonEdit.addClickListener(e -> {
            getUI().ifPresent(ui -> {
                if (ui.isAttached()) {
                    ui.access(() -> {
                        openChangeTrueNASConfigDialog();
                        ui.push();
                    });
                }
            });
        });

        HorizontalLayout headerLayout = new HorizontalLayout(h5server, buttonEdit);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        h5server.setText("TrueNAS Server: " + service.getTrueNASConfig().getServer());

        Button buttonAdd = new Button("Add", e -> openAddDashboardDialog());
        buttonAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonAdd.getStyle().set("width", "min-content");

        Hr hr = new Hr();

        configureGrid();

        layout.add(headerLayout, hr, buttonAdd, grid);
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(new ComponentRenderer<>(Button::new, (button, dashboard) -> {
            button.setIcon(new Icon(VaadinIcon.TRASH));
            button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            button.addClickListener(e -> removeDashboard(dashboard));
        })).setHeader("Manage");
        grid.setItems(service.getDashboardConfig());
    }

    private void openAddDashboardDialog() {
        TextField resource = new TextField("Resource");
        TextField description = new TextField("Description");
        TextField param = new TextField("Param");

        Dialog dialog = buildDialog("Add Dashboard", List.of(resource, description, param), () -> {
            Config config = service.getConfig();
            DashboardConfig dashboardConfig = new DashboardConfig();
            dashboardConfig.setUrl("https://" + service.getTrueNASConfig().getServer() + resource.getValue());
            dashboardConfig.setDescription(description.getValue());
            dashboardConfig.setParam(param.getValue());
            config.getDashboard().add(dashboardConfig);
            service.saveConfig(config);
            grid.setItems(service.getDashboardConfig());
            dashboardViewUpdateService.addItem("New element: " + Instant.now());
        });
        getUI().ifPresent(ui -> {
            if (ui.isAttached()) {
                ui.access(() -> {
                    ui.add(dialog);
                    dialog.open();
                    ui.push();
                });
            }
        });
    }

    private void openChangeTrueNASConfigDialog() {
        TextField server = new TextField("Server");
        TextField login = new TextField("Login");
        PasswordField passwordField = new PasswordField("Password");

        TrueNASConfig config = service.getTrueNASConfig();
        server.setValue(config.getServer());
        login.setValue(config.getLogin());
        String hashedPassword = config.getHashedPassword();
        if (hashedPassword != null && !hashedPassword.isEmpty()) {
            try {
                passwordField.setValue(AESCryptoUtil.decrypt(hashedPassword));
            } catch (Exception ignored) {
            }
        }

        Dialog dialog = buildDialog("Change TrueNAS Config", List.of(server, login, passwordField), () -> {
            config.setServer(server.getValue());
            config.setLogin(login.getValue());
            try {
                config.setHashedPassword(AESCryptoUtil.encrypt(passwordField.getValue()));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            service.saveConfig(service.getConfig());
            h5server.setText("TrueNAS Server: " + config.getServer());
            dashboardViewUpdateService.addItem("New element: " + Instant.now());
        });
        getUI().ifPresent(ui -> {
            if (ui.isAttached()) {
                ui.access(() -> {
                    ui.add(dialog);
                    dialog.open();
                    ui.push();
                });
            }
        });
    }

    private Dialog buildDialog(String title, List<com.vaadin.flow.component.Component> fields, Runnable onSave) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title);

        VerticalLayout content = new VerticalLayout();
        content.add(fields.toArray(new com.vaadin.flow.component.Component[0]));
        content.setPadding(false);
        content.setSpacing(false);
        content.setAlignItems(FlexComponent.Alignment.STRETCH);
        content.getStyle().set("width", "18rem").set("max-width", "100%");

        Button save = new Button("Save", e -> {
            getUI().ifPresent(ui -> {
                if (ui.isAttached()) {
                    ui.access(() -> {
                        onSave.run();
                        dialog.close();
                        ui.push();
                    });
                }
            });
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Cancel", e -> dialog.close());

        dialog.add(content);
        dialog.getFooter().add(cancel, save);
        return dialog;
    }

    private void removeDashboard(DashboardConfig dashboard) {
        Config config = service.getConfig();
        config.getDashboard().remove(dashboard);
        service.saveConfig(config);
        grid.setItems(service.getDashboardConfig());
        dashboardViewUpdateService.addItem("Removed element: " + Instant.now());
    }

    private void styleMainLayout(VerticalLayout layout) {
        layout.setSizeFull();
        layout.addClassNames(Gap.SMALL, Padding.SMALL);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);
        layout.getStyle()
                .set("border", "1px solid lightgray")
                .set("border-radius", "8px")
                .set("padding", "10px")
                .set("margin", "10px");
    }

    void onTabActivated() {
       
    }
}
