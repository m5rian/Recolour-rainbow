import kotlinx.coroutines.*
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.ForkJoinPool
import javax.imageio.ImageIO

/**
 * C:\Users\Marian\Desktop\coolfile.png
 */
suspend fun main() {
    val scope = CoroutineScope(ForkJoinPool.commonPool().asCoroutineDispatcher())
    val reader = BufferedReader(InputStreamReader(System.`in`))

    println("Specify path of image")
    val rawFile = reader.readLine().removePrefix("\"").removeSuffix("\"")
    val file = File(rawFile)
    if (!file.exists()) return println("This path doesn't exist")

    println("Enter amount of iteration")
    val rawIterations = reader.readLine()
    val iterations: Int = rawIterations.toIntOrNull() ?: return println("This path doesn't exist")
    if (iterations <= 0 || iterations > 360) return println("Your iterations have to stay between 1 and 360")
    val increaseHuePerIteration = 1f / iterations

    val image = ImageIO.read(file)
    val rainbowImage = BufferedImage(image.width, image.height * iterations, BufferedImage.TYPE_INT_ARGB)

    val tasks = mutableListOf<Deferred<Unit>>()
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            tasks.add(scope.async {
                recolourPixel(iterations, increaseHuePerIteration, image, rainbowImage, x, y)
            })
        }
    }
    tasks.awaitAll()

    val newFile = File(file.parentFile.path + File.separator + "rainbow.png")
    newFile.createNewFile()
    ImageIO.write(rainbowImage, "png", newFile)
    println("Done! New file is located at ${newFile.path}")
}

fun recolourPixel(iterations: Int, increaseHuePerIteration: Float, oldImage: BufferedImage, newImage: BufferedImage, x: Int, y: Int) {
    val pixel = Color(oldImage.getRGB(x, y), true)
    if (pixel.alpha == 0) return
    val hsb = Color.RGBtoHSB(pixel.red, pixel.green, pixel.blue, null)

    var currentHue: Float = hsb[0]
    repeat(iterations) { iteration ->
        currentHue += increaseHuePerIteration
        if (currentHue > 1) currentHue -= 1

        val iterationHeight = iteration * oldImage.height + y
        newImage.setRGB(x, iterationHeight, Color.HSBtoRGB(currentHue, hsb[1], hsb[2]))
    }
}