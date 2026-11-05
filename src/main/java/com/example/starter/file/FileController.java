package com.example.starter.file;

import com.example.starter.common.response.ApiResponse;
import com.example.starter.file.dto.FileUploadResponse;
import com.example.starter.file.service.FileService;
import com.example.starter.file.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Files")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final StorageService storageService;

    @Operation(summary = "Upload a file (multipart field name: file)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FileUploadResponse> upload(@RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(fileService.upload(file));
    }

    @Operation(summary = "Download a file by key")
    @GetMapping
    public ResponseEntity<Resource> download(@RequestParam String key) {
        Resource resource = storageService.load(key);
        MediaType type = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(resource.getFilename()).build().toString())
                .body(resource);
    }

    @Operation(summary = "Delete a file (requires FILE_DELETE)")
    @PreAuthorize("hasAuthority('FILE_DELETE')")
    @DeleteMapping
    public ApiResponse<Void> delete(@RequestParam String key) {
        storageService.delete(key);
        return ApiResponse.ok(null);
    }
}
