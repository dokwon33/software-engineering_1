package UOSense.UOSense_Backend.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewImageService {
    void register(int reviewId, List<MultipartFile> images);
    List<String> find(int reviewId);
}
