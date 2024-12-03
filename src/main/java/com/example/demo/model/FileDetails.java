package com.example.demo.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDetails {

  public interface Summary {
  }

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @JsonView(Summary.class)
  private UUID id;

  private String fileName;
  private String fileUri;

  @JsonView(Summary.class)
  private String fileOriginalName;

  private long size;
}
