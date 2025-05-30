package org.warm4ik.ims.service.impl;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.warm4ik.ims.exception.custom.FileStorageException;
import org.warm4ik.ims.service.FileStorageService;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

  @Value("${file.storage.url-prefix}")
  private String urlPrefix;

  @Value("${file.storage.dir}")
  private String storageDir;

  @Value("${file.storage.default-logo}")
  private String defaultLogo;

  private File storageRoot;

  @PostConstruct
  public void init() {
    storageRoot = new File(storageDir);

    if (!storageRoot.exists() && !storageRoot.mkdirs()) {
      throw new FileStorageException("Ошибка создания папки");
    }
  }

  private static final Set<String> ALLOWED_MIME_IMG_TYPES =
      Set.of("image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp");

  public void validateImageFile(MultipartFile file) {

    if (file.isEmpty()) {
      throw new FileStorageException("Файл пустой! Ошибка сохранения.");
    }

    String contentType = file.getContentType();

    if (contentType == null || !ALLOWED_MIME_IMG_TYPES.contains(contentType.toLowerCase())) {
      throw new FileStorageException(
          "Недопустимый формат файла. Загружать можно только изображения.");
    }
  }

  public void saveAvatarToFileDb(MultipartFile file, String logoUrl) {

    validateImageFile(file);

    File dest = new File(storageRoot, logoUrl.replaceFirst(urlPrefix, ""));
    ensureSafe(dest);

    try {
      file.transferTo(dest);
    } catch (IOException e) {
      throw new FileStorageException("Ошибка сохранения файла");
    }
  }

  private void ensureSafe(File dest) {
    if (!dest.getParentFile().equals(storageRoot)) {
      throw new FileStorageException("Недопустимый путь");
    }
  }

  public void updateAvatarInFileDb(MultipartFile file, String oldLogoUrl, String newLogoUrl) {

    validateImageFile(file);

    String fileName = newLogoUrl.replaceFirst(urlPrefix, "");
    File dest = new File(storageRoot, fileName);

    File oldDest = new File(storageRoot, oldLogoUrl.replaceFirst(urlPrefix, ""));

    ensureSafe(dest);

    try {
      file.transferTo(dest);
      if (!oldLogoUrl.equals(defaultLogo)) {
        oldDest.delete();
      }
    } catch (IOException e) {
      throw new FileStorageException("Ошибка сохранения файла");
    }
  }

  public String generateUrlLogo(MultipartFile file) {

    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

    return urlPrefix + fileName;
  }

  public void deleteImgFromFileDb(String oldLogoUrl) {
    if (oldLogoUrl.equals(defaultLogo)) {
      return;
    }

    File oldDest = new File(storageRoot, oldLogoUrl.replaceFirst(urlPrefix, ""));
    ensureSafe(oldDest);

    boolean isDeleted = oldDest.delete();

    if (!isDeleted) {
      throw new FileStorageException("Ошибка удаление файла.");
    }
  }

  public String getDefaultLogo() {
    return defaultLogo;
  }
}
