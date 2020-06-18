package javachat;

import java.net.*;
import java.io.*;
import java.util.*;

/* 특정 포트로 클라이언트 연결을 무한정 기다린다.
 * 클과 연결이 이뤄지면 클과 통신을 담당할 스레드 (JavaChatHandler)를 생성한 후 스레드를 동작시킨다.
 * 여러 명과 통신하기 위해 JavaChatHandler를 Vector에 저장하여 관리
 * */
public class JavaChatServer extends Thread{
	
	private ServerSocket server;
	private final int port =9999;
	Vector <JavaChatHandler> userV = new Vector<>(5,3);
	
	public JavaChatServer() {
		try {
			server= new ServerSocket(port);
			System.out.println("##채팅 서버가 시작##");
			System.out.println("##["+port+"]포트에서  대기중##");
			
		}catch(IOException e) {
			System.out.println("##채팅서버 시작 중 예외: "+e.getMessage()+"##");
		}
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				Socket sock = server.accept();
				System.out.println("["+sock.getInetAddress()+"]님이 접속");
				JavaChatHandler handler
				= new JavaChatHandler(sock,userV);//통신 담당 스레트
				handler.start();//스레드 동작
			} catch (IOException e) {
				System.out.println("##소켓 생성 중 예외: "+e.getMessage()+"##");
			}
		}
	}//run()-----------
	
	public static void main(String[] args) {
		new JavaChatServer().start();
	}
}
