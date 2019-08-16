package io.github.jroy.punish.model;

import lombok.Data;

import java.util.UUID;

@Data
public class BanToken {

  private final int id;
  private final UUID targetUuid;
  private final long epoch;
  private final long wait;
  private final String reason;
  private final String type;
  private final String category;
  private final String sev;
  private final UUID staffUuid;
  private final UUID removedUuid;
  private final String removedReason;
}
