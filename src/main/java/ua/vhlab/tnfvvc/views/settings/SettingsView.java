package ua.vhlab.tnfvvc.views.settings;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Settings")
@Route("settings")
@Menu(order = 1, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@RolesAllowed("ADMIN")
public class SettingsView extends Composite<VerticalLayout> {

    public SettingsView() {
        Tabs tabs = new Tabs();
        TextField textField = new TextField();
        TextField textField2 = new TextField();
        PasswordField passwordField = new PasswordField();
        TextField textField3 = new TextField();
        TextField textField4 = new TextField();
        TextField textField5 = new TextField();
        Button buttonPrimary = new Button();
        getContent().addClassName(Gap.SMALL);
        getContent().addClassName(Padding.SMALL);
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        tabs.setWidth("100%");
        setTabsSampleData(tabs);
        textField.setLabel("vCenter Host");
        getContent().setAlignSelf(FlexComponent.Alignment.CENTER, textField);
        textField.setWidth("300px");
        textField2.setLabel("Username");
        getContent().setAlignSelf(FlexComponent.Alignment.CENTER, textField2);
        textField2.setWidth("300px");
        passwordField.setLabel("Password");
        getContent().setAlignSelf(FlexComponent.Alignment.CENTER, passwordField);
        passwordField.setWidth("300px");
        textField3.setLabel("Plugin Manifest URL");
        getContent().setAlignSelf(FlexComponent.Alignment.CENTER, textField3);
        textField3.setWidth("300px");
        textField4.setLabel("vCenter Server Thumbprint (SHA-1)");
        getContent().setAlignSelf(FlexComponent.Alignment.CENTER, textField4);
        textField4.setWidth("300px");
        textField5.setLabel("Plugin Server Thumbprint (SHA-1)");
        getContent().setAlignSelf(FlexComponent.Alignment.CENTER, textField5);
        textField5.setWidth("300px");
        buttonPrimary.setText("Register Plugin");
        getContent().setAlignSelf(FlexComponent.Alignment.CENTER, buttonPrimary);
        buttonPrimary.setWidth("min-content");
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getContent().add(tabs);
        getContent().add(textField);
        getContent().add(textField2);
        getContent().add(passwordField);
        getContent().add(textField3);
        getContent().add(textField4);
        getContent().add(textField5);
        getContent().add(buttonPrimary);
    }

    private void setTabsSampleData(Tabs tabs) {
        tabs.add(new Tab("Dashboard"));
        tabs.add(new Tab("Payment"));
        tabs.add(new Tab("Shipping"));
    }
}
