# Compose Navigation with Ease from Everywhere

In this guide, I will explain how I have managed to use Compose Navigation seamlessly throughout our app. From view models and services to screens, and all of that type safe.

If you haven't familiarized yourself with Compose Navigation, it's crucial to read the original [documentation](https://developer.android.com/jetpack/compose/navigation) first.

This guide strongly advises against using complex arguments, and my solution aligns with this recommendation:
> It is strongly advised not to pass around complex data objects when navigating, but instead pass the minimum necessary information, such as a unique identifier or other form of ID, as arguments when performing navigation actions

However, if there is a need to pass complex data between screens, you can use `SavedStateHandle` in your view model while still adhering to the principles outlined in this guide.

## The Goal

By the end of this guide, you will have the capability to seamlessly navigate between screens, dialogs, and even bottom sheets with just a single line:
```kotlin
router.dispatch(NavigationType.NavigateTo(SettingsScreens.Register.get()))
```
Or if you want to also pass in some parameters then like this:
```kotlin
router.dispatch(NavigationType.NavigateTo(HomeScreens.ExtraData.get("MyTitle", 5)))
```
What about navigating back? Easy:
```kotlin
router.dispatch(NavigationType.NavigateUp)
```
Deeplinks? No problem:
```kotlin
router.dispatch(NavigationType.DeeplinkTo("deeplink://extra?text=Title%20name&count=20"))
```
Snackbars? Sure:
```kotlin
router.dispatch(NavigationType.Snackbar("Show my pretty snackbar"))
```

In the end, only your imagination limits what these actions can achieve. You can modify the `NavigationType` to also support opening a web view, or you can initiate an intent to open the sharing component.

## Building Our Navigation Graph

Initially, you need to construct a catalog of your screens, which you can utilize in the future for navigating to specific screens. To establish this catalog, I have created the following interface(*I know it is rather an abstract class but since kotlin doesn't allow inheritance from two class I decided to go with an interface, to make it possible to use inheritance for the bottom navigation*) which will help us later identify the routes to register:

```kotlin
private const val uri = "deeplink://"
sealed interface NavRoute {
    val screen: NavGraphView
    val arguments: List<NamedNavArgument>
        get() = listOf()
    val deepLink: List<String>
        get() = listOf()
    val absoluteDeepLinks: List<NavDeepLink>
        get() = deepLink.map { navDeepLink { uriPattern = "$uri${it}" } }
    private val route: Route
        get() = Route(this, arguments.map { it.name })

    fun getRouteUrlWithParams(): String {
        return route.routeUrlWithParams
    }

    fun getRouteWithParams(vararg parameters: Pair<String, String?>): NavigationDestination {
        return route.getRouteWithParams(*parameters)
    }
}
```
- The Interface requires a `screen` that will be used to draw the correct screen.
- `arguments` will help in registering the arguments that belong to the screen on navigation
- `deepLink` will provide the list of deep-links belonging to the screen, while absoluteDeepLinks provides the whole path with the uri.
- `getRouteUrlWithParams` will provide the url that is being used to register the screens
- `getRouteWithParams` will provide the url with all the provided parameters injected into the route

### NavGraphView
The `NavGraphView` can be used to register a normal compose view, a dialog or a bottom sheet:
```kotlin
sealed class NavGraphView {
    data class BottomSheetGraphView(
        val navRoute: NavRoute,
        val content: @Composable ColumnScope.(backstackEntry: NavBackStackEntry) -> Unit
    ) : NavGraphView() {
        @OptIn(ExperimentalMaterialNavigationApi::class)
        override fun build(navGraphBuilder: NavGraphBuilder) {
            navGraphBuilder.bottomSheet(
                route = navRoute.getRouteUrlWithParams(),
                arguments = navRoute.arguments,
                deepLinks = navRoute.absoluteDeepLinks,
                content = content
            )
        }
    }

    data class ComposableGraphView(
        val navRoute: NavRoute,
        val content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
    ) : NavGraphView() {
        override fun build(navGraphBuilder: NavGraphBuilder) {
            navGraphBuilder.composable(
                route = navRoute.getRouteUrlWithParams(),
                arguments = navRoute.arguments,
                deepLinks = navRoute.absoluteDeepLinks,
                content = content
            )
        }
    }

    data class DialogGraphView(
        val navRoute: NavRoute,
        val content: @Composable (NavBackStackEntry) -> Unit
    ) : NavGraphView() {
        override fun build(navGraphBuilder: NavGraphBuilder) {
            navGraphBuilder.dialog(
                route = navRoute.getRouteUrlWithParams(),
                arguments = navRoute.arguments,
                deepLinks = navRoute.absoluteDeepLinks,
                content = content
            )
        }
    }

    abstract fun build(navGraphBuilder: NavGraphBuilder)
}
```
*DISCLAIMER: for bottom sheet I use the [library](https://github.com/google/accompanist/issues/1480) library which currently only supports material but doesn't support material3, there is an open [issue](https://github.com/google/accompanist/issues/1480) on the library but it doesn't look like it will be fixed soon*


### Implementing all the NavRoutes
After this, all of your navigation destinations should implement this interface, here is an easy example without arguments:

```kotlin
data object Home : NavRoute {
    fun get(): NavigationDestination = getRouteWithParams()
    override val screen = NavGraphView.ComposableGraphView(this) {
        HomeScreen()
    }
}
```
Basically, you need to override the `screen` variable getter, which creates your desired composable screen defining the type(simple composable, dialog, bottom sheet), and create a `get()` function that constructs your route.

Concerning scenarios involving arguments, it is imperative to define all the arguments and their respective types. Additionally, you must ensure that these parameters are added into the `arguments` variable and you have to create the corresponding `get()` function with the specified arguments.
```kotlin
data object ExtraData : NavRoute {
    private const val textParam = "text"
    private const val countParam = "count"
    override val screen = NavGraphView.ComposableGraphView(this) { backStackEntry ->
        ExtraDataScreen(
            backStackEntry.arguments?.getString(textParam) ?: "",
            backStackEntry.arguments?.getInt(countParam) ?: 0
        )
    }
    override val deepLink: List<String>
        get() = listOf(
            "extra?text={$textParam}&count={$countParam}",
            "extraData/{$textParam}?amount={$countParam}"
        )
    override val arguments: List<NamedNavArgument>
        get() = listOf(
            navArgument(textParam) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(countParam) {
                type = NavType.IntType
            }
        )
    fun get(title: String?, count: Int): NavigationDestination = getRouteWithParams(
        textParam to title,
        countParam to count.toString()
    )
}
```

### Deeplinks

If you also want to make it possible to deeplink into the page then you should add a `deeplinks` parameter to the router like this:
```kotlin
override val deepLink: List<String>
    get() = listOf(
        "extra?text={$textParam}&count={$countParam}",
        "extraData/{$textParam}?amount={$countParam}"
    )
```
After this you will be able to navigate to the the screen using the following deeplinks:
- `"deeplink://extra?text=Title%20name&count=20"`
- `"deeplink://extraData/Title%20example?amount=2"`,
- `"deeplink://extra?count=20"`

## The routing
The code for the `Router` is quite simple. It is basically just a channel with a dispatch function:
```kotlin
open class Router : CoroutineScope by CoroutineScope(Dispatchers.Main) {
    private val _eventChannel = Channel<NavigationType>(Channel.BUFFERED)
    val sharedFlow = _eventChannel.receiveAsFlow()

    open fun dispatch(navTarget: NavigationType) {
        launch {
            _eventChannel.send(navTarget)
        }
    }
}
```

The dispatched `NavigationType` in this example, supports the following actions:
```kotlin
sealed class NavigationType {
    data class DeeplinkTo(val href: String) : NavigationType()
    data class NavigateTo(val target: NavigationDestination, val navOptions: NavOptions? = null) : NavigationType()
    data object NavigateUp : NavigationType()
    data class PopUpTo(val target: Route, val inclusive: Boolean) : NavigationType()
    data class Snackbar(
        val message: String,
        val style: SnackBarStyle = SnackBarStyle.NEUTRAL,
        val actionLabel: String? = null,
        val withDismissAction: Boolean = false,
        val duration: SnackbarDuration = SnackbarDuration.Short,
        val onAction: (() -> Unit)? = null,
        val onDismiss: (() -> Unit)? = null,
    ) : NavigationType()
}
```

## Registering screens

At one point you the child classes of `NavRoute` have to be registered in the `NavHost` based on the navigation documentation provided by google. For this I decided to use reflection, to make sure nobody ever forgets to register a class.
To use reflection you have to add `implementation(kotlin("reflect"))` to our `gradle.kts` file.
After having reflection in the project you can just create this simple extension function:
```kotlin
fun NavGraphBuilder.registerScreens() {
    NavRoute::class.sealedSubclasses.forEach { it.objectInstance?.screen?.build(this) }
}
```
It will go through all of the classes that are implementing `NavRoute` class and call their `getScreen()` function.
after this you can just call this function in the `NavHost`:
```kotlin
NavHost(
    modifier = modifier,
    navController = navController,
    startDestination = startDestination
) {
    registerScreens()
}
```
*DISCLAIMER: to have it working on a production build you either have to mark all the classes implementing `NavRoute` with `@Keep` or add proguard rules that keep the files*

## Getting it all to work

The last piece of the puzzle is to make our `NavHostController` react on the actions that arrive when you call the `router.dispatch(navTarget: NavigationType)` function. For this you have to create a `LaunchedEffect` inside the `MainActivity` `setContent` that looks like this:

```kotlin
@Composable
fun ListenToNavigation(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onSnackBarStyleChanged: (SnackBarStyle) -> Unit,
    router: Router = koinInject(),
) {
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        router.sharedFlow.collect {
            when (it) {
                is NavigationType.NavigateUp -> navController.navigateUp()
                is NavigationType.PopUpTo -> navController.popBackStack(
                    it.target.getRouteUrlWithParams(),
                    it.inclusive
                )

                is NavigationType.NavigateTo -> navController.navigate(
                    it.target.route,
                    it.navOptions
                )

                is NavigationType.DeeplinkTo -> {
                    val uri = Uri.parse(it.href)
                    if (navController.graph.hasDeepLink(uri)) {
                        navController.navigate(uri)
                    }
                }

                is NavigationType.Snackbar -> {
                    onSnackBarStyleChanged(it.style)
                    coroutineScope.coroutineContext.cancelChildren()
                    coroutineScope.launch {
                        val snackbarResult = snackbarHostState.showSnackbar(
                            message = it.message,
                            actionLabel = it.actionLabel,
                            duration = it.duration,
                            withDismissAction = it.withDismissAction
                        )
                        when (snackbarResult) {
                            SnackbarResult.Dismissed -> {
                                it.onDismiss?.invoke()
                            }

                            SnackbarResult.ActionPerformed -> {
                                it.onAction?.invoke()
                            }
                        }
                    }
                }
            }
        }
    }
}

```

## Logic behind creating the route
```kotlin
class Route(baseRoute: NavRoute, vararg parameters: String, val deepLinks: List<String>? = null) {
    val routeWithParams: String
    private val routeName = baseRoute::class.java.canonicalName?.split(".")?.takeLast(2)?.joinToString(".") ?: ""

    init {
        this.routeWithParams = if (parameters.isNotEmpty()) {
            "$routeName?" + parameters.joinToString("&") { parameter ->
                "$parameter={$parameter}"
            }
        } else {
            routeName
        }
    }


    fun getRouteWithParams(vararg parameters: Pair<String, String?>): NavigationDestination {
        val route = if (parameters.isNotEmpty()) {
            "$routeName?" + parameters.filter { it.second != null }.joinToString("&") { item ->
                "${item.first}=${item.second}"
            }
        } else routeName

        return NavigationDestination(route)
    }
}
```
This class will generate all the navigation routes, meaning you don't have to worry about the naming. Currently, it expects that the `NavRoutes` are part of one or more collector objects. I prefer placing them in multiple files to avoid creating giant files.
```kotlin
object HomeScreens {
    data object Home: NavRoute ..
    data object ExtraData: NavRoute ..
    ..
}
object SettingsScreens {
    data object Settings : NavRoute ..
    data object Register : NavRoute ..
    ..
```
In the following scenario, the route name for home will be `HomeScreens.Home`. In the case of having arguments, it will be constructed like this: `HomeScreens.ExtraData?text={text}&count={count}`. As mentioned above for this, you have to keep these files when obfuscating. However, I found numerous advantages with this approach. By keeping the files, I can use reflection, eliminating the need to manually register all the screens. Additionally, the route names are distinct. During the development phase, I noticed that it's quite easy to forget to rename the route after using copy-pasting or to forget to register a screen. Therefore, I aimed to build a solution that prevents both cases, opting for reflection and using the class name for the route.
## Testing

An another advantage of making it possible to navigate from everywhere is testing. Now you are able to unit test the navigation. For this you can use the following mock object:
```kotlin
class RouterMock : Router() {
    var collectedActions: MutableList<NavigationType> = mutableListOf()

    override fun dispatch(navTarget: NavigationType){
        collectedActions.add(navTarget)
    }

    fun removeFirst() {
        collectedActions.removeFirst()
    }

    inline fun <reified Type> assert(current: ((action: Type) -> Boolean)): RouterMock {
        Assert.assertTrue(this.collectedActions.first() is Type)

        val act = this.collectedActions.first() as Type
        Assert.assertTrue(current(act))

        removeFirst()

        return this
    }

    inline fun <reified Type> assertNot(): RouterMock {
        Assert.assertFalse(collectedActions.firstOrNull() is Type)

        return this
    }

    inline fun <reified Type> assert(): RouterMock {
        Assert.assertTrue(collectedActions.first() is Type)

        removeFirst()

        return this
    }
}
```

And then you can test the navigation the following way:
```kotlin
router.assert<NavigationType.NavigateTo> {
    it.target == HomeScreens.ExtraData.get("test", 3)
}
router.assert<NavigationType.NavigateUp>()
```

## The project
The project is available on [github](https://github.com/katajona/compose-navigation) where you can see a bit more about how to get the navigation to  work, how to add a bottom navigation into our project and how to implement the snackbar.
