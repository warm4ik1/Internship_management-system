package org.warm4ik.ims.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

  void saveAvatarToFileDb(MultipartFile file, String logoUrl);

  void updateAvatarInFileDb(MultipartFile file, String oldLogoUrl, String newLogoUrl);

  String generateUrlLogo(MultipartFile file);

  void deleteImgFromFileDb(String oldLogoUrl);

  String getDefaultLogo();
}
