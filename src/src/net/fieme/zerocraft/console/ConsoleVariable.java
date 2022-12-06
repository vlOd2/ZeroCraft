package net.fieme.zerocraft.console;

public interface ConsoleVariable extends ConsoleCallable {
    public ConsoleVariableType getValueType();
    public Object getValue();
    public void setValue(Object value);
}