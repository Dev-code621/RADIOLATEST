import android.util.Log
import com.radiogapp.app.interfaces.MetadataListener
import java.io.FilterInputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.util.regex.Matcher
import java.util.regex.Pattern

class MetaExtractor(
    private val   inputStream: InputStream,
    private val interval: Int,
    private val characterEncoding: String? = "UTF-8",
    private val metadataListener: MetadataListener?
) : FilterInputStream(inputStream) {
    private val TAG = "Taggg"
    private var remaining: Int = interval

    override fun read(): Int {
        val ret = super.read()
        if (--remaining == 0) {
            getMetadata()
        }
        return ret
    }

    override fun read(buffer: ByteArray, offset: Int, len: Int): Int {
        val ret = super.read(buffer, offset, Math.min(remaining, len))
        if (remaining == ret) {
            getMetadata()
        } else {
            remaining -= ret
        }
        return ret
    }

    private fun getMetadata() {
        remaining = interval
        val size = super.read()

        // Either no metadata or EOF
        if (size < 1) return

        val buffer = ByteArray(size * 16)
        val readSize = readFully(buffer, 0, size * 16)

        // Find the string end
        var stringEnd = readSize
        for (i in 0 until readSize) {
            if (buffer[i] == 0.toByte()) {
                stringEnd = i
                break
            }
        }

        val charset = Charset.forName(characterEncoding ?: "UTF-8")
        val metadata: String = String(buffer, 0, stringEnd, charset)

        Log.d(TAG, "Metadata string: $metadata")

        parseMetadata(metadata)
    }

    private fun readFully(buffer: ByteArray, offset: Int, size: Int): Int {
        var n = 0
        var currentOffset = offset
        var remainingSize = size

        while (remainingSize > 0 && inputStream.read(buffer, currentOffset, remainingSize).also { n = it } != -1) {
            currentOffset += n
            remainingSize -= n
        }

        return currentOffset - offset
    }



    private fun parseMetadata(data: String) {
        val match: Matcher = Pattern.compile("StreamTitle='([^;]*)'").matcher(data.trim())
        if (match.find()) {
            val metadata = match.group(1).split(" - ")
            when (metadata.size) {
                3 -> metadataReceived(metadata[1], metadata[2], metadata[0])
                2 -> metadataReceived(metadata[0], metadata[1], null)
                1 -> metadataReceived(null, null, metadata[0])
            }
        }
    }

    private fun metadataReceived(artist: String?, song: String?, show: String?) {
        Log.i(TAG, "Metadata received:")
        Log.i(TAG, "Show: $show")
        Log.i(TAG, "Artist: $artist")
        Log.i(TAG, "Song: $song")

        metadataListener?.onMetadataReceived(artist, song, show)
    }
}
