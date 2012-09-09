## Airbrake.io notifier for Play 2.0

## Instalation

Add `play-airbrake` to your `project/Build.scala` file

``` scala
val appDependencies = Seq(
  "eu.teamon" %% "play-airbrake" % "0.1.1"
)

val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
  resolvers += "scalajars.org repo" at "http://scalajars.org/repository"
)
```

Your `app/Global.scala` should look like this

``` scala
import play.api._
import play.api.mvc._
import play.airbrake.Airbrake

object Global extends GlobalSettings {

  override def onError(request: RequestHeader, ex: Throwable) = {
    Airbrake.notify(request, ex)
    super.onError(request, ex)
  }

}

```

## Configuration

<table>
  <tr>
    <th>Key</th>
    <th></th>
    <th>Description</th>
  </tr>
  <tr>
    <td><code>airbrake.apiKey</code></td>
    <td>String, <strong>required</strong></td>
    <td>airbrake project api key</td>
  </tr>

  <tr>
    <td><code>airbrake.ssl</code></td>
    <td>Boolean, optional, defaults to <code>false</code></td>
    <td>set to <code>true</code> if you have airbrake plan with SSL support</td>
  </tr>

  <tr>
    <td><code>airbrake.enabled</code></td>
    <td>Boolean, optional, default to <code>Play.isProd</code></td>
    <td>optionally enable/disable notifications for different environment</td>
  </tr>

</table>
