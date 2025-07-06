package ua.vhlab.tnfvvc.views.settings;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vmware.automatic.plugin.registration.PluginRegistrationMain;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;

@RolesAllowed("ADMIN")
public class PluginView extends Composite<VerticalLayout> {
public PluginView(){
    TextField textFieldVCenter = new TextField();
    TextField textFieldUsername = new TextField();
    PasswordField passwordField = new PasswordField();
    TextField textFieldPluginManifestURL = new TextField();
    TextField textFieldVCenterServerThumbprint = new TextField();
    TextField textFieldPluginServerThumbprint = new TextField();
    Button buttonRegisterPlugin = new Button();
    getContent().addClassName(LumoUtility.Gap.SMALL);
    getContent().addClassName(LumoUtility.Padding.SMALL);
    getContent().setWidth("100%");
    getContent().getStyle().set("flex-grow", "1");
    textFieldVCenter.setLabel("vCenter Host");
    getContent().setAlignSelf(FlexComponent.Alignment.CENTER, textFieldVCenter);
    textFieldVCenter.setWidth("300px");
    textFieldUsername.setLabel("Username");
    getContent().setAlignSelf(FlexComponent.Alignment.CENTER, textFieldUsername);
    textFieldUsername.setWidth("300px");
    passwordField.setLabel("Password");
    getContent().setAlignSelf(FlexComponent.Alignment.CENTER, passwordField);
    passwordField.setWidth("300px");
    textFieldPluginManifestURL.setLabel("Plugin Manifest URL");
    getContent().setAlignSelf(FlexComponent.Alignment.CENTER, textFieldPluginManifestURL);
    textFieldPluginManifestURL.setWidth("300px");
    textFieldVCenterServerThumbprint.setLabel("vCenter Server Thumbprint (SHA-1)");
    getContent().setAlignSelf(FlexComponent.Alignment.CENTER, textFieldVCenterServerThumbprint);
    textFieldVCenterServerThumbprint.setWidth("300px");
    textFieldPluginServerThumbprint.setLabel("Plugin Server Thumbprint (SHA-1)");
    getContent().setAlignSelf(FlexComponent.Alignment.CENTER, textFieldPluginServerThumbprint);
    textFieldPluginServerThumbprint.setWidth("300px");
    buttonRegisterPlugin.setText("Register Plugin");
    getContent().setAlignSelf(FlexComponent.Alignment.CENTER, buttonRegisterPlugin);
    buttonRegisterPlugin.setWidth("min-content");
    buttonRegisterPlugin.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    getContent().add(textFieldVCenter);
    getContent().add(textFieldUsername);
    getContent().add(passwordField);
    getContent().add(textFieldPluginManifestURL);
    getContent().add(textFieldVCenterServerThumbprint);
    getContent().add(textFieldPluginServerThumbprint);
    getContent().add(buttonRegisterPlugin);
    buttonRegisterPlugin.addSingleClickListener(buttonClickEvent -> {
        try {
            List<String> args = new ArrayList<>();
            args.add("-action"); args.add("registerPlugin");
            args.add("-url"); args.add("https://" + textFieldVCenter.getValue() + "/sdk");
            args.add("-u"); args.add(textFieldUsername.getValue());
            args.add("-p"); args.add(passwordField.getValue());
            args.add("-pu"); args.add(textFieldPluginManifestURL.getValue());
            args.add("-vct"); args.add(textFieldVCenterServerThumbprint.getValue());
            args.add("-remote");
            args.add("-n"); args.add("TrueNAS for VMware vSphere Client");
            args.add("-v"); args.add("1.0.0");
            args.add("-k"); args.add("ua.vhlab.tnfvvc");
            args.add("-c"); args.add("VHLab");
            args.add("-s"); args.add("Displays summary information about your TrueNAS");

            if (!textFieldPluginServerThumbprint.getValue().isEmpty()) {
                args.add("-st");
                args.add(textFieldPluginServerThumbprint.getValue());
            } else {
                args.add("-insecure");
            }

            PluginRegistrationMain.main(args.toArray(new String[0]));
            Notification.show("Registration complete",5000, Notification.Position.MIDDLE);
        } catch (Exception ex) {
            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    });
}
}
