package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

import jakarta.mail.Message.RecipientType;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

@Service
public class GmailService {

	private static final String APPLICATION_NAME = "ClassReservationApp";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

	/**
	 * Gmail API を用いてメール送信
	 * @param accessToken Google OAuth で取得したアクセストークン
	 * @param to 送信先メールアドレス
	 * @param subject 件名
	 * @param body 本文
	 */
	public void sendEmail(String accessToken, String to, String subject, String body) throws Exception {
		GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null));

		Gmail service = new Gmail.Builder(
				GoogleNetHttpTransport.newTrustedTransport(),
				JSON_FACTORY,
				new HttpCredentialsAdapter(credentials))
						.setApplicationName(APPLICATION_NAME)
						.build();

		MimeMessage mimeMessage = createEmail(to, "me", subject, body);
		Message message = createMessageWithEmail(mimeMessage);

		service.users().messages().send("me", message).execute();
	}

	/**
	 * JavaMail の MimeMessage を作成
	 */
	private MimeMessage createEmail(String to, String from, String subject, String bodyText) throws Exception {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		MimeMessage email = new MimeMessage(session);

		email.setFrom(new InternetAddress(from));
		email.addRecipient(RecipientType.TO, new InternetAddress(to));
		email.setSubject(subject, StandardCharsets.UTF_8.name());
		email.setText(bodyText, StandardCharsets.UTF_8.name());

		return email;
	}

	/**
	 * Gmail API 向けのメッセージ形式に変換
	 */
	private Message createMessageWithEmail(MimeMessage email) throws Exception {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		email.writeTo(buffer);
		String encodedEmail = Base64.getUrlEncoder().encodeToString(buffer.toByteArray());
		Message message = new Message();
		message.setRaw(encodedEmail);
		return message;
	}
}
