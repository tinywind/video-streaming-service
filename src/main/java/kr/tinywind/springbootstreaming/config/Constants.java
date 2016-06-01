package kr.tinywind.springbootstreaming.config;

/**
 * Created by tinywind on 2016-06-01.
 */
public class Constants {
    public static final String VIDEO_ROOT_DIR_PATH;  //동영상 파일이 저장된 디렉토리 경로

    static {
        VIDEO_ROOT_DIR_PATH = System.getProperty("video.root.dir.path");
    }
}
