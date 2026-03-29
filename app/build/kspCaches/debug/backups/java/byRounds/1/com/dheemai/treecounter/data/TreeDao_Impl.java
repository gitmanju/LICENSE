package com.dheemai.treecounter.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TreeDao_Impl implements TreeDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Tree> __insertionAdapterOfTree;

  private final EntityDeletionOrUpdateAdapter<Tree> __deletionAdapterOfTree;

  private final EntityDeletionOrUpdateAdapter<Tree> __updateAdapterOfTree;

  private final SharedSQLiteStatement __preparedStmtOfDeleteTreesByPlotId;

  public TreeDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTree = new EntityInsertionAdapter<Tree>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `trees` (`id`,`species`,`notes`,`latitude`,`longitude`,`photoPath`,`timestamp`,`additionalNames`,`plotName`,`plotId`,`canvasX`,`canvasY`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Tree entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSpecies());
        statement.bindString(3, entity.getNotes());
        statement.bindDouble(4, entity.getLatitude());
        statement.bindDouble(5, entity.getLongitude());
        if (entity.getPhotoPath() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getPhotoPath());
        }
        statement.bindLong(7, entity.getTimestamp());
        statement.bindString(8, entity.getAdditionalNames());
        statement.bindString(9, entity.getPlotName());
        statement.bindLong(10, entity.getPlotId());
        statement.bindDouble(11, entity.getCanvasX());
        statement.bindDouble(12, entity.getCanvasY());
      }
    };
    this.__deletionAdapterOfTree = new EntityDeletionOrUpdateAdapter<Tree>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `trees` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Tree entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfTree = new EntityDeletionOrUpdateAdapter<Tree>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `trees` SET `id` = ?,`species` = ?,`notes` = ?,`latitude` = ?,`longitude` = ?,`photoPath` = ?,`timestamp` = ?,`additionalNames` = ?,`plotName` = ?,`plotId` = ?,`canvasX` = ?,`canvasY` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Tree entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSpecies());
        statement.bindString(3, entity.getNotes());
        statement.bindDouble(4, entity.getLatitude());
        statement.bindDouble(5, entity.getLongitude());
        if (entity.getPhotoPath() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getPhotoPath());
        }
        statement.bindLong(7, entity.getTimestamp());
        statement.bindString(8, entity.getAdditionalNames());
        statement.bindString(9, entity.getPlotName());
        statement.bindLong(10, entity.getPlotId());
        statement.bindDouble(11, entity.getCanvasX());
        statement.bindDouble(12, entity.getCanvasY());
        statement.bindLong(13, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteTreesByPlotId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM trees WHERE plotId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final Tree tree, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTree.insertAndReturnId(tree);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Tree tree, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTree.handle(tree);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Tree tree, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTree.handle(tree);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteTreesByPlotId(final long plotId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteTreesByPlotId.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, plotId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteTreesByPlotId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Tree>> getAllTrees() {
    final String _sql = "SELECT * FROM trees ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"trees"}, new Callable<List<Tree>>() {
      @Override
      @NonNull
      public List<Tree> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSpecies = CursorUtil.getColumnIndexOrThrow(_cursor, "species");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfAdditionalNames = CursorUtil.getColumnIndexOrThrow(_cursor, "additionalNames");
          final int _cursorIndexOfPlotName = CursorUtil.getColumnIndexOrThrow(_cursor, "plotName");
          final int _cursorIndexOfPlotId = CursorUtil.getColumnIndexOrThrow(_cursor, "plotId");
          final int _cursorIndexOfCanvasX = CursorUtil.getColumnIndexOrThrow(_cursor, "canvasX");
          final int _cursorIndexOfCanvasY = CursorUtil.getColumnIndexOrThrow(_cursor, "canvasY");
          final List<Tree> _result = new ArrayList<Tree>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Tree _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSpecies;
            _tmpSpecies = _cursor.getString(_cursorIndexOfSpecies);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final String _tmpPhotoPath;
            if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
              _tmpPhotoPath = null;
            } else {
              _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpAdditionalNames;
            _tmpAdditionalNames = _cursor.getString(_cursorIndexOfAdditionalNames);
            final String _tmpPlotName;
            _tmpPlotName = _cursor.getString(_cursorIndexOfPlotName);
            final long _tmpPlotId;
            _tmpPlotId = _cursor.getLong(_cursorIndexOfPlotId);
            final float _tmpCanvasX;
            _tmpCanvasX = _cursor.getFloat(_cursorIndexOfCanvasX);
            final float _tmpCanvasY;
            _tmpCanvasY = _cursor.getFloat(_cursorIndexOfCanvasY);
            _item = new Tree(_tmpId,_tmpSpecies,_tmpNotes,_tmpLatitude,_tmpLongitude,_tmpPhotoPath,_tmpTimestamp,_tmpAdditionalNames,_tmpPlotName,_tmpPlotId,_tmpCanvasX,_tmpCanvasY);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getById(final long id, final Continuation<? super Tree> $completion) {
    final String _sql = "SELECT * FROM trees WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Tree>() {
      @Override
      @Nullable
      public Tree call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSpecies = CursorUtil.getColumnIndexOrThrow(_cursor, "species");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfAdditionalNames = CursorUtil.getColumnIndexOrThrow(_cursor, "additionalNames");
          final int _cursorIndexOfPlotName = CursorUtil.getColumnIndexOrThrow(_cursor, "plotName");
          final int _cursorIndexOfPlotId = CursorUtil.getColumnIndexOrThrow(_cursor, "plotId");
          final int _cursorIndexOfCanvasX = CursorUtil.getColumnIndexOrThrow(_cursor, "canvasX");
          final int _cursorIndexOfCanvasY = CursorUtil.getColumnIndexOrThrow(_cursor, "canvasY");
          final Tree _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSpecies;
            _tmpSpecies = _cursor.getString(_cursorIndexOfSpecies);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final String _tmpPhotoPath;
            if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
              _tmpPhotoPath = null;
            } else {
              _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpAdditionalNames;
            _tmpAdditionalNames = _cursor.getString(_cursorIndexOfAdditionalNames);
            final String _tmpPlotName;
            _tmpPlotName = _cursor.getString(_cursorIndexOfPlotName);
            final long _tmpPlotId;
            _tmpPlotId = _cursor.getLong(_cursorIndexOfPlotId);
            final float _tmpCanvasX;
            _tmpCanvasX = _cursor.getFloat(_cursorIndexOfCanvasX);
            final float _tmpCanvasY;
            _tmpCanvasY = _cursor.getFloat(_cursorIndexOfCanvasY);
            _result = new Tree(_tmpId,_tmpSpecies,_tmpNotes,_tmpLatitude,_tmpLongitude,_tmpPhotoPath,_tmpTimestamp,_tmpAdditionalNames,_tmpPlotName,_tmpPlotId,_tmpCanvasX,_tmpCanvasY);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Tree>> getTreesByPlot(final long plotId) {
    final String _sql = "SELECT * FROM trees WHERE plotId = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, plotId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"trees"}, new Callable<List<Tree>>() {
      @Override
      @NonNull
      public List<Tree> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSpecies = CursorUtil.getColumnIndexOrThrow(_cursor, "species");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfPhotoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "photoPath");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfAdditionalNames = CursorUtil.getColumnIndexOrThrow(_cursor, "additionalNames");
          final int _cursorIndexOfPlotName = CursorUtil.getColumnIndexOrThrow(_cursor, "plotName");
          final int _cursorIndexOfPlotId = CursorUtil.getColumnIndexOrThrow(_cursor, "plotId");
          final int _cursorIndexOfCanvasX = CursorUtil.getColumnIndexOrThrow(_cursor, "canvasX");
          final int _cursorIndexOfCanvasY = CursorUtil.getColumnIndexOrThrow(_cursor, "canvasY");
          final List<Tree> _result = new ArrayList<Tree>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Tree _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSpecies;
            _tmpSpecies = _cursor.getString(_cursorIndexOfSpecies);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final String _tmpPhotoPath;
            if (_cursor.isNull(_cursorIndexOfPhotoPath)) {
              _tmpPhotoPath = null;
            } else {
              _tmpPhotoPath = _cursor.getString(_cursorIndexOfPhotoPath);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpAdditionalNames;
            _tmpAdditionalNames = _cursor.getString(_cursorIndexOfAdditionalNames);
            final String _tmpPlotName;
            _tmpPlotName = _cursor.getString(_cursorIndexOfPlotName);
            final long _tmpPlotId;
            _tmpPlotId = _cursor.getLong(_cursorIndexOfPlotId);
            final float _tmpCanvasX;
            _tmpCanvasX = _cursor.getFloat(_cursorIndexOfCanvasX);
            final float _tmpCanvasY;
            _tmpCanvasY = _cursor.getFloat(_cursorIndexOfCanvasY);
            _item = new Tree(_tmpId,_tmpSpecies,_tmpNotes,_tmpLatitude,_tmpLongitude,_tmpPhotoPath,_tmpTimestamp,_tmpAdditionalNames,_tmpPlotName,_tmpPlotId,_tmpCanvasX,_tmpCanvasY);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
