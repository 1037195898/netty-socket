package com.com

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.Unpooled
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.Inflater
import kotlin.io.path.Path
import kotlin.io.path.inputStream

class ZlibTest {
    @Before
    fun before() {
        System.setProperty("rootDir", "D:\\WorkSpace\\Idea\\Java\\NettySocket/webClient")
    }

    @Test
    fun test() {
        val fileInputStream = Path("D:\\WorkSpace\\LayaBox\\OneGame\\assets\\common.res").inputStream()
        val b = ByteArray(fileInputStream.available())
        fileInputStream.read(b)


        val bytes = Unpooled.wrappedBuffer(b)
        val input = ByteBufInputStream(bytes)

        println(0x46475549)
        println(bytes.readUnsignedInt())
        val version: Int = input.readInt()
        println(version)
        val compressed: Boolean = input.readBoolean()
        println(compressed)
        println(input.readUTF())
        println(input.readUTF())
        input.skip(20)
        if (compressed) {
            throw Exception("Cannot use compression!")

            //            b = new byte[input.available()];
//            input.read(b);
//            b = decompress(b);
//
//            bytes = Unpooled.wrappedBuffer(b);
//            input = new ByteBufInputStream(bytes);
        }

        val ver2 = version >= 2
        val indexTablePos: Int = bytes.readerIndex()

        seek(input, bytes, indexTablePos, 4)

        var cnt: Int = input.readInt()
        stringTable = mutableListOf()
        for (i in 0 until cnt) stringTable?.add(input.readUTF())

        seek(input, bytes, indexTablePos, 0)
        cnt = input.readUnsignedShort()
        val _dependencies = mutableListOf<Map<String, String?>>()
        for (i in 0 until cnt) {
            val finalInput: ByteBufInputStream = input
            _dependencies.add(
                mapOf(
                    "id" to readS(finalInput),
                    "name" to readS(finalInput)
                )
            )
        }
        val branchIncluded: Boolean
        if (ver2) {
            cnt = input.readShort().toInt()
            if (cnt > 0) {
                this._branches = readSArray(input, cnt)
                if (!_branch.isEmpty()) this._branchIndex =
                    _branches!!.indexOf(_branch)
            }

            branchIncluded = cnt > 0
        }

        seek(input, bytes, indexTablePos, 1)

        cnt = input.readUnsignedShort()
        var nextPos: Int
        for (i in 0 until cnt) {
//            nextPos = input.readInt();
//            nextPos += bytes.readerIndex();
//
//            pi.type = buffer.readByte();
//            pi.id = buffer.readS();
//            pi.name = buffer.readS();
//            buffer.readS(); //path
//            str = buffer.readS();
//            if (str)
//                pi.file = str;
//            buffer.readBool();//exported
//            pi.width = buffer.getInt32();
//            pi.height = buffer.getInt32();
//
//            switch (pi.type) {
//                case PackageItemType.Image:
//                {
//                    pi.objectType = ObjectType.Image;
//                    var scaleOption: number = buffer.readByte();
//                    if (scaleOption == 1) {
//                        pi.scale9Grid = new Laya.Rectangle();
//                        pi.scale9Grid.x = buffer.getInt32();
//                        pi.scale9Grid.y = buffer.getInt32();
//                        pi.scale9Grid.width = buffer.getInt32();
//                        pi.scale9Grid.height = buffer.getInt32();
//
//                        pi.tileGridIndice = buffer.getInt32();
//                    }
//                    else if (scaleOption == 2)
//                        pi.scaleByTile = true;
//
//                    pi.smoothing = buffer.readBool();
//                    break;
//                }
//
//                case PackageItemType.MovieClip:
//                {
//                    pi.smoothing = buffer.readBool();
//                    pi.objectType = ObjectType.MovieClip;
//                    pi.rawData = buffer.readBuffer();
//                    break;
//                }
//
//                case PackageItemType.Font:
//                {
//                    pi.rawData = buffer.readBuffer();
//                    break;
//                }
//
//                case PackageItemType.Component:
//                {
//                    var extension: number = buffer.readByte();
//                    if (extension > 0)
//                        pi.objectType = extension;
//                    else
//                        pi.objectType = ObjectType.Component;
//                    pi.rawData = buffer.readBuffer();
//
//                    UIObjectFactory.resolvePackageItemExtension(pi);
//                    break;
//                }
//
//                case PackageItemType.Atlas:
//                case PackageItemType.Sound:
//                case PackageItemType.Misc:
//                {
//                    pi.file = path + pi.file;
//                    break;
//                }
//
//                case PackageItemType.Spine:
//                case PackageItemType.DragonBones:
//                {
//                    pi.file = shortPath + pi.file;
//                    pi.skeletonAnchor = new Laya.Point();
//                    pi.skeletonAnchor.x = buffer.getFloat32();
//                    pi.skeletonAnchor.y = buffer.getFloat32();
//                    break;
//                }
//            }
//
//            if (ver2) {
//                str = buffer.readS();//branch
//                if (str)
//                    pi.name = str + "/" + pi.name;
//
//                var branchCnt: number = buffer.getUint8();
//                if (branchCnt > 0) {
//                    if (branchIncluded)
//                        pi.branches = buffer.readSArray(branchCnt);
//                    else
//                        this._itemsById[buffer.readS()] = pi;
//                }
//
//                var highResCnt: number = buffer.getUint8();
//                if (highResCnt > 0)
//                    pi.highResolution = buffer.readSArray(highResCnt);
//            }
//
//            this._items.push(pi);
//            this._itemsById[pi.id] = pi;
//            if (pi.name != null)
//                this._itemsByName[pi.name] = pi;
//
//            buffer.pos = nextPos;
        }
    }

    private var _branchIndex = 0

    private fun readSArray(input: ByteBufInputStream, cnt: Int): MutableList<String?> {
        val ret = mutableListOf<String?>()
        for (i in 0 until cnt) ret.add(i, this.readS(input))
        return ret
    }

    var stringTable: MutableList<String>? = null
    var _branches: MutableList<String?>? = null

    private fun readS(input: ByteBufInputStream): String? {
        val index: Int = input.readUnsignedShort()
        return if (index == 65534) //null
            null
        else if (index == 65533) ""
        else stringTable!![index]
    }


    fun seek(input: ByteBufInputStream, bytes: ByteBuf, indexTablePos: Int, blockIndex: Int): Boolean {
        val tmp: Int = bytes.readerIndex()
        bytes.readerIndex(indexTablePos)
        val segCount: Int = input.read()
        if (blockIndex < segCount) {
            val useShort: Boolean = input.readBoolean()
            val newPos: Int
            if (useShort) {
                input.skip(2L * blockIndex)
                newPos = input.readUnsignedShort()
            } else {
                input.skip(4L * blockIndex)
                newPos = bytes.readInt()
            }

            if (newPos > 0) {
                bytes.readerIndex(indexTablePos + newPos)
                return true
            } else {
                bytes.readerIndex(tmp)
                return false
            }
        } else {
            bytes.readerIndex(tmp)
            return false
        }
    }

    companion object {

        var _branch: String = ""

        /**
         * 解压缩
         * @param data 需要解压缩的字节数组
         */
        fun decompress(data: ByteArray): ByteArray {
            val output: ByteArray
            val inflater = Inflater()
            inflater.setInput(data)
            ByteArrayOutputStream(data.size).use {
                while (!inflater.finished()) {
                    val i: Int = inflater.inflate(data)
                    it.write(data, 0, i)
                }
                output = it.toByteArray()
            }
            inflater.end()
            return output
        }
    }
}
