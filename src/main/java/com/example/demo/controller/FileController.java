package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

import com.example.demo.exception.FileNotSupportedException;
import com.example.demo.model.FileDetails;
import com.example.demo.payload.FileUploadResponse;
import com.example.demo.service.FileUploadService;
import com.fasterxml.jackson.annotation.JsonView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileUploadService fileUploadService;

    @GetMapping(value = { "", "/" }, produces = "application/json")
    @JsonView(FileDetails.Summary.class)
    public List<FileDetails> getAllFiles() {
        return fileUploadService.getAllFiles();
    }

    @PostMapping(value = "/upload")
    public ResponseEntity<Object> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            List<FileUploadResponse> fileUploadResponses = Arrays.stream(files).map(file -> {
                try {
                    return fileUploadService.uploadFile(file);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }).collect(Collectors.toList());

            return new ResponseEntity<>(fileUploadResponses, HttpStatus.OK);
        } catch (UncheckedIOException e) {
            return new ResponseEntity<>(e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (FileNotSupportedException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = { "/{fileUuid:.+}" }, produces = "application/json")
    public ResponseEntity<?> serveFile(@PathVariable UUID fileUuid) {
        try {
            Pair<Resource, String> result = fileUploadService.serveFile(fileUuid);
            Resource resource = result.getFirst();
            String blobFileName = result.getSecond();
            String fileExtension = blobFileName.substring(blobFileName.lastIndexOf('.') + 1).toLowerCase();

            // Map file extensions to MediaTypes
            Map<String, MediaType> mediaTypeMap = new HashMap<>();
            mediaTypeMap.put("jpg", MediaType.IMAGE_JPEG);
            mediaTypeMap.put("jpeg", MediaType.IMAGE_JPEG);
            mediaTypeMap.put("png", MediaType.IMAGE_PNG);
            mediaTypeMap.put("gif", MediaType.IMAGE_GIF);
            mediaTypeMap.put("pdf", MediaType.APPLICATION_PDF);
            // Defaulting to OCTET_STREAM (binary) if not found:
            MediaType mediaType = mediaTypeMap.getOrDefault(fileExtension, MediaType.APPLICATION_OCTET_STREAM);

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .body(resource);
            } else {
                logger.warn("Resource does not exist or is not readable: {}", fileUuid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "error", "File not found",
                        "fileUuid", fileUuid.toString()));
            }
        } catch (FileNotFoundException e) {
            logger.error("File not found: {}", fileUuid, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "File not found",
                    "fileUuid", fileUuid.toString()));
        } catch (IOException e) {
            logger.error("Error accessing InputStreamResource: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", "Error accessing resource",
                    "fileUuid", fileUuid.toString()));
        }
    }

    @DeleteMapping(value = { "/{fileUuid:.+}" })
    public ResponseEntity<Object> deleteFile(@PathVariable UUID fileUuid) {
        try {
            fileUploadService.deleteBlobFile(fileUuid);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "success", true,
                    "message", "File deleted successfully.",
                    "fileUuid", fileUuid.toString()));
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "File not found",
                    "fileUuid", fileUuid.toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", "Error occurred while deleting file",
                    "fileUuid", fileUuid.toString()));
        }
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> fileDownload(@PathVariable String fileName) {
        try {
            Resource resource = fileUploadService.downloadFile(fileName);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
