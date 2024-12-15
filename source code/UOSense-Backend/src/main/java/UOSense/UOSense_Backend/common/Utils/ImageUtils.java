package UOSense.UOSense_Backend.common.Utils;

import org.springframework.web.multipart.MultipartFile;

/** 사진 관련 공통 기능 모음. */
public interface ImageUtils {
    /** S3 스토리지 내 주어진 이름의 폴더에 이미지를 저장합니다.
     * @throws RuntimeException
     */
    String uploadImageToS3(MultipartFile file, String folderName);

    /** S3 스토리지 내 주어진 이름의 폴더에 이미지를 저장합니다.
     * @throws RuntimeException
     */
    void deleteImageInS3(String fileName);
}
