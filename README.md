# Compose Navigation with Ease from Everywhere

In this guide, I will explain how we've managed to use Compose Navigation seamlessly throughout our apps. From view models and services to screens, and all of that type safe.

If you haven't familiarized yourself with Compose Navigation, it's crucial to read the original [documentation](https://developer.android.com/jetpack/compose/navigation) first.

This guide strongly advises against using complex arguments, and my solution aligns with this recommendation:
> It is strongly advised not to pass around complex data objects when navigating, but instead pass the minimum necessary information, such as a unique identifier or other form of ID, as arguments when performing navigation actions

However, if there is a need to pass complex data between screens, you can use `SavedStateHandle` in your view model while still adhering to the principles outlined in this guide.

## The Goal

By the end of this guide, you will have the capability to seamlessly navigate between screens, dialogs, and even bottom sheets with just a single line:
```
router.dispatch(NavigationType.NavigateTo(SettingsScreens.Register.get()))
```
Or if you want to also pass in some parameters then like this:
```
router.dispatch(NavigationType.NavigateTo(HomeScreens.ExtraData.get("MyTitle", 5)))
```
What about navigating back? Easy:
```
router.dispatch(NavigationType.NavigateUp)
```
Deeplinks? No problem:
```
router.dispatch(NavigationType.DeeplinkTo("deeplink://extra?text=Title%20name&count=20"))
```
Snackbars? Sure:
```
router.dispatch(NavigationType.Snackbar("Show my pretty snackbar"))
```

In the end, only your imagination limits what these actions can achieve. You can modify the `NavigationType` to also support opening a web view, or you can initiate an intent to open the sharing component.

## Building Our Navigation Graph

Initially, we need to construct a catalog of our screens, which we can utilize in the future for navigating to specific screens. To establish this catalog, I have created the following interface:

```
private const val uri = "deeplink://"
sealed interface NavRoute {

    val route: Route

    fun getArguments(): List<NamedNavArgument> = listOf()

    fun getScreen(navGraph: NavGraphBuilder)

    fun getDeepLinks(): List<NavDeepLink> {
        return route.deepLinks?.map { navDeepLink { uriPattern = "$uri${it}" } } ?: listOf()
    }
}
```
Now, all of our navigation destinations should implement this interface, here is on easy example without arguments:

```
data object Home : NavRoute {
    override val route = Route(this)
    fun get(): NavigationDestination = route.getRouteWithParams()
    override fun getScreen(navGraph: NavGraphBuilder) {
        navGraph.composable(this) {
            HomeScreen()
        }
    }
}
```
Basically, you need to override the `getScreen()` function, which initializes your desired composable screen, and create a `get()` function that constructs your route.

Concerning scenarios involving arguments, it is imperative to define all the arguments and their respective types. Additionally, you must ensure that these parameters are added into your navigation route. This means adding them as constructor parameters to the Route and creating a corresponding `get()` function with the specified arguments.

```
data object ExtraData : NavRoute {
    private const val textParam = "text"
    private const val countParam = "count"
    override val route = Route(
        this,
        textParam,
        countParam
    )

    fun get(title: String?, count: Int): NavigationDestination = route.getRouteWithParams(
        textParam to title,
        countParam to count.toString()
    )

    override fun getArguments(): List<NamedNavArgument> {
        return listOf(
            navArgument(textParam) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(countParam) {
                type = NavType.IntType
            }
        )
    }

    override fun getScreen(navGraph: NavGraphBuilder) {
        navGraph.composable(this) { backStackEntry ->
            ExtraDataScreen(
                backStackEntry.arguments?.getString(textParam) ?: "",
                backStackEntry.arguments?.getInt(countParam) ?: 0
            )
        }
    }
}
```
### Dialogs

You can also use navigation to open dialogs, you would just have to use
`navGraph.dialog(this)` instead of `navGraph.composable(this)`

### Bottom Sheets

If you also want to be able to navigate to a bottom sheet you could use the  [acccompanist](https://google.github.io/accompanist/navigation-material/) library and use `navGraph.bottomSheet(this)` instead of `navGraph.composable(this)`

*DISCLAIMER: currently accompanist only supports `material` but doesn't support `material3`, there is an open issue on the [library](https://github.com/google/accompanist/issues/1480) but it doesn't look like it will be fixed soon*

### Deeplinks

If you also want to make it possible to deeplink into the page then you should add a `deeplinks` parameter to the router like this:
```
override val route = Route(
    this,
    textParam,
    countParam,
    deepLinks = listOf(
        "extra?text={$textParam}&count={$countParam}",
        "extraData/{$textParam}?amount={$countParam}"
    )
)
```
After this you will be able to navigate to the the screen using the following deeplinks:
- `"deeplink://extra?text=Title%20name&count=20"`
- `"deeplink://extraData/Title%20example?amount=2"`,
- `"deeplink://extra?count=20"`

## The routing
The code for the `Router` is quite simple. It is basically just a channel with a dispatch function:
```
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

And the router expect `NavigationType` to be dispatched. In this example our navigation support the following actions:
```
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

At one point you have to register the classes we created at the `NavHost`. For this I decided to use reflection, to make sure nobody ever forgets to register a class.
To use reflection we have to add `implementation(kotlin("reflect"))` to our `gradle.kts` file.
After having reflection in the project you can just create this simple extension function:
```
fun NavGraphBuilder.registerScreens() {
    NavRoute::class.sealedSubclasses.forEach { it.objectInstance?.getScreen(this) }
}
```
It will go through all of the classes that are implementing `NavRoute` class and call their `getScreen()` function.
after this you can just call this function in the `NavHost`:
```
NavHost(
    modifier = modifier,
    navController = navController,
    startDestination = startDestination
) {
    registerScreens()
}
```
*DISCLAIMER: to have it working on a production build you either have to mark all the classes implementing `NavRoute` with `@Keep` or add proguard rules that keep the files*


#### NavGraphBuilder extensions
As mentioned above the `getScreen` can use `navGraph.composable(this)`, `navGraph.dialog(this)` or `navGraph.bottomSheet(this)` for this I wrote the following extension function to avoid some tedious work:
```
@OptIn(ExperimentalMaterialNavigationApi::class)
fun NavGraphBuilder.bottomSheet(
    navRoute: NavRoute,
    content: @Composable ColumnScope.(backstackEntry: NavBackStackEntry) -> Unit,
) {
    bottomSheet(
        route = navRoute.route.routeWithParams,
        arguments = navRoute.getArguments(),
        deepLinks = navRoute.getDeepLinks(),
        content = content
    )
}

fun NavGraphBuilder.composable(
    navRoute: NavRoute,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = navRoute.route.routeWithParams,
        arguments = navRoute.getArguments(),
        deepLinks = navRoute.getDeepLinks(),
        content = content
    )
}

fun NavGraphBuilder.dialog(
    navRoute: NavRoute,
    content: @Composable (NavBackStackEntry) -> Unit,
) {
    dialog(
        route = navRoute.route.routeWithParams,
        arguments = navRoute.getArguments(),
        deepLinks = navRoute.getDeepLinks(),
        content = content
    )
}
```

## Getting it all to work

The last peace of the puzzle is to make our `NavHostController` to react on the actions that arrive when we call the `router.dispatch(navTarget: NavigationType)` function. For this we have to create a launched effect inside the `MainActivity` `setContent` that looks like this:

```
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
                    it.target.routeWithParams,
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
```
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
```
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
In the following scenario, the route name for home will be `HomeScreens.Home`. In the case of having arguments, it will be constructed like this: `HomeScreens.ExtraData?text={text}&count={count}`. As mentioned above for this, we have to keep these files when obfuscating. However, I found numerous advantages with this approach. By keeping the files, we can use reflection, eliminating the need to manually register all the screens. Additionally, the route names are distinct. During the development phase, I noticed that it's quite easy to forget to rename the route after using copy-pasting or to forget to register a screen. Therefore, I aimed to build a solution that prevents both cases, opting for reflection and using the class name for the route.

## Testing

An another advantage of making it possible to navigate from everywhere is testing. Now we are able to unit test the navigation. For this we can use the following mock object:
```
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

And then we can test the navigation the following way:
```
router.assert<NavigationType.NavigateTo> {
    it.target == HomeScreens.ExtraData.get("test", 3)
}
router.assert<NavigationType.NavigateUp>()
```

## The project
The project is available on [github](https://github.com/katajona/compose-navigation) where you can see a bit more about how to get the navigation to  work, how to add a bottom navigation into our project and how to implement the snackbar.
