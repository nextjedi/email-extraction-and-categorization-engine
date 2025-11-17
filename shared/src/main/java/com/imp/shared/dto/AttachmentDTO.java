package com.imp.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a message attachment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDTO {

    private String attachmentId;
    private String filename;
    private String mimeType;
    private Long size;
    private String downloadUrl;
    private byte[] data; // For small attachments
}
