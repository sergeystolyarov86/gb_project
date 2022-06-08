package server;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ConnectionMetadata {

    private Map<String, String> metadataParams;
    private final ByteBuffer metadataBuffer = ByteBuffer.allocate(512);

    public Map<String, String> getMetadataParams() {
        return metadataParams;
    }

    public ByteBuffer getMetadataBuffer() {
        return metadataBuffer;
    }

    public boolean isMetadataLoaded() {
        return metadataParams != null;
    }

    public void buildMetadata() {
        Map<String, String> metadataParams = new HashMap<>();
        String rawMetadata = new String(metadataBuffer.array(), StandardCharsets.UTF_8);
        String[] keyValueParamsArray = rawMetadata.split("&");
        for (String rawKeyValue : keyValueParamsArray) {
            String[] keyValueArr = rawKeyValue.split("=");
            metadataParams.put(keyValueArr[0], keyValueArr[1]);
        }

        if (!metadataParams.isEmpty()) {
            this.metadataParams = metadataParams;
        }
    }

}

