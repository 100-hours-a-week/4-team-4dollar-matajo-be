package org.ktb.matajo.service.location;

import org.ktb.matajo.entity.LocationInfo;
import java.util.List;

public interface LocationInfoService {
    
    /**
     * 동이름과 구이름으로 위치 정보를 검색합니다.
     * @param dongName 동 이름
     * @param guName 구 이름
     * @return 검색된 위치 정보 목록
     */
    List<LocationInfo> findLocationInfo(String dongName, String guName);

    /**
     * 검색어로 위치를 검색합니다.
     * @param searchTerm 검색어
     * @return 검색된 주소 문자열 목록
     */
    List<String> searchLocations(String searchTerm);
}