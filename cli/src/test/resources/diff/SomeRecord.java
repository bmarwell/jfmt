import java.lang.String;

import static io.github.bmarwell.jdtfmt.format.FormatterMode.DIFF;

import io.github.bmarwell.jdtfmt.writer.OutputWriter;

/**
 * This file will fail with any formatter profile.
 * @param some
 * @param record
 */
public record SomeRecord
        (
        String some
        , String record

)
{

    public String
    getSome()
{
        return some;
    }

    public String
    getRecord()
{
        return record;
    }
}
