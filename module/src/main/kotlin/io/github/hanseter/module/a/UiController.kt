package io.github.hanseter.module.a

import io.github.hanseter.startup.api.MainWindowProvider
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.osgi.service.component.annotations.Activate
import org.osgi.service.component.annotations.Component
import org.osgi.service.component.annotations.Reference

@Component(immediate = true)
class UiController @Activate constructor(
    @Reference windowProvider: MainWindowProvider
) {

    init {
        windowProvider.window.scene.root = VBox(
            Button("Some Button").apply {
                setOnAction { doSomethingWithCoroutines() }
            },
            TextField()
        )
    }

    private fun doSomethingWithCoroutines() {
        Thread {
            println("Start")
            runBlocking(Dispatchers.IO) {
                (0..50).map {
                    async { Thread.sleep(100) }
                }.lastOrNull()?.await()
            }
            println("Done - please check log for exceptions")
        }
            .apply { isDaemon = true }
            .start()
    }
}