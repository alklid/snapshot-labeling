package com.alklid.batch.model.data;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SnapshotFile {

    private LocalDateTime dateLabel;
    private Path path;

    @Builder
    public SnapshotFile(LocalDateTime dateLabel, Path path) {
        this.dateLabel = dateLabel;
        this.path = path;
    }

    public static SnapshotFile NoLabelSnapshotFile(Path path) {
        return SnapshotFile.builder().path(path).build();
    }

    public void labeling() {
        this.dateLabel = LocalDateTime.now();
    }
    
}
