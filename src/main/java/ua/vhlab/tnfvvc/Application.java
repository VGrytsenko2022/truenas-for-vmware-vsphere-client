package ua.vhlab.tnfvvc;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@PageTitle("TrueNAS for VMware vSphere Client")
@Theme(value = "truenas-for-vmware-vsphere-client", variant = Lumo.DARK)
@Push(PushMode.MANUAL)
public class Application implements AppShellConfigurator {

    public static void main(String[] args) throws UnknownHostException {
        SpringApplication.run(Application.class, args);
        String ip = InetAddress.getLocalHost().getHostAddress();
        System.out.println("Application running at: https://" + ip + ":8443/");
    }
}
