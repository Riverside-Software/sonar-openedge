package eu.rssw.pct;

public interface IParameter {
    public String getName();

    public String getMode();

    public int getExtent();

    public String getDataType();

    public ParameterType getParameterType();

    public boolean isClassDataType();
}

