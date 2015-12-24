package misc1.commons.ph;

import com.google.common.collect.ImmutableMap;
import misc1.commons.ds.Struct;
import misc1.commons.ds.StructKey;

class ProcessHelperStruct extends Struct<ProcessHelperStruct, ProcessHelper> {
    public ProcessHelperStruct(ImmutableMap<StructKey<ProcessHelperStruct, ?, ?>, Object> map) {
        super(ProcessHelper.TYPE, map);
    }
}
