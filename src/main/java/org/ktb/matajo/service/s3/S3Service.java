package org.ktb.matajo.service.s3;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

  /**
   * MultipartFile 형식의 이미지를 S3에 업로드하고 URL 반환
   *
   * @param file MultipartFile 형식의 이미지 파일
   * @return 업로드된 이미지의 S3 URL
   */
  String uploadImage(MultipartFile file, String category);

  /**
   * 다수의 MultipartFile 이미지를 S3에 업로드하고 URL 목록 반환
   *
   * @param files MultipartFile 목록
   * @return 업로드된 이미지들의 S3 URL 목록
   */
  List<String> uploadImages(List<MultipartFile> files);

  /**
   * S3에서 이미지 삭제
   *
   * @param imageUrl 삭제할 이미지의 URL
   */
  void deleteImage(String imageUrl);
}
