package net.fieme.zerocraft.console;

import net.fieme.zerocraft.configuration.MessagesConfig;
import net.fieme.zerocraft.game.Player;

public class PlayerConsoleHandler extends ConsoleHandler {
	public Player player;
	
	public PlayerConsoleHandler(Player player) {
		super(null);
		this.player = player;
		this.addDefaults();
	}
	
	@Override
	public void start() {
		throw new IllegalStateException();
	}
	
	@Override
	public void close() {
    	this.commands.clear();
    	this.variables.clear();
	}
	
	@Override
    public boolean processCommand(String cmd, String[] cmdArgs) {
        ConsoleCommand conCmd = getCommandByName(cmd);
        ConsoleVariable conVar = getVariableByName(cmd);

        if (conCmd != null) {
        	conCmd.setCaller(this.player);
            if (!(cmdArgs.length < conCmd.getMinArgsCount() || cmdArgs.length > conCmd.getMaxArgsCount()))
                conCmd.execute(cmdArgs);
            else
            	player.packetHandler.sendChatMessage("Usage: " + conCmd.getUsage());
            return true;
        }
        else if (conVar != null) {
        	conVar.setCaller(this.player);
            if (cmdArgs.length < 1) {
            	printConVarHelp(conVar);
            } else {
                Object argsValue = ConsoleVariableTypeTools.getStringAsVarType(cmdArgs[0], conVar.getValueType());

                if (argsValue != null)
                	conVar.setValue(argsValue);
                else
                	player.packetHandler.sendChatMessage("&cInvalid value provided for this convar!");
            }
            
            return true;
        }
        
        player.packetHandler.sendChatMessage(MessagesConfig.chatInvalidCommand);
        return false;
    }
	
	@Override
    public void printConCmdHelp(ConsoleCommand conCmd) {
		player.packetHandler.sendChatMessage("\"" + conCmd.getName() + "\" " + 
        		conCmd.getMinArgsCount() + "/" + 
        		conCmd.getMaxArgsCount()+ " (" + 
        		conCmd.getUsage() + ") - " + 
        		conCmd.getDescription());
    }
    
	@Override
    public void printConVarHelp(ConsoleVariable conVar) {
		player.packetHandler.sendChatMessage("\"" + conVar.getName() + "\" \"" + 
        		conVar.getValue() + "\" (" + 
        		conVar.getValueType() + ") - " + 
        		conVar.getDescription());
    }
    
	@Override
    public void doHelpCommand(String cmd) {
		if (this.commands.size() < 1) {
			player.packetHandler.sendChatMessage("&aThere are no &bcommands &aavailable.");
		}
		if (this.variables.size() < 1) {
			player.packetHandler.sendChatMessage("&aThere are no &bvariables &aavailable.");
		}
		
    	if (cmd != null) {
            ConsoleCommand conCmd = getCommandByName(cmd);
            ConsoleVariable conVar = getVariableByName(cmd);
            
            if (conCmd != null)
            	printConCmdHelp(conCmd);
            else if (conVar != null)
            	printConVarHelp(conVar);
            else
            	player.packetHandler.sendChatMessage("help: " + MessagesConfig.chatInvalidCommand);
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
