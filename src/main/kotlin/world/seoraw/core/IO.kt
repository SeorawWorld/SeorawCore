package world.seoraw.core

import java.io.File
import java.util.zip.ZipFile

fun newFile(file: File, path: String, create: Boolean = true, folder: Boolean = false): File {
    return newFile(File(file, path), create, folder)
}

fun newFile(path: String, create: Boolean = true, folder: Boolean = false): File {
    return newFile(File(path), create, folder)
}

fun newFile(file: File, create: Boolean = true, folder: Boolean = false): File {
    if (!file.parentFile.exists()) {
        file.parentFile.mkdirs()
    }
    if (!file.exists() && create) {
        if (folder) {
            file.mkdirs()
        } else {
            file.createNewFile()
        }
    }
    return file
}

fun File.unzip(target: File) {
    unzip(target.path)
}

fun File.unzip(destDirPath: String) {
    ZipFile(this).use { zipFile ->
        zipFile.stream().forEach { entry ->
            if (entry.isDirectory) {
                newFile(destDirPath + "/" + entry.name).mkdirs()
            } else {
                zipFile.getInputStream(entry).use {
                    newFile(destDirPath + "/" + entry.name).writeBytes(it.readBytes())
                }
            }
        }
    }
}

fun File.deepDelete() {
    if (exists()) {
        if (isDirectory) {
            listFiles()?.forEach { it.deepDelete() }
        }
        delete()
    }
}