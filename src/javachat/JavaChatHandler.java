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
	boolean isStop=false;
	
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
				isStop=false;
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
			}//100인 경우
			
			while(!isStop) {
				//클이 보내오는 메시지를 계속 듣고 그 내용을 분석해서 로직별로 처리하자.
				String cMsg=in.readUTF();
				process(cMsg);
			}
			
		}catch(IOException e) {
			System.out.println("JavaChatHandler run()에서 예외: "+e);
		}
	}
	//프로토콜 별로 로직을 처리하는 메소드
	private void process(String cMsg) {
		System.out.println(cMsg);
		String tks[]=cMsg.split("\\|");
		switch(tks[0]) {
			case "300":{//클=>서버 "300|이모티콘번호"
				String emoNo=tks[1];
				sendMessageAll("300|"+chatName+"|"+emoNo);
				//300|보내는사람대화명|이모티콘번호를 모든 참여자에게 보낸다.
				
			}break;
			case "400":{//클=>서버 "400|보내는사람의대화명|글자색|메시지"
				String fntRgb=tks[1];
				String message=tks[2];
				sendMessageAll("400|"+chatName+"|"+fntRgb+"|"+message);			
			}break;
			case "500":{//클=>서버 "500|toChatName|귓속말메시지"
				String toChatName=tks[1];
				String oneMsg=tks[2];
				for(JavaChatHandler userChat:userV) {
					if(userChat.chatName.equals(toChatName)) {
						String str="500|"+this.chatName+"|"+oneMsg;
						//500|보내는사람의대화명|귓속말메시지
						try {
							userChat.sendMessageTo(str);
						} catch (IOException e) {
							userV.remove(userChat);
							break;
						}
					}
				}
			}break;
			case "800":{//클=>서버에게 "800|퇴장한는사람Id|대화명"
				String logoutId=tks[1];
				String logoutChatName=tks[2];
				sendMessageAll("800|"+logoutId+"|"+logoutChatName);
				//userV에서 JavaChatHandler를 제거
				userV.remove(this);
				closeAll();
			}break;
			case "900":{//클=>서버에게 "800|종료한는사람Id|대화명"
				String exitId=tks[1];
				String exitChatName=tks[2];
				sendMessageAll("900|"+exitId+"|"+exitChatName);
				//userV에서 JavaChatHandler를 제거
				userV.remove(this);
				closeAll();
			}break;
		}
	}

	private void closeAll() {
		try{
			isStop=true;
			if(in!=null) in.close();
			if(out!=null) out.close();
			if(sock!=null) {
				sock.close();
				sock=null;
			}
		}catch(Exception e) {
			System.out.println("closeAll()에서 예외: "+e);
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
