package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import lyrics.LyricsWikiaFinder;
import org.apache.commons.io.FileUtils;
import org.jaudiotagger.tag.FieldKey;

import tagging.AudioFileRecord;
import tagging.Tagger;



public class MainFrame extends JFrame implements ActionListener {
	private static final String ActionCommandFindAudioFiles = "findAudioFiles";
	private static final String ActionCommandFindLyrics = "findLyrics";
	private static final String ActionCommandWriteLyrics = "writeLyrics";
	private static final String ActionCommandCancel = "cancel";
	
	private JFileChooser fileChooser;
    private JButton button = new JButton("Add music library path");
    private JTable table;
    
    private MyProgressBar progress = new MyProgressBar();
    
	private File baseDir;
	private String[] columns = new String[]{"Nr", "Artist", "Album", "Title", "File", "Status"};
    private Tagger tagger;
   
    private boolean running;
    
    public MainFrame(String title) {
        super(title);
        
        JPanel p = new JPanel(new BorderLayout());
        
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        JPanel btnsNorth = new JPanel();
        p.add(btnsNorth, BorderLayout.NORTH);
        
        button.addActionListener(this);
        button.setActionCommand(ActionCommandFindAudioFiles);
        btnsNorth.add(button);

        JPanel btnsSouth = new JPanel();
        progress.setStringPainted(true);
        progress.setString("Title");
        btnsSouth.add(progress);
        
        p.add(btnsSouth, BorderLayout.SOUTH);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(this);
        cancelBtn.setActionCommand(ActionCommandCancel);
        btnsSouth.add(cancelBtn);
        
        table = new JTable() {
        	public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        		Component c = super.prepareRenderer(renderer, row, col);
        		AudioFileRecord record = tagger.getRecords().get(row);
        		if (record.getLyrics()!=null && c instanceof JComponent) {
        			JComponent jc = (JComponent)c; 
        			jc.setToolTipText("<html>"+record.getLyrics().replaceAll("\n", "<br/>")+"</html>"); 
        		}
        		return c; 
        	} 
        };
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        
        getContentPane().add(p);
    }
    
    public static void main(String[] args) {
        JFrame frame = new MainFrame("Batched Lyrics Finder");
        
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    
    private void findAudioFiles() {
        if (baseDir!=null && baseDir.exists() && baseDir.isDirectory()) {
        	tagger = new Tagger();
        	
        	new Thread(new Runnable() {
        		public void run() {
        			running = true;
        			
        			SwingUtilities.invokeLater(new Runnable() {
        				public void run() {
        		    		button.setEnabled(false);
        		    	}
        			});
        			
        			progress.setShowValues(false);
		        	progress.setIndeterminate(true);
        			
        			Collection<File> allFiles = FileUtils.listFiles(baseDir, null, true);
        			
        			progress.setMaximum(allFiles.size());
		        	progress.setValue(0);
		        	progress.setShowValues(true);
		        	progress.setIndeterminate(false);
        			
        			for (File aFile : allFiles) {
        				if (!running)
        					break;
        				tagger.readFile(aFile);
        				SwingUtilities.invokeLater(new Runnable() {
    						public void run() {
    							table.repaint();
    							table.revalidate();
    						}
   	         			});
        				progress.setValue(progress.getValue()+1);
        	        }
        			
        			changeButton(false);
				}
        	}).start();
            
            table.setModel(new DefaultTableModel() {
            	public int getColumnCount() {
                    return columns.length;
                }

                public int getRowCount() {
                    return tagger.getRecords().size();
                }

                public Object getValueAt(int row, int col) {
                	if (col==0) // row number
                		return row+1;
                	else { // record value
	                    AudioFileRecord record = tagger.getRecords().get(row);
	                    if (col==1)
	                    	return record.getArtist();
	                    if (col==2)
	                    	return record.getAlbum();
	                    if (col==3)
	                    	return record.getTitle();
	                    if (col==4)
	                    	return record.getFile().getName();
	                    if (col==5)
	                    	return record.getStatus();
                	}
					return null;
                }
                
                public String getColumnName(int col) {
                    return columns[col];
                }
            });
        }
    }
    
    private void findLyrics() {
    	new Thread(new Runnable() {
    		public void run() {
    			running = true;
				
    			SwingUtilities.invokeLater(new Runnable() {
    				public void run() {
    		    		button.setEnabled(false);
    		    	}
    			});
    			
    			progress.setMaximum(tagger.getRecords().size());
	        	progress.setValue(0);
	        	progress.setShowValues(true);
	        	progress.setIndeterminate(false);
    			
		    	for (AudioFileRecord aRecord : tagger.getRecords()) {
		    		if (!running)
    					break;
    				try {
		    			String lyrics = LyricsWikiaFinder.findLyrics(
								aRecord.getArtist(), aRecord.getTitle());
						if (lyrics!=null) {
							aRecord.setLyrics(lyrics);
							aRecord.setStatus("LYRICS FOUND");
						}
						else {
							aRecord.setStatus("NO LYRICS FOUND");
						}
						SwingUtilities.invokeLater(new Runnable() {
    						public void run() {
    							table.repaint();
    							table.revalidate();
    						}
            			}); 
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					progress.setValue(progress.getValue()+1);
		    	}
		    	
		    	if (running)
		    		changeButton(false);
    		}
    	}).start();
    }
    
    private void writeLyrics() {
    	new Thread(new Runnable() {
    		public void run() {
    			running = true;
				
    			SwingUtilities.invokeLater(new Runnable() {
    				public void run() {
    		    		button.setEnabled(false);
    		    	}
    			});
    			
    			progress.setMaximum(tagger.getRecords().size());
	        	progress.setValue(0);
	        	progress.setShowValues(true);
	        	progress.setIndeterminate(false);
    			
		    	for (AudioFileRecord aRecord : tagger.getRecords()) {
		    		if (!running)
    					break;
    				try {
    					if (aRecord.getLyrics()!=null) {
							tagger.writeToFile(
									aRecord.getFile(), FieldKey.LYRICS, aRecord.getLyrics());
							aRecord.setStatus("LYRICS WRITTEN");
							SwingUtilities.invokeLater(new Runnable() {
	    						public void run() {
	    							table.repaint();
	    							table.revalidate();
	    						}
	            			});
    					}
					} catch (Exception e) {
						e.printStackTrace();
					}
					progress.setValue(progress.getValue()+1);
		    	}
		    	if (running)
		    		changeButton(false);
    		}
    	}).start();
    }
    
    private void changeButton(boolean reset) {
    	if (reset) {
    		SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					button.setActionCommand(ActionCommandFindAudioFiles);
		    		button.setText("Add music library path");
		    		button.setEnabled(true);
				}
			});
    	}
    	else if (ActionCommandFindAudioFiles.equals(button.getActionCommand())) {
    		SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					button.setActionCommand(ActionCommandFindLyrics);
		    		button.setText("Find lyrics");
		    		button.setEnabled(true);
				}
			});
    	}
    	else if (ActionCommandFindLyrics.equals(button.getActionCommand())) {
    		SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					button.setActionCommand(ActionCommandWriteLyrics);
		    		button.setText("Write lyrics");
		    		button.setEnabled(true);
				}
			});
    	}
    	else if (ActionCommandWriteLyrics.equals(button.getActionCommand())) {
    		SwingUtilities.invokeLater(new Runnable() {
				public void run() {
		    		button.setEnabled(false);
		    	}
			});
    	}
    }
    
    public void actionPerformed(ActionEvent e) {
    	if (ActionCommandFindAudioFiles.equals(e.getActionCommand())) {
            int returnVal = fileChooser.showOpenDialog(MainFrame.this);
            if (returnVal==JFileChooser.APPROVE_OPTION) {
            	baseDir = fileChooser.getSelectedFile();
                findAudioFiles();
            }
    	}
    	else if (ActionCommandFindLyrics.equals(e.getActionCommand())) {
    		findLyrics();
    	}
    	else if (ActionCommandWriteLyrics.equals(e.getActionCommand())) {
    		writeLyrics();
    	}
    	else if (ActionCommandCancel.equals(e.getActionCommand())) {
    		running=false;
    		changeButton(true);
    	}
    }
    
    private static class MyProgressBar extends JProgressBar {
    	private boolean showValues;
    	
    	public void setShowValues(boolean b) {
    		showValues = b;
    	}
    	
    	public String getString() {
    		StringBuilder sb = new StringBuilder();
    		if (showValues) {
    			sb.append(getValue());
    			sb.append("/");
    			sb.append(getMaximum());
    			sb.append(" ");
    		}
    		sb.append(super.getString());
    		return sb.toString();
    	}
    }
    
}