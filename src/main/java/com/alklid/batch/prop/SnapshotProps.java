package com.alklid.batch.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "snapshot")
@Getter
@Setter
public class SnapshotProps {

    private Integer dateExtractType;
    private String source;
    private String target;

}
