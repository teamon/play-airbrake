## Airbrake.io notifier for Play 2.0

## Instalation

Add `play-airbrake` to your `project/Build.scala` file

``` scala
val appDependencies = Seq(
  "eu.teamon" %% "play-airbrake" % "0.2.0"
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

For javascript notifications

```scala

<head>
  @Html(play.airbrake.Airbrake.js)
</head>

```

For java integration your app/Global.java should look like this

```java
@Override
public Result onError(RequestHeader request, Throwable t) {
	Map<String, String> data = new HashMap<String, String>();
	for (Entry<String, String[]> value : request.headers().entrySet()) {
		data.put(value.getKey(), Arrays.deepToString(value.getValue()));
	}
	Airbrake.notify(request.method(), request.uri(), data, t);
	return super.onError(request, t);
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
