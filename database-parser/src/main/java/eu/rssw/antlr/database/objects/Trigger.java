package eu.rssw.antlr.database.objects;

public class Trigger {
  private final TriggerType type;
  private final String procedure;
  private boolean noOverride = false;
  private boolean override = false;
  private String crc;

  public Trigger(TriggerType type, String procedure) {
    this.type = type;
    this.procedure = procedure;
  }

  public boolean isNoOverride() {
    return noOverride;
  }

  public boolean isOverride() {
    return override;
  }

  public void setNoOverride(boolean noOverride) {
    this.noOverride = noOverride;
  }

  public void setOverride(boolean override) {
    this.override = override;
  }

  public String getCrc() {
    return crc;
  }

  public void setCrc(String crc) {
    this.crc = crc;
  }

  public TriggerType getType() {
    return type;
  }

  public String getProcedure() {
    return procedure;
  }

}
