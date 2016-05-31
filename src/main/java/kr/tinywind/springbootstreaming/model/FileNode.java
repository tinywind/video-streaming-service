package kr.tinywind.springbootstreaming.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by tinywind on 2016-05-31.
 */
@Data
@AllArgsConstructor
public class FileNode {
    private String fileName;
    private boolean isFIle;
}
