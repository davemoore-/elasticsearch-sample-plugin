package org.elasticsearch.plugin.sampleplugin;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.*;

import java.util.List;
import java.util.function.Supplier;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

public class SamplePlugin extends Plugin implements ActionPlugin {

    @Override
    public List<RestHandler> getRestHandlers(final Settings settings,
                                             final RestController restController,
                                             final ClusterSettings clusterSettings,
                                             final IndexScopedSettings indexScopedSettings,
                                             final SettingsFilter settingsFilter,
                                             final IndexNameExpressionResolver indexNameExpressionResolver,
                                             final Supplier<DiscoveryNodes> nodesInCluster) {

        return List.of(new SampleCreateIndexAction(), new SampleSearchAction());
    }
}

class SampleCreateIndexAction extends BaseRestHandler {

    @Override
    public String getName() {
        return "create_sample_index_action";
    }

    @Override
    public List<Route> routes() {
        return List.of(new Route(POST, "/_sample/create"));
    }

    @Override
    protected RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) {
        return channel -> {
            try {
                String mapping = "{\n" +
                    "  \"properties\": {\n" +
                    "    \"foo\": {\n" +
                    "      \"type\": \"keyword\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
                client.admin().indices().prepareCreate("sample-index")
                    .setSettings(Settings.builder()
                            .put("index.number_of_shards", 1)
                            .put("index.number_of_replicas", 0)
                    )
                    .addMapping("doc", mapping, XContentType.JSON)
                    .get();
                XContentBuilder content = XContentFactory.jsonBuilder();
                content.startObject().field("acknowledged", true).endObject();
                channel.sendResponse(new BytesRestResponse(RestStatus.OK, content));
            } catch (final Exception e) {
                channel.sendResponse(new BytesRestResponse(channel, e));
            }
        };
    }
}

class SampleSearchAction extends BaseRestHandler {

    @Override
    public String getName() {
        return "sample_search_action";
    }

    @Override
    public List<Route> routes() {
        return List.of(new Route(GET, "/_sample/search"));
    }

    @Override
    protected RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) {
        return channel -> {
            try {
                client.prepareSearch("*").get();
                XContentBuilder content = XContentFactory.jsonBuilder();
                content.startObject().field("acknowledged", true).endObject();
                channel.sendResponse(new BytesRestResponse(RestStatus.OK, content));
            } catch (final Exception e) {
                channel.sendResponse(new BytesRestResponse(channel, e));
            }
        };
    }
}