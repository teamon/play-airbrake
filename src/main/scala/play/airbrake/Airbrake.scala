package play.airbrake

import play.api._
import play.api.PlayException.UsefulException
import play.api.mvc.RequestHeader
import play.api.libs.ws.WS
import play.api.libs.concurrent.Akka

object Airbrake {

  def notify(request: RequestHeader, th: Throwable)(implicit app: Application) = {
    val enabled = app.configuration.getBoolean("airbrake.enabled") getOrElse { Play.isProd }

    if(enabled){
      val apiKey = app.configuration.getString("airbrake.apiKey") getOrElse { throw PlayException("Configuration error", "Could not find airbrake.apiKey in settings") }
      val ex = liftThrowable(th)

      Akka.future {
        val ssl = app.configuration.getBoolean("airbrake.ssl").getOrElse(false)
        val scheme = if(ssl) "https" else "http"
        WS.url(scheme + "://api.airbrake.io/notifier_api/v2/notices").post(formatNotice(app, apiKey, request, ex)).onRedeem { response =>
          Logger.info("Notice sent to Airbrake:" + "\n" + response.body)
        }
      }
    }
  }

  protected def liftThrowable(th: Throwable) = th match {
    case e: PlayException.UsefulException => e
    case e => UnexpectedException(unexpected = Some(e))
  }

  protected def formatNotice(app: Application, apiKey: String, request: RequestHeader, ex: UsefulException) = {
    <notice version="2.2">
      <api-key>{ apiKey }</api-key>
      <notifier>
        <name>play-airbrake</name>
        <version>0.1.0</version>
        <url>http://github.com/teamon/play-airbrake</url>
      </notifier>
      <error>
        <class>{ ex.title }</class>
        <message>{ ex.description }</message>
        <backtrace>
          { ex.cause.toList.flatMap(e => formatStacktrace(e.getStackTrace)) }
        </backtrace>
      </error>
      <request>
        <url>{ request.method + " " + request.uri }</url>
        <component/>
        <action/>
        <session>
          { formatVars(request.session.data) }
        </session>
      </request>
      <server-environment>
        <environment-name>{ app.mode }</environment-name>
      </server-environment>
    </notice>
  }

  protected def formatVars(vars: Map[String, String]) = vars.map { case (key, value) =>
    <var key={ key }>{ value }</var>
  }

  protected def formatTitle(ex: UsefulException) =
    ex.title + ex.cause.map(e => " :: " + e.getClass.getName).getOrElse("")

  protected def formatMessage(ex: UsefulException) =
    ex.description + ex.cause.map(e => "\n---\n" + e.getMessage).getOrElse("")

  protected def formatStacktrace(trace: Array[StackTraceElement]) = trace.flatMap { e =>
    val line = "%s.%s(%s)" format (e.getClassName, e.getMethodName, e.getFileName)
    <line file={line} number={e.getLineNumber.toString}/>
  }

}
