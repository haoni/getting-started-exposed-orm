package orc

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hive.ql.exec.vector.*
import org.apache.orc.OrcFile
import org.apache.orc.TypeDescription
import java.io.IOException
import java.util.*
import java.util.function.BiFunction


object OrcFileReader {
    private const val BATCH_SIZE = 2048

    @Throws(IOException::class)
    fun read(configuration: Configuration?, path: String?): List<Map<String, Any?>> {
        // Create a list to collect rows
        val rows: MutableList<Map<String, Any?>> = LinkedList()
        OrcFile.createReader(Path(path), OrcFile.readerOptions(configuration)).use { reader ->
            // Extract the schema and metadata from the reader
            val schema = reader.schema
            val fieldNames = schema.fieldNames
            val columnTypes = schema.children

            // Get the column vector references
            val size = fieldNames.size
            val mappers: Array<BiFunction<ColumnVector, Int, Any>?> = arrayOfNulls(size)
            for (i in 0 until size) {
                val type = columnTypes[i]
                mappers[i] = createColumnReader(type)
            }

            reader.rows(reader.options()).use { records ->
                // Read rows in batch for better performance.
                val batch = reader.schema.createRowBatch(BATCH_SIZE)
                while (records.nextBatch(batch)) {
                    for (row in 0 until batch.size) {
                        // Read rows from the batch
                        val map: MutableMap<String, Any?> = HashMap()
                        for (col in 0 until batch.numCols) {
                            val columnVector = batch.cols[col]
                            if (columnVector.isNull[row]) {
                                map[fieldNames[col]] = null
                            } else {
                                val value = mappers[col]?.apply(columnVector, row)
                                map[fieldNames[col]] = value
                            }
                        }
                        rows.add(map)
                    }
                }
            }
        }
        return rows
    }

    fun createColumnReader(description: TypeDescription): BiFunction<ColumnVector, Int, Any> {
        val type = description.category.getName()
        val mapper: BiFunction<ColumnVector, Int, Any>
        mapper = if ("tinyint" == type) {
            BiFunction { columnVector: ColumnVector, row: Int? ->
                (columnVector as LongColumnVector).vector[row!!].toByte()
            }
        } else if ("smallint" == type) {
            BiFunction { columnVector: ColumnVector, row: Int? ->
                (columnVector as LongColumnVector).vector[row!!].toShort()
            }
        } else if ("int" == type || "date" == type) {
            // Date is represented as int epoch days
            BiFunction { columnVector: ColumnVector, row: Int? ->
                (columnVector as LongColumnVector).vector[row!!].toInt()
            }
        } else if ("bigint" == type) {
            BiFunction { columnVector: ColumnVector, row: Int? ->
                (columnVector as LongColumnVector).vector[row!!]
            }
        } else if ("boolean" == type) {
            BiFunction { columnVector: ColumnVector, row: Int? -> (columnVector as LongColumnVector).vector[row!!].equals(1)
            }
        } else if ("float" == type) {
            BiFunction { columnVector: ColumnVector, row: Int? ->
                (columnVector as DoubleColumnVector).vector[row!!].toFloat()
            }
        } else if ("double" == type) {
            BiFunction { columnVector: ColumnVector, row: Int? ->
                (columnVector as DoubleColumnVector).vector[row!!]
            }
        } else if ("decimal" == type) {
            BiFunction { columnVector: ColumnVector, row: Int? ->
                (columnVector as DecimalColumnVector).vector[row!!].hiveDecimal.bigDecimalValue()
            }
        } else if ("string" == type || type.startsWith("varchar")) {
            BiFunction { columnVector: ColumnVector, row: Int? ->
                (columnVector as BytesColumnVector).toString(
                    row!!
                )
            }
        } else if ("char" == type) {
            BiFunction { columnVector: ColumnVector, row: Int? ->
                (columnVector as BytesColumnVector).toString(
                    row!!
                )[0]
            }
        } else if ("timestamp" == type) {
            BiFunction { columnVector: ColumnVector, row: Int? ->
                (columnVector as TimestampColumnVector).getTimestampAsLong(
                    row!!
                )
            }
        } else {
            throw RuntimeException("Unsupported type $type")
        }
        return mapper
    }
}

