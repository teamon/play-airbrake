package plugins

import javax.inject.Inject

import com.typesafe.config.Config
import play.api._
import play.api.mvc.RequestHeader
import play.api.libs.ws.WSClient
import play.api.libs.concurrent.Execution.Implicits._
import play.api.inject.{Binding, Module}

import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.util.Try

class Airbrake @Inject() (config: Config, environment: Environment, wsClient: WSClient) {

  val enabled: Boolean = Try(config.getBoolean("airbrake.enabled")) getOrElse { environment.mode == Mode.Prod }
  val apiKey: String = config.getString("airbrake.apiKey")
  val ssl: Boolean = Try(config.getBoolean("airbrake.ssl")).getOrElse(false)
  val endpoint: String = Try(config.getString("airbrake.endpoint")) getOrElse "api.airbrake.io/notifier_api/v2/notices"


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
  def notify(request: RequestHeader, th: Throwable): Unit =
    if(enabled) _notify(request.method, request.uri, request.session.data, th, Some(request.headers.toMap), Some(request.queryString))

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
    val data = request.getHeaders.toMap.mapValues(_.toList.toString).toMap
    _notify(request.method, request.uri, data, th, None, None)
  }

  /**
    * Notify when not a Play-related error.
    */
  def notify(description: String, th: Throwable, method: Option[String], uri: Option[String]): Unit = if (enabled) {
    _notify(method getOrElse "(none)", (uri getOrElse "(none)"), Map(("description" -> description)), th, None, None)
  }


  protected def _notify(method: String, uri: String, data: Map[String, String], th: Throwable, headers: Option[Map[String, Seq[String]]], params: Option[Map[String, Seq[String]]]): Future[Unit] =
    Future.successful {
      val scheme = if(ssl) "https" else "http"
      wsClient.url(scheme + "://" + endpoint).post(formatNotice(environment.mode.toString, apiKey, method, uri, data, liftThrowable(th), headers, params)).onComplete { response =>
        Logger.error("Exception notice sent to Airbrake", th)
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
    """.format(apiKey, environment.mode.toString)
  } else ""

  protected def liftThrowable(th: Throwable) = th match {
    case e: PlayException => e
    case e => UnexpectedException(unexpected = Some(e))
  }

  protected def formatNotice(mode: String, apiKey: String, method: String, uri: String, data: Map[String,String], ex: UsefulException, headers: Option[Map[String, Seq[String]]], params: Option[Map[String, Seq[String]]]) = {
    <notice version="2.2">
      <api-key>{ apiKey }</api-key>
      <notifier>
        <name>play-airbrake</name>
        <version>0.3.1-SNAPSHOT</version>
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
        { formatParams(params) }
        { formatSession(data) }
        { formatHeaders(headers) }
      </request>
      <server-environment>
        <environment-name>{ mode }</environment-name>
      </server-environment>
    </notice>
  }

  protected def formatParams(data: Option[Map[String, Seq[String]]]) =
    if (data.isDefined && !data.get.isEmpty) <params>{ formatValuesMap(data.get) }</params>
    else Nil

  protected def formatHeaders(data: Option[Map[String, Seq[String]]]) =
    if (data.isDefined && !data.get.isEmpty) <cgi-data>{ formatValuesMap(data.get) } </cgi-data>
    else Nil

  protected def formatValuesMap(map: Map[String, Seq[String]]) = map.flatMap { case (key, seq) =>
    seq map { value => <var key={ key }>{ value }</var> }
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

class AirbrakeModule @Inject() (config: Config, environment: Environment, wsClient: WSClient) extends Module {

  val airbrake: Airbrake = new Airbrake(config, environment, wsClient)

  override def bindings(environment: Environment, playConfig: Configuration): Seq[Binding[_]] = {
    Seq.empty
  }


  def notify(request: RequestHeader, th: Throwable): Unit = airbrake.notify(request, th)

  def notify(request: play.mvc.Http.RequestHeader, th: Throwable): Unit = airbrake.notify(request, th)

}
