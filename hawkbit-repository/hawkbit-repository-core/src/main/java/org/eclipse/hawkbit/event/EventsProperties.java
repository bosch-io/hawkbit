package org.eclipse.hawkbit.event;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("org.eclipse.hawkbit.events")
public class EventsProperties {
    private boolean remoteEnabled = false;
    private String remoteFanoutChannelOut = "fanoutChannel-out-0";
    private String remoteGroupedChannelOut = "groupedChannel-out-0";
}
