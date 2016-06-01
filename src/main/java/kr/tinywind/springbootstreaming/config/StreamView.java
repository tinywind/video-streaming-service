package kr.tinywind.springbootstreaming.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Map;

import static kr.tinywind.springbootstreaming.config.Constants.VIDEO_ROOT;

@Component("streamView")
public class StreamView extends AbstractView {
    private static final Logger logger = LoggerFactory.getLogger(StreamView.class);

    @Override
    protected void renderMergedOutputModel(Map<String, Object> map, HttpServletRequest request, HttpServletResponse response) throws Exception {
        final File movieFIle = new File(VIDEO_ROOT + (String) map.get("movieName"));
        final RandomAccessFile randomFile = new RandomAccessFile(movieFIle, "r");

        long rangeStart = 0; //요청 범위의 시작 위치
        long rangeEnd = 0; //요청 범위의 끝 위치
        boolean isPart = false; //부분 요청일 경우 true, 전체 요청의 경우 false

        // randomFile 을 클로즈 하기 위하여 try~finally 사용
        try {
            long movieSize = randomFile.length();//동영상 파일 크기
            String range = request.getHeader("range");//스트림 요청 범위, request의 헤더에서 range를 읽는다.
            logger.debug("range: {}", range);

            //브라우저에 따라 range 형식이 다른데, 기본 형식은 "bytes={start}-{end}" 형식이다.
            //range가 null이거나, reqStart가  0이고 end가 없을 경우 전체 요청이다.
            //요청 범위를 구한다.
            if (range != null) {
                if (range.endsWith("-")) {//처리의 편의를 위해 요청 range에 end 값이 없을 경우 넣어줌</font>
                    range = range + (movieSize - 1);
                }
                int idxm = range.trim().indexOf("-");//"-" 위치
                rangeStart = Long.parseLong(range.substring(6, idxm));
                rangeEnd = Long.parseLong(range.substring(idxm + 1));
                if (rangeStart > 0) {
                    isPart = true;
                }
            } else {//range가 null인 경우 동영상 전체 크기로 초기값을 넣어줌. 0부터 시작하므로 -1
                rangeStart = 0;
                rangeEnd = movieSize - 1;
            }

            long partSize = rangeEnd - rangeStart + 1; //전송 파일 크기
            logger.debug("accepted range: {}", rangeStart + "-" + rangeEnd + "/" + partSize + " isPart:" + isPart);
            response.reset();//전송시작
            response.setStatus(isPart ? 206 : 200);// 전체 요청일 경우 200, 부분 요청일 경우 206을 반환상태 코드로 지정
            response.setContentType("video/mp4");// mime type 지정

            // 전송 내용을 헤드에 넣어준다. 마지막에 파일 전체 크기를 넣는다.
            response.setHeader("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + movieSize);
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-Length", "" + partSize);

            OutputStream out = response.getOutputStream();
            randomFile.seek(rangeStart);//동영상 파일의 전송시작 위치 지정

            //파일 전송...  java io는 1회 전송 byte수가 int로 지정됨
            //동영상 파일의 경우 int형으로는 처리 안되는 크기의 파일이 있으므로
            //8kb로 잘라서 파일의 크기가 크더라도 문제가 되지 않도록 구현
            int bufferSize = 8 * 1024;
            byte[] buf = new byte[bufferSize];
            do {
                int block = partSize > bufferSize ? bufferSize : (int) partSize;
                int len = randomFile.read(buf, 0, block);
                out.write(buf, 0, len);
                partSize -= block;
            } while (partSize > 0);
            logger.debug("sent " + movieFIle.getAbsolutePath() + " " + rangeStart + "-" + rangeEnd);
        } catch (IOException e) {
            //전송 중에 브라우저를 닫거나, 화면을 전환한 경우 종료해야 하므로 전송취소.
            //progressBar를 클릭한 경우에는 클릭한 위치값으로 재요청이 들어오므로 전송 취소.
            logger.debug("전송이 취소 되었음");
        } finally {
            randomFile.close();
        }
    }
}