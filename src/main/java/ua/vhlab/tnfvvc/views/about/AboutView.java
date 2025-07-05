package ua.vhlab.tnfvvc.views.about;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("About")
@Route("about")
@Menu(order = 2, icon = LineAwesomeIconUrl.INFO_SOLID)
@AnonymousAllowed
public class AboutView extends Composite<VerticalLayout> {

    public AboutView() {
        getContent().addClassName(Gap.SMALL);
        getContent().addClassName(Padding.SMALL);
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }
}
