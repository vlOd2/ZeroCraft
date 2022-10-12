package net.fieme.zerocraft.console;

public interface ConsoleCallable {
    public String getName();
    public String getDescription();
    public void setCaller(Object caller);
}