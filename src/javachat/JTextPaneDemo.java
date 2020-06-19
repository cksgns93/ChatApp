package javachat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;

public class JTextPaneDemo extends JFrame{
	JTextPane tp;
	JScrollPane sp;
	JPanel p = new JPanel(new BorderLayout());
	StyledDocument doc;//
	
	public JTextPaneDemo() {
		super("::JTextPaneDemo::");
		Container cp = getContentPane();
		cp.add(p,"Center");
		p.setBackground(Color.white);
		tp = new JTextPane();
		sp = new JScrollPane(tp);
		p.add(sp,"Center");
		tp.setText("asdfasdfasdfsdfsdfsdfsdfsdasdfasasdfa");
		//1. 문서 모델 얻기
		doc=tp.getStyledDocument();//텍스트페인의 문서 모델
		//2. SimpleAttributeSet객체 생성
		SimpleAttributeSet attr= new SimpleAttributeSet();
		//attr에 style속성을 부여한다.
		//4.문서 모델에 해당 속성(attr)을 적용시킨다.
		StyleConstants.setFontFamily(attr, "궁서체");
		StyleConstants.setFontSize(attr, 28);
		StyleConstants.setItalic(attr, true);
		StyleConstants.setForeground(attr, Color.magenta);
		doc.setCharacterAttributes(0,20,attr,true);
		
		//문단 특성 적용
		attr=new SimpleAttributeSet();
		StyleConstants.setAlignment(attr, StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, tp.getText().length(), attr, true);
		
		//문서 끝에 문자열 추가
		int caretPos=doc.getEndPosition().getOffset()-1;//문서끝의 커릿 위치
		tp.setCaretPosition(caretPos);
		
		attr= new SimpleAttributeSet();
		StyleConstants.setFontSize(attr, 30);
		StyleConstants.setForeground(attr, Color.blue);
		try {
			doc.insertString(caretPos, "\n이만 총총..\n", attr);
		} catch (BadLocationException e) {
			System.out.println("예외"+e);
		}
		//텍스트페인에 라벨 띄우기
		String str="[Bangry님]\r\n";
		ImageIcon icon = new ImageIcon(getClass().getResource("11.PNG"));
		JLabel lb = new JLabel(str,icon,JLabel.CENTER);
		lb.setPreferredSize(new Dimension(90,90));

		attr= new SimpleAttributeSet();
		StyleConstants.setAlignment(attr, StyleConstants.ALIGN_RIGHT);
		
		tp.setCaretPosition(doc.getEndPosition().getOffset()-1);
		tp.insertComponent(lb);
		doc.setParagraphAttributes(doc.getEndPosition().getOffset()-1, 20, attr,true);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static void main(String[] args) {
		JTextPaneDemo ss = new JTextPaneDemo();
		ss.setSize(500,500);
		ss.setVisible(true);
	}
}
