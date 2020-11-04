package gateway;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class FilterAndRouterInitializer {
    // 是否要启动过滤器
    private static boolean filterStatus = false;
    // 把filter.yml的routes缓存到这里
    private static Map<String, String> routers = new HashMap<>();
    // 需要做 拦截的路由
    private static List<String> secretRoutes = new ArrayList<>();
    //
    private static String defaultHost;

    public String getDefaultHost() {
        return defaultHost;
    }

    public void setDefaultHost(String defaultHost) {
        FilterAndRouterInitializer.defaultHost = defaultHost;
    }

    public List<String> getSecretRoutes() {
        return secretRoutes;
    }

    public boolean isFilterStatus() {
        return filterStatus;
    }

    public void setFilterStatus(boolean filterStatus) {
        this.filterStatus = filterStatus;
    }

    public Map<String, String> getRoutes() {
        return routers;
    }

    public void setRoutes(Map<String, String> routers) {
        this.routers = routers;
    }

    public void setFilter () {
        File file = new File("src/gateway.yml");

        if (file.exists()) {
            this.setFilterStatus(true);
            Yaml yaml = new Yaml();
            Map<String, Object> filter = null;
            try {
                filter = (Map<String, Object>) yaml.load(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            // 设置默认服务host
            if (filter.containsKey("defaulthost")) {
                this.setDefaultHost((String) filter.get("defaulthost"));
            }
            handleRoutes(filter);
            this.getRoutes().put("default", this.getDefaultHost());
        } else {
            this.setFilterStatus(false);
        }
    }

    private void handleRoutes(Map<String, Object> filter) {
        if (filter.containsKey("routers")) {
            Map<String, Object> routers = (Map<String, Object>) filter.get("routers");
            routers.forEach((key, value) -> {
                value = (Map<String, Object>) value;
                // 映射的路由及请求的服务
                this.getRoutes().put(key, ((Map<String, String>) value).get("host"));
                // 需要验证的路由地址
                if (((Map<?, ?>) value).containsKey("secret")) {
                    this.getSecretRoutes().add(key);
                }
            });
        }
    }

}
