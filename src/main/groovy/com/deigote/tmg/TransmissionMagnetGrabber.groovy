package com.deigote.tmg

import com.deigote.tmg.transmission.AddTorrentAction
import com.deigote.tmg.transmission.RPC
import com.deigote.tmg.transmission.Result
import retrofit.RestAdapter

import javax.mail.Flags
import javax.mail.Folder
import javax.mail.Session
import javax.mail.Store
import javax.mail.internet.MimeMultipart
import javax.mail.search.FlagTerm

@Singleton
class TransmissionMagnetGrabber {

	private static final String gmailPopHost = 'pop.gmail.com'
	private RPC transmissionClient = new RestAdapter.Builder()
		.setEndpoint(env('TRANS_URL'))
		.setLogLevel(RestAdapter.LogLevel.BASIC)
		.setLog([ log: { String msg -> println(msg) } ] as RestAdapter.Log)
		.build().create(RPC)

	private String gmailUser = env('GM_USER'), gmailPassword = env('GM_PWD'),
		transmissionAuth = "${env('TRANS_USER')}:${env('TRANS_PWD')}".toString().bytes.encodeBase64()

	static void main(String[] args) {
		TransmissionMagnetGrabber.instance.transferMagnets()
	}

	void transferMagnets() {
		Session emailSession = Session.getDefaultInstance(buildPop3Properties())
		Store store = emailSession.getStore("pop3s")
		store.connect(gmailPopHost, gmailUser, gmailPassword)

		Folder inbox = store.getFolder('INBOX')
		inbox.open(Folder.READ_ONLY)
		inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false))
			.findAll { it.getSubject().startsWith('magnet:') }
			.collect { it.getSubject() }
			.collectEntries { [(it): transferMagnet(it)] }
			.each { println "Sent magnet ${it.key} and received ${it.value.result}" }
	}

	private List<String> getAllBodyParts(MimeMultipart multipart) {
		(0..multipart.getCount() - 1)
			.collect { index -> multipart.getBodyPart(index).getContent() }
			.collectMany { it.toString().tokenize() }
	}

	private Result transferMagnet(String magnetLink) {
		try {
			transmissionClient.addTorrent(transmissionAuth, AddTorrentAction.for(magnetLink))
		} catch (e) {
			e.printStackTrace()
			new Result(result: "Error: ${e.getMessage()}")
		}
	}

	private static String env(String varName) {
		System.getenv(varName)
	}

	private static Properties buildPop3Properties() {
		[
			'mail.pop3.host': gmailPopHost,
			'mail.pop3.port': '995',
			'mail.pop3.starttls.enable': 'true'
		].inject(new Properties()) { Properties properties, String propertyName, String propertyValue ->
			properties.put(propertyName, propertyValue)
			return properties
		}
	}


}
