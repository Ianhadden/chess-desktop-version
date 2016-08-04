import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;


public class DisplayTest {

    public static void main(String[] args){
        JFrame frame = new JFrame("Test");
        frame.setLayout(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(576, 600));
        JLabel pic1 = new JLabel();
        pic1.setBounds(64, 64, 64, 64);
        pic1.setIcon(new ImageIcon("C:\\Users\\Ian\\Documents\\GitHub\\chess\\chess\\pics\\blackpawn.png"));
        
        
        MouseHandler handler = new MouseHandler();
        handler.frame = frame;
        pic1.addMouseListener(handler);
        pic1.addMouseMotionListener(handler);
        
        frame.add(pic1);
        
        frame.pack();
        frame.setVisible(true);
    }
}
