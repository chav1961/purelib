package chav1961.purelib.concurrent;

import chav1961.purelib.concurrent.SyncSchemesFactory.Transform;

interface MapperReducer<S,T> extends SyncSchemeControl, Transform<S,T> {
}
