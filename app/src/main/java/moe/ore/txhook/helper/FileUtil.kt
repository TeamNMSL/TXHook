/*
 * English :
 *  The project is protected by the MPL open source agreement.
 * Open source agreement warning that prohibits deletion of project source code files.
 * The project is prohibited from acting in illegal areas.
 * All illegal activities arising from the use of this project are the responsibility of the second author, and the original author of the project is not responsible
 *
 *  中文：
 *  该项目由MPL开源协议保护。
 *  禁止删除项目源代码文件的开源协议警告内容。
 * 禁止使用该项目在非法领域行事。
 * 使用该项目产生的违法行为，由使用者或第二作者全责，原作者免责
 *
 * 日本语：
 * プロジェクトはMPLオープンソース契約によって保護されています。
 *  オープンソース契約プロジェクトソースコードファイルの削除を禁止する警告。
 * このプロジェクトは違法地域の演技を禁止しています。
 * このプロジェクトの使用から生じるすべての違法行為は、2番目の著者の責任であり、プロジェクトの元の著者は責任を負いません。
 *
 */

package moe.ore.util

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtil {
    @JvmStatic
    fun has(filePath: String) = File(filePath).exists()

    @JvmStatic
    fun readFileBytes(f: File): ByteArray = f.readBytes()

    @JvmStatic
    fun readFileBytes(filePath: String) = readFileBytes(File(filePath).also { f ->
        check(f.exists()) { "file not exits" }
        check(f.isFile) { "File is not a file" }
        check(f.canRead()) { "file can not read" }
    })

    @JvmStatic fun readFileString(f: File) = String(readFileBytes(f))

    @JvmStatic fun readFileString(path: String) = String(readFileBytes(path))

    @JvmStatic
    fun saveFile(path: String, content: String) = saveFile(path, content.toByteArray())

    @JvmStatic
    fun saveFile(path: String, content: ByteArray) {
        val file = File(path)
        if (!file.exists()) {
            checkFileExists(file.parentFile) { exists ->
                if(!exists) mkdirs()
            }
            if (!file.createNewFile()) return
        }
        file.writeBytes(content)
    }

    @JvmStatic
    fun saveFile(path: String, content: InputStream) {
        val file = File(path)
        if (!file.exists()) {
            checkFileExists(file.parentFile) { exists ->
                if(!exists) mkdirs()
            }
            if (!file.createNewFile()) return
        }
        FileOutputStream(file).use { out ->
            content.use {
                var len: Int
                val bytes = ByteArray(1024)
                while (true) {
                    len = it.read(bytes)
                    if(len != -1) {
                        out.write(bytes, 0, len)
                    } else {
                        break
                    }
                }
                out.flush()
            }
        }
    }

    @JvmStatic
    private fun checkFileExists(file: File, block: File.(Boolean) -> Unit) = block.invoke(file, file.exists())
}