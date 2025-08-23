/**
 * This file will fail with any formatter profile.
 * @param some some string
 * @param record some other string
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

    public
    String
    getRecord()
    {
        return record;
    }
}
