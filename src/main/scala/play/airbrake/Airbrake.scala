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

object Airbrake {
  private lazy val app = play.api.Play.current
  private lazy val enabled = app.configuration.getBoolean("airbrake.enabled") getOrElse { Play.isProd }
  private lazy val apiKey = app.configuration.getString("airbrake.apiKey") getOrElse { throw UnexpectedException(Some("Could not find airbrake.apiKey in settings")) }
  private lazy val ssl = app.configuration.getBoolean("airbrake.ssl").getOrElse(false)
  private lazy val endpoint = app.configuration.getString("airbrake.endpoint") getOrElse "api.airbrake.io/notifier_api/v2/notices"

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
        Logger.info("Exception notice sent to Airbrake")
      }
    }


  def js = if(enabled) { """
    <script src="http://cdn.airbrake.io/notifier.min.js"></script>
    <script type="text/javascript">
      Airbrake.setKey(%s);
      Airbrake.setHost('api.airbrake.io');
      Airbrake.setEnvironment(%s);
      Airbrake.setGuessFunctionName(true);
    </script>
    """.format(apiKey, app.mode)
  } else ""

  protected def liftThrowable(th: Throwable) = th match {
    case e: PlayException => e
    case e => UnexpectedException(unexpected = Some(e))
  }

  protected def formatNotice(app: Application, apiKey: String, method: String, uri: String, data: Map[String,String], ex: UsefulException) = {
    <notice version="2.2">
      <api-key>{ apiKey }</api-key>
      <notifier>
        <name>play-airbrake</name>
        <version>0.3.2</version>
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
        <environment-name>{ app.mode }</environment-name>
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
    val line = "%s.%s(%s)" format (e.getClassName, e.getMethodName, e.getFileName)
    <line file={line} number={e.getLineNumber.toString}/>
  }

}
