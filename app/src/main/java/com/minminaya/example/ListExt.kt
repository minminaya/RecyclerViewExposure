import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import java.util.concurrent.Executors

/**
 * 扩展 ItemCallback 实现 AsyncDifferConfig
 * @receiver DiffUtil.ItemCallback<T>
 * @return AsyncDifferConfig<T>
 */
fun <T> DiffUtil.ItemCallback<T>.asConfig(): AsyncDifferConfig<T> {
    return AsyncDifferConfig.Builder(this)
        .setBackgroundThreadExecutor(Executors.newFixedThreadPool(1))
        .build()
}