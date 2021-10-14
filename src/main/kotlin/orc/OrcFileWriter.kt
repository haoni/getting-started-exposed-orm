package orc

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hive.common.type.HiveDecimal
import org.apache.hadoop.hive.ql.exec.vector.*
import org.apache.orc.OrcFile
import org.apache.orc.TypeDescription
import java.io.IOException
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.sql.Timestamp
import java.util.*
import java.util.function.BiConsumer

object OrcFileWriter {

    @Throws(IOException::class)
    fun write(configuration: Configuration?, path: String?, struct: String?, data: List<Map<String, Any>>) {
        // Create the schemas and extract metadata from the schema
        val schema = TypeDescription.fromString(struct)
        val fieldNames = schema.fieldNames
        val columnTypes = schema.children

        // Create a row batch
        val batch = schema.createRowBatch()

        // Get the column vector references
        val consumers: MutableList<BiConsumer<Int, Any?>> = ArrayList(columnTypes.size)
        for (i in columnTypes.indices) {
            val type = columnTypes[i]
            val vector = batch.cols[i]
            consumers.add(createColumnWriter(type, vector))
        }
        OrcFile.createWriter(
            Path(path),
            OrcFile.writerOptions(configuration)
                .setSchema(schema)
        ).use { writer ->
            for (row in data) {
                // batch.size should be increased externally
                val rowNum = batch.size++

                // Write each column to the associated column vector
                for (i in fieldNames.indices) {
                    consumers[i].accept(rowNum, row[fieldNames[i]])
                }

                // If the buffer is full, write it to disk
                if (batch.size == batch.maxSize) {
                    writer.addRowBatch(batch)
                    batch.reset()
                }
            }

            // Check unwritten rows before closing
            if (batch.size != 0) {
                writer.addRowBatch(batch)
            }
        }
    }

    fun createColumnWriter(description: TypeDescription, columnVector: ColumnVector): BiConsumer<Int, Any?> {
        val type = description.category.getName()
        val consumer: BiConsumer<Int, Any?>
        if ("tinyint" == type) {
            consumer =
                BiConsumer { row: Int?, `val`: Any? ->
                    (columnVector as LongColumnVector).vector[row!!] = (`val` as Number?)!!.toLong()
                }
        } else if ("smallint" == type) {
            consumer =
                BiConsumer { row: Int?, `val`: Any? ->
                    (columnVector as LongColumnVector).vector[row!!] = (`val` as Number?)!!.toLong()
                }
        } else if ("int" == type || "date" == type) {
            // Date is represented as int epoch days
            consumer = BiConsumer { row: Int?, `val`: Any? ->
                (columnVector as LongColumnVector).vector[row!!] = (`val` as Number?)!!.toLong()
            }
        } else if ("bigint" == type) {
            consumer =
                BiConsumer { row: Int?, `val`: Any? ->
                    (columnVector as LongColumnVector).vector[row!!] = (`val` as Number?)!!.toLong()
                }
        } else if ("boolean" == type) {
            consumer =
                BiConsumer { row: Int?, `val`: Any? ->
                    (columnVector as LongColumnVector).vector[row!!] = if ((`val` as Boolean?)!!) 1 else 0
                }
        } else if ("float" == type) {
            consumer =
                BiConsumer { row: Int?, `val`: Any? ->
                    (columnVector as DoubleColumnVector).vector[row!!] = (`val` as Number?)!!.toFloat().toDouble()
                }
        } else if ("double" == type) {
            consumer =
                BiConsumer { row: Int?, `val`: Any? ->
                    (columnVector as DoubleColumnVector).vector[row!!] = (`val` as Number?)!!.toDouble()
                }
        } else if ("decimal" == type) {
            consumer =
                BiConsumer { row: Int?, `val`: Any? ->
                    (columnVector as DecimalColumnVector).vector[row!!].set(HiveDecimal.create(`val` as BigDecimal?))
                }
        } else if ("string" == type || type.startsWith("varchar") || "char" == type) {
            consumer = BiConsumer { row: Int?, `val`: Any? ->
                val buffer = `val`.toString().toByteArray(StandardCharsets.UTF_8)
                (columnVector as BytesColumnVector).setRef(row!!, buffer, 0, buffer.size)
            }
        } else if ("timestamp" == type) {
            consumer =
                BiConsumer { row: Int?, `val`: Any? ->
                    (columnVector as TimestampColumnVector)[row!!] = `val` as Timestamp?
                }
        } else {
            throw RuntimeException("Unsupported type $type")
        }
        return consumer
    }
}