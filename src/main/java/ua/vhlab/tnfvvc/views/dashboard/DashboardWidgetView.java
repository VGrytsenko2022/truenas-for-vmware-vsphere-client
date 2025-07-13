package ua.vhlab.tnfvvc.views.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import ua.vhlab.tnfvvc.data.config.DashboardConfig;
import ua.vhlab.tnfvvc.data.config.TrueNASConfig;
import ua.vhlab.tnfvvc.util.AESCryptoUtil;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@PermitAll
public class DashboardWidgetView extends Composite<VerticalLayout> {

    private final ProgressBar progressBar = new ProgressBar();
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TreeGrid<TreeNode> treeGrid = new TreeGrid<>();
    private static final Logger logger = LoggerFactory.getLogger(DashboardWidgetView.class);

    public DashboardWidgetView(DashboardConfig dashboardConfig, TrueNASConfig trueNASConfig, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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
        Div widgetContent = createWidgetContent();
        widgetContent.setWidth("100%");
        widgetContent.getStyle().set("overflow-x", "auto");
        getContent().add(widgetContent);
        progressBar.setIndeterminate(true);
        treeGrid.setSizeFull();
        UI ui = UI.getCurrent();
        CompletableFuture.runAsync(() -> {
            try {
                String body = getAPIResponseBody(dashboardConfig.getUrl(), trueNASConfig.getLogin(), AESCryptoUtil.decrypt(trueNASConfig.getHashedPassword()));

                List<TreeNode> rootNodes = new ArrayList<>();
                handleApiResponse(body, rootNodes);

                if (ui != null) {
                    ui.access(() -> {
                        treeGrid.removeAllColumns();
                        treeGrid.addHierarchyColumn(TreeNode::getKey).setHeader("Key").setWidth("300px") // або більше, залежно від очікуваного JSON
                                .setFlexGrow(0)
                                .setResizable(true);
                        treeGrid.addColumn(TreeNode::getValue).setHeader("Value").setHeader("Value");

                        treeGrid.setDataProvider(new TreeNodeDataProvider(rootNodes));
                        //treeGrid.getColumns().forEach(col -> col.setResizable(true));

                        progressBar.setIndeterminate(false);
                        ui.push();
                    });
                }
            } catch (Exception e) {
                logger.error("Failed to load API info", e);
                if (ui != null) {
                    ui.access(() -> Notification.show("Error: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR));
                }
            }
        });
    }

    private String getAPIResponseBody(String url, String username, String password) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        String auth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + auth);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Failed to fetch data: " + response.getStatusCode());
        }
        return response.getBody();
    }

    private void handleApiResponse(String body, List<TreeNode> root) throws Exception {
        Object parsed = objectMapper.readValue(body, Object.class);

        if (parsed instanceof Map<?, ?> map) {
            processJson((Map<String, Object>) map, null, root);
        } else if (parsed instanceof List<?> list) {
            
            for (int i = 0; i < list.size(); i++) {
                TreeNode node = new TreeNode("Item "  + i, "", null);
                root.add(node);
                Object item = list.get(i);
                if (item instanceof Map<?, ?> mapItem) {
                    processJson((Map<String, Object>) mapItem, node, root);
                } else {
                    TreeNode leaf = new TreeNode("Value", item != null ? item.toString() : "null", node);
                    node.getChildren().add(leaf);
                }
            }
        } else {
            TreeNode node = new TreeNode("Value", parsed != null ? parsed.toString() : "null", null);
            root.add(node);
        }
    }

    private void processJson(Map<String, Object> jsonMap, TreeNode parent, List<TreeNode> root) {
        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                continue; // ⛔ пропускаємо null значення повністю
            }

            String stringValue;
            if ("$date".equals(key) && value instanceof Number) {
                long millis = ((Number) value).longValue();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
                stringValue = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(formatter);
            } else if (value instanceof Map || value instanceof List) {
                stringValue = ""; // ❌ не показуємо Map/List як текст
            } else {
                stringValue = value.toString();
            }

            TreeNode node = new TreeNode(key, stringValue, parent);
            if (parent != null) {
                parent.getChildren().add(node);
            } else {
                root.add(node);
            }

            // рекурсивна обробка
            switch (value) {
                case Map<?, ?> mapValue ->
                    processJson((Map<String, Object>) mapValue, node, root);
                case List<?> listValue ->
                    processList(listValue, node, root);
                default -> {
                }
            }
        }
    }

    private void processList(List<?> list, TreeNode parent, List<TreeNode> root) {
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            TreeNode arrayNode = new TreeNode("Element " + i, "", parent);
            parent.getChildren().add(arrayNode);

            if (item instanceof Map<?, ?> mapItem) {
                processJson((Map<String, Object>) mapItem, arrayNode, root);
            } else if (item instanceof List<?> nestedList) {
                processList(nestedList, arrayNode, root);
            } else {
                TreeNode leaf = new TreeNode("Value", item != null ? item.toString() : "null", arrayNode);
                arrayNode.getChildren().add(leaf);
            }
        }
    }

    private Div createWidgetContent() {

        Div content = new Div(progressBar, treeGrid);
        content.setSizeFull();
        content.setWidth("100%");
        content.getStyle().set("overflow-x", "auto");
        content.setClassName("dashboard-widget-content");
        return content;
    }

    public static class TreeNode {

        private final String key;
        private final String value;
        private final TreeNode parent;
        private final List<TreeNode> children = new ArrayList<>();

        public TreeNode(String key, String value, TreeNode parent) {
            this.key = key;
            this.value = value;
            this.parent = parent;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public List<TreeNode> getChildren() {
            return children;
        }

        public TreeNode getParent() {
            return parent;
        }
    }

    public class TreeNodeDataProvider implements HierarchicalDataProvider<TreeNode, String> {

        private final List<TreeNode> rootNodes;

        public TreeNodeDataProvider(List<TreeNode> rootNodes) {
            this.rootNodes = rootNodes;
        }

        @Override
        public int getChildCount(HierarchicalQuery<TreeNode, String> query) {
            TreeNode parent = query.getParent();
            return (parent != null) ? parent.getChildren().size() : rootNodes.size();
        }

        @Override
        public Stream<TreeNode> fetchChildren(HierarchicalQuery<TreeNode, String> query) {
            TreeNode parent = query.getParent();
            return (parent != null) ? parent.getChildren().stream() : rootNodes.stream();
        }

        @Override
        public boolean hasChildren(TreeNode item) {
            return !item.getChildren().isEmpty();
        }

        @Override
        public boolean isInMemory() {
            return true;
        }

        @Override
        public void refreshItem(TreeNode item) {
        }

        @Override
        public void refreshAll() {
        }

        @Override
        public Registration addDataProviderListener(DataProviderListener<TreeNode> listener) {
            return null;
        }
    }
}
