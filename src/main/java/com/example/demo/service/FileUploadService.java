package com.example.demo.service;

import com.example.demo.model.FileDetails;
import com.example.demo.payload.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.data.util.Pair;

import java.io.FileNotFoundException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface FileUploadService {
  FileUploadResponse uploadFile(MultipartFile file) throws IOException;

  List<FileDetails> getAllFiles();

  Pair<Resource, String> serveFile(UUID fileNameUuid) throws FileNotFoundException;

  Resource downloadFile(String fileName) throws FileNotFoundException;

  void deleteBlobFile(UUID fileNameUuid) throws FileNotFoundException;
}