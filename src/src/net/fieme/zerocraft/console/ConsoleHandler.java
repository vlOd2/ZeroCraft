package net.fieme.zerocraft.console;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import net.fieme.zerocraft.Tuple;
import net.fieme.zerocraft.Utils;
import net.fieme.zerocraft.configuration.MessagesConfig;
import net.fieme.zerocraft.console.builtin.BanCmd;
import net.fieme.zerocraft.console.builtin.ClearCmd;
import net.fieme.zerocraft.console.builtin.KickCmd;
import net.fieme.zerocraft.console.builtin.ManageWorldCmd;
import net.fieme.zerocraft.console.builtin.MuteCmd;
import net.fieme.zerocraft.console.builtin.PlaceCmd;
import net.fieme.zerocraft.console.builtin.PlayersCmd;
import net.fieme.zerocraft.console.builtin.PositionCmd;
import net.fieme.zerocraft.console.builtin.RageQuitCmd;
import net.fieme.zerocraft.console.builtin.ReloadCmd;
import net.fieme.zerocraft.console.builtin.RestartCmd;
import net.fieme.zerocraft.console.builtin.RulesCmd;
import net.fieme.zerocraft.console.builtin.SayCmd;
import net.fieme.zerocraft.console.builtin.StopCmd;
import net.fieme.zerocraft.console.builtin.TeleportCmd;
import net.fieme.zerocraft.console.builtin.TeleportToCmd;
import net.fieme.zerocraft.console.builtin.UnbanCmd;
import net.fieme.zerocraft.console.builtin.UnmuteCmd;
import net.fieme.zerocraft.console.builtin.WorldCmd;
import net.fieme.zerocraft.console.builtin.WorldsCmd;
import net.fieme.zerocraft.logging.Logging;

/**
 * Handler that handles console commands and variables
 */
public class ConsoleHandler {
	protected ConsoleCaller caller;
	protected boolean isClosed = true;
	private BufferedReader inputReader;
	protected Thread handleThread;
	public final String consoleInputPrefix;
    public final ArrayList<ConsoleCommand> commands = new ArrayList<ConsoleCommand>();
    public final ArrayList<ConsoleVariable> variables = new ArrayList<ConsoleVariable>();

    /**
     * Handler that handles console commands and variables
     * 
     * @param consoleInputPrefix the prefix to use
     */
    public ConsoleHandler(String consoleInputPrefix) {
    	this.caller = new ConsoleCaller();
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
                    while (System.in.available() > 0) {
                    	input += this.inputReader.readLine();
                    }
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
    
    /**
     * Handles the specified input
     * 
     * @param input the input
     */
	public void handleInput(String input) {
		if (input == null || input.isEmpty()) return;
    	Tuple<String, String[]> inputParsed = this.parseInput(input.trim());
    	
    	if (inputParsed.item1.equalsIgnoreCase("help")) {
        	if (inputParsed.item2.length > 0) {
        		String helpArg = inputParsed.item2[0];
        		if (Utils.isNumeric(helpArg, false)) {
        			this.doHelpCommand(Integer.valueOf(helpArg), null);
        		} else {
        			this.doHelpCommand(0, helpArg);
        		}
        	} else {
        		this.doHelpCommand(0, null);
        	}
        } else if (inputParsed.item1.equalsIgnoreCase("helpvar")) {
        	if (inputParsed.item2.length > 0) {
        		String helpArg = inputParsed.item2[0];
        		if (Utils.isNumeric(helpArg, false)) {
        			this.doHelpVarCommand(Integer.valueOf(helpArg), null);
        		} else {
        			this.doHelpVarCommand(0, helpArg);
        		}
        	} else {
        		this.doHelpVarCommand(0, null);
        	}
        } else {
    		this.processCommand(inputParsed.item1, inputParsed.item2);
    	}
	}
    
    protected void addDefaults() {
    	// General commands
    	this.commands.add(new RulesCmd());
    	this.commands.add(new PlayersCmd());
    	
    	// Position commands
    	this.commands.add(new PositionCmd());
    	this.commands.add(new TeleportCmd());
    	this.commands.add(new TeleportToCmd());
    	
    	// World commands
    	this.commands.add(new WorldCmd());
    	this.commands.add(new WorldsCmd());
    	this.commands.add(new PlaceCmd());
    	
    	// Misc commands
    	this.commands.add(new SayCmd());
		this.commands.add(new ClearCmd());
		this.commands.add(new RageQuitCmd());

		// Admin: Moderation commands
		this.commands.add(new KickCmd());
		this.commands.add(new MuteCmd());
		this.commands.add(new UnmuteCmd());
		this.commands.add(new BanCmd());
		this.commands.add(new UnbanCmd());

		// Admin: Server management commands
		this.commands.add(new StopCmd());
		this.commands.add(new RestartCmd());
		this.commands.add(new ReloadCmd());
		this.commands.add(new ManageWorldCmd());
    }
    
    /**
     * Starts taking input
     */
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
    
    /**
     * Closes this handler, you may start it again using {@link ConsoleHandler#start()}
     */
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

                if (cmdArg.startsWith("\"")) {
                	cmdArg = cmdArg.substring(1, cmdArg.length() - 1);
                }
                    
                if (cmdArg.endsWith("\"")) {
                	cmdArg = cmdArg.substring(0, cmdArg.length() - 1);
                }
                
                cmdArgs[cmdArgIndex] = cmdArg;
            }
        } else {
        	cmd = input;
        }

        return new Tuple<String, String[]>(cmd, cmdArgs);
    }

    public boolean processCommand(String cmd, String[] cmdArgs) {
        ConsoleCommand conCmd = getCommandByName(cmd);
        ConsoleVariable conVar = getVariableByName(cmd);

        if (conCmd != null) {
        	if (!this.caller.hasPermission(conCmd.getRequiredPermission())) {
        		this.caller.sendMessage(MessagesConfig.instance.feedbackNoPermission);
        	} else if (!(cmdArgs.length < conCmd.getMinArgsCount() || cmdArgs.length > conCmd.getMaxArgsCount())) {
            	try {
            		conCmd.execute(this.caller, cmdArgs);
            	} catch (Exception ex) {
            		this.caller.sendMessage("&cAn error has occured whilst executing the specified command!");
            		Logging.logError("Unable to execute \"" + cmd + "\": " + 
            				Utils.getExceptionStackTraceAsStr(ex));
            	}
            } else {
            	this.caller.sendMessage("&aUsage&b:&f " + conCmd.getUsage());
            }
            return true;
        } else if (conVar != null) {
        	if (!this.caller.hasPermission(conVar.getRequiredPermission())) {
        		this.caller.sendMessage(MessagesConfig.instance.feedbackNoPermission);
        	} else if (cmdArgs.length < 1) {
            	printConVarHelp(conVar);
            } else {
                Object argsValue = ConsoleVariableTypeTools.getStringAsVarType(cmdArgs[0], conVar.getValueType());

                if (argsValue != null) {
                	conVar.setValue(argsValue);
                } else {
                	this.caller.sendMessage("&cInvalid value provided for this convar!");
                }
            }
            
            return true;
        }
        
        this.caller.sendMessage(MessagesConfig.instance.feedbackInvalidCommand);
        return false;
    }

    /**
     * Gets a command by it's name
     * 
     * @param name the command name
     * @return the command or null (if the command was not found)
     */
    public ConsoleCommand getCommandByName(String name) {
        for (ConsoleCommand conCmd : this.commands) {
            if (conCmd.getName().equalsIgnoreCase(name) || 
            	Arrays.asList(conCmd.getAliases())
            		.stream().anyMatch(x -> x.equalsIgnoreCase(name))) {
            	return conCmd;
            }
        }

        return null;
    }

    /**
     * Gets a variable by it's name
     * 
     * @param name the variable name
     * @return the variable or null (if the variable was not found)
     */
    public ConsoleVariable getVariableByName(String name) {
        for (ConsoleVariable conVar : this.variables) {
            if (conVar.getName().equalsIgnoreCase(name) ||
            	Arrays.asList(conVar.getAliases())
            		.stream().anyMatch(x -> x.equalsIgnoreCase(name))) {
            	return conVar;
            }
        }

        return null;
    }
    
    /**
     * Prints help for the specified command
     * 
     * @param conCmd the command
     */
    public void printConCmdHelp(ConsoleCommand conCmd) {
    	this.caller.sendMessage("\"" + conCmd.getName() + "\" " + 
        		conCmd.getMinArgsCount() + "/" + 
        		conCmd.getMaxArgsCount()+ " (" + 
        		conCmd.getUsage() + ") - " + 
        		conCmd.getDescription());
    }
    
    /**
     * Prints help for the specified variable
     * 
     * @param conVar the variable
     */
    public void printConVarHelp(ConsoleVariable conVar) {
    	this.caller.sendMessage("\"" + conVar.getName() + "\" \"" + 
        		conVar.getValue() + "\" (" + 
        		conVar.getValueType() + ") - " + 
        		conVar.getDescription());
    }
    
    /**
     * Does the help command using the specified parameters
     * 
     * @param pageNumber the help page number
     * @param cmd the command name to do help for (to do a global help, set this to null)
     */
    public void doHelpCommand(int pageNumber, String cmd) {
    	if (cmd != null) {
            ConsoleCommand conCmd = this.getCommandByName(cmd);
            
            if (conCmd != null && this.caller.hasPermission(conCmd.getRequiredPermission())) {
            	this.printConCmdHelp(conCmd);
            } else {
            	this.caller.sendMessage("&cUnrecognized command \"" + cmd + "\"!");	
            }
    	} else {
    		if (this.commands.size() < 1) {
    			this.caller.sendMessage("&aThere are no &bcommands&a available!");
    			return;
    		} else if (this.commands.size() >= 10) {
    			int commandsProccessed = 0;
    			for (int commandIndex = 10 * pageNumber; 
    				commandIndex < this.commands.size(); 
    				commandIndex++) {
    				commandsProccessed++;
    				if (commandsProccessed > 10) break;
    				
    				ConsoleCommand command = this.commands.get(commandIndex);
                	if (this.caller.hasPermission(command.getRequiredPermission())) {
                		this.printConCmdHelp(command);
                	}
    			}
    		} else {
                for (ConsoleCommand conCmd : this.commands) {
                	if (this.caller.hasPermission(conCmd.getRequiredPermission())) {
                		this.printConCmdHelp(conCmd);
                	}
                }
    		}
    		
    		this.caller.sendMessage("&aHelp format: &fname minargs/maxargs (usage) - description");
    		this.caller.sendMessage("&aViewing page &e" + pageNumber + "&a/&b" + this.commands.size() / 10);
    	}
    }
    
    /**
     * Does the help variables command using the specified parameters
     * 
     * @param pageNumber the help page number
     * @param var the variable name to do help for (to do a global help, set this to null)
     */
    public void doHelpVarCommand(int pageNumber, String var) {
    	if (var != null) {
            ConsoleVariable conVar = this.getVariableByName(var);
            
            if (conVar != null && this.caller.hasPermission(conVar.getRequiredPermission())) {
            	this.printConVarHelp(conVar);
            } else {
            	this.caller.sendMessage("&cUnrecognized variable \"" + var + "\"!");	
            }
    	} else {
    		if (this.variables.size() < 1) {
    			this.caller.sendMessage("&aThere are no &bvariables&a available!");
    			return;
    		} else if (this.variables.size() >= 10) {
    			int variablesProccessed = 0;
    			for (int variableIndex = 10 * pageNumber; 
    					variableIndex < this.commands.size(); 
    					variableIndex++) {
    				variablesProccessed++;
    				if (variablesProccessed > 10) break;
    				
    				ConsoleVariable variable = this.variables.get(variableIndex);
                	if (this.caller.hasPermission(variable.getRequiredPermission())) {
                		this.printConVarHelp(variable);
                	}
    			}
    		} else {
                for (ConsoleVariable conVar : this.variables) {
                	if (this.caller.hasPermission(conVar.getRequiredPermission())) {
                		this.printConVarHelp(conVar);
                	}
                }
    		}
    		
    		this.caller.sendMessage("&aHelp format: &fname value (value_type) - description");
    		this.caller.sendMessage("&aViewing page &e" + pageNumber + "&a/&b" + this.variables.size() / 10);
    	}
    }
}