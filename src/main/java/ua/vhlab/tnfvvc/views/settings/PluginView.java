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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RolesAllowed("ADMIN")
@Component
@Scope("prototype")
public class PluginView extends Composite<VerticalLayout> {

    public PluginView() {
        VerticalLayout layout = getContent();
        layout.setSizeFull(); // ← ВАЖЛИВО!
        layout.addClassName(LumoUtility.Gap.SMALL);
        layout.addClassName(LumoUtility.Padding.SMALL);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH); // ← Виправлено
        layout.getStyle().set("border", "1px solid lightgray");
        layout.getStyle().set("border-radius", "8px"); // необов’язково — скруглення
        layout.getStyle().set("padding", "10px"); // всередині відступ
        layout.getStyle().set("margin", "10px"); // зовнішній відступ

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
        textFieldVCenter.setHelperText("Example:<vcsa-fqdn>");
        getContent().setAlignSelf(FlexComponent.Alignment.CENTER, textFieldVCenter);
        textFieldVCenter.setWidth("450px");
        textFieldUsername.setLabel("Username");
        textFieldUsername.setHelperText("Example:<vCenter SSO-user>");
        getContent().setAlignSelf(FlexComponent.Alignment.CENTER, textFieldUsername);
        textFieldUsername.setWidth("450px");
        passwordField.setLabel("Password");
        passwordField.setHelperText("Example:<vCenter SSO-user password>");
        getContent().setAlignSelf(FlexComponent.Alignment.CENTER, passwordField);
        passwordField.setWidth("450px");
        textFieldPluginManifestURL.setLabel("Plugin Manifest URL");
        textFieldPluginManifestURL.setHelperText("Example:https://<this-app-server-fqdn>/plugin-manifest.json");
        getContent().setAlignSelf(FlexComponent.Alignment.CENTER, textFieldPluginManifestURL);
        textFieldPluginManifestURL.setWidth("450px");
        textFieldVCenterServerThumbprint.setLabel("vCenter Server Thumbprint (SHA-1)");
        textFieldVCenterServerThumbprint.setHelperText("Example:openssl s_client -connect <vcsa-fqdn>:443 -showcerts </dev/null 2>/dev/null | openssl x509 -noout -fingerprint -sha1");
        getContent().setAlignSelf(FlexComponent.Alignment.CENTER, textFieldVCenterServerThumbprint);
        textFieldVCenterServerThumbprint.setWidth("450px");
        textFieldPluginServerThumbprint.setLabel("Plugin Server Thumbprint (SHA-1)");
        textFieldPluginServerThumbprint.setHelperText("Example:openssl s_client -connect <this-app-server-fqdn>:443 -showcerts </dev/null 2>/dev/null | openssl x509 -noout -fingerprint -sha1");
        getContent().setAlignSelf(FlexComponent.Alignment.CENTER, textFieldPluginServerThumbprint);
        textFieldPluginServerThumbprint.setWidth("450px");
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
                args.add("-action");
                args.add("registerPlugin");
                args.add("-url");
                args.add("https://" + textFieldVCenter.getValue() + "/sdk");
                args.add("-u");
                args.add(textFieldUsername.getValue());
                args.add("-p");
                args.add(passwordField.getValue());
                args.add("-pu");
                args.add(textFieldPluginManifestURL.getValue());
                args.add("-vct");
                args.add(textFieldVCenterServerThumbprint.getValue());
                args.add("-remote");
                args.add("-n");
                args.add("TrueNAS for VMware vSphere Client");
                args.add("-v");
                args.add("1.0.0");
                args.add("-k");
                args.add("ua.vhlab.tnfvvc");
                args.add("-c");
                args.add("VHLab");
                args.add("-s");
                args.add("Displays summary information about your TrueNAS");

                if (!textFieldPluginServerThumbprint.getValue().isEmpty()) {
                    args.add("-st");
                    args.add(textFieldPluginServerThumbprint.getValue());
                } else {
                    args.add("-insecure");
                }

                PluginRegistrationMain.main(args.toArray(new String[0]));
                Notification.show("Registration complete", 5000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
    }
}
