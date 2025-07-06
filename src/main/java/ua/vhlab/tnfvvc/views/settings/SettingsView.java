package ua.vhlab.tnfvvc.views.settings;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
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
    Tab plugin =new Tab("Plugin");
    Tab payment =new Tab("Payment");
    Tab shipping =new Tab("Shipping");
    PluginView pluginView = new PluginView();
    VerticalLayout layoutColumn2 = new VerticalLayout();
    public SettingsView() {

        Tabs tabs = new Tabs();
        setTabsSampleData(tabs);
        tabs.addSelectedChangeListener(
                event -> setContent(event.getSelectedTab()));
        getContent().add(tabs);
        layoutColumn2.add(pluginView);
        getContent().add(layoutColumn2);

    }
    private void setTabsSampleData(Tabs tabs) {
        tabs.add(plugin);
        tabs.add(payment);
        tabs.add(shipping);
    }
    private void setContent(Tab tab) {
        var ui = UI.getCurrent();
        ui.access(() -> {
            layoutColumn2.removeAll();
            if (tab.equals(plugin)) {
                layoutColumn2.add(pluginView);
            }
            ui.push();
        });
    }
}