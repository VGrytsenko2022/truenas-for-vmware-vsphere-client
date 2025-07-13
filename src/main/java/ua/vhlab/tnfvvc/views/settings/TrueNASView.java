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
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
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
import ua.vhlab.tnfvvc.util.AESCryptoUtil;
import ua.vhlab.tnfvvc.services.DashboardViewUpdateService;

import java.time.Instant;
import java.util.List;

@Component
@Scope("prototype")
@RolesAllowed("ADMIN")
public class TrueNASView extends Composite<VerticalLayout> {

    private final TextField server = new TextField("Server");
    private final TextField login = new TextField("Login");
    private final PasswordField passwordField = new PasswordField("Password");
    private final ConfigService service;
    private final H5 h5server = new H5();
    private final Grid<DashboardConfig> grid = new Grid(DashboardConfig.class);

    private final TextField resource = new TextField("Resource");
    private final TextField description = new TextField("Description");
    private final TextField param = new TextField("Param");
    private final DashboardViewUpdateService dashboardViewUpdateService;

    public TrueNASView(ConfigService service, DashboardViewUpdateService dashboardViewUpdateService) {
        this.service = service;
        this.dashboardViewUpdateService = dashboardViewUpdateService;
        VerticalLayout layout = getContent();
        layout.setSizeFull(); // ← ВАЖЛИВО!
        layout.addClassName(Gap.SMALL);
        layout.addClassName(Padding.SMALL);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        layout.setAlignItems(Alignment.STRETCH); // ← Виправлено
        layout.getStyle().set("border", "1px solid lightgray");
        layout.getStyle().set("border-radius", "8px"); // необов’язково — скруглення
        layout.getStyle().set("padding", "10px"); // всередині відступ
        layout.getStyle().set("margin", "10px"); // зовнішній відступ

        HorizontalLayout layoutRow = new HorizontalLayout();

        HorizontalLayout layoutRow3 = new HorizontalLayout();
        Button buttonEdit = new Button();
        buttonEdit.addClickListener(e -> {
            try {
                changeTrueNASConfig();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        Hr hr = new Hr();
        Button buttonAdd = new Button();
        buttonAdd.addSingleClickListener(buttonClickEvent -> {
            addDashboard();
        });
        grid.setSizeFull(); // ← ВАЖЛИВО!
        grid.addColumn(
                new ComponentRenderer<>(Button::new, (button, dashboard) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON,
                            ButtonVariant.LUMO_ERROR,
                            ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> removeDashboard(dashboard));
                    button.setIcon(new Icon(VaadinIcon.TRASH));
                })
        ).setHeader("Manage");

        getContent().addClassName(Gap.SMALL);
        getContent().addClassName(Padding.SMALL);
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        getContent().setJustifyContentMode(JustifyContentMode.START);
        getContent().setAlignItems(Alignment.START);

        layoutRow.setWidthFull();
        getContent().setFlexGrow(1.0, layoutRow);
        layoutRow.addClassName(Gap.SMALL);
        layoutRow.addClassName(Padding.SMALL);
        layoutRow.setWidth("100%");
        layoutRow.setHeight("min-content");
        layoutRow.setAlignItems(Alignment.START);
        layoutRow.setJustifyContentMode(JustifyContentMode.START);

        h5server.setText("TrueNAS Server: " + service.getTrueNASConfig().getServer());
        layoutRow.setAlignSelf(FlexComponent.Alignment.CENTER, h5server);
        h5server.setWidth("400px");
        layoutRow3.setHeightFull();
        layoutRow.setFlexGrow(1.0, layoutRow3);
        layoutRow3.addClassName(Gap.SMALL);
        layoutRow3.addClassName(Padding.SMALL);
        layoutRow3.setWidth("100%");
        layoutRow3.getStyle().set("flex-grow", "1");
        layoutRow3.setAlignItems(Alignment.START);
        layoutRow3.setJustifyContentMode(JustifyContentMode.END);
        buttonEdit.setText("Edit");
        buttonEdit.setWidth("min-content");
        buttonEdit.setHeight("32px");
        buttonEdit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        hr.setWidth("100%");
        buttonAdd.setText("Add");
        buttonAdd.setWidth("min-content");
        buttonAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        grid.setWidth("100%");
        grid.setHeight("100%");
        grid.setItems(service.getDashboardConfig());
        getContent().add(layoutRow);
        layoutRow.add(h5server);
        layoutRow.add(layoutRow3);
        layoutRow3.add(buttonEdit);
        getContent().add(hr);
        getContent().add(buttonAdd);
        getContent().add(grid);

    }

    private void addDashboard() {
        Dialog addDashboardDialog = new Dialog();
        addDashboardDialog.setHeaderTitle("Change TrueNAS config");
        addDashboardDialog.add(addDashboardTrueNASConfigLayout());
        Button addDashboardTrueNASConfigButton = addDashboardTrueNASConfigButton(addDashboardDialog, service.getTrueNASConfig());
        Button cancelButtonChangePassword = new Button("Cancel", e -> addDashboardDialog.close());
        addDashboardDialog.getFooter().add(cancelButtonChangePassword);
        addDashboardDialog.getFooter().add(addDashboardTrueNASConfigButton);
        addDashboardDialog.open();

    }

    private Button addDashboardTrueNASConfigButton(Dialog dialog, TrueNASConfig trueNASConfig) {
        Button saveButton = new Button("Save", e -> {
            Config config = service.getConfig();
            List<DashboardConfig> dashboards = config.getDashboard();
            DashboardConfig dashboardConfig = new DashboardConfig();
            dashboardConfig.setUrl("https://" + trueNASConfig.getServer() + resource.getValue());
            dashboardConfig.setDescription(description.getValue());
            dashboardConfig.setParam(param.getValue());
            dashboards.add(dashboardConfig);
            service.saveConfig(config);
            dialog.close();
            grid.setItems(service.getDashboardConfig());
            dashboardViewUpdateService.addItem("Новий елемент: " + Instant.now());
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        return saveButton;
    }

    private VerticalLayout addDashboardTrueNASConfigLayout() {
        VerticalLayout dialogLayout = new VerticalLayout(resource, description, param);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");
        return dialogLayout;
    }

    private void removeDashboard(DashboardConfig dashboard) {
        List<DashboardConfig> dashboards = service.getConfig().getDashboard();
        dashboards.remove(dashboard);
        service.saveConfig(service.getConfig());
        grid.setItems(service.getDashboardConfig());
        dashboardViewUpdateService.addItem("Новий елемент: " + Instant.now());
    }

    private void changeTrueNASConfig() throws Exception {
        Dialog changeTrueNASConfigDialog = new Dialog();
        changeTrueNASConfigDialog.setHeaderTitle("Change TrueNAS config");
        changeTrueNASConfigDialog.add(changeTrueNASConfigLayout());
        Button changeTrueNASConfigButton = changeTrueNASConfigButton(changeTrueNASConfigDialog, service.getTrueNASConfig());
        Button cancelButtonChangePassword = new Button("Cancel", e -> changeTrueNASConfigDialog.close());
        changeTrueNASConfigDialog.getFooter().add(cancelButtonChangePassword);
        changeTrueNASConfigDialog.getFooter().add(changeTrueNASConfigButton);
        changeTrueNASConfigDialog.open();
    }

    private Button changeTrueNASConfigButton(Dialog dialog, TrueNASConfig trueNASConfig) {
        Button saveButton = new Button("Save", e -> {
            trueNASConfig.setServer(server.getValue());
            trueNASConfig.setLogin(login.getValue());

            try {
                trueNASConfig.setHashedPassword(AESCryptoUtil.encrypt(passwordField.getValue()));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            service.saveConfig(service.getConfig());
            dialog.close();
            h5server.setText("TrueNAS Server: " + service.getTrueNASConfig().getServer());
            dashboardViewUpdateService.addItem("Новий елемент: " + Instant.now());
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        return saveButton;
    }

    private VerticalLayout changeTrueNASConfigLayout() throws Exception {
        server.setValue(service.getTrueNASConfig().getServer());
        login.setValue(service.getTrueNASConfig().getLogin());
        String hashedPassword = service.getTrueNASConfig().getHashedPassword();
        //System.out.println("hashed password: >>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+hashedPassword);
        if (hashedPassword != null || !hashedPassword.isEmpty()) {
            passwordField.setValue(AESCryptoUtil.decrypt(service.getTrueNASConfig().getHashedPassword()));
        }
        VerticalLayout dialogLayout = new VerticalLayout(server, login, passwordField);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");
        return dialogLayout;
    }
}
