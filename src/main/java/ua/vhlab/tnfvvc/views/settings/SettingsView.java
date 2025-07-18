package ua.vhlab.tnfvvc.views.settings;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.ObjectProvider;
import jakarta.annotation.security.RolesAllowed;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Settings")
@Route("settings")
@Menu(order = 1, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@RolesAllowed("ADMIN")
public class SettingsView extends Composite<VerticalLayout> {

    public SettingsView(ObjectProvider<PluginView> pluginViewProvider,
            ObjectProvider<UserView> userViewProvider,
            ObjectProvider<TrueNASView> trueNASViewProvider) {

        VerticalLayout layout = getContent();
        layout.setSizeFull();
        layout.addClassName(LumoUtility.Gap.SMALL);
        layout.addClassName(LumoUtility.Padding.MEDIUM);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.addClassName(LumoUtility.Gap.SMALL);
        tabSheet.addClassName(LumoUtility.Padding.MEDIUM);
        tabSheet.addThemeVariants(TabSheetVariant.LUMO_BORDERED);

        PluginView pluginView = pluginViewProvider.getObject();
        UserView userView = userViewProvider.getObject();
        TrueNASView trueNASView = trueNASViewProvider.getObject();

        tabSheet.add("Plugin", pluginView);
        tabSheet.add("Users", userView);
        tabSheet.add("TrueNAS", trueNASView);

        tabSheet.addSelectedChangeListener(event -> {
            String selectedLabel = event.getSelectedTab().getLabel();

            if ("TrueNAS".equals(selectedLabel)) {
                trueNASView.onTabActivated();
            } else if ("Users".equals(selectedLabel)) {
                userView.onTabActivated();
            }else if ("Plugin".equals(selectedLabel)) {
                pluginView.onTabActivated();
            }
        });

        layout.add(tabSheet);
    }

}
