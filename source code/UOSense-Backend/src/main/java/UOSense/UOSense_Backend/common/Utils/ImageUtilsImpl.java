package UOSense.UOSense_Backend.common.Utils;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ImageUtilsImpl implements ImageUtils {
    private final AmazonS3 s3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public String uploadImageToS3(MultipartFile file, String folderName) {
        validateFile(file);

        String fileName = generateFileName(file,folderName);
        ObjectMetadata metadata = getMetaDataOf(file);
        try {
            // 퍼블릭 읽기 권한 추가
            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead);
            // S3에 업로드
            s3Client.putObject(request);
        } catch (IOException | SdkClientException e) {
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
        // 업로드된 파일의 S3 URL 반환
        return s3Client.getUrl(bucketName, fileName).toString();
    }

    @Override
    public void deleteImageInS3(String fileName) {
        String key = fileName.substring(61);
        try {
            DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, key);
            s3Client.deleteObject(deleteObjectRequest);
        } catch (SdkClientException e) {
            throw new RuntimeException("스토리지에서 파일 삭제에 실패했습니다.");
        }
    }

    /**
     파일 유효성을 검사합니다.
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB 제한
            throw new IllegalArgumentException("파일 크기가 너무 큽니다.");
        }
    }

    /**
     고유 파일명을 생성합니다.
     */
    private String generateFileName(MultipartFile file, String folderName) {
        String originName = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        return folderName + "/"+uuid + "_" + originName;
    }

    /**
     파일 메타데이터를 설정하고 반환합니다.
     */
    private ObjectMetadata getMetaDataOf(MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        return metadata;
    }
}
