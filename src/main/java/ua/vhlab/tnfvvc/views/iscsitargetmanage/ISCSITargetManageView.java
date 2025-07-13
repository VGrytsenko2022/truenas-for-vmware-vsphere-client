package ua.vhlab.tnfvvc.views.iscsitargetmanage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import java.net.URLEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import ua.vhlab.tnfvvc.data.iscsitargets.Dataset;
import ua.vhlab.tnfvvc.services.ConfigService;
import ua.vhlab.tnfvvc.util.AESCryptoUtil;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@PageTitle("iSCSI Target Manage")
@Route("iSCSITargetManage")
@Menu(order = 2, icon = LineAwesomeIconUrl.ANCHOR_SOLID)
@RolesAllowed("ADMIN")
public class ISCSITargetManageView extends Composite<VerticalLayout> {

    private final RestTemplate restTemplate;
    private final ConfigService service;
    private static final Logger logger = LoggerFactory.getLogger(ISCSITargetManageView.class);
    private final TreeGrid<Dataset> treeGrid = new TreeGrid<>();
    private final ProgressBar progressBar = new ProgressBar();
    private final UI ui = UI.getCurrent();

    public ISCSITargetManageView(RestTemplate restTemplate, ConfigService service) {
        this.restTemplate = restTemplate;
        this.service = service;

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

        Button buttonAdd = new Button("Add");
        buttonAdd.setWidth("min-content");
        buttonAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonAdd.addClickListener(e -> addISCSITarget());

        treeGrid.setWidthFull();
        treeGrid.addHierarchyColumn(ds -> "[" + ds.type + "] " + ds.name).setHeader("Dataset");

        treeGrid.addColumn(ds -> {
            try {
                if ("VOLUME".equals(ds.type) && ds.volsize != null && ds.volsize.rawvalue != null) {
                    long bytes = Long.parseLong(ds.volsize.rawvalue);
                    return String.format("%.2f GB", bytes / 1_073_741_824.0);
                }
                if ("FILESYSTEM".equals(ds.type) && ds.used != null && ds.used.rawvalue != null && ds.available != null && ds.available.rawvalue != null) {
                    long used = Long.parseLong(ds.used.rawvalue);
                    long available = Long.parseLong(ds.available.rawvalue);
                    return String.format("%.2f GB", (used + available) / 1_073_741_824.0);
                }
            } catch (Exception ignored) {
            }
            return "";
        }).setHeader("Size");

        treeGrid.addColumn(ds -> {
            try {
                if (ds.used != null && ds.used.rawvalue != null) {
                    long used = Long.parseLong(ds.used.rawvalue);
                    return String.format("%.2f GB", used / 1_073_741_824.0);
                }
            } catch (Exception ignored) {
            }
            return "";
        }).setHeader("Used");

        treeGrid.addColumn(ds -> {
            try {
                if (ds.available != null && ds.available.rawvalue != null) {
                    long available = Long.parseLong(ds.available.rawvalue);
                    return String.format("%.2f GB", available / 1_073_741_824.0);
                }
            } catch (Exception ignored) {
            }
            return "";
        }).setHeader("Available");

        treeGrid.addComponentColumn(ds -> {
            if ("VOLUME".equals(ds.type)) {
                Button expand = new Button("Expand");
                expand.addClickListener(e -> openExpandWizard(ds));
                return expand;
            }
            return new Span(); // Пусто для інших типів
        }).setHeader("Actions");

        layout.add(buttonAdd, progressBar, treeGrid);
        refreshTreeGrid();
    }

    private void openExpandWizard(Dataset ds) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Expand VOLUME: " + ds.name);

        final double[] currentSizeGB = {0};
        if (ds.volsize != null && ds.volsize.rawvalue != null) {
            try {
                currentSizeGB[0] = Long.parseLong(ds.volsize.rawvalue) / 1_073_741_824.0;
            } catch (Exception ignored) {
            }
        }

        NumberField sizeField = new NumberField("New Size (GB)");
        sizeField.setValue(currentSizeGB[0]);
        sizeField.setMin(currentSizeGB[0]);
        sizeField.setStep(1);
        sizeField.setSuffixComponent(new Span("GB"));

        Button expandBtn = new Button("Expand");
        expandBtn.setEnabled(false);

        sizeField.addValueChangeListener(e -> {
            Double newSize = e.getValue();
            expandBtn.setEnabled(newSize != null && newSize > currentSizeGB[0]);
        });

        expandBtn.addClickListener(e -> {
            dialog.close();
            long newSizeBytes = ((long) (sizeField.getValue() * 1024 * 1024 * 1024)) & ~0xFFF;

            CompletableFuture.runAsync(() -> {
                try {
                    Map<String, Object> update = new HashMap<>();
                    update.put("volsize", newSizeBytes);

                    sendPostRequest(
                            "/api/v2.0/pool/dataset/id/" + URLEncoder.encode(ds.name, StandardCharsets.UTF_8),
                            update
                    );

                    UI.getCurrent().access(() -> {
                        Notification.show("Volume resized successfully", 4000, Notification.Position.TOP_CENTER)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        refreshTreeGrid();
                    });
                } catch (Exception ex) {
                    logger.error("Failed to expand VOLUME", ex);
                    UI.getCurrent().access(() -> Notification
                            .show("Failed to resize: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR));
                }
            });
        });

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        dialog.add(sizeField);
        dialog.getFooter().add(new HorizontalLayout(cancelBtn, expandBtn));
        dialog.open();
    }

    private void addISCSITarget() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Create iSCSI ZVOL");

        TextField zvolName = new TextField("ZVOL Name");
        NumberField zvolSize = new NumberField("Size (GB)");
        zvolSize.setMin(0.1);
        zvolSize.setStep(0.1);
        zvolSize.setClearButtonVisible(true);
        zvolSize.setErrorMessage("Please enter a valid size greater than 0");

        zvolSize.addValueChangeListener(e -> {
            Double val = e.getValue();
            zvolSize.setInvalid(val == null || val <= 0);
        });

        Select<String> filesystemSelect = new Select<>();
        filesystemSelect.setLabel("Filesystem (Pool)");

        Select<LabeledId> initiatorSelect = new Select<>();
        initiatorSelect.setLabel("iSCSI Initiator");
        initiatorSelect.setItemLabelGenerator(LabeledId::toString);

        Select<LabeledId> portalSelect = new Select<>();
        portalSelect.setLabel("iSCSI Portal");
        portalSelect.setItemLabelGenerator(LabeledId::toString);

        Label availableInfo = new Label("Available: -");
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setIndeterminate(true);
        loadingBar.setVisible(false);

        Button createBtn = new Button("Create");
        createBtn.setEnabled(false);

        FormLayout formLayout = new FormLayout(zvolName, zvolSize, filesystemSelect, availableInfo, initiatorSelect, portalSelect);
        dialog.add(formLayout, loadingBar);

        Runnable validateForm = () -> {
            boolean valid = zvolName.getValue() != null && !zvolName.getValue().isBlank()
                    && zvolSize.getValue() != null && zvolSize.getValue() > 0
                    && filesystemSelect.getValue() != null
                    && initiatorSelect.getValue() != null
                    && portalSelect.getValue() != null;

            String text = availableInfo.getText();
            if (text.startsWith("Available: ")) {
                try {
                    double available = Double.parseDouble(text.substring(11, text.indexOf(" GB")));
                    valid &= zvolSize.getValue() <= available;
                } catch (Exception ignored) {
                    valid = false;
                }
            } else {
                valid = false;
            }

            createBtn.setEnabled(valid);
        };

        zvolName.addValueChangeListener(e -> validateForm.run());
        zvolSize.addValueChangeListener(e -> validateForm.run());
        filesystemSelect.addValueChangeListener(e -> validateForm.run());
        initiatorSelect.addValueChangeListener(e -> validateForm.run());
        portalSelect.addValueChangeListener(e -> validateForm.run());

        // Load pools
        CompletableFuture.runAsync(() -> {
            try {
                String body = getAPIResponseBody(
                        "https://" + service.getTrueNASConfig().getServer() + "/api/v2.0/pool/dataset",
                        service.getTrueNASConfig().getLogin(),
                        AESCryptoUtil.decrypt(service.getTrueNASConfig().getHashedPassword())
                );

                ObjectMapper mapper = new ObjectMapper();
                List<Dataset> rootDatasets = mapper.readValue(body, new TypeReference<>() {
                });
                List<String> pools = rootDatasets.stream()
                        .filter(ds -> "FILESYSTEM".equals(ds.type) && ds.name != null && !ds.name.contains("/"))
                        .map(ds -> ds.name)
                        .collect(Collectors.toList());

                ui.access(() -> {
                    filesystemSelect.setItems(pools);
                    if (!pools.isEmpty()) {
                        filesystemSelect.setValue(pools.get(0));
                    }
                    loadingBar.setVisible(false);
                    ui.push();
                });

            } catch (Exception e) {
                logger.error("Failed to load Filesystem pools", e);
                ui.access(() -> {
                    Notification.show("Failed to load Filesystem pools: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    ui.push();
                    loadingBar.setVisible(false);
                });
            }
        });

        filesystemSelect.addValueChangeListener(event -> {
            String fs = event.getValue();
            if (fs == null || fs.isBlank()) {
                return;
            }

            loadingBar.setVisible(true);
            CompletableFuture.runAsync(() -> {
                try {
                    String body = getAPIResponseBody(
                            "https://" + service.getTrueNASConfig().getServer() + "/api/v2.0/pool/dataset",
                            service.getTrueNASConfig().getLogin(),
                            AESCryptoUtil.decrypt(service.getTrueNASConfig().getHashedPassword())
                    );
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node = mapper.readTree(body);

                    Optional<JsonNode> match = StreamSupport.stream(node.spliterator(), false)
                            .filter(obj -> obj.has("name") && obj.has("type")
                            && "FILESYSTEM".equals(obj.get("type").asText())
                            && fs.equals(obj.get("name").asText()))
                            .findFirst();

                    long available = match.map(n -> n.get("available").get("rawvalue").asLong()).orElse(0L);
                    double availableGB = available / 1_073_741_824.0;

                    ui.access(() -> {
                        availableInfo.setText("Available: " + String.format("%.2f GB", availableGB));
                        loadingBar.setVisible(false);
                        validateForm.run();
                        ui.push();
                    });

                } catch (Exception e) {
                    ui.access(() -> {
                        loadingBar.setVisible(false);
                        Notification.show("Error fetching available space: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        ui.push();
                    });
                }
            });
        });

        // Load initiators
        CompletableFuture.runAsync(() -> {
            try {
                String body = getAPIResponseBody(
                        "https://" + service.getTrueNASConfig().getServer() + "/api/v2.0/iscsi/initiator",
                        service.getTrueNASConfig().getLogin(),
                        AESCryptoUtil.decrypt(service.getTrueNASConfig().getHashedPassword())
                );
                ObjectMapper mapper = new ObjectMapper();
                List<JsonNode> list = mapper.readValue(body, new TypeReference<>() {
                });
                List<LabeledId> initiators = list.stream()
                        .map(n -> new LabeledId(n.get("id").asInt(), n.has("comment") ? n.get("comment").asText() : ""))
                        .collect(Collectors.toList());

                ui.access(() -> {
                    initiatorSelect.setItems(initiators);
                    loadingBar.setVisible(false);
                    ui.push();
                });
            } catch (Exception e) {
                logger.error("Failed to fetch iSCSI initiators", e);
                ui.access(() -> {
                    Notification.show("Failed to fetch iSCSI initiators: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    ui.push();
                    loadingBar.setVisible(false);
                });
            }
        });

        // Load portals
        CompletableFuture.runAsync(() -> {
            try {
                String body = getAPIResponseBody(
                        "https://" + service.getTrueNASConfig().getServer() + "/api/v2.0/iscsi/portal",
                        service.getTrueNASConfig().getLogin(),
                        AESCryptoUtil.decrypt(service.getTrueNASConfig().getHashedPassword())
                );
                ObjectMapper mapper = new ObjectMapper();
                List<JsonNode> list = mapper.readValue(body, new TypeReference<>() {
                });
                List<LabeledId> portals = list.stream()
                        .map(n -> new LabeledId(n.get("id").asInt(), n.has("comment") ? n.get("comment").asText() : ""))
                        .collect(Collectors.toList());

                ui.access(() -> {
                    portalSelect.setItems(portals);
                    loadingBar.setVisible(false);
                    ui.push();
                });
            } catch (Exception e) {
                logger.error("Failed to fetch iSCSI portals", e);
                ui.access(() -> {
                    Notification.show("Failed to fetch iSCSI portals: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    ui.push();
                    loadingBar.setVisible(false);
                });
            }
        });

        createBtn.addClickListener(event -> {

            loadingBar.setVisible(true);

            CompletableFuture.runAsync(() -> {
                try {
                    String pool = filesystemSelect.getValue();
                    String zvol = zvolName.getValue();
                    long size = ((long) (zvolSize.getValue() * 1024 * 1024 * 1024)) & ~0xFFF;

                    // Check if ZVOL already exists
                    String checkBody = getAPIResponseBody(
                            "https://" + service.getTrueNASConfig().getServer() + "/api/v2.0/pool/dataset",
                            service.getTrueNASConfig().getLogin(),
                            AESCryptoUtil.decrypt(service.getTrueNASConfig().getHashedPassword())
                    );
                    ObjectMapper mapper = new ObjectMapper();
                    List<JsonNode> existing = mapper.readValue(checkBody, new TypeReference<>() {
                    });
                    boolean exists = existing.stream().anyMatch(n -> n.has("name") && n.get("name").asText().equals(pool + "/" + zvol));
                    if (exists) {
                        ui.access(() -> Notification.show("ZVOL already exists", 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR));
                        ui.push();
                        return;
                    }

                    // Create ZVOL
                    Map<String, Object> zvolData = new HashMap<>();
                    zvolData.put("name", pool + "/" + zvol);
                    zvolData.put("volsize", size);
                    zvolData.put("type", "VOLUME");
                    sendPostRequest("/api/v2.0/pool/dataset/", zvolData);

                    // Create Extent
                    Map<String, Object> extent = new HashMap<>();
                    extent.put("name", zvol);
                    extent.put("type", "DISK");
                    extent.put("disk", "zvol/" + pool + "/" + zvol);
                    extent.put("blocksize", 512);
                    extent.put("xen", false);
                    extent.put("enabled", true);
                    JsonNode extentResp = sendPostRequest("/api/v2.0/iscsi/extent", extent);
                    int extentId = extentResp.get("id").asInt();

                    // Create Target
                    Map<String, Object> group = new HashMap<>();
                    group.put("portal", portalSelect.getValue().getId());
                    group.put("initiator", initiatorSelect.getValue().getId());

                    Map<String, Object> target = new HashMap<>();
                    target.put("name", zvol);
                    target.put("groups", List.of(group));
                    int targetId = sendPostRequest("/api/v2.0/iscsi/target", target).get("id").asInt();

                    // Link target + extent
                    Map<String, Object> targetExtent = new HashMap<>();
                    targetExtent.put("target", targetId);
                    targetExtent.put("extent", extentId);
                    sendPostRequest("/api/v2.0/iscsi/targetextent", targetExtent);

                    Map<String, Object> payload = new HashMap<>();
                    payload.put("service", "iscsitarget");
                    sendPostRequest("/api/v2.0/service/restart", payload);

                    ui.access(() -> Notification.show("iSCSI target successfully created", 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS));

                    refreshTreeGrid();
                    dialog.close();
                    ui.push();
                } catch (Exception ex) {
                    ui.access(() -> Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR));
                    ui.push();
                    logger.error("Failed to create iSCSI target", ex);
                }
            });
        });

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout(cancelBtn, createBtn);
        dialog.getFooter().add(buttons);
        dialog.open();
    }

    private void refreshTreeGrid() {
        progressBar.setIndeterminate(true);

        CompletableFuture.runAsync(() -> {
            try {
                String body = getAPIResponseBody(
                        "https://" + service.getTrueNASConfig().getServer() + "/api/v2.0/pool/dataset",
                        service.getTrueNASConfig().getLogin(),
                        AESCryptoUtil.decrypt(service.getTrueNASConfig().getHashedPassword())
                );

                ObjectMapper mapper = new ObjectMapper();
                List<Dataset> rootDatasets = mapper.readValue(body, new TypeReference<>() {
                });

                List<Dataset> topLevel = rootDatasets.stream()
                        .filter(ds -> ds.name != null && !ds.name.contains("/"))
                        .collect(Collectors.toList());

                ui.access(() -> {
                    treeGrid.setItems(topLevel, Dataset::getChildren);
                    progressBar.setIndeterminate(false);
                    ui.push();
                });

            } catch (Exception e) {
                logger.error("Failed to load API info", e);
                ui.access(() -> Notification.show("Error: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR));
                ui.push();
            }
        });
    }

    private String getAPIResponseBody(String url, String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        String auth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + auth);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Failed to fetch data: " + response.getStatusCode());
        }
        return response.getBody();
    }

    private JsonNode sendPostRequest(String path, Map<String, Object> payload) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = Base64.getEncoder().encodeToString((service.getTrueNASConfig().getLogin() + ":"
                + AESCryptoUtil.decrypt(service.getTrueNASConfig().getHashedPassword())).getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + auth);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "https://" + service.getTrueNASConfig().getServer() + path,
                HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException("POST request failed: " + response.getStatusCode());
        }
        try {
            return new ObjectMapper().readTree(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }

    class LabeledId {

        private final int id;
        private final String label;

        public LabeledId(int id, String label) {
            this.id = id;
            this.label = label;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
