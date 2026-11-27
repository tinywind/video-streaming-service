package kr.tinywind.springbootstreaming.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AnnotationForm {
    @NotBlank
    @Size(max = 1024)
    private String videoName;

    private List<@Valid @NotNull Entry> materialDataList = new ArrayList<>();

    @Getter
    @Setter
    public static class Entry {
        @NotEmpty
        @Size(max = 255)
        private String key;
        private Double timestamp;

        @AssertTrue
        public boolean isTimestampValid() {
            return timestamp != null && Double.isFinite(timestamp) && timestamp >= 0;
        }
    }
}
