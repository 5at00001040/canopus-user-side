package controllers

import java.io.ByteArrayInputStream
import java.util.Collections
import java.util.concurrent.TimeUnit
import javax.inject._

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http._
import com.google.cloud.storage._
import play.api.libs.ws._
import play.api.mvc._

import scala.language.postfixOps



@Singleton
class GcsController @Inject()(cc: ControllerComponents, ws: WSClient) extends AbstractController(cc) {

  val ZERO_BYTE: Array[Byte] = Array[Byte]()
  val BUCKET_NAME = "bucket-name"

  def fileList() = Action {

    val storage = StorageOptions.getDefaultInstance.getService
    val bucket = storage.get(BUCKET_NAME)

    import scala.collection.JavaConverters._
    val objList = bucket.list().iterateAll().iterator().asScala
    val fileList: Seq[String] = objList.map(_.getName).toList

    Ok(views.html.debug_page("file list: ", fileList))
  }

  def createFile(fileName: String) = Action {

    val storage = StorageOptions.getDefaultInstance.getService
    val bucket = storage.get(BUCKET_NAME)

    bucket.create(fileName, new ByteArrayInputStream("test file".getBytes("utf-8")))

    Ok(views.html.debug_page("create file: ", List(fileName)))
  }

  def signUrl(fileName: String) = Action {

    val storage = StorageOptions.getDefaultInstance.getService

    val blobInfo = BlobInfo.newBuilder(BUCKET_NAME, fileName).build()
    val signUrl = storage.signUrl(blobInfo, 3, TimeUnit.DAYS)

    Ok(views.html.debug_page("sign url: ", List(signUrl.toString)))
  }

  def createResumeUrl(fileName: String) = Action {

    val STORAGE_SCOPE = "https://www.googleapis.com/auth/devstorage.read_write"

    val credential = GoogleCredential.getApplicationDefault()
      .createScoped(Collections.singleton(STORAGE_SCOPE))

    val httpTransport = GoogleNetHttpTransport.newTrustedTransport
    val requestFactory = httpTransport.createRequestFactory(credential)


    val resumableTarget = "https://storage.googleapis.com" + "/" + BUCKET_NAME + "/" + fileName

    val headers = new HttpHeaders().setContentLength(0L).set("x-goog-resumable", "start")

    val byteArrayContent = new ByteArrayContent("text/plain", ZERO_BYTE)
    val request = requestFactory.buildPostRequest(new GenericUrl(resumableTarget), byteArrayContent).setHeaders(headers)
    val response = request.execute
    val uploadLocation = response.getHeaders.getLocation


    // curl - X PUT 'https://storage.googleapis.com/...' - F "file=@/Users/user/file.txt;type=text/plain"

    Ok(views.html.debug_page("resumable url: ", List(uploadLocation)))
  }

}
