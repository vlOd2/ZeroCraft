package net.fieme.zerocraft.console;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;

import net.fieme.zerocraft.Tuple;
import net.fieme.zerocraft.Utils;
import net.fieme.zerocraft.console.builtin.ClearCmd;
import net.fieme.zerocraft.console.builtin.RestartCmd;
import net.fieme.zerocraft.console.builtin.SayCmd;
import net.fieme.zerocraft.console.builtin.StopCmd;
import net.fieme.zerocraft.console.builtin.WorldCmd;
import net.fieme.zerocraft.logging.Logging;

public class ConsoleHandler {
	protected boolean isClosed = true;
	private BufferedReader inputReader;
	protected Thread handleThread;
	public final String consoleInputPrefix;
    public final ArrayList<ConsoleCommand> commands = new ArrayList<ConsoleCommand>();
    public final ArrayList<ConsoleVariable> variables = new ArrayList<ConsoleVariable>();

    public ConsoleHandler(String consoleInputPrefix) {
    	this.inputReader = new BufferedReader(new InputStreamReader(System.in));
    	this.consoleInputPrefix = consoleInputPrefix;
    }
    
    private void handleThread_func() {
    	Logging.didUpdateInWindow = true;
    	System.out.print(this.consoleInputPrefix);
    	
    	while (!this.isClosed) {
    		try {
    			if (System.in.available() > 0) {
                    String input = "";
                    while (System.in.available() > 0)
                    	input += this.inputReader.readLine();
                    this.handleInput(input);
                    
                    Logging.didUpdateInWindow = true;
                    System.out.print(this.consoleInputPrefix);
    			}
    			
    			Thread.sleep(1);
    		} catch (Exception ex) {
    			if (ex instanceof InterruptedException) break;
				Logging.logSevere("Unable to handle console input: " + 
						Utils.getExceptionStackTraceAsStr(ex));
    		}
    	}
    }
    
	public void handleInput(String input) {
		if (input == null || input.isEmpty()) return;
    	Tuple<String, String[]> inputParsed = this.parseInput(input.trim());
    	
    	if (inputParsed.item1.equalsIgnoreCase("help")) {
        	if (inputParsed.item2.length > 0)
        		this.doHelpCommand(inputParsed.item2[0]);
        	else
        		this.doHelpCommand(null);
        }
    	else
    		this.processCommand(inputParsed.item1, inputParsed.item2);
	}
    
    protected void addDefaults() {
		this.commands.add(new ClearCmd());
		this.commands.add(new StopCmd());
		this.commands.add(new RestartCmd());
		this.commands.add(new SayCmd());
		this.commands.add(new WorldCmd());
    }
    
    public void start() {
    	this.close();
    	this.addDefaults();

    	this.handleThread = new Thread() {
    		@Override
    		public void run() {
    			handleThread_func();
    		}
    	};
    	
    	this.isClosed = false;
    	this.handleThread.start();
    }
    
	public void close() {
    	this.isClosed = true;
    	if (this.handleThread != null) this.handleThread.interrupt();
    	this.commands.clear();
    	this.variables.clear();
    }
    
    protected Tuple<String, String[]> parseInput(String input) {
        String cmd = null;
        String[] cmdArgs = new String[0];

        if (input.contains(String.valueOf((char)0x20))) {
            String[] splittedInput = Utils.splitBySpace(input);
            cmd = splittedInput[0];
            cmdArgs = ArrayUtils.subarray(splittedInput, 1, splittedInput.length);

            for (int cmdArgIndex = 0; cmdArgIndex < cmdArgs.length; cmdArgIndex++) {
                String cmdArg = cmdArgs[cmdArgIndex];

                if (cmdArg.startsWith("\""))
                    cmdArg = cmdArg.substring(1, cmdArg.length() - 1);
                if (cmdArg.endsWith("\""))
                    cmdArg = cmdArg.substring(0, cmdArg.length() - 1);

                cmdArgs[cmdArgIndex] = cmdArg;
            }
        }
        else
            cmd = input;

        return new Tuple<String, String[]>(cmd, cmdArgs);
    }

    public boolean processCommand(String cmd, String[] cmdArgs) {
        ConsoleCommand conCmd = getCommandByName(cmd);
        ConsoleVariable conVar = getVariableByName(cmd);

        if (conCmd != null) {
            if (!(cmdArgs.length < conCmd.getMinArgsCount() || cmdArgs.length > conCmd.getMaxArgsCount()))
                conCmd.execute(cmdArgs);
            else
                Logging.logInfo("Usage: " + conCmd.getUsage());
            return true;
        }
        else if (conVar != null) {
            if (cmdArgs.length < 1) {
            	printConVarHelp(conVar);
            } else {
                Object argsValue = ConsoleVariableTypeTools.getStringAsVarType(cmdArgs[0], conVar.getValueType());

                if (argsValue != null)
                	conVar.setValue(argsValue);
                else
                	Logging.logWarn("Invalid value provided for this convar!");
            }
            
            return true;
        }
        
        Logging.logWarn("Unrecognized command \"" + cmd + "\"!");
        return false;
    }

    public ConsoleCommand getCommandByName(String name) {
        for (ConsoleCommand conCmd : this.commands) {
            if (conCmd.getName().equalsIgnoreCase(name))
                return conCmd;
        }

        return null;
    }

    public ConsoleVariable getVariableByName(String name) {
        for (ConsoleVariable conVar : this.variables) {
            if (conVar.getName().equalsIgnoreCase(name))
                return conVar;
        }

        return null;
    }
    
    public void printConCmdHelp(ConsoleCommand conCmd) {
        Logging.logInfo("\"" + conCmd.getName() + "\" " + 
        		conCmd.getMinArgsCount() + "/" + 
        		conCmd.getMaxArgsCount()+ " (" + 
        		conCmd.getUsage() + ") - " + 
        		conCmd.getDescription());
    }
    
    public void printConVarHelp(ConsoleVariable conVar) {
        Logging.logInfo("\"" + conVar.getName() + "\" \"" + 
        		conVar.getValue() + "\" (" + 
        		conVar.getValueType() + ") - " + 
        		conVar.getDescription());
    }
    
    public void doHelpCommand(String cmd) {
    	if (cmd != null) {
            ConsoleCommand conCmd = getCommandByName(cmd);
            ConsoleVariable conVar = getVariableByName(cmd);
            
            if (conCmd != null)
            	printConCmdHelp(conCmd);
            else if (conVar != null)
            	printConVarHelp(conVar);
            else
            	Logging.logWarn("Unrecognized command \"" + cmd + "\"!");
    	} else {
            for (ConsoleCommand conCmd : this.commands) {
            	printConCmdHelp(conCmd);
            }
            for (ConsoleVariable conVar : this.variables) {
            	printConVarHelp(conVar);
            }
    	}
    }
}