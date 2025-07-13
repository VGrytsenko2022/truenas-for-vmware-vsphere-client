package ua.vhlab.tnfvvc.views.dashboard;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.client.RestTemplate;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import ua.vhlab.tnfvvc.data.config.DashboardConfig;
import ua.vhlab.tnfvvc.services.ConfigService;
import ua.vhlab.tnfvvc.services.DashboardViewUpdateService;

import java.util.List;

import static com.vaadin.hilla.ApplicationContextProvider.getApplicationContext;

@PageTitle("Dashboard")
@Route("")
@Menu(order = 0, icon = LineAwesomeIconUrl.FILE_MEDICAL_ALT_SOLID)
@PermitAll

public class DashboardView extends Composite<VerticalLayout> {

    private final Runnable refreshCallback;
    private final ConfigService service;
    private final TabSheet tabSheet = new TabSheet();
    private final RestTemplate restTemplate;

    public DashboardView(ConfigService service, DashboardViewUpdateService dashboardViewUpdateService, RestTemplate restTemplate) {
        this.service = service;
        this.restTemplate = restTemplate;
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

        tabSheet.setSizeFull();
        tabSheet.addClassName(LumoUtility.Gap.SMALL);
        tabSheet.addClassName(LumoUtility.Padding.MEDIUM);
        getContent().add(tabSheet);
        refresh();

        refreshCallback = () -> {
            UI ui = getUI().orElse(null);
            if (ui != null) {
                ui.access(this::refresh);
            }
        };

        dashboardViewUpdateService.registerListener(refreshCallback);

    }

    private void refresh() {

        tabSheet.getChildren().forEach(component -> {
            // Делай что нужно с каждым component
            tabSheet.remove(component);
        });
        List<DashboardConfig> dashboardConfigs = service.getDashboardConfig();
        for (DashboardConfig dashboardConfig : dashboardConfigs) {
            //System.out.println("Dashboard config: >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + dashboardConfig.getDescription());
            // dashboard.add(new DashboardWidgetView(dashboardConfig,service.getTrueNASConfig(),restTemplate));
            tabSheet.add(dashboardConfig.getDescription(), new DashboardWidgetView(dashboardConfig, service.getTrueNASConfig(), restTemplate));

        }
        UI.getCurrent().push();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        DashboardViewUpdateService ds = getApplicationContext().getBean(DashboardViewUpdateService.class);
        ds.unregisterListener(refreshCallback);
    }

}
