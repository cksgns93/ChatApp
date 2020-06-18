package javachat;

import java.io.*;
import java.util.*;
import java.net.*;
/*
 * 실질적으로 클라이언트와 메시지를 주고받는 일을 한다.
 * */
public class JavaChatHandler extends Thread {
	Socket sock;
	Vector<JavaChatHandler> userV;
	String userId, chatName;
	ObjectInputStream in;
	ObjectOutputStream out;
	
	public JavaChatHandler(Socket sock, Vector<JavaChatHandler> userV) {
		this.sock = sock;
		this.userV = userV;
		try {
			in = new ObjectInputStream(this.sock.getInputStream());
			out = new ObjectOutputStream(this.sock.getOutputStream());
		}catch(IOException e) {
			System.out.println("JavaChatHandler() 예외"+e);
		}
	}//생성자 -----

	@Override
	public void run() {
		try {
			//클이 접속하면 먼저 아이디 대화명을 보낸다. "100|아이디|대화명을 보낸다."
			String str = in.readUTF();
			String []tokens=str.split("\\|");
			int protocol = Integer.parseInt(tokens[0]);
			if(protocol==100) {
				this.userId=tokens[1];
				//대화명 중복 여부를 체크
				boolean isExist = isDuplicatedChatName(tokens[2]);
				if(isExist) {
					//대화명이 중복 된다면..
					sendMessageTo("700|");
				}else {
					//대화명이 중복되지 않는다면
					//1)모든 클에게 방금 입장한 사람의 아이디와 대화명을 보내준다.
					this.chatName=tokens[2];
					//방금 접속한 클에게 기존에 입장한 클들의 정보를 보내준다.
					for(JavaChatHandler userChat:userV) {
						String msg="100|"+userChat.userId+"|"+userChat.chatName;
						sendMessageTo(msg);
					}
					userV.add(this);//JavaChatHandler를 저장
					String sendMsg="100|"+userId+"|"+chatName;
					sendMessageAll(sendMsg);
				}
			}
			
		}catch(IOException e) {
			System.out.println("JavaChatHandler run()에서 예외: "+e);
		}
	}
	/*서버에 접속해 있는 모든 클에게 메시지를 보내는 메소드
	 * */
	private synchronized void sendMessageAll(String sendMsg) {
		for(JavaChatHandler userChat : userV) {
			try {
				userChat.out.writeUTF(sendMsg);
				userChat.out.flush();				
			}catch(IOException e){
				System.out.println("sendMessageAll(): "+e);
				userV.remove(userChat);
				break;
			}
		}
	}
	private synchronized void sendMessageTo(String sendMsg) 
	throws IOException
	{
		out.writeUTF(sendMsg);
		out.flush();
	}

	private boolean isDuplicatedChatName(String cname) {
		Iterator<JavaChatHandler> it=userV.iterator();
		while(it.hasNext()) {
			JavaChatHandler userChat = it.next();
			if(userChat.chatName.equals(cname)) {
				//동일한 대화명이 있다면 true 반환
				return true;
			}
		}
		return false;//동일한 대화명이 없다면 false 반환
	}
}
