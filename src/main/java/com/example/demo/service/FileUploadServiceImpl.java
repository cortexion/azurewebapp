package com.example.demo.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.example.demo.controller.FileController;
import com.example.demo.exception.FileNotSupportedException;
import com.example.demo.model.FileDetails;
import com.example.demo.payload.FileUploadResponse;
import com.example.demo.repository.FileDetailsRepository;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private static final String JPEG_CONTENT_TYPE = "image/jpeg";
    private static final String PNG_CONTENT_TYPE = "image/png";
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    @Autowired
    private FileDetailsRepository fileDetailsRepository;
    // private final Path UPLOAD_PATH = Paths.get("src", "main", "resources",
    // "static", "uploads");

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    @Override
    public List<FileDetails> getAllFiles() {
        return fileDetailsRepository.findAll();
    }

    @Override
    public Pair<Resource, String> serveFile(UUID id) throws FileNotFoundException {
        Optional<FileDetails> fileDetailsOpt = fileDetailsRepository.findById(id);

        if (fileDetailsOpt.isEmpty()) {
            throw new FileNotFoundException("File not found in the database for id: " + id);
        }

        FileDetails fileDetails = fileDetailsOpt.get();
        String blobFileName = fileDetails.getFileName(); // Use the stored fileName for Azure Blob Storage

        BlobContainerClient containerClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient();

        BlobClient blobClient = containerClient.getBlobClient(blobFileName);

        if (!blobClient.exists()) {
            fileDetailsRepository.delete(fileDetails);
            throw new FileNotFoundException("File not found in blob storage: " + blobFileName
                    + ". Corresponding database record has been removed.");
        }

        try {
            InputStream inputStream = blobClient.openInputStream();
            Resource resource = new InputStreamResource(inputStream);
            return Pair.of(resource, blobFileName); // Return both Resource and filename
        } catch (Exception e) {
            throw new FileNotFoundException(
                    "Failed to fetch file from blob storage: " + blobFileName + ". Error: " + e.getMessage());
        }
    }

    @Override
    public FileUploadResponse uploadFile(MultipartFile file) throws IOException {
        if (!JPEG_CONTENT_TYPE.equals(file.getContentType()) && !PNG_CONTENT_TYPE.equals(file.getContentType())
                && !PDF_CONTENT_TYPE.equals(file.getContentType())) {
            throw new FileNotSupportedException("Only .jpeg, .png and .pdf files are supported");
        }

        BlobContainerClient containerClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient();

        // Create the container if it doesn't exist
        try {
            containerClient.create();
            logger.info("Created container: " + containerClient.getBlobContainerName());
        } catch (Exception e) {
            logger.info("Container already exists: " + containerClient.getBlobContainerName());
        }

        long unixTimestamp = System.currentTimeMillis() / 1000;
        String timestampString = Long.toString(unixTimestamp);
        String originalFilename = file.getOriginalFilename();
        String name = originalFilename.replaceAll("\\.[^.]+$", "_"); // Remove the extension
        String extension = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : "";
        String filenameWithTimestamp = name + timestampString + extension;

        try {
            containerClient.getBlobClient(filenameWithTimestamp)
                    .upload(file.getInputStream(), file.getSize(), true);

            logger.info("Successfully uploaded file: {}", file.getOriginalFilename());
        } catch (Exception e) {
            // Log the error
            logger.error("Failed to upload file: {}. Error: {}", file.getOriginalFilename(), e.getMessage(), e);
        }

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/files/")
                .path(filenameWithTimestamp)
                .toUriString();

        FileDetails fileDetails = FileDetails.builder()
                .fileName(filenameWithTimestamp)
                .fileOriginalName(file.getOriginalFilename())
                .fileUri("https://eerikanttilastorage.blob.core.windows.net/uploads/" + file.getOriginalFilename())
                .size(file.getSize())
                .build();

        long fileSize = file.getSize();
        logger.info("Uploaded file size: {} bytes", fileSize);

        fileDetailsRepository.save(fileDetails);
        logger.info("fileDetails id: " + fileDetails.getId());

        FileUploadResponse fileUploadResponse = FileUploadResponse.builder()
                .id(fileDetails.getId())
                .fileOriginalName(file.getOriginalFilename())
                .build();

        logger.info("File uploaded successfully: " + file.getOriginalFilename());
        return fileUploadResponse;
    }

    @Override
    public void deleteBlobFile(UUID fileNameUuid) throws FileNotFoundException {
        Optional<FileDetails> fileDetails = fileDetailsRepository.findById(fileNameUuid);

        if (fileDetails.isPresent()) {
            String fileName = fileDetails.get().getFileName();
            logger.info("fileName: {}", fileName);

            BlobContainerClient containerClient = new BlobContainerClientBuilder()
                    .connectionString(connectionString)
                    .containerName(containerName)
                    .buildClient();

            BlobClient blobClient = containerClient.getBlobClient(fileName);

            if (!blobClient.exists()) {
                throw new FileNotFoundException("File not found in blob storage: " +
                        fileName);
            }

            try {
                blobClient.delete();
                logger.info("Successfully deleted file: {}", fileName);
                fileDetailsRepository.delete(fileDetails.get()); // getter because it's Optional without it
            } catch (Exception e) {
                logger.error("Failed to delete file: {}. Error: {}", fileName,
                        e.getMessage(), e);
                throw new RuntimeException("Failed to delete file from blob storage", e);
            }

            logger.info("File details removed from the database for file: {}", fileNameUuid);
        } else {
            logger.info("File details not found in the database for file: {}", fileNameUuid);
            throw new FileNotFoundException("File not found for UUID: " + fileNameUuid);
        }
    }

    @Override
    public Resource downloadFile(String fileName) throws FileNotFoundException {
        BlobContainerClient containerClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient();

        BlobClient blobClient = containerClient.getBlobClient(fileName);

        if (!blobClient.exists()) {
            throw new FileNotFoundException("File not found in blob storage: " + fileName);
        }

        try {
            InputStream inputStream = blobClient.openInputStream();

            return new InputStreamResource(inputStream);
        } catch (Exception e) {
            throw new FileNotFoundException(
                    "Failed to fetch file from blob storage: " + fileName + ". Error: " + e.getMessage());
        }
    }
}
