package ua.vhlab.tnfvvc.views.about;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import jakarta.annotation.security.PermitAll;

import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("About")
@Route("about")
@Menu(order = 3, icon = LineAwesomeIconUrl.INFO_SOLID)
@PermitAll
public class AboutView extends Composite<VerticalLayout> {
    private final Button donateButton = new Button("Help this project");
    public AboutView() {
        VerticalLayout layout = getContent();
        layout.setSizeFull();
        layout.addClassName(LumoUtility.Gap.SMALL);
        layout.addClassName(LumoUtility.Padding.SMALL);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);
        layout.getStyle().set("border", "1px solid lightgray");
        layout.getStyle().set("border-radius", "8px");
        layout.getStyle().set("padding", "10px");
        layout.getStyle().set("margin", "10px");

        layout.setSpacing(false);

        Image img = new Image("images/PidginBird.png", "placeholder plant");
        img.setWidth("200px");
        layout.add(img);

        H2 header = new H2("TrueNAS for VMware vSphere Client");
        header.addClassNames(Margin.Top.XLARGE, Margin.Bottom.MEDIUM);
        layout.add(header);
        layout.add(new Paragraph("Software developer: Valentyn Hrytsenko, vallico@ukr.net, Ukraine, Kyiv 2025 🤗."));
        layout.add(new Paragraph("GitHub home: https://github.com/VGrytsenko2022/truenas-for-vmware-vsphere-client"));

        donateButton.addClickListener(e -> this.openPaypal());
        layout.add(donateButton);

        layout.setSizeFull();
        layout.setJustifyContentMode(JustifyContentMode.CENTER);
        layout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        layout.getStyle().set("text-align", "center");
    }

    private void openPaypal() {
        getUI().get().getPage().open("https://www.paypal.com/donate/?hosted_button_id=GRQBC554NA356", "_blank");
    }
}
