package net.fieme.zerocraft.console;

public interface ConsoleCommand extends ConsoleCallable {
    public String getUsage();
    public int getMinArgsCount();
    public int getMaxArgsCount();
    public void execute(ConsoleCaller caller, String[] args) throws Exception;
}