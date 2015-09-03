package com.deigote.tmg

import com.deigote.tmg.transmission.AddTorrentAction
import com.deigote.tmg.transmission.RPC
import com.deigote.tmg.transmission.Result
import retrofit.RestAdapter
import retrofit.RetrofitError

import javax.mail.Flags
import javax.mail.Folder
import javax.mail.Message
import javax.mail.Session
import javax.mail.Store
import javax.mail.internet.MimeMultipart
import javax.mail.search.FlagTerm

@Singleton
class TransmissionMagnetGrabber {

	private RPC transmissionClient = new RestAdapter.Builder()
		.setEndpoint(env('TRANS_URL'))
		.setLogLevel(RestAdapter.LogLevel.BASIC)
		.setLog([ log: { String msg -> println(msg) } ] as RestAdapter.Log)
		.build().create(RPC)

	private String gmailUser = env('GM_USER'), gmailPassword = env('GM_PWD'),
		transmissionAuth = 'Basic ' + "${env('TRANS_USER')}:${env('TRANS_PWD')}".toString().bytes.encodeBase64()

	static void main(String[] args) {
		TransmissionMagnetGrabber.instance.transferMagnets()
	}

	void transferMagnets() {
		Session emailSession = Session.getDefaultInstance(buildPop3Properties())
		Store store = emailSession.getStore("imaps")
		store.connect('imap.gmail.com', gmailUser, gmailPassword)

		Folder inbox = store.getFolder('INBOX')
		inbox.open(Folder.READ_WRITE)
		inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false))
			.findAll { it.getSubject().startsWith('magnet:') }
			.each { archive(it, inbox) }
			.collect { it.getSubject() }
			.collectEntries { [(it): transferMagnet(it)] }
			.each { println "Sent magnet ${it.key} and received ${it.value.result}" }
		inbox.close(true)
	}

	private void archive(Message message, Folder folder) {
		[Flags.Flag.SEEN, Flags.Flag.DELETED].each { flag ->
			folder.setFlags([message] as Message[], new Flags(flag), true)
		}
	}

	private Result transferMagnet(String magnetLink, String sessionId = null) {
		try {
			transmissionClient.addTorrent(transmissionAuth, sessionId ?: '', AddTorrentAction.for(magnetLink))
		}
		catch (RetrofitError e) {
			if (e.response?.status == 409 && !sessionId && findSessionId(e)) {
				transferMagnet(magnetLink, findSessionId(e))
			}
			else {
				treatTransferException(e)
			}
		}
		catch (Throwable e) {
			treatTransferException(e)
		}
	}

	private String findSessionId(RetrofitError e) {
		e.response.headers.find { it.name == RPC.sessionIdHeader }?.value
	}

	private Result treatTransferException(Throwable e) {
		new Result(result: "Error: ${e.getMessage()}")
	}

	private static String env(String varName) {
		System.getenv(varName)
	}

	private static Properties buildPop3Properties() {
		[
			'mail.store.protocol': 'imaps'
		].inject(new Properties()) { Properties properties, String propertyName, String propertyValue ->
			properties.put(propertyName, propertyValue)
			return properties
		}
	}


}
