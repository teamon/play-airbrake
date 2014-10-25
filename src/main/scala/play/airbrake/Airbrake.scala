package play.airbrake

import play.api._
import play.api.PlayException
import play.api.UnexpectedException
import play.api.mvc.RequestHeader
import play.api.libs.ws.WS
import play.api.libs.concurrent.Akka
import play.api.Play.current
import scala.collection.JavaConversions._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import scala.util.Success

object Airbrake {
  private lazy val app = play.api.Play.current
  private lazy val conf = app.configuration
  private lazy val enabled = conf.getBoolean("airbrake.enabled") getOrElse Play.isProd
  private lazy val apiKey = conf.getString("airbrake.apiKey") getOrElse { throw UnexpectedException(Some("Could not find airbrake.apiKey in settings")) }
  private lazy val ssl = conf.getBoolean("airbrake.ssl") getOrElse false
  private lazy val mode = conf.getString("airbrake.environment") getOrElse app.mode
  private lazy val endpoint = conf.getString("airbrake.endpoint") getOrElse "api.airbrake.io/notifier_api/v2/notices"
  private lazy val version = conf.getString("airbrake.appVersion").orElse(conf.getString("application.version")) getOrElse "0.1"

  private val AirbrakeSuccessCode = 200

  /**
    * Scala API
    *
    * {{{
    * // app/Global.scala
    * override def onError(request: RequestHeader, ex: Throwable) = {
    *   Airbrake.notify(request, ex)
    *   super.onError(request, ex)
    * }
    * }}}
    */
  def notify(request: RequestHeader, th: Throwable): Unit = if(enabled) _notify(request.method, request.uri, request.session.data, th)

  /**
    * Java API
    *
    * {{{
    * // app/Global.java
    * @Override
    * public Result onError(RequestHeader request, Throwable t) {
    *   Airbrake.notify(request, t);
    *   return super.onError(request, t);
    * }
    * }}}
    */
  def notify(request: play.mvc.Http.RequestHeader, th: Throwable): Unit = if(enabled){
    val data = request.headers.toMap.mapValues(_.toList.toString)
    _notify(request.method, request.uri, data, th)
  }

  protected def _notify(method: String, uri: String, data: Map[String, String], th: Throwable): Unit =
    Future {
      val scheme = if(ssl) "https" else "http"
      WS.url(scheme + "://" + endpoint).post(formatNotice(app, apiKey, method, uri, data, liftThrowable(th))).onComplete { response =>
        response match{
          case Success(r) if r.status == AirbrakeSuccessCode =>
            Logger.info("Exception notice sent to Airbrake.")
          case _ =>
            Logger.warn("Failed to report exception to Airbrake. Response: " + response.map(_.statusText))
        }
      }
    }


  def js = if(enabled) { """
    <script src="http://cdn.airbrake.io/notifier.min.js"></script>
    <script type="text/javascript">
      Airbrake.setKey('%s');
      Airbrake.setHost('api.airbrake.io');
      Airbrake.setEnvironment('%s');
      Airbrake.setGuessFunctionName(true);
    </script>
    """.format(apiKey, mode)
  } else ""

  protected def liftThrowable(th: Throwable) = th match {
    case e: PlayException => e
    case e => UnexpectedException(unexpected = Some(e))
  }

  protected def formatNotice(app: Application, apiKey: String, method: String, uri: String, data: Map[String,String], ex: UsefulException) = {
    <notice version="2.3">
      <api-key>{ apiKey }</api-key>
      <notifier>
        <name>play-airbrake</name>
        <version>0.3.3</version>
        <url>http://github.com/teamon/play-airbrake</url>
      </notifier>
      <error>
        <class>{ ex.title }</class>
        <message>{ ex.description }</message>
        <backtrace>
          { ex.cause.getStackTrace.flatMap(e => formatStacktrace(e)) }
        </backtrace>
      </error>
      <request>
        <url>{ method + " " + uri }</url>
        <component/>
        <action/>
        { formatSession(data) }
      </request>
      <server-environment>
        <environment-name>{ mode }</environment-name>
        <app-version>{ version }</app-version>
      </server-environment>
    </notice>
  }

  protected def formatSession(vars: Map[String, String]) =
    if(!vars.isEmpty) <session>{ formatVars(vars) }</session>
    else Nil

  protected def formatVars(vars: Map[String, String]) = vars.map { case (key, value) =>
    <var key={ key }>{ value }</var>
  }

  protected def formatStacktrace(e: StackTraceElement) = {
    val file = s"${e.getClassName}.${e.getFileName}()"
    <line file={file} number={e.getLineNumber.toString} method={e.getMethodName}/>
  }

}
