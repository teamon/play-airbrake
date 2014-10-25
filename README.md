## Airbrake.io notifier for Play 2.0

## Instalation

Add `play-airbrake` to your `project/Build.scala` file

``` scala
val appDependencies = Seq(
  "eu.teamon" %% "play-airbrake" % "0.3.0"
)

val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
  resolvers += "teamon.eu repo" at "http://repo.teamon.eu"
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

For javascript notifications (not free plan)

```scala

<head>
  @Html(play.airbrake.Airbrake.js)
</head>

```

For java integration your app/Global.java should look like this

```java
class Global extends GlobalSettings {
  @Override
  public Result onError(RequestHeader request, Throwable t) {
    Airbrake.notify(request, t);
    return super.onError(request, t);
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
    <td>Boolean, optional, defaults to <code>Play.isProd</code></td>
    <td>optionally enable/disable notifications for different environment</td>
  </tr>

  <tr>
    <td><code>airbrake.endpoint</code></td>
    <td>String, optional, defaults to <code>api.airbrake.io/notifier_api/v2/notices</code></td>
    <td>point notifier to you custom airbrake compatible service (e.g. errbit)</td>
  </tr>
  <tr>
    <td><code>airbrake.environment</code></td>
    <td>String, optional, defaults to <code>Play.current.mode</code></td>
    <td>defines the environment the application is executed in, e.g. <code>Prod</code></td>
  </tr>
  <tr>
    <td><code>airbrake.appVersion</code></td>
    <td>String, optional, defaults to configuration value <code>application.version</code> or to <code>0.1</code> if none of the before mentioned is present</td>
    <td>defines the current version of the application</td>
  </tr>
</table>
