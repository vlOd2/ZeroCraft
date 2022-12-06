package net.fieme.zerocraft.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import net.fieme.zerocraft.ColorUtil;
import net.fieme.zerocraft.Tuple;
import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.event.EventBasicObject;
import net.fieme.zerocraft.event.EventFirer;

/**
 * The main window for ZeroCraft
 */
public final class WindowMain extends Window {
	public static final long EVENT_INPUTSUBMITTED_ID = 365087200;
	private JTextPane txtContents;
	private JScrollPane scrlContents;
	private JTextField txtInput;
	private JButton btnSubmit;
	/**
	 * Basic event firer that fires when the user input has been submitted
	 * @implNote the object is the user input
	 */
	public EventFirer<EventBasicObject> inputSubmitted;

	private void init() {
		this.frame = new JFrame();
		this.frame.setTitle(ZeroCraft.VERSION_DISPLAYABLE_STR);
		this.frame.setSize(854, 480);
		this.frame.setLayout(null);
		this.frame.setResizable(false);
		this.frame.setLocationRelativeTo(null);
		this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				windowClose.fire(new EventBasicObject(Window.EVENT_WINDOWCLOSE_ID, null));
			}
		});
		
		this.txtContents = new JTextPane();
		this.txtContents.addKeyListener(new KeyAdapter() {
	        @Override
	        public void keyTyped(KeyEvent e) {
	        	e.consume();
	        }
	        
	        @Override
	        public void keyPressed(KeyEvent e) {
	        	e.consume();
	        }
		});
		
		this.scrlContents = new JScrollPane(this.txtContents, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.scrlContents.setBounds(
				0, 
				0, 
				this.frame.getWidth() - 6, 
				this.frame.getHeight() - 25 - 35);
		
		this.txtInput = new JTextField();
		this.txtInput.setBounds(
				5, 
				this.frame.getHeight() - 25 - 32, 
				this.frame.getWidth() - 5 - 10 - 105, 
				25);
		this.txtInput.addKeyListener(new KeyAdapter() {
	        @Override
	        public void keyPressed(KeyEvent e) {
	            if (e.getKeyCode() == KeyEvent.VK_ENTER){
	            	btnSubmit.doClick();
	            }
	        }
		});
		
		this.btnSubmit = new JButton();
		this.btnSubmit.setText("Send CMD");
		this.btnSubmit.setBounds(
				this.frame.getWidth() - 5 - 10 - 115 + 18, 
				this.frame.getHeight() - 25 - 32,
				100, 
				25);
		this.btnSubmit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = txtInput.getText();
				inputSubmitted.fire(new EventBasicObject(EVENT_INPUTSUBMITTED_ID, text));
				txtInput.setText(null);
			}
		});
		
		this.frame.add(this.scrlContents);
		this.frame.add(this.txtInput);
		this.frame.add(this.btnSubmit);
	}
	
	/**
	 * Appends text to the contents without newline
	 * 
	 * @param text the text
	 * @param color the color of the text
	 */
	public void appendTextRaw(String text, Color color) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
		        StyleContext styleContext = StyleContext.getDefaultStyleContext();
		        AttributeSet attributeSet = styleContext.addAttribute(
		        		SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
		        
		        attributeSet = styleContext.addAttribute(attributeSet, 
		        		StyleConstants.FontFamily, "Lucida Console");
		        attributeSet = styleContext.addAttribute(attributeSet, 
		        		StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
		        
		        txtContents.setCaretPosition(txtContents.getDocument().getLength());
		        txtContents.setCharacterAttributes(attributeSet, false);
		        txtContents.replaceSelection(text);
		        txtContents.setCharacterAttributes(styleContext.addAttribute(
		        		SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.black), false);
			}
		});
	}
	
	/**
	 * Appends text to the contents
	 * 
	 * @param text the text
	 * @param color the color of the text
	 */
	public void appendText(String text, Color color, boolean enableColorCodes) {
		if (enableColorCodes) {
			text = "&0" + text.replace("&f", "&0");
			
			Tuple<Character, String>[] colorCodes = ColorUtil.parseColorCodes(text, '&');
			for (Tuple<Character, String> colorCode : colorCodes) {
				color = ColorUtil.getColorFromColorCode(colorCode.item1);
				this.appendTextRaw(colorCode.item2, color);
			}
			
			this.appendTextRaw("\n", Color.black);
		} else {
			this.appendTextRaw(text + "\n", color);
		}
	}

	/**
	 * Appends text to the contents with no color codes
	 * 
	 * @param text the text
	 * @param color the color of the text
	 */
	public void appendText(String text, Color color) {
		this.appendText(text, color, false);
	}
	
	/**
	 * Appends text to the contents with the black color
	 * 
	 * @param text the text
	 */
	public void appendText(String text, boolean enableColorCodes) {
		this.appendText(text, Color.black, enableColorCodes);
	}
	
	/**
	 * Appends text to the contents with the black color and no color codes
	 * 
	 * @param text the text
	 */
	public void appendText(String text) {
		this.appendText(text, Color.black, false);
	}
	
	/**
	 * Clears the contents
	 */
	public void clear() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				txtContents.setText("");
			}
		});
	}
	
	@Override
	public void show() {
		this.init();
		this.windowClose = new EventFirer<EventBasicObject>();
		this.inputSubmitted = new EventFirer<EventBasicObject>();
		this.frame.setVisible(true);
	}
	
	@Override
	public void close() {
		this.windowClose = null;
		this.inputSubmitted = null;
		this.frame.setVisible(false);
		this.frame.dispose();
	}
}
