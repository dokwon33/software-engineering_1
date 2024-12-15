package UOSense.UOSense_Backend.service;

import UOSense.UOSense_Backend.dto.ReportRequest;
import UOSense.UOSense_Backend.dto.ReportResponse;

import java.util.List;

public interface ReportService {
    void register(ReportRequest reportRequest, int userId);
    List<ReportResponse> findList();
}
